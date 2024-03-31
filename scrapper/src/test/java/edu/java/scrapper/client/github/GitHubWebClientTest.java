package edu.java.scrapper.client.github;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import edu.java.scrapper.client.configuration.retry.RetryPolicy;
import edu.java.scrapper.client.configuration.retry.strategy.ConstantBackOffStrategy;
import edu.java.scrapper.client.configuration.retry.strategy.LinearBackOffStrategy;
import edu.java.scrapper.dto.github.GHEventResponse;
import edu.java.scrapper.dto.github.GHRepoResponse;
import edu.java.scrapper.exception.ServerException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;
import reactor.core.Exceptions;
import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Component
public class GitHubWebClientTest {
    private static final int WIREMOCK_PORT = 8089;
    private static WireMockServer wireMockServer;

    @BeforeAll
    public static void setUp() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(WIREMOCK_PORT));
        wireMockServer.start();
        WireMock.configureFor("localhost", WIREMOCK_PORT);
    }

    @AfterAll
    public static void tearDown() {
        wireMockServer.stop();
    }

    @Test
    @DisplayName("Тестирование получения репозитория")
    public void fetchRepositoryTest() {
        // given
        String jsonResponse = """
                {
                  "name": "Hello-World",
                  "html_url": "https://github.com/octocat/Hello-World",
                  "pushed_at": "2011-01-26T19:06:43Z",
                  "created_at": "2011-01-26T19:01:12Z",
                  "updated_at": "2022-02-24T16:40:42Z"
                }
                """;

        stubFor(get(urlEqualTo("/repos/octocat/Hello-World"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(jsonResponse)));

        Set<Integer> emptyCodes = new HashSet<>(); ;
        RetryPolicy retryPolicy = new RetryPolicy(0,0, emptyCodes, new ConstantBackOffStrategy());

        GitHubClient gitHubClient = new GitHubWebClient("http://localhost:" + WIREMOCK_PORT, retryPolicy);

        // when
        GHRepoResponse response = gitHubClient.fetchRepository("octocat", "Hello-World");

        // then
        assertNotNull(response);
        assertEquals("Hello-World", response.repoName());
        assertEquals("https://github.com/octocat/Hello-World", response.link());
        assertEquals(OffsetDateTime.parse("2011-01-26T19:06:43Z"), response.lastActivityDate());
        assertEquals(OffsetDateTime.parse("2011-01-26T19:01:12Z"), response.createdAt());
        assertEquals(OffsetDateTime.parse("2022-02-24T16:40:42Z"), response.updateAt());
    }

    @Test
    @DisplayName("Тестирование получения событий")
    public void fetchEventsTest() {
        // given
        String jsonResponse = """
            {
              "type": "PushEvent",
              "payload": {
                "issue": {
                  "title": "Issue title"
                },
                "pull_request": {
                  "title": "PR title"
                }
              },
              "created_at": "2022-02-24T16:40:42Z"
            }
            """;

        stubFor(get(urlEqualTo("/repos/octocat/Hello-World/events"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(jsonResponse)));

        Set<Integer> emptyCodes = new HashSet<>(); ;
        RetryPolicy retryPolicy = new RetryPolicy(0,0, emptyCodes, new ConstantBackOffStrategy());

        GitHubClient gitHubClient = new GitHubWebClient("http://localhost:" + WIREMOCK_PORT, retryPolicy);

        // when
        GHEventResponse response = gitHubClient.fetchEvents("octocat", "Hello-World");

        // then
        assertNotNull(response);
        assertEquals("PushEvent", response.type());
        assertEquals("Issue title", response.payload().issue().title());
        assertEquals("PR title", response.payload().pullRequest().title());
        assertEquals(OffsetDateTime.parse("2022-02-24T16:40:42Z"), response.createdAt());
    }

    @Test
    @DisplayName("Тестирование получения репозитория с повторными попытками")
    public void fetchRepositoryTestWithRetry() {
        // given
        stubFor(get(urlEqualTo("/repos/octocat/Hello-World"))
            .willReturn(aResponse()
                .withStatus(500)
                .withBody("Server Error")));

        Set<Integer> retryStatuses = Set.of(500);
        RetryPolicy retryPolicy = new RetryPolicy(3, 1, retryStatuses, new LinearBackOffStrategy());

        GitHubClient gitHubClient = new GitHubWebClient("http://localhost:" + WIREMOCK_PORT, retryPolicy);

        assertThrows(Throwable.class, () -> {
            gitHubClient.fetchRepository("octocat", "Hello-World");
        });
    }


    @Test
    @DisplayName("Тестирование получения событий с повторными попытками")
    public void fetchEventsTestWithRetry() {
        stubFor(get(urlEqualTo("/repos/octocat/Hello-World/events"))
            .willReturn(aResponse()
                .withStatus(500)
                .withBody("Server Error")));

        Set<Integer> retryStatuses = Set.of(500);
        RetryPolicy retryPolicy = new RetryPolicy(3, 1, retryStatuses, new ConstantBackOffStrategy());

        GitHubClient gitHubClient = new GitHubWebClient("http://localhost:" + WIREMOCK_PORT, retryPolicy);


        assertThrows(Throwable.class, () -> {
            gitHubClient.fetchEvents("octocat", "Hello-World");
        });

    }
}

