package edu.java.scrapper.client.configuration.retry;

import edu.java.scrapper.exception.ServerException;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RetryPolicy {
    @Value("${clients.configurations.retry.statuses}")
    private final Set<Integer> retryStatuses;

    private final BackOffStrategy backOffStrategy;

    public ExchangeFilterFunction getRetryFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (retryStatuses.contains(clientResponse.statusCode().value())) {
                return Mono.<ClientResponse>error(new ServerException("Error status code"))
                    .retryWhen(backOffStrategy.getBackOff())
                    .onErrorResume(throwable -> Mono.error(new ServerException("Server temporarily unavailable")));
            } else {
                return Mono.just(clientResponse);
            }
        });
    }
}
