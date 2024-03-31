package edu.java.scrapper.configuration;

import edu.java.scrapper.client.bot.BotClient;
import edu.java.scrapper.client.bot.BotWebClient;
import edu.java.scrapper.client.configuration.retry.BackOffStrategy;
import edu.java.scrapper.client.configuration.retry.RetryPolicy;
import edu.java.scrapper.client.configuration.retry.strategy.ConstantBackOffStrategy;
import edu.java.scrapper.client.configuration.retry.strategy.ExponentialBackOffStrategy;
import edu.java.scrapper.client.configuration.retry.strategy.LinearBackOffStrategy;
import edu.java.scrapper.client.github.GitHubClient;
import edu.java.scrapper.client.github.GitHubWebClient;
import edu.java.scrapper.client.stackoverflow.StackOverflowClient;
import edu.java.scrapper.client.stackoverflow.StackOverflowWebClient;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientConfiguration {
    @Value("${clients.bot.host}")
    private String botHost;

    @Value("${clients.github.host}")
    private String githubHost;

    @Value("${clients.stack.host}")
    private String stackHost;

    @Value("${clients.configurations.retry.max-attempts}")
    private int retryMaxAttempts;

    @Value("${clients.configurations.retry.interval}")
    private int retryInterval;

    @Value("${clients.configurations.retry.statuses}")
    private Set<Integer> retryStatuses;

    @Value("${clients.configurations.retry.strategy}")
    private String retryStrategy;

    @Bean
    public BotClient botClient() {
        return new BotWebClient(botHost, retryPolicy());
    }

    @Bean
    public GitHubClient gitHubClient() {
        return new GitHubWebClient(githubHost, retryPolicy());
    }

    @Bean
    public StackOverflowClient stackOverflowClient() {
        return new StackOverflowWebClient(stackHost, retryPolicy());
    }

    @Bean
    public RetryPolicy retryPolicy() {
        return new RetryPolicy(retryMaxAttempts, retryInterval, retryStatuses, backOffStrategy());
    }

    @Bean
    public BackOffStrategy backOffStrategy() {
        return switch (retryStrategy) {
            case "constant" -> new ConstantBackOffStrategy();
            case "linear" -> new LinearBackOffStrategy();
            case "exponential" -> new ExponentialBackOffStrategy();
            default -> throw new IllegalArgumentException("Unknown retry strategy: " + retryStrategy);
        };
    }
}



