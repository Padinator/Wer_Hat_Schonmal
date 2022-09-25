package com.example.ichhabschonmal.adapter;

import android.annotation.SuppressLint;
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
import com.example.ichhabschonmal.server_client_communication.SocketCommunicator;
import com.example.ichhabschonmal.server_client_communication.SocketEndPoint;

import java.io.IOException;
import java.util.concurrent.Semaphore;

public class HostOnlineGameAdapter extends RecyclerView.Adapter<HostOnlineGameAdapter.ViewHolder> {

    private final LayoutInflater inflater;
    private final Context context;
    private final ServerSocketEndPoint serverEndPoint;
    private final SocketCommunicator.Receiver receiverAction;
    private final TextView connectedClients;
    private final int maxClientNumber;

    public HostOnlineGameAdapter(Context context, ServerSocketEndPoint serverEndPoint, SocketCommunicator.Receiver receiverAction, TextView connectedClients, int maxClientNumber) {
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.serverEndPoint = serverEndPoint;
        this.receiverAction = receiverAction;
        this.connectedClients = connectedClients;
        this.maxClientNumber = maxClientNumber;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.client_item, parent, false);
        return new HostOnlineGameAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.deviceName.setText(serverEndPoint.getAClient(position).getDeviceName());
        holder.IPAddress.setText(serverEndPoint.getAClient(position).getIPAddress());
    }

    @Override
    public int getItemCount() {
        return serverEndPoint.sizeOfClients();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName, IPAddress;
        ImageButton kickClient;

        @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
        ViewHolder(View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.deviceName);
            IPAddress = itemView.findViewById(R.id.IPAddress);
            kickClient = itemView.findViewById(R.id.kickClient);

            kickClient.setOnClickListener(view -> {
                // Disconnect and remove client
                try {
                    // Try notifying the client about the disconnecting a maximum of 5 times
                    boolean clientIsDisconnected = serverEndPoint.sendMessageToClient(getLayoutPosition(), SocketEndPoint.CLOSE_CONNECTION); // Inform client to close connection

                    /*
                    for (int i = 0; !clientIsDisconnected && i < 4; i++) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        clientIsDisconnected = serverEndPoint.sendMessageToClient(getLayoutPosition(), SocketEndPoint.CLOSE_CONNECTION);
                    }
                    */

                    serverEndPoint.disconnectClientFromServer(getLayoutPosition()); // Works correct with all Threads? How many Thread are running???
                    notifyDataSetChanged();
                    connectedClients.setText("Verbunden:\t\t" + serverEndPoint.sizeOfClients() + " / " + (maxClientNumber - 1));
                } catch (IOException e) {
                    e.printStackTrace();
                } finally { // Start another connection for a new client
                    serverEndPoint.createConnection(1, receiverAction);
                }
            });
        }
    }

}


