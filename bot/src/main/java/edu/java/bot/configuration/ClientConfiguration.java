package edu.java.bot.configuration;

import edu.java.bot.client.configuration.retry.RetryPolicy;
import edu.java.bot.client.scrapper.ScrapperClient;
import edu.java.bot.client.scrapper.ScrapperWebClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ClientConfiguration {
    @Value("${clients.scrapper.host}")
    private String scrapperHost;
    private final RetryPolicy retryPolicy;

    @Bean
    public ScrapperClient scrapperClient() {
        return new ScrapperWebClient(scrapperHost, retryPolicy);
    }
}
