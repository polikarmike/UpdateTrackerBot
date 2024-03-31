package edu.java.bot.client.configuration.retry;

import edu.java.bot.exception.ClientException;
import java.util.Set;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

public class RetryPolicy {
    private final Set<Integer> retryStatuses;
    private final BackOffStrategy backOffStrategy;
    private final int maxAttempts;
    private final int interval;

    public RetryPolicy(int maxAttempts, int interval, Set<Integer> retryStatuses, BackOffStrategy backOffStrategy) {
        this.maxAttempts = maxAttempts;
        this.interval = interval;
        this.backOffStrategy = backOffStrategy;
        this.retryStatuses = retryStatuses;
    }

    public ExchangeFilterFunction getRetryFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (retryStatuses.contains(clientResponse.statusCode().value())) {
                return Mono.<ClientResponse>error(new ClientException("Error status code"))
                    .retryWhen(backOffStrategy.getBackOff(maxAttempts, interval));
            } else {
                return Mono.just(clientResponse);
            }
        });
    }
}
