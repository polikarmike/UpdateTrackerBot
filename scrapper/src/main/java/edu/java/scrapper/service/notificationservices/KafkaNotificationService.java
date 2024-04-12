package edu.java.scrapper.service.notificationservices;

import edu.java.common.dto.requests.LinkUpdateRequest;
import edu.java.scrapper.kafka.ScrapperQueueProducer;
import edu.java.scrapper.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "useQueue", havingValue = "true")
public class KafkaNotificationService implements NotificationService {
    private final ScrapperQueueProducer scrapperQueueProducer;

    @Override
    public void sendNotification(LinkUpdateRequest update) {
        scrapperQueueProducer.send(update);
    }
}
