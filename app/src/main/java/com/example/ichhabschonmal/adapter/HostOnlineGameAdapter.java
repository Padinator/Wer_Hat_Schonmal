package com.example.ichhabschonmal.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ichhabschonmal.R;
import com.example.ichhabschonmal.server_client_communication.ServerSocketEndPoint;

public class HostOnlineGameAdapter extends RecyclerView.Adapter<HostOnlineGameAdapter.ViewHolder> {

    private final LayoutInflater inflater;
    private final Context context;
    private final ServerSocketEndPoint serverEndPoint;


    public HostOnlineGameAdapter(Context context, ServerSocketEndPoint serverEndPoint) {
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.serverEndPoint = serverEndPoint;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.client_item, parent, false);
        return new HostOnlineGameAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Log.e("position value", position + "");
        Log.e("device name", serverEndPoint.getClientsDeviceName(position));
        Log.e("ip", serverEndPoint.getClientsIPAddress(position));

        holder.deviceName.setText(serverEndPoint.getClientsDeviceName(position));
        holder.IPAddress.setText(serverEndPoint.getClientsIPAddress(position));
    }

    @Override
    public int getItemCount() {
        return serverEndPoint.sizeOfClients();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName, IPAddress;
        ImageButton kickClient;

        ViewHolder(View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.deviceName);
            IPAddress = itemView.findViewById(R.id.IPAddress);
            kickClient = itemView.findViewById(R.id.kickClient);

            kickClient.setOnClickListener(view -> {
                Log.e("RecyclerView", "kick client");
            });
        }


    }
}


