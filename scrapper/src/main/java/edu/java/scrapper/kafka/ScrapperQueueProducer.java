package edu.java.scrapper.kafka;

import edu.java.common.dto.requests.LinkUpdateRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ScrapperQueueProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topic}")
    private String topic;

    public ScrapperQueueProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(LinkUpdateRequest update) {
        kafkaTemplate.send(topic, update.toJson());
    }
}
