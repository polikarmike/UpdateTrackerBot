package edu.java.scrapper.client.configuration.retry.strategy;

import edu.java.scrapper.client.configuration.retry.BackOffStrategy;
import java.time.Duration;
import reactor.util.retry.Retry;

public class ExponentialBackOffStrategy implements BackOffStrategy {
    @Override
    public Retry getBackOff(int maxAttempts, int interval) {
        return Retry.backoff(maxAttempts, Duration.ofMillis(interval));
    }
}
