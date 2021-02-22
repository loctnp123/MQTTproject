package com.loctnp.mqttproject;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    static String MQTTHOST="tcp://m13.cloudmqtt.com:14462";
    static String USERNAME="llvazgdi";
    static String PASSWORD="Tz7Z_y4AogFF";
    static String PORT = "14462";
    String topicStr = "/uel/farm/degreeph";
    MqttAndroidClient client;

    TextView txtStatus,txtSubStatus,txtMessage;
    EditText edtPub;
    Ringtone myRingtone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addControls();
        connectMqtt();
    }
    public void addControls(){
        txtStatus = findViewById(R.id.txtStatus);
        txtSubStatus = findViewById(R.id.txtSubStatus);
        edtPub = findViewById(R.id.edtPub);
        txtMessage = findViewById(R.id.message);
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        myRingtone = RingtoneManager.getRingtone(getApplicationContext(),uri);
    }

    public void connectMqtt() {
        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), MQTTHOST,clientId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(USERNAME);
        options.setPassword(PASSWORD.toCharArray());

        try {
            //IMqttToken token = client.connect();
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Toast.makeText(MainActivity.this, "Connected Successfully!", Toast.LENGTH_SHORT).show();
                    txtStatus.setText("Connected Successfully");
                    txtStatus.setTextColor(Color.GREEN);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Toast.makeText(MainActivity.this, "Cannot Connect To Mqtt Server", Toast.LENGTH_SHORT).show();
                    txtStatus.setText("Cannot Connect To Mqtt Server");
                    txtStatus.setTextColor(Color.RED);
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    public void publish(View v){
        String topic = topicStr;
        String message = edtPub.getText().toString();
        if (message.length()<1) message = "Hello World";
        try {
            client.publish(topic, message.getBytes(),0,false);
            Toast.makeText(this, "Publish Successfully", Toast.LENGTH_SHORT).show();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    public void subscribe(View v){
        String topic = topicStr;
        int qos = 1;
        try {
            IMqttToken subToken = client.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The message was published
                    txtSubStatus.setText("Subscribe to server Successfully");
                    txtSubStatus.setTextColor(Color.GREEN);
                    client.setCallback(new MqttCallback() {
                        @Override
                        public void connectionLost(Throwable cause) {
                            txtSubStatus.setText("Connection Lost!");
                            txtSubStatus.setTextColor(Color.RED);

                        }

                        @Override
                        public void messageArrived(String topic, MqttMessage message) throws Exception {
                            txtMessage.setText(new String(message.getPayload()));
                            myRingtone.play();
                        }

                        @Override
                        public void deliveryComplete(IMqttDeliveryToken token) {

                        }
                    });
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards
                    txtSubStatus.setText("Subscribe failed");
                    txtSubStatus.setTextColor(Color.RED);
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void unsubscribe(View v){
        final String topic = topicStr;
        try {
            IMqttToken unsubToken = client.unsubscribe(topic);
            unsubToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The subscription could successfully be removed from the client
                    txtStatus.setText("Subsription Status");
                    txtStatus.setTextColor(Color.YELLOW);
                    Toast.makeText(MainActivity.this, "Unsubscribe Successfully", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // some error occurred, this is very unlikely as even if the client
                    // did not had a subscription to the topic the unsubscribe action
                    // will be successfully
                    Toast.makeText(MainActivity.this, "Cannot unsubscribe", Toast.LENGTH_SHORT).show();
                    txtStatus.setText("Trying to unsubscribe but an error may have happened");
                    txtStatus.setTextColor(Color.RED);
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void disconnect(View v){
        try {
            IMqttToken disconToken = client.disconnect();
            disconToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // we are now successfully disconnected
                    Toast.makeText(MainActivity.this, "Disconnect Successfully", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // something went wrong, but probably we are disconnected anyway
                    Toast.makeText(MainActivity.this, "Cannot Disconnect", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}