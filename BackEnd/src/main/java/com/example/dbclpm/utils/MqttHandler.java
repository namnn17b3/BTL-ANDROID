package com.example.dbclpm.utils;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttHandler {

	private MqttClient client;

	private static final String BROCKER_URL = "tcp://192.168.43.214:1883";

	public static final boolean SEND_NOTI = true;

	public MqttHandler(String clientId) {
		this.connect(BROCKER_URL, clientId);
	}

	public MqttHandler() {
	}

	public void connect(String brockerUrl, String clientId) {
		try {
			// Set up the persistence layer
			MemoryPersistence persistence = new MemoryPersistence();

			// Initialize the MQTT client
			client = new MqttClient(brockerUrl, clientId, persistence);

			// Set up the connection options
			MqttConnectOptions connectOptions = new MqttConnectOptions();
			connectOptions.setCleanSession(true);

			// Connect to the broker
			client.connect(connectOptions);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	public void disconnect() {
		try {
			client.disconnect();
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	public void publish(String topic, String message) {
		try {
			MqttMessage mqttMessage = new MqttMessage(message.getBytes("UTF-8"));
			client.publish(topic, mqttMessage);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void subscribe(String topic, IMqttMessageListener listener) {
		try {
			client.subscribe(topic, listener);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
}
