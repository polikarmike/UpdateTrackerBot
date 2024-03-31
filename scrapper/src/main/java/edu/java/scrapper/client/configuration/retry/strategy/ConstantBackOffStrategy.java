package edu.java.scrapper.client.configuration.retry.strategy;

import edu.java.scrapper.client.configuration.retry.BackOffStrategy;
import java.time.Duration;
import lombok.NoArgsConstructor;
import reactor.util.retry.Retry;

@NoArgsConstructor
public class ConstantBackOffStrategy implements BackOffStrategy {
    @Override
    public Retry getBackOff(int maxAttempts, int interval) {
        return Retry.fixedDelay(maxAttempts, Duration.ofSeconds(interval));
    }
}
