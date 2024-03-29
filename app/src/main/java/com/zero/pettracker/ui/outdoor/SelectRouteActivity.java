package com.zero.pettracker.ui.outdoor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.zero.pettracker.MainActivity;
import com.zero.pettracker.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SelectRouteActivity extends AppCompatActivity implements NodeAdapter.NodeAdapterCallback {

    @BindView(R.id.node_recycle_view)
    RecyclerView recyclerView;

    @BindView(R.id.btn_calculate)
    Button calculateBtn;

    private ArrayList<Node> listNode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_route);
        ButterKnife.bind(this);

        listNode = getIntent().getParcelableArrayListExtra("listNode");

        setNode(listNode); // call recycle view

        Log.d("Test", "onCreate: ");

        calculateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // check validroute
                if(validRoute(listNode)) {
                    // calculate floyd button
                    ResultFloydPopUp resultPopUp = new ResultFloydPopUp(getApplicationContext(), listNode);
                    resultPopUp.showPopupWindow(v);
                } else {
                    Toast.makeText(getApplicationContext(), "Make sure all nodes have connection to a node!", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    public void setNode(ArrayList<Node> listNode){
        NodeAdapter nodeAdapter = new NodeAdapter(this,listNode, this);

        recyclerView.setAdapter(nodeAdapter);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2,
                GridLayoutManager.VERTICAL, false); // set jadi 2 kolom
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setClipToPadding(false);
        nodeAdapter.notifyDataSetChanged();
        Log.d("setNode", "setNode: ");
    }

    @Override
    public void onRowNodeClicked(int position) {

        //String nama = listNode.get(position).getName();
        //Toast.makeText(this, "Node " + nama, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void getNodeList(ArrayList<Node> nodeList) {
        // updateList to the newest
        this.listNode = nodeList;

        for (Node node : nodeList){
            Log.d("ConnectedList" , node.getName());
            if(node.getConnectedList()!= null)
            for (String value : node.getConnectedList()){
                Log.d("ConnectedList", "Connected Node: " + value);
            }
        }
    }


    public boolean validRoute(ArrayList<Node> nodeList){
        int size = nodeList.size();
        int totalRoute = 0;
        for (Node node : nodeList){
            //Log.d("ConnectedList" , node.getName());
            if(node.getConnectedList()!= null)
                if(node.getConnectedList().size() > 0)
                    totalRoute++; // increment the totalRoute if not null
        }

        // all node has edge
        if(totalRoute >= size){
            return true;
        } else{
            return false;
        }
    }
}