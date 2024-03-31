package edu.java.bot.client.configuration.retry;

import reactor.util.retry.Retry;

public  interface BackOffStrategy {
    Retry getBackOff(int maxAttempts, int interval);
}
