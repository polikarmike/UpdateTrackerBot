# Update Tracker Bot

## Project Description

This project is a Telegram bot that allows users to track content updates from various web resources, such as StackOverflow and GitHub. Users can add or remove links for tracking, and the bot will notify them of any new changes.

## Key Features

- **User Registration**: Users can register with the bot through Telegram.
- **Link Tracking**: Users can add or remove links for tracking through chat commands, such as `/track https://stackoverflow.com/questions/292357`.
- **Notifications**: The bot sends notifications to users about new changes, such as new answers on StackOverflow or changes in GitHub repositories.

## Architecture
The project is written in `Java 21` using `Spring Boot 3`.

The system consists of two main services:
1. **Scrapper** — responsible for collecting data from tracked resources.
2. **Bot** — handles user commands and sends notifications.

### Components

- **PostgreSQL**: For data storage.
- **Apache Kafka**: For asynchronous messaging between services.
- **Prometheus and Grafana**: For metrics collection and visualization.
- **Spring Framework**: The main application framework.
- **JDBC, JOOQ, Hibernate (JPA)**: For database interaction.
- **Bucket4j**: For rate limiting by IP.

### System Workflow

```
                                +-----------------+
+-------------------+           |                 |
|                   |      /-----    Postgres     |
|    Scrapper       |------     |                 |
|                   |--         +-----------------+
+-------------------+  \---
               |           \--- +-----------------+
               |               \-                 |
               | HTTP           |     Kafka       |
               |               /-                 |
               |           /--- +-----------------+
+-------------------+  /---
|                   |--
|       Bot         |           +-----------------+
|                   |------------                 |
+-------------------+           |  Telegram API   |
                                |                 |
                                +-----------------+
```

## Configurations

### Bot Configurations:

```yaml
app:
  telegram-token: ${TELEGRAM_TOKEN} # Token for working with Telegram API

management:
  endpoints:
    web:
      exposure:
        include: "info,health,prometheus" # Available endpoints for monitoring
      base-path: / # Base path for all endpoints
      path-mapping:
        prometheus: "metrics" # Path for Prometheus metrics
  metrics:
    tags:
      application: "${spring.application.name}" # Metrics tag with application name
  server:
    port: 8095 # Port on which the service operates

kafka:
  bootstrapAddress: localhost:9092 # Kafka broker address
  groupId: UTBGroupId # Group ID for Kafka consumers
  topic: message-update # Topic for sending updates
  dlq: UTB_dlq # Dead letter queue topic

spring:
  application:
    name: bot # Application name
  jackson:
    time-zone: UTC # Default time zone for date serialization
  liquibase:
    enabled: false # Disable Liquibase
  cache:
    cache-names:
      - rate-limit-buckets # Cache name for rate limiting
    caffeine:
      spec: maximumSize=100000,expireAfterAccess=3600s # Caffeine cache configuration

bucket4j:
  enabled: true # Enable rate limiting
  filters:
    - cache-name: rate-limit-buckets
      strategy: first
      http-response-body: "{ \"status\": 429, \"error\": \"Too Many Requests\", \"message\": \"You have exhausted your API Request Quota\" }"
      rate-limits:
        - cache-key: "getRemoteAddr()"
          bandwidths:
            - capacity: 100 # Request limit per second
              time: 1
              unit: seconds

springdoc:
  swagger-ui:
    path: /swagger-ui # Path to Swagger UI

clients:
  scrapper:
    host: http://localhost:8080 # Scrapper service address
  configurations:
    retry:
      strategy: constant # Retry strategy on errors
      max-attempts: 3 # Maximum retry attempts
      interval: 2 # Interval between attempts (in seconds)
      statuses: 500, 501 # HTTP statuses that trigger retries

server:
  port: 8090 # Port on which the bot operates

logging:
  config: classpath:log4j2-plain.xml # Logging configuration
```

### Scrapper Configurations:

```yaml
app:
  useQueue: true # Use message queue
  scheduler:
    enable: true # Enable task scheduler
    interval: 20000 # Task execution interval (in milliseconds)
    force-check-delay: 10000 # Delay before forced checks
  link-updater:
    batch-size: 10 # Batch size for link updates
  link-cleaner:
    enable: true # Enable old link cleaning
    time-cron-expression: 0 0 2 * * ? # Cleaning schedule (every day at 2:00 AM)
  database-access-type: jpa # Database access type (JPA)

management:
  endpoints:
    web:
      exposure:
        include: "info,health,prometheus" # Available endpoints for monitoring
      base-path: / # Base path for all endpoints
      path-mapping:
        prometheus: "metrics" # Path for Prometheus metrics
  metrics:
    tags:
      application: "${spring.application.name}" # Metrics tag with application name
  server:
    port: 8085 # Port on which the service operates

kafka:
  bootstrapAddress: localhost:9092 # Kafka broker address
  groupId: UTBGroupId # Group ID for Kafka consumers
  topic: message-update # Topic for sending updates

clients:
  github:
    host: https://api.github.com # GitHub API address
  stack:
    host: https://api.stackexchange.com/2.3 # StackExchange API address
  bot:
    host: http://localhost:8090 # Bot service address
  strategy: constant # Retry strategy
  configurations:
    retry:
      strategy: constant # Retry strategy on errors
      max-attempts: 3 # Maximum retry attempts
      interval: 2 # Interval between attempts (in seconds)
      statuses: 500, 501 # HTTP statuses that trigger retries

spring:
  application:
    name: scrapper # Application name
  liquibase:
    enabled: false # Disable Liquibase
  datasource:
    url: jdbc:postgresql://localhost:5432/scrapper # Database URL
    username: postgres # Database username
    password: postgres # Database password
    driver-class-name: org.postgresql.Driver # Database driver
  cache:
    cache-names:
      - rate-limit-buckets # Cache name for rate limiting
    caffeine:
      spec: maximumSize=100000,expireAfterAccess=3600s # Caffeine cache configuration

springdoc:
  swagger-ui:
    path: /swagger-ui # Path to Swagger UI

server:
  port: 8080 # Port on which the scrapper operates

logging:
  config: classpath:log4j2-plain.xml # Logging configuration
```
