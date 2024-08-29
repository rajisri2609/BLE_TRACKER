
import org.eclipse.paho.client.mqttv3.*

fun main() {
    val broker = "tcp://test.mosquitto.org:1883"
    val topic = "IATM_BLEData"
    val message = "Hello MQTT from Kotlin!"
    val clientId = "8f5121fd4d894aae82ade040af487416" // Replace with your desired client ID

    val mqttClient = MqttAsyncClient(broker, clientId)

    val options = MqttConnectOptions()
    options.isAutomaticReconnect = true // Enable automatic reconnect

    mqttClient.setCallback(object : MqttCallbackExtended {
        override fun connectComplete(reconnect: Boolean, serverURI: String) {
            println("Connected to MQTT broker: $serverURI")
            if (reconnect) {
                println("Reconnected to MQTT broker")
            }
            // Publish message after reconnecting
            val mqttMessage = MqttMessage(message.toByteArray())
            mqttClient.publish(topic, mqttMessage)
            println("Message published: $message")
        }

        override fun connectionLost(cause: Throwable?) {
            println("Connection lost to MQTT broker: ${cause?.message}")
        }

        override fun messageArrived(topic: String?, message: MqttMessage?) {
            // Handle incoming messages if needed
        }

        override fun deliveryComplete(token: IMqttDeliveryToken?) {
            // Called when message delivery is complete
        }
    })

    try {
        mqttClient.connect(options)

        // Subscribe to topics if needed
        // mqttClient.subscribe(topic)

        // Handle other operations as needed

    } catch (e: Exception) {
        e.printStackTrace()
    }
}

