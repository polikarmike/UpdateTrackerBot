package edu.java.scrapper.service;

import edu.java.common.dto.requests.LinkUpdateRequest;
import edu.java.scrapper.client.bot.BotClient;
import edu.java.scrapper.kafka.ScrapperQueueProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private final ScrapperQueueProducer scrapperQueueProducer;
    private final BotClient botClient;

    @Value("${app.useQueue}")
    private boolean useQueue;

    public NotificationService(ScrapperQueueProducer scrapperQueueProducer, BotClient botClient) {
        this.scrapperQueueProducer = scrapperQueueProducer;
        this.botClient = botClient;
    }

    public void sendNotification(LinkUpdateRequest update) {
        if (useQueue) {
            scrapperQueueProducer.send(update);
        } else {
            botClient.sendUpdate(update);
        }
    }
}
