package com.zero.pettracker.ui.outdoor;

import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

public class floydWarshall {

    private ArrayList<Node> listNode;
    private int[][] pathMatrix;
    private double[][] distMatrix;
    private int size;

    public static int INF = 99999;

    public floydWarshall(ArrayList<Node> node) {
        this.listNode = node;
        this.size = node.size();
    }

    public void orderNode() {
        // find end node
        Node endNode = OutdoorFragment.findSameNode("End", listNode);
        if(endNode != null){
            // note order correctly
            listNode.remove(endNode); // remove
            listNode.add(endNode); // add again to last
        }
    }


    public ArrayList<Node> getListNode() {
        return this.listNode;
    }

    /**
     * Calculate distance between two points in latitude and longitude taking
     * into account height difference. If you are not interested in height
     * difference pass 0.0. Uses Haversine method as its base.
     * <p>
     * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
     * el2 End altitude in meters
     *
     * @returns Distance in Meters
     */
    public static double getHeaversineDistance(double lat1, double lat2, double lon1,
                                               double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }

    public double[][] getDistanceMatrix() {

        double[][] distanceMatrix = new double[size][size]; // create 2 dimensional matrix based size
        int i, j;
        for (i = 0; i < size; i++) {
            for (j = 0; j < size; j++) {
                // poistion move along rows
                double latitude1 = listNode.get(i).getLatLng().latitude;
                double longitude1 = listNode.get(i).getLatLng().longitude;

                // position move along cols
                double latitude2 = listNode.get(j).getLatLng().latitude;
                double longitude2 = listNode.get(j).getLatLng().longitude;

                // get distance betweern two position
                distanceMatrix[i][j] = getHeaversineDistance(latitude1, latitude2, longitude1, longitude2, 0, 0);
            }
        }

        return distanceMatrix;
    }

    public double[][] getFloyWarshalldMatrix(double[][] floyd_input) {
        double[][] matrixA = new double[size][size];
        int i, j, k;

        // duplicateMatrix
        for (i = 0; i < size; i++)
            for (j = 0; j < size; j++)
                matrixA[i][j] = floyd_input[i][j];

        // predecesor for reconstruction path
        int[][] next = new int[size][size];
        for (i = 0; i < size; i++) {
            for (j = 0; j < size; j++)
                if (i != j)
                    next[i][j] = j;
        }


        /* Add all vertices one by one to the set of intermediate
           vertices.
          ---> Before start of an iteration, we have shortest
               distances between all pairs of vertices such that
               the shortest distances consider only the vertices in
               set {0, 1, 2, .. k-1} as intermediate vertices.
          ----> After the end of an iteration, vertex no. k is added
                to the set of intermediate vertices and the set
                becomes {0, 1, 2, .. k} */
        for (k = 0; k < size; k++) {
            // Pick all vertices as source one by one
            for (i = 0; i < size; i++) {
                // Pick all vertices as destination for the
                // above picked source
                for (j = 0; j < size; j++) {
                    // If vertex k is on the shortest path from
                    // i to j, then update the value of matrixA[i][j]
                    if (matrixA[i][k] + matrixA[k][j] < matrixA[i][j]){
                        matrixA[i][j] = matrixA[i][k] + matrixA[k][j];
                        // put path
                        next[i][j] = next[i][k];
                    }
                }
            }
        }

        // set it to class for access
        this.pathMatrix = next;
        this.distMatrix = matrixA;

        return matrixA;
    }

    public int[][] getPathMatrix() {
        return pathMatrix;
    }

    public int[][] getConnectedMatrix() {
        int[][] connectedMatrix = new int[size][size];

        // iterate over rows
        for (int i = 0; i < size; i++) {
            // iterate over cols
            for (int j = 0; j < size; j++) {

                // create diagonal 1, as on its node its connected
                if (i == j) {
                    connectedMatrix[i][j] = 1;
                } else {
                    // iteratre over list connected node
                    if (listNode.get(i).getConnectedList() != null) {
                        if (listNode.get(i).getConnectedList().size() > 0) {
                            ArrayList<String> listConnected = listNode.get(i).getConnectedList();

                            // iterate
                            for (String name : listConnected) {
                                // compare to rows
                                if (name.equals(listNode.get(j).getName())) {
                                    connectedMatrix[i][j] = 1;
                                    break; // exit loop list connected node
                                } else {
                                    connectedMatrix[i][j] = 0;
                                }
                            }
                        }
                    }
                }
            }
        }
        return connectedMatrix;
    }

    public double[][] getFloydInput(double[][] distanceMatrix, int[][] connectedMatrix){
        double[][] flyodInput = new double[size][size];

        for (int i = 0; i < size; i++)
            for(int j = 0; j < size; j++){
                // check if index has 1 value in connectedMatrix
                if(connectedMatrix[i][j] > 0){
                    // set floydInput from distanceMatrix value
                    flyodInput[i][j] = distanceMatrix[i][j];
                } else{
                    flyodInput[i][j] = INF; // Infinity Value
                }
            }

        return flyodInput;
    }

    public StringBuilder reconstructionPath(){
        StringBuilder strBuild = new StringBuilder();
        strBuild.append("The fastest path : \n");
        //strBuild.append("pair \tdist \tpath");
        //for(int i = 0; i < pathMatrix.length; i++){
            //for(int j = 0; j < pathMatrix.length; j++){
                int i = 0;
                int j = listNode.size() - 1; // get the last cols
                if( i != j){
                    // not itself
                    int u = i;
                    int v = j;
                    String path = listNode.get(i).getName() + " to " + listNode.get(j).getName() + " has " + String.format(Locale.ENGLISH, "%.1fm", distMatrix[i][j]) + " with path " + listNode.get(i).getName();

                    // reconstruction path
                    do {
                        u = pathMatrix[u][v];
                        path += "->" + listNode.get(u).getName();
                    } while (u != v );

                    strBuild.append(path + "\n"); // append path
                }

            //}
            strBuild.append("\n");
        //}

        return strBuild;
    }


}
