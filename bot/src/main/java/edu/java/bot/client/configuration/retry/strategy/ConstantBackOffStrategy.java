package edu.java.bot.client.configuration.retry.strategy;

import edu.java.bot.client.configuration.retry.BackOffStrategy;
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
