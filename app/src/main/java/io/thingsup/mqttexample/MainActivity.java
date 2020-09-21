package io.thingsup.mqttexample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {

    private static  String LOG_TAG = MainActivity.class.getSimpleName();

    public MqttAndroidClient mqttAndroidClient;

    final String serverUri = "ssl://mqtt.thingsup.io:1883";
    String clientId = "<Your MQTT Client ID>";
    final String MqttTopic = "<Your MQTT Topic>";
    final String Username = "<Your MQTT Username>";
    final String Password = "<Your MQTT Password>";

    TextView mTextView;
    EditText mSendMessage;
    Button mSendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSendButton = findViewById(R.id.sendButton);
        mSendMessage = findViewById(R.id.sendText);
        mTextView = findViewById(R.id.textView);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*
                    Checks if TextInput contains String and MQTT is connected
                 */
                if(!mSendMessage.getText().toString().equals("") && mqttAndroidClient.isConnected())
                {
                    try
                    {
                        MqttMessage mqttMessage = new MqttMessage();
                        mqttMessage.setPayload(mSendMessage.getText().toString().getBytes());
                        mqttMessage.setQos(1);

                        mqttAndroidClient.publish(MqttTopic,mqttMessage);
                        postMessage("Message Published : " +MqttTopic + " -> " +  new String(mqttMessage.getPayload()));
                    }
                    catch (MqttException e)
                    {
                        e.printStackTrace();
                    }
                }

            }
        });

        connectMQTT();
    }

    /*
        To Update UI from Non UI Thread
     */
    void postMessage(final String message)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                String newText = mTextView.getText().toString() + "\n$:" + message;
                mTextView.setText(newText);
            }
        });

    }


    /*
        Creates MQTT Client and Connected with provided MQTT credentials.
    */
    void connectMQTT()
    {
        /*
            In case of Dynamic Client ID
            clientId = clientId + System.currentTimeMillis();
         */
        if(mqttAndroidClient == null) {
            mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), serverUri, clientId);
            mqttAndroidClient.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {

                    Log.d(LOG_TAG, "MQTT Connected : Reconnect:" + reconnect);
                    postMessage("MQTT Connected");
                }

                @Override
                public void connectionLost(Throwable cause) {

                    Log.d(LOG_TAG, "MQTT Connection Lost");
                    postMessage("MQTT Connection Lost");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {

                    Log.d(LOG_TAG, "MQTT Message Arrived Topic:" + topic);
                    postMessage("Message Arrived : " +topic + " -> " +  new String(message.getPayload()));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });
        }

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(true);
        mqttConnectOptions.setUserName(Username);
        mqttConnectOptions.setPassword(Password.toCharArray());


        try {

            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    try {
                        mqttAndroidClient.subscribe(MqttTopic,0);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                    Log.d(LOG_TAG,"MQTT Connection Success");

                    postMessage("MQTT Connection Success");

                    MqttMessage mqttMessage = new MqttMessage();
                    mqttMessage.setPayload("Test".getBytes());
                    mqttMessage.setQos(1);

                    try
                    {
                        mqttAndroidClient.publish(MqttTopic,mqttMessage);
                        postMessage("Message Arrived : " +MqttTopic + " -> " +  new String(mqttMessage.getPayload()));
                    }
                    catch (MqttException e)
                    {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(LOG_TAG,"MQTT Connection Failure");
                    exception.printStackTrace();
                }
            });


        } catch (MqttException ex){
            ex.printStackTrace();
        }
    }
}