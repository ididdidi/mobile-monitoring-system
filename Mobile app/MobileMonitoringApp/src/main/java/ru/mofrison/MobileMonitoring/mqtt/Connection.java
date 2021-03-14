package ru.mofrison.MobileMonitoring.mqtt;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
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

public class Connection {
    private static final String logTag = "CONNECTION";

    public enum Status {
        DISCONNECT,
        CONNECT
    }

    private Status status = Status.DISCONNECT;
    private String clientId;
    private MqttAndroidClient client;
    private String clientUserName;

    private static Connection instance = null;
    private Connection() {
        clientId = MqttClient.generateClientId();
    }

    public static void initInstance() {
        Log.d(logTag, "Connection::InitInstance()");
        if (instance == null) {
            instance = new Connection();
        }
    }

    public static Connection getInstance() {
            if(instance == null)
                Log.e(logTag, "Connection don't  InitInstance");
        return instance;
    }

    public Status getStatus() {
        Status tmp = status;
        return tmp;
    }

    public String getClientUserName() {
        return new String(clientUserName);
    }

    public void connect(@NonNull final Application appContext, String serverURI, final String userName, String password,
                        final Intent intent) {
        if(status == Status.CONNECT) {
            disconnect();
        }

        client = new MqttAndroidClient(appContext, serverURI, clientId);

        // Connect with Username / Password
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(userName);
        options.setPassword(password.toCharArray());

        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener(){
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    clientUserName = userName;
                    // We are connected
                    Toast.makeText(appContext,
                            "Connect!",
                            Toast.LENGTH_SHORT).show();
                    status = Status.CONNECT;
                    //receiveMessages(appContext); // moved to MainActivity
                    appContext.startActivity(intent);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Toast.makeText(appContext,
                            "\t\t\t Connection failed!\n" +
                                    "Check your connection settings!",
                            Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            Toast.makeText(appContext,
                    "Error!",
                    Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void publish(String topic, String payload) {
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            //message.setRetained(true);
            client.publish(topic, message);
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribe(@NonNull final Context appContext, final String topic) {
        int qos = 1;
        try {
            IMqttToken subToken = client.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards
                    Toast.makeText(appContext,
                            "Failed subscribing to " + topic,
                            Toast.LENGTH_SHORT).show();
                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    public void unsubscribe(final String topic){
        try {
            IMqttToken unsubToken = client.unsubscribe(topic);
            unsubToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The subscription could successfully be removed from the client
                    Log.d(logTag, "unsubscribe " + topic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // some error occurred, this is very unlikely as even if the client
                    // did not had a subscription to the topic the unsubscribe action
                    // will be successfully
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    // создаем колбек и его метод
    public interface MessagesReceiver{
        void receivingMessage(String topic, MqttMessage message);
    }

    MessagesReceiver messagesReceiver;

    public void setMessagesListener(MessagesReceiver messagesListener){
        this.messagesReceiver = messagesListener;
    }


    public void receiveMessages( ) {
        if(client != null) {
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {

                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // отпарвляем сообщение получателю
                    messagesReceiver.receivingMessage(topic, message);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });
        }
    }

    public void disconnect() {
        if(status != Status.DISCONNECT) {
            try {
                IMqttToken disconToken = client.disconnect();
                disconToken.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        // we are now successfully disconnected
                        status = Status.DISCONNECT;
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken,
                                          Throwable exception) {
                        // something went wrong, but probably we are disconnected anyway
                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

}
