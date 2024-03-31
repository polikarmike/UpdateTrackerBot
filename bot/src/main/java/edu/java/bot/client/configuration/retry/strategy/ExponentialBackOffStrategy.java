package edu.java.bot.client.configuration.retry.strategy;

import edu.java.bot.client.configuration.retry.BackOffStrategy;
import java.time.Duration;
import reactor.util.retry.Retry;

public class ExponentialBackOffStrategy implements BackOffStrategy {
    @Override
    public Retry getBackOff(int maxAttempts, int interval) {
        return Retry.backoff(maxAttempts, Duration.ofMillis(interval));
    }
}
