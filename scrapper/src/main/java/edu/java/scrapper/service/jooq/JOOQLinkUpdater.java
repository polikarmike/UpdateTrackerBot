package edu.java.scrapper.service.jooq;

import edu.java.common.dto.requests.LinkUpdateRequest;
import edu.java.scrapper.client.bot.BotClient;
import edu.java.scrapper.dto.entity.Link;
import edu.java.scrapper.service.LinkService;
import edu.java.scrapper.service.LinkUpdater;
import edu.java.scrapper.updater.Updater;
import edu.java.scrapper.updater.UpdaterHolder;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

@RequiredArgsConstructor
public class JOOQLinkUpdater implements LinkUpdater {
    private final LinkService linkService;
    private final UpdaterHolder updaterHolder;
    private final BotClient botClient;

    @Value("${app.link-updater.batch-size}")
    private int batchSize;

    @Override
    public int update() {
        List<Link> links = linkService.findOldestLinks(batchSize);
        int updatedCount = 0;
        for (Link link : links) {
            Optional<Updater> optionalUpdater = updaterHolder.getUpdaterByHost(link.getUri().getHost());
            Optional<String> message = optionalUpdater.map(updater -> updater.getUpdateMessage(link));

            if (message.isPresent()) {
                List<Long> chatsIds = linkService.getChatIdsByLinkId(link.getId());
                botClient.sendUpdate(new LinkUpdateRequest(link.getId(), link.getUri(), message.get(), chatsIds));
            }

            linkService.updateLastUpdatedTime(link.getId());

            updatedCount++;
        }

        return updatedCount;
    }
}
