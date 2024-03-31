package edu.java.scrapper.client.bot;

import edu.java.common.dto.requests.LinkUpdateRequest;
import edu.java.common.dto.responses.ApiErrorResponse;
import edu.java.scrapper.client.configuration.retry.RetryPolicy;
import edu.java.scrapper.exception.BadRequestException;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class BotWebClient implements BotClient {
    private static final String DEFAULT_BASE_URL = "http://localhost:8080";
    private final WebClient webClient;

    public BotWebClient(RetryPolicy retryPolicy) {
        this.webClient = WebClient.builder()
            .baseUrl(DEFAULT_BASE_URL)
            .filter(retryPolicy.getRetryFilter())
            .build();
    }

    public BotWebClient(String baseUrl, RetryPolicy retryPolicy) {
        this.webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .filter(retryPolicy.getRetryFilter())
            .build();
    }

    @Override
    public String sendUpdate(LinkUpdateRequest updateRequest) {
        return webClient.post()
            .uri("/updates")
            .body(BodyInserters.fromValue(updateRequest))
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, this::handle4xxError)
            .bodyToMono(String.class)
            .block();
    }

    private Mono<? extends Throwable> handle4xxError(ClientResponse clientResponse) {
        return clientResponse.bodyToMono(ApiErrorResponse.class)
            .flatMap(body -> {
                ApiErrorResponse errorResponse =
                    new ApiErrorResponse(body.description(),
                        body.code(),
                        body.toString(),
                        body.exceptionName(),
                        body.stacktrace());

                BadRequestException exception = new BadRequestException(errorResponse);
                return Mono.error(exception);
            });
    }
}
