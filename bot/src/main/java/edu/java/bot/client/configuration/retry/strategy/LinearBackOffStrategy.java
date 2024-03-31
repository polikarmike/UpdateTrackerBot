package edu.java.bot.client.configuration.retry.strategy;

import edu.java.bot.client.configuration.retry.BackOffStrategy;
import edu.java.bot.exception.ClientException;
import java.time.Duration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

public class LinearBackOffStrategy implements BackOffStrategy {
    @Override
    public Retry getBackOff(int maxAttempts, int interval) {
        return Retry.from(companion -> companion
            .zipWith(Flux.range(1, maxAttempts), (error, index) -> index)
            .flatMap(index -> {
                if (index >= maxAttempts) {
                    return Mono.error(new ClientException(("Max attempts reached")));
                }

                return Mono.delay(Duration.ofMillis((long) index * interval));
            }));
    }
}
