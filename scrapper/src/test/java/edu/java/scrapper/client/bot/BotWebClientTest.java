package edu.java.scrapper.client.bot;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import edu.java.common.dto.requests.LinkUpdateRequest;
import edu.java.scrapper.client.configuration.retry.RetryPolicy;
import edu.java.scrapper.client.configuration.retry.strategy.ConstantBackOffStrategy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BotWebClientTest {
    private WireMockServer wireMockServer;
    private BotWebClient client;

    @BeforeEach
    public void setup() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
        Set<Integer> emptyCodes = new HashSet<>(); ;
        RetryPolicy retryPolicy = new RetryPolicy(emptyCodes, new ConstantBackOffStrategy());
        client = new BotWebClient("http://localhost:" + wireMockServer.port(), retryPolicy);
    }

    @Test
    public void testSendUpdate() {
        LinkUpdateRequest updateRequest = new LinkUpdateRequest(1L, URI.create("http://example.com"),
            "description",
            Arrays.asList(12345L, 67890L));

        String requestBody = "{\"id\":1,\"url\":\"http://example.com\",\"description\":\"description\",\"tgChatIds\":[12345,67890]}";

        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/updates"))
            .withRequestBody(WireMock.equalTo(requestBody))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody("Update sent")));

        String response = client.sendUpdate(updateRequest);

        assertEquals("Update sent", response);

        WireMock.verify(postRequestedFor(urlEqualTo("/updates"))
            .withRequestBody(WireMock.equalTo(requestBody)));
    }

    @AfterEach
    public void teardown() {
        wireMockServer.stop();
    }
}
