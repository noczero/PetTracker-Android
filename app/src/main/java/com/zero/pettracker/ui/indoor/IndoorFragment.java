package com.zero.pettracker.ui.indoor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.zero.pettracker.R;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class IndoorFragment extends Fragment {

    // create indoor view model
    private IndoorViewModel indoorViewModel;
    private TextView statusNetwork;
    private TextView rssiNetwork;
    private TextView ssidNetwork;
    private Button findMyPetBtn;
    private WifiReceiver wifiReceiver;
    private BroadcastReceiver broadcastReceiver;

    // onCreateView
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        indoorViewModel = ViewModelProviders.of(this).get(IndoorViewModel.class);

        View root = inflater.inflate(R.layout.fragment_indoor, container, false);

        statusNetwork = root.findViewById(R.id.status_network);
        rssiNetwork = root.findViewById(R.id.rssi_wifi);
        ssidNetwork = root.findViewById(R.id.ssid);


        indoorViewModel.getStatusNetwork().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                statusNetwork.setText(s);
            }
        });

        findMyPetBtn = root.findViewById(R.id.btn_find);
        findMyPetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // start instruction
                PopUpInstruction popUpInstruction = new PopUpInstruction(getContext());
                popUpInstruction.showPopupWindow(v);

            }
        });

        DisplayWifiState();

        getActivity().registerReceiver(this.myWifiReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        getActivity().registerReceiver(this.myRssiChangeReceiver, new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));

        return root;
    }

        private BroadcastReceiver myRssiChangeReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                WifiManager wifiMan=(WifiManager)getActivity().getSystemService(Context.WIFI_SERVICE);
                wifiMan.startScan();
                int newRssi = wifiMan.getConnectionInfo().getRssi();
                rssiNetwork.setText("RSSI Level : "+ newRssi);
            }};

        private BroadcastReceiver myWifiReceiver
                = new BroadcastReceiver(){

            @Override
            public void onReceive(Context arg0, Intent arg1) {
                // TODO Auto-generated method stub
                NetworkInfo networkInfo = arg1.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI){
                    DisplayWifiState();
                }
            }};

    private void DisplayWifiState(){

        ConnectivityManager myConnManager = (ConnectivityManager) getActivity().getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo myNetworkInfo = myConnManager.getActiveNetworkInfo();
        WifiManager myWifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        myWifiManager.startScan();
        WifiInfo myWifiInfo = myWifiManager.getConnectionInfo();

        Log.d("myWifiInfo", "Frequency : " + myWifiInfo.getFrequency());

        if (myNetworkInfo.isConnected()){
            int myIp = myWifiInfo.getIpAddress();

            statusNetwork.setText("Connecton status : Connected");
            rssiNetwork.setText("RSSI Level : "+ myWifiInfo.getRssi());
            ssidNetwork.setText("SSID : "+ myWifiInfo.getSSID());
        }
        else{
            statusNetwork.setText("Connecton status : Disconnected");
            rssiNetwork.setText(""); // empty
            ssidNetwork.setText("");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //Note: Not using RSSI_CHANGED_ACTION because it never calls me back.
        IntentFilter rssiFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getActivity().registerReceiver(myRssiChangeReceiver, rssiFilter);

        WifiManager wifiMan=(WifiManager)getActivity().getSystemService(Context.WIFI_SERVICE);
        wifiMan.startScan();
    }
    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(myRssiChangeReceiver);
    }







}
