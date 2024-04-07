package edu.java.bot.client.scrapper;

import edu.java.bot.client.configuration.retry.RetryPolicy;
import edu.java.bot.exception.BadRequestException;
import edu.java.common.dto.requests.AddLinkRequest;
import edu.java.common.dto.requests.RemoveLinkRequest;
import edu.java.common.dto.responses.ApiErrorResponse;
import edu.java.common.dto.responses.ListLinksResponse;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
public class ScrapperWebClient implements ScrapperClient {
    private static final String DEFAULT_BASE_URL = "http://localhost:8080";
    public static final String LINKS_ENDPOINT = "/links";
    public static final String CHATS_ENDPOINT = "/chats/{id}";
    public static final String TG_CHAT_ID_HEADER = "Tg-Chat-Id";
    private final WebClient webClient;

    public ScrapperWebClient(RetryPolicy retryPolicy) {
        this.webClient = WebClient.builder()
            .baseUrl(DEFAULT_BASE_URL)
            .filter(retryPolicy.getRetryFilter())
            .build();
    }

    public ScrapperWebClient(String baseUrl, RetryPolicy retryPolicy) {
        this.webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .filter(retryPolicy.getRetryFilter())
            .build();
    }

    @Override
    public ListLinksResponse getAllLinks(long tgChatId) {
        return webClient.get()
            .uri(LINKS_ENDPOINT)
            .header(TG_CHAT_ID_HEADER, String.valueOf(tgChatId))
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, this::handle4xxError)
            .bodyToMono(ListLinksResponse.class)
//            .onErrorComplete()
            .block();

    }

    @Override
    public void addLink(long tgChatId, URI link) {
        AddLinkRequest request = new AddLinkRequest(link);
        webClient.post()
            .uri(LINKS_ENDPOINT)
            .header(TG_CHAT_ID_HEADER, String.valueOf(tgChatId))
            .bodyValue(request)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, this::handle4xxError)
            .toBodilessEntity()
//            .onErrorComplete()
            .block();


    }

    @Override
    public void removeLink(long tgChatId, URI link) {
        RemoveLinkRequest request = new RemoveLinkRequest(link);
        webClient.method(HttpMethod.DELETE)
            .uri(LINKS_ENDPOINT)
            .header(TG_CHAT_ID_HEADER, String.valueOf(tgChatId))
            .bodyValue(request)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, this::handle4xxError)
            .toBodilessEntity()
//            .onErrorComplete()
            .block();


    }

    @Override
    public String registerChat(Long tgChatId) {
        return webClient.post()
            .uri(CHATS_ENDPOINT, tgChatId)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, this::handle4xxError)
            .bodyToMono(String.class)
//            .onErrorComplete()
            .block();

    }

    @Override
    public String deleteChat(Long tgChatId) {
        return webClient.delete()
            .uri(CHATS_ENDPOINT, tgChatId)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, this::handle4xxError)
            .bodyToMono(String.class)
//            .onErrorComplete()
            .block();
    }

    private Mono<? extends Throwable> handle4xxError(ClientResponse clientResponse) {
        return clientResponse.bodyToMono(ApiErrorResponse.class)
            .flatMap(body -> {
                ApiErrorResponse errorResponse =
                    new ApiErrorResponse(body.description(),
                        body.code(),
                        body.exceptionName(),
                        body.exceptionMessage(),
                        body.stacktrace());

                BadRequestException exception = new BadRequestException(errorResponse);
                return Mono.error(exception);
            });
    }
}

