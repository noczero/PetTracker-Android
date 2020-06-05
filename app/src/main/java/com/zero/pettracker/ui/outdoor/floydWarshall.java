package com.zero.pettracker.ui.outdoor;

import java.util.ArrayList;

public class floydWarshall {

    private ArrayList<Node> listNode;
    private int size;

    public floydWarshall(ArrayList<Node> node) {
        this.listNode = node;
        this.size = node.size();
    }

    public void orderNode(){
        // move end node to last node, we know end node in the second element of node

            Node endNode = OutdoorFragment.findSameNode("End",listNode);
            listNode.remove(endNode); // remove
            listNode.add(endNode); // add again to last
    }


    public ArrayList<Node> getListNode(){
        return this.listNode;
    }

    /**
     * Calculate distance between two points in latitude and longitude taking
     * into account height difference. If you are not interested in height
     * difference pass 0.0. Uses Haversine method as its base.
     *
     * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
     * el2 End altitude in meters
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

    public double[][] getDistanceMatrix(){

        double[][] distanceMatrix = new double[size][size]; // create 2 dimensional matrix based size
        int i, j, k;
        for (i = 0; i < size; i++){
            for (j = 0; j < size; j++){
                // poistion move along rows
                double latitude1 = listNode.get(i).getLatLng().latitude;
                double longitude1 = listNode.get(i).getLatLng().longitude;

                // position move along cols
                double latitude2 = listNode.get(j).getLatLng().latitude;
                double longitude2 = listNode.get(j).getLatLng().longitude;

                // get distance betweern two position
                distanceMatrix[i][j] = getHeaversineDistance(latitude1,latitude2,longitude1,longitude2,0,0);
                // set infinity to lower triangle of matrix
                for(k = j; k < i; k++){
                    distanceMatrix[i][k] = 9999; // as infinty
                }

                // set infinity to upper triangle
            }
        }

        return distanceMatrix;
    }

    public double[][] getFloyWarshalldMatrix(double[][] distanceMatrix){
        double[][] matrixA = new double[size][size];
        int i, j , k;

        // duplicateMatrix
        for (i = 0; i < size; i++)
            for (j = 0; j < size; j++)
                matrixA[i][j] = distanceMatrix[i][j];


        /* Add all vertices one by one to the set of intermediate
           vertices.
          ---> Before start of an iteration, we have shortest
               distances between all pairs of vertices such that
               the shortest distances consider only the vertices in
               set {0, 1, 2, .. k-1} as intermediate vertices.
          ----> After the end of an iteration, vertex no. k is added
                to the set of intermediate vertices and the set
                becomes {0, 1, 2, .. k} */
        for (k = 0; k < size; k++)
        {
            // Pick all vertices as source one by one
            for (i = 0; i < size; i++)
            {
                // Pick all vertices as destination for the
                // above picked source
                for (j = 0; j < size; j++)
                {
                    // If vertex k is on the shortest path from
                    // i to j, then update the value of matrixA[i][j]
                    if (matrixA[i][k] + matrixA[k][j] < matrixA[i][j])
                        matrixA[i][j] = matrixA[i][k] + matrixA[k][j];
                }
            }
        }

        return matrixA;
    }


}
