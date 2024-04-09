package edu.java.bot.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScrapperQueueProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    @Value("${kafka.dlq}")
    private String dlqTopic;

    public void sendToDLQ(String payload) {
        kafkaTemplate.send(dlqTopic, payload);
    }
}
