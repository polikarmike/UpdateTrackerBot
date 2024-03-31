package edu.java.scrapper.client.configuration.retry.strategy;

import edu.java.scrapper.client.configuration.retry.BackOffStrategy;
import edu.java.scrapper.exception.ServerException;
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
                    return Mono.error(new ServerException(("Max attempts reached")));
                }

                return Mono.delay(Duration.ofMillis((long) index * interval));
            }));
    }
}
