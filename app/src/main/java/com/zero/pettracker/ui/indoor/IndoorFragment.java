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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.zero.pettracker.R;

import java.util.Locale;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class IndoorFragment extends Fragment {

    // create indoor view model
    private IndoorViewModel indoorViewModel;
    private TextView statusNetwork;
    private TextView rssiNetwork;
    private TextView ssidNetwork;

    private Button connectToWiFi;
    private Button btnFindMyPet;
    private Button btnTurnOffAlarm;

    private LinearLayout distanceInformationLayout;
    private TextView distance;
    private int rssi = -100;
    private double current_distance = 100;

    // onCreateView
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        indoorViewModel = ViewModelProviders.of(this).get(IndoorViewModel.class);

        // set view as root
        View root = inflater.inflate(R.layout.fragment_indoor, container, false);

        // find the id from xml
        statusNetwork = root.findViewById(R.id.status_network);
        rssiNetwork = root.findViewById(R.id.rssi_wifi);
        ssidNetwork = root.findViewById(R.id.ssid);
        distanceInformationLayout = root.findViewById(R.id.distance_information);
        distance = root.findViewById(R.id.total_distance);

        indoorViewModel.getStatusNetwork().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                statusNetwork.setText(s);
            }
        });

        // Connect to WiFi on click listener
        connectToWiFi = root.findViewById(R.id.btn_connect_wifi);
        connectToWiFi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start instruction
                PopUpInstruction popUpInstruction = new PopUpInstruction(getContext());
                popUpInstruction.showPopupWindow(v);
            }
        });

        // Find My Pet on click listener
        btnFindMyPet = root.findViewById(R.id.btn_find_pet);
        btnFindMyPet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRSSItoDevice(); // send rssi to device
            }
        });

        // Turn of Alarm on click listener
        btnTurnOffAlarm = root.findViewById(R.id.btn_turn_off_alarm);
        btnTurnOffAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOffAlarm(); // turn off alarm
            }
        });

        // display connection infromation
        DisplayWifiState();

        // register receiver for rssi and wifi state
        getActivity().registerReceiver(this.myWifiReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        getActivity().registerReceiver(this.myRssiChangeReceiver, new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));

        return root;
    }

    // display WiFi information method
    private void DisplayWifiState(){
        ConnectivityManager myConnManager = (ConnectivityManager) getActivity().getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo myNetworkInfo = myConnManager.getActiveNetworkInfo();
        if (myNetworkInfo != null ){
            WifiManager myWifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
            myWifiManager.startScan();
            WifiInfo myWifiInfo = myWifiManager.getConnectionInfo();
            Log.d("myWifiInfo", "Frequency : " + myWifiInfo.getFrequency());
            if (myNetworkInfo.isConnected()) {
                int frequency = myWifiInfo.getFrequency();
                rssi = myWifiInfo.getRssi();
                String ssid = WiFiUtils.convertSSID(myWifiInfo.getSSID());
                current_distance = WiFiUtils.calculateDistance(frequency,rssi);

                statusNetwork.setText("Status : Connected");
                rssiNetwork.setText("RSSI Level : "+ rssi);
                ssidNetwork.setText("SSID : "+ ssid);
                distance.setText(String.format(Locale.ENGLISH, "~%.1fm", current_distance)); // set distance

                // Toast.makeText(getContext(), "Connected...", Toast.LENGTH_SHORT).show();
                connectToWiFi.setVisibility(View.GONE); // remove button
                distanceInformationLayout.setVisibility(View.VISIBLE); // show distance

            }
        }
        else{
            statusNetwork.setText("Status : Waiting for connection...");
            rssiNetwork.setText(""); // empty
            ssidNetwork.setText("");

            Toast.makeText(getContext(), "Waiting for connection...", Toast.LENGTH_SHORT).show();
            connectToWiFi.setVisibility(View.VISIBLE); // show button
            distanceInformationLayout.setVisibility(View.GONE); // show distance

        }
    }

    // send request to server with rssi parameter
    private double prev_distance = 100;
    private void sendRSSItoDevice(){
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String api_url = "http://192.168.4.1/set_rssi?rssi=" + rssi;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, api_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        // compare the distance for notification
                        if (current_distance < prev_distance){
                            Toast.makeText(getContext(), "My pet is getting closer!", Toast.LENGTH_SHORT).show();
                            prev_distance = current_distance;
                        } else {
                            Toast.makeText(getContext(), "My pet is getting further away!", Toast.LENGTH_SHORT).show();
                            prev_distance = current_distance;
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // textView.setText("That didn't work!");

                Log.e("Volly Error", error.toString());

                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null) {
                    Log.e("Status code", String.valueOf(networkResponse.statusCode));
                }

                if( error instanceof NetworkError) {
                    //handle your network error here.
                    Toast.makeText(getContext(), "Can't established connection to device", Toast.LENGTH_SHORT).show();
                } else if( error instanceof ServerError) {
                    //handle if server error occurs with 5** status code
                    Toast.makeText(getContext(), "Server Error...", Toast.LENGTH_SHORT).show();
                } else if( error instanceof AuthFailureError) {
                    //handle if authFailure occurs.This is generally because of invalid credentials
                    Toast.makeText(getContext(), "Invalid Credentials", Toast.LENGTH_SHORT).show();
                } else if( error instanceof ParseError) {
                    //handle if the volley is unable to parse the response data.
                    Toast.makeText(getContext(), "Parse error...", Toast.LENGTH_SHORT).show();
                } else if( error instanceof NoConnectionError) {
                    //handle if no connection is occurred
                    Toast.makeText(getContext(), "Can't established connection to device", Toast.LENGTH_SHORT).show();
                } else if( error instanceof TimeoutError) {
                    //handle if socket time out is occurred.
                    Toast.makeText(getContext(), "Request timed out..", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    // Request to server -100 rssi as turn off alarm
    private void turnOffAlarm(){
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String api_url = "http://192.168.4.1/set_rssi?rssi=-100";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, api_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        // textView.setText("Response is: "+ response.substring(0,500));

                        Toast.makeText(getContext(), "The alarm has been turned off", Toast.LENGTH_SHORT).show();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // textView.setText("That didn't work!");

                Log.e("Volly Error", error.toString());

                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null) {
                    Log.e("Status code", String.valueOf(networkResponse.statusCode));
                }

                if( error instanceof NetworkError) {
                    //handle your network error here.
                    Toast.makeText(getContext(), "Can't established connection to device", Toast.LENGTH_SHORT).show();
                } else if( error instanceof ServerError) {
                    //handle if server error occurs with 5** status code
                    Toast.makeText(getContext(), "Server Error...", Toast.LENGTH_SHORT).show();
                } else if( error instanceof AuthFailureError) {
                    //handle if authFailure occurs.This is generally because of invalid credentials
                    Toast.makeText(getContext(), "Invalid Credentials", Toast.LENGTH_SHORT).show();
                } else if( error instanceof ParseError) {
                    //handle if the volley is unable to parse the response data.
                    Toast.makeText(getContext(), "Parse error...", Toast.LENGTH_SHORT).show();
                } else if( error instanceof NoConnectionError) {
                    //handle if no connection is occurred
                    Toast.makeText(getContext(), "Can't established connection to device", Toast.LENGTH_SHORT).show();
                } else if( error instanceof TimeoutError) {
                    //handle if socket time out is occurred.
                    Toast.makeText(getContext(), "Request timed out..", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    // on background cycle do register receiver
    @Override
    public void onResume() {
        super.onResume();
        //Note: Not using RSSI_CHANGED_ACTION because it never calls me back.
        IntentFilter rssiFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getActivity().registerReceiver(myRssiChangeReceiver, rssiFilter);

        getActivity().registerReceiver(this.myWifiReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        WifiManager wifiMan=(WifiManager)getActivity().getSystemService(Context.WIFI_SERVICE);
        wifiMan.startScan();
    }

    // on pause cycle do unregister receiver
    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(myRssiChangeReceiver);
        getActivity().unregisterReceiver(myWifiReceiver);
    }

    // on destroy cycle do unregister receiver
    @Override
    public void onDestroy() {
        try{
            getActivity().unregisterReceiver(myRssiChangeReceiver);
            getActivity().unregisterReceiver(myWifiReceiver);
        }catch(Exception e){}

        super.onDestroy();
    }

    // listen to change of rssi
    private BroadcastReceiver myRssiChangeReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            WifiManager wifiMan = (WifiManager)getActivity().getSystemService(Context.WIFI_SERVICE);
            wifiMan.startScan();
            rssi = wifiMan.getConnectionInfo().getRssi();

            // check if rssi -127, means it not connected
            if (rssi >= -100){
                int frequency =  wifiMan.getConnectionInfo().getFrequency();
                current_distance = WiFiUtils.calculateDistance(frequency,rssi);

                rssiNetwork.setText("RSSI Level : "+ rssi);
                distance.setText(String.format(Locale.ENGLISH, "~%.1fm", current_distance));

                //sendRSSItoDevice(); // send rssi to device
            } else {
                rssiNetwork.setText("");
            }
        }
    };

    // listen to state of wifi
    private BroadcastReceiver myWifiReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub
            NetworkInfo networkInfo = arg1.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI){
                DisplayWifiState();
            }
        }
    };

}
