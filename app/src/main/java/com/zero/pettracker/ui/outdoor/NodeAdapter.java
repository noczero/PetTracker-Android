package com.zero.pettracker.ui.outdoor;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zero.pettracker.R;
import com.zero.pettracker.ui.indoor.PopUpInstruction;

import org.w3c.dom.NodeList;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NodeAdapter extends RecyclerView.Adapter<NodeAdapter.NodeViewHolder> {

    private ArrayList<Node> nodeList;
    private Context mContext;
    private NodeAdapterCallback nodeAdapterCallback;

    public NodeAdapter(Context mContext, ArrayList<Node> nodeList, NodeAdapterCallback adapterCallback){
        this.nodeList = nodeList;
        this.mContext = mContext;
        this.nodeAdapterCallback = adapterCallback;
    }

    @NonNull
    @Override
    public NodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.list_item_node, parent, false); // set
        return new NodeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NodeAdapter.NodeViewHolder holder, int position) {
        Node node = nodeList.get(position);
        holder.longitude.setText("Longitude : \n" + node.getLatLng().longitude);
        holder.latitude.setText("Latitude : \n" + node.getLatLng().latitude);
        holder.nodeName.setText(node.getName());

        Log.d("adapter", "onBindViewHolder: " + node.getName());
    }

    @Override
    public int getItemCount() {
        return nodeList.size();
    }

    public class NodeViewHolder extends RecyclerView.ViewHolder{

        // butterknife
        @BindView(R.id.node_name)
        TextView nodeName;
        @BindView(R.id.node_latitude)
        TextView latitude;
        @BindView(R.id.node_longitude)
        TextView longitude;

        public NodeViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView); // butterknife

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*
                    Memanggil interface dan juga methodnya. getAdapterPosition ini adalah method bawaan
                    adapter untuk memanggil index posisi.
                     */

                    // start route pop up
                    AvailableRoutePopUp availableRoutePopUp = new AvailableRoutePopUp(mContext,nodeList);
                    availableRoutePopUp.showPopupWindow(v,getAdapterPosition());

                    // callback
                    nodeAdapterCallback.onRowNodeClicked(getAdapterPosition());
                    nodeAdapterCallback.getNodeList(availableRoutePopUp.getNodeList()); // getNodeList from pop up window
                }
            });

        }
    }



    /*
   interface sebagai listener onclick adapter ke parent activity
    */
    public interface NodeAdapterCallback {
        /*
        Disini kalian bisa membuat beberapa fungsi dengan parameter sesuai kebutuhan. Kebutuhan
        disini adalah untuk mendapatkan pada posisi mana user mengklik listnya.
         */
        void onRowNodeClicked(int position);
        void getNodeList(ArrayList<Node> nodeList);
    }
}
