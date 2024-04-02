package edu.java.scrapper.scheduler;

import edu.java.scrapper.service.LinkUpdater;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.scheduler", name = "enable", havingValue = "true")
@RequiredArgsConstructor
public class LinkUpdaterScheduler {
    private final LinkUpdater linkUpdater;

    @Scheduled(fixedDelayString = "${app.scheduler.interval}")
    public void update() {
        linkUpdater.update();
        log.info("Update method executed");
    }
}
