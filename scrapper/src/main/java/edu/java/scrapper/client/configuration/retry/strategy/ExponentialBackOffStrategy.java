package edu.java.scrapper.client.configuration.retry.strategy;

import edu.java.scrapper.client.configuration.retry.BackOffStrategy;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.util.retry.Retry;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "clients.configurations.retry", name = "strategy", havingValue = "exponential")
public class ExponentialBackOffStrategy implements BackOffStrategy {
    @Value("${clients.configurations.retry.max-attempts}")
    private int maxAttempts;

    @Value("${clients.configurations.retry.interval}")
    private int interval;

    public Retry getBackOff() {
        return Retry.backoff(maxAttempts, Duration.ofMillis(interval));
    }
}
