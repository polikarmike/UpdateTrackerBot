package edu.java.bot.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.java.bot.service.LinkUpdateService;
import edu.java.common.dto.requests.LinkUpdateRequest;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaUpdateListener {

    private final LinkUpdateService linkUpdateService;
    private final ScrapperQueueProducer scrapperQueueProducer;

    @Value("${kafka.dlq}")
    private String dlqTopic;

    @KafkaListener(topics = "${kafka.topic}")
    public void listen(String update) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            LinkUpdateRequest updates = objectMapper.readValue(update, LinkUpdateRequest.class);
            linkUpdateService.processLinkUpdate(updates);
        } catch (IOException e) {
            scrapperQueueProducer.sendToDLQ(update);
            log.error("Error processing link update: {}", e.getMessage());
        }
    }
}
