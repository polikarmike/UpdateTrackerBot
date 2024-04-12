package edu.java.scrapper.service.notificationservices;

import edu.java.common.dto.requests.LinkUpdateRequest;
import edu.java.scrapper.client.bot.BotClient;
import edu.java.scrapper.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "useQueue", havingValue = "false")
public class RestNotificationService implements NotificationService {
    private final BotClient botClient;

    @Override
    public void sendNotification(LinkUpdateRequest update) {
        botClient.sendUpdate(update);
    }
}
