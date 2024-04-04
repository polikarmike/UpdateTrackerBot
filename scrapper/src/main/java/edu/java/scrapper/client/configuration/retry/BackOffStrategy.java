package edu.java.scrapper.client.configuration.retry;

import reactor.util.retry.Retry;

public  interface BackOffStrategy {
    Retry getBackOff();
}
