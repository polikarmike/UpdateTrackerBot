package edu.java.scrapper.client.stackoverflow;

import edu.java.scrapper.client.configuration.retry.RetryPolicy;
import edu.java.scrapper.dto.stackoverflow.SOQuestResponse;
import edu.java.scrapper.dto.stackoverflow.SQQuestAnswerResponse;
import org.springframework.web.reactive.function.client.WebClient;

public class StackOverflowWebClient implements StackOverflowClient {
    private static final String DEFAULT_BASE_URL = "https://api.stackexchange.com/2.3/";
    private static final String QUESTION_ENDPOINT_TEMPLATE = "/questions/{questionId}?site=stackoverflow";
    private static final String ANSWER_ENDPOINT_TEMPLATE = "/questions/{questionId}/answers?site=stackoverflow";
    private final WebClient webClient;

    public StackOverflowWebClient(RetryPolicy retryPolicy) {
        this.webClient = WebClient.builder()
            .baseUrl(DEFAULT_BASE_URL)
            .filter(retryPolicy.getRetryFilter())
            .build();
    }

    public StackOverflowWebClient(String baseUrl, RetryPolicy retryPolicy) {
        this.webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .filter(retryPolicy.getRetryFilter())
            .build();
    }

    @Override
    public SOQuestResponse fetchQuestion(String questionId) {
        return webClient.get()
            .uri(QUESTION_ENDPOINT_TEMPLATE, questionId)
            .retrieve()
            .bodyToMono(SOQuestResponse.class)
            .block();
    }

    @Override
    public SQQuestAnswerResponse fetchAnswers(String questionId) {
        return webClient.get()
            .uri(ANSWER_ENDPOINT_TEMPLATE, questionId)
            .retrieve()
            .bodyToMono(SQQuestAnswerResponse.class)
            .block();
    }
}
