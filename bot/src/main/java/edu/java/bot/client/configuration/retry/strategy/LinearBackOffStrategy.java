package edu.java.bot.client.configuration.retry.strategy;

import edu.java.bot.client.configuration.retry.BackOffStrategy;
import edu.java.bot.exception.ClientException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "clients.configurations.retry", name = "strategy", havingValue = "linear")
public class LinearBackOffStrategy implements  BackOffStrategy {
    @Value("${clients.configurations.retry.max-attempts}")
    private int maxAttempts;

    @Value("${clients.configurations.retry.interval}")
    private int interval;

    public Retry getBackOff() {
        return Retry.from(companion -> companion
            .zipWith(Flux.range(1, maxAttempts), (error, index) -> index)
            .flatMap(index -> {
                if (index >= maxAttempts) {
                    return Mono.error(new ClientException(("Server temporarily unavailable")));
                }

                return Mono.delay(Duration.ofMillis((long) index * interval));
            }));
    }
}
