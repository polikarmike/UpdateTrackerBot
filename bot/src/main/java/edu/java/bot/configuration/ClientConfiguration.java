package edu.java.bot.configuration;

import edu.java.bot.client.configuration.retry.BackOffStrategy;
import edu.java.bot.client.configuration.retry.RetryPolicy;
import edu.java.bot.client.configuration.retry.strategy.ConstantBackOffStrategy;
import edu.java.bot.client.configuration.retry.strategy.ExponentialBackOffStrategy;
import edu.java.bot.client.configuration.retry.strategy.LinearBackOffStrategy;
import edu.java.bot.client.scrapper.ScrapperClient;
import edu.java.bot.client.scrapper.ScrapperWebClient;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientConfiguration {
    @Value("${clients.scrapper.host}")
    private String scrapperHost;

    @Value("${clients.configurations.retry.max-attempts}")
    private int retryMaxAttempts;

    @Value("${clients.configurations.retry.interval}")
    private int retryInterval;

    @Value("${clients.configurations.retry.statuses}")
    private Set<Integer> retryStatuses;

    @Value("${clients.configurations.retry.strategy}")
    private String retryStrategy;

    @Bean
    public ScrapperClient scrapperClient() {
        return new ScrapperWebClient(scrapperHost, retryPolicy());
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
