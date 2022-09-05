package com.example.ichhabschonmal.online;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.ichhabschonmal.R;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class HostOnlineGame extends AppCompatActivity {


    EditText title;
    EditText message;
    final private String FCM_API = "https://fcm.googleapis.com/fcm/send";
    final private String serverKey = "key=" + "AAAAZh8Vh90:APA91bFNBjGbW1wJXgelppP8X2Lat_eCT2oJOshFTcWd8KlVSBdwmJPPInT_ZG30Nhcr_rlUdvHszAFxYyr7gOpLvuTb0GMxN_GwkVeEgX9R-ILB11X366VX1-ofRX2b32A5gfA5fXNO";
    final private String contentType = "application/json";
    final String TAG = "NOTIFICATION TAG";

    String NOTIFICATION_TITLE;
    String NOTIFICATION_MESSAGE;
    String TOPIC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.host_online_game);
        title = findViewById(R.id.title);
        message = findViewById(R.id.message);
        Button btnSend = findViewById(R.id.send);

        btnSend.setOnClickListener(v -> {
            TOPIC = "/topics/userABC"; //topic must match with what the receiver subscribed to
            NOTIFICATION_TITLE = title.getText().toString();
            NOTIFICATION_MESSAGE = message.getText().toString();

            JSONObject notification = new JSONObject();
            JSONObject notifcationBody = new JSONObject();
            try {
                String token = FirebaseMessaging.getInstance().getToken().toString();

                notifcationBody.put("title", NOTIFICATION_TITLE);
                notifcationBody.put("message", token);

                notification.put("to", TOPIC);
                notification.put("data", notifcationBody);
                Log.e(TAG, "onCreate: " + notifcationBody);

            } catch (JSONException e) {
                Log.e(TAG, "onCreate: " + e.getMessage());
            }
            sendNotification(notification);
        });
    }

    private void sendNotification(JSONObject notification) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_API, notification,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "onResponse: " + response.toString());
                        title.setText("");
                        message.setText("");
                    }
                },
                error -> {
                    Toast.makeText(HostOnlineGame.this, "Request error", Toast.LENGTH_LONG).show();
                    Log.i(TAG, "onErrorResponse: Didn't work");
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", serverKey);
                params.put("Content-Type", contentType);
                return params;
            }
        };
    }

}