package fr.tp.inf112.projects.robotsim.model.notifier;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.tp.inf112.projects.robotsim.app.RemoteSimulatorController;
import fr.tp.inf112.projects.robotsim.utils.SimulationServiceUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.logging.Level;

import static fr.tp.inf112.projects.robotsim.app.SimulatorApplication.LOGGER;

public class FactorySimulationEventConsumer {

    private final KafkaConsumer<String, String> consumer;
    private final RemoteSimulatorController controller;

    public FactorySimulationEventConsumer(final RemoteSimulatorController controller) {
        this.controller = controller;
        final Properties props = SimulationServiceUtils.getDefaultConsumerProperties();
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        this.consumer = new KafkaConsumer<>(props);
        final String topicName = "simulation-" + controller.getCanvas().getId();
        this.consumer.subscribe(Collections.singletonList(topicName));
    }

    public void consumeMessages() {
        try {
            while (controller.isAnimationRunning()) {
                final ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (final ConsumerRecord<String, String> record : records) {
                    LOGGER.fine("Received JSON Factory text '" + record.value() + "'.");
                    try {
                        controller.setCanvas(controller.readFactory(record.value()));
                    } catch (JsonProcessingException e) {
                        LOGGER.log(Level.SEVERE, "Une erreur est survenue", e);
                    }
                }
            }
        }
        finally {
            consumer.close();
        }
    }

}
