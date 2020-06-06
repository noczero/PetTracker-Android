package com.zero.pettracker.ui.outdoor;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.widget.NestedScrollView;

import com.bskim.maxheightscrollview.widgets.MaxHeightScrollView;
import com.zero.pettracker.R;

import java.util.ArrayList;

public class AvailableRoutePopUp  {
    private Context context;
    private ArrayList<Node> nodeList;
    private ArrayList<String> connectedList = new ArrayList<>();

    public AvailableRoutePopUp(Context context, ArrayList<Node> nodeList){
        this.context = context.getApplicationContext();
        this.nodeList = nodeList;
    }


    public void showPopupWindow(final View view, final int clickPosition) {
        //Create a View object yourself through inflater
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.pop_up_connected_route, null);


        //Specify the length and width through constants
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;

        //Make Inactive Items Outside Of PopupWindow
        boolean focusable = true;

        //Create a window with our parameters
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        //Set the location of the window on the screen
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        //Initialize the elements of our window, install the handler

        // set node name
        TextView nodeText = popupView.findViewById(R.id.node_connected_title);
        nodeText.setText(nodeList.get(clickPosition).getName());

        // Add checkbox
        MaxHeightScrollView nestedScrollView = (MaxHeightScrollView) popupView.findViewById(R.id.check_box_node_linear_layout);
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        // loop for listNode
        for (Node node: nodeList){
            CheckBox ch = new CheckBox(context);

            // check if not same name, add to list
            if(!nodeList.get(clickPosition).getName().equals(node.getName())){

                // check checkbox if exist
                if(nodeList.get(clickPosition).getConnectedList() != null){
                    // get conected list of currnet node
                    ArrayList<String> listConnectedNode = nodeList.get(clickPosition).getConnectedList();
                    for (String value : listConnectedNode){
                        // compare to current checkbox
                        if(node.getName().equals(value)){
                            // if exist then set checked true
                            ch.setChecked(true);
                        }
                    }
                }

                ch.setText(node.getName());
                ch.setOnClickListener(checkCheckBox); // add checkboxlistener
                linearLayout.addView(ch);
            }

        }

        nestedScrollView.addView(linearLayout);


        Button buttonEdit = popupView.findViewById(R.id.messageButton);
        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open wifi setting
                // save to list
                nodeList.get(clickPosition).setConnectedList(connectedList);

                // hide popUpWindows
                popupWindow.dismiss();
            }
        });

        //Handler for clicking on the inactive zone of the window
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                //Close the window when clicked
                popupWindow.dismiss();
                return true;
            }
        });
    }

    // return nodeList, get it on select route activity
    public ArrayList<Node> getNodeList() {
        return this.nodeList;
    }

    private View.OnClickListener checkCheckBox = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CheckBox checkBox = (CheckBox) v;
            boolean checked = checkBox.isChecked();
            if(checked){
                //Log.d("checkbox", "onClick: " + checkBox.getText());
                Toast.makeText(context,  checkBox.getText() + " is connected" , Toast.LENGTH_SHORT).show();
                connectedList.add(checkBox.getText().toString()); // add to list connection

                for (String value:  connectedList){
                    Log.d("ConnectedList", value);
                }
            } else {
                Toast.makeText(context,  checkBox.getText() + " is disconnected", Toast.LENGTH_SHORT).show();
                connectedList.remove(checkBox.getText().toString());
                // remove
            }
        }

    };



}
