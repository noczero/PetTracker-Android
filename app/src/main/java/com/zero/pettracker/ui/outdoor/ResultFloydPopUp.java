package com.zero.pettracker.ui.outdoor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.zero.pettracker.R;

import org.w3c.dom.NodeList;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

import static com.zero.pettracker.ui.outdoor.floydWarshall.INF;


public class ResultFloydPopUp  {
    private Context context;
    private ArrayList<Node> nodeList;

    public ResultFloydPopUp(Context context, ArrayList<Node> nodeList){
        this.context = context.getApplicationContext();
        this.nodeList = nodeList;
    }

    public void showPopupWindow(final View view) {

        //Create a View object yourself through inflater
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.pop_up_result_floyd, null);

        //Specify the length and width through constants
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;

        //Make Inactive Items Outside Of PopupWindow
        boolean focusable = true;

        //Create a window with our parameters
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        //Set the location of the window on the screen
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        // invoke algorithm here
        double[][] result_matrix = calculateFLoydWarshall(popupView);

        //Initialize the elements of our window, install the handler
        TextView resultText = popupView.findViewById(R.id.result_floyd_text);
        resultText.setText(getString(result_matrix,"Result : "));

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

    public double[][] calculateFLoydWarshall(View v){
        //  Step 1 : make instance of algorithm
        floydWarshall fw_algorithm = new floydWarshall(this.nodeList);

        // Step 2 : Order the node
        fw_algorithm.orderNode();

        //  Step 3 : calculate distance for all node
        double[][] distanceMatrix = fw_algorithm.getDistanceMatrix();

        // Step 4 : get Connected Matrix from connected List;
        int[][] connectedMatrix = fw_algorithm.getConnectedMatrix();

        // Step 5 : get correct graph of node, for floyd case;
        double[][] floyd_input = fw_algorithm.getFloydInput(distanceMatrix, connectedMatrix);

        //Initialize the elements of our window, install the handler
        TextView inputText = v.findViewById(R.id.input_floyd);
        inputText.setText(getString(floyd_input,"Input : "));

        // Step 6 : get result floyd algorithm
        double[][] floyd_ouput = fw_algorithm.getFloyWarshalldMatrix(floyd_input);

        // Step 7 : Reconstruction Path
        StringBuilder pathReconstruction = fw_algorithm.reconstructionPath();
        TextView pathResult = v.findViewById(R.id.result_path);
        pathResult.setText(pathReconstruction);

        return floyd_ouput;
    }

    public StringBuilder getString(double[][] array, String title){
        StringBuilder strBuild = new StringBuilder();
        strBuild.append(title +"\n");
        for(int i = 0; i < array[0].length; i++){
            for(int j = 0; j < array.length; j++){
                double value = array[i][j];
                if(value < INF){
                    strBuild.append(String.format(Locale.ENGLISH, "%.3f", array[i][j]));
                } else {
                    strBuild.append("    ~    ");
                }
                strBuild.append(" ");
            }
            strBuild.append("\n");
        }

        return strBuild;
    }


}
