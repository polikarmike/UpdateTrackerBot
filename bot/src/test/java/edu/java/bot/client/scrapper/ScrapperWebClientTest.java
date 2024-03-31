package edu.java.bot.client.scrapper;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import edu.java.bot.client.configuration.retry.RetryPolicy;
import edu.java.bot.client.configuration.retry.strategy.ConstantBackOffStrategy;
import edu.java.common.dto.responses.LinkResponse;
import edu.java.common.dto.responses.ListLinksResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScrapperWebClientTest {

    private WireMockServer wireMockServer;
    private ScrapperWebClient client;

    @BeforeEach
    public void setup() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
        Set<Integer> emptyCodes = new HashSet<>(); ;
        RetryPolicy retryPolicy = new RetryPolicy(0,0, emptyCodes, new ConstantBackOffStrategy());
        client = new ScrapperWebClient("http://localhost:" + wireMockServer.port(), retryPolicy);
    }

    @Test
    public void testGetAllLinks() {
        String responseBody = "{\"links\":[{\"id\":1,\"url\":\"http://example.com\"}],\"size\":1}";

        WireMock.stubFor(WireMock.get(urlEqualTo("/links"))
            .withHeader("Tg-Chat-Id", equalTo("12345"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(responseBody)));

        ListLinksResponse expectedResponse = new ListLinksResponse(
            Collections.singletonList(new LinkResponse(1L, URI.create("http://example.com"))), 1);

        ListLinksResponse actualResponse = client.getAllLinks(12345L);
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void testAddLink() {
        WireMock.stubFor(WireMock.post(urlEqualTo("/links"))
            .withHeader("Tg-Chat-Id", equalTo("12345"))
            .willReturn(aResponse()
                .withStatus(200)));

        URI link = URI.create("http://example.com");
        client.addLink(12345L, link);

        WireMock.verify(postRequestedFor(urlEqualTo("/links"))
            .withHeader("Tg-Chat-Id", equalTo("12345"))
            .withRequestBody(equalTo("{\"link\":\"http://example.com\"}")));
    }

    @Test
    public void testRemoveLink() {
        WireMock.stubFor(WireMock.delete(urlEqualTo("/links"))
            .withHeader("Tg-Chat-Id", equalTo("12345"))
            .willReturn(aResponse()
                .withStatus(200)));

        URI link = URI.create("http://example.com");
        client.removeLink(12345L, link);

        WireMock.verify(deleteRequestedFor(urlEqualTo("/links"))
            .withHeader("Tg-Chat-Id", equalTo("12345"))
            .withRequestBody(equalTo("{\"link\":\"http://example.com\"}")));
    }

    @Test
    public void testRegisterChat() {
        WireMock.stubFor(WireMock.post(urlEqualTo("/chats/12345"))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody("Chat registered")));

        String response = client.registerChat(12345L);

        assertEquals("Chat registered", response);
    }

    @Test
    public void testDeleteChat() {
        WireMock.stubFor(WireMock.delete(urlEqualTo("/chats/12345"))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody("Chat deleted")));

        String response = client.deleteChat(12345L);


        assertEquals("Chat deleted", response);
    }

    @AfterEach
    public void teardown() {
        wireMockServer.stop();
    }
}
