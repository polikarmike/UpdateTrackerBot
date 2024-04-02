package edu.java.scrapper.scheduler;

import edu.java.scrapper.service.LinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.link-cleaner", name = "enable", havingValue = "true")
public class LinkCleanerScheduler {
    private final LinkService linkService;

    @Scheduled(cron = "${app.link-cleaner.time-cron-expression}")
    public int update() {
        log.info("link cleaner executed");
        return linkService.cleanUpUnusedLink();
    }
}
