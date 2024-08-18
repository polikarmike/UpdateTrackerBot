# Update Tracker Bot

## Описание проекта

Этот проект представляет собой Telegram-бота, который позволяет пользователям отслеживать обновления контента по ссылкам на различные веб-ресурсы, такие как StackOverflow и GitHub. Пользователи могут добавлять или удалять ссылки для отслеживания, а бот будет уведомлять их о новых изменениях.

## Основные функции

- **Регистрация пользователей**: пользователи могут зарегистрироваться в боте через Telegram.
- **Отслеживание ссылок**: пользователи могут добавлять или удалять ссылки для отслеживания через команды в чате, такие как `/track https://stackoverflow.com/questions/292357`.
- **Уведомления**: бот отправляет уведомления пользователям о новых изменениях, таких как новые ответы на вопросы на StackOverflow или изменения в репозиториях GitHub.

## Архитектура
Проект написан на `Java 21` с использованием `Spring Boot 3`.

Система состоит из двух основных сервисов:
1. **Scrapper** — отвечает за сбор данных с отслеживаемых ресурсов.
2. **Bot** — обрабатывает команды пользователей и отправляет уведомления.

### Компоненты

- **PostgreSQL**: для хранения данных.
- **Apache Kafka**: для асинхронного обмена сообщениями между сервисами.
- **Prometheus и Grafana**: для сбора и визуализации метрик.
- **Spring Framework**: основной каркас приложения.
- **JDBC, JOOQ, Hibernate (JPA)**: для работы с базой данных.
- **Bucket4j**: для ограничения запросов по IP.

### Схема работы системы

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

## Конфигурации

### Конфигурации для бота:

```yaml
app:
  telegram-token: ${TELEGRAM_TOKEN} # Токен для работы с Telegram API

management:
  endpoints:
    web:
      exposure:
        include: "info,health,prometheus" # Доступные эндпоинты для мониторинга
      base-path: / # Базовый путь для всех эндпоинтов
      path-mapping:
        prometheus: "metrics" # Путь для метрик Prometheus
  metrics:
    tags:
      application: "${spring.application.name}" # Тег метрик с названием приложения
  server:
    port: 8095 # Порт, на котором работает сервис

kafka:
  bootstrapAddress: localhost:9092 # Адрес Kafka брокера
  groupId: UTBGroupId # ID группы для Kafka консюмеров
  topic: message-update # Топик для отправки обновлений
  dlq: UTB_dlq # Топик для мёртвых сообщений (DLQ)

spring:
  application:
    name: bot # Название приложения
  jackson:
    time-zone: UTC # Часовой пояс по умолчанию для сериализации дат
  liquibase:
    enabled: false # Отключение Liquibase
  cache:
    cache-names:
      - rate-limit-buckets # Название кэша для ограничения запросов
    caffeine:
      spec: maximumSize=100000,expireAfterAccess=3600s # Конфигурация кэша Caffeine

bucket4j:
  enabled: true # Включение ограничения запросов
  filters:
    - cache-name: rate-limit-buckets
      strategy: first
      http-response-body: "{ \"status\": 429, \"error\": \"Too Many Requests\", \"message\": \"You have exhausted your API Request Quota\" }"
      rate-limits:
        - cache-key: "getRemoteAddr()"
          bandwidths:
            - capacity: 100 # Лимит запросов в секунду
              time: 1
              unit: seconds

springdoc:
  swagger-ui:
    path: /swagger-ui # Путь к Swagger UI

clients:
  scrapper:
    host: http://localhost:8080 # Адрес сервиса Scrapper
  configurations:
    retry:
      strategy: constant # Стратегия повторных попыток при ошибках
      max-attempts: 3 # Максимальное количество попыток
      interval: 2 # Интервал между попытками (в секундах)
      statuses: 500, 501 # Статусы HTTP, при которых будут выполняться повторные попытки

server:
  port: 8090 # Порт, на котором работает бот

logging:
  config: classpath:log4j2-plain.xml # Конфигурация логирования
```

### Конфигурации для скраппера:

```yaml
app:
  useQueue: true # Использование очереди сообщений
  scheduler:
    enable: true # Включение планировщика задач
    interval: 20000 # Интервал выполнения задач (в миллисекундах)
    force-check-delay: 10000 # Задержка перед принудительной проверкой
  link-updater:
    batch-size: 10 # Размер партии для обновления ссылок
  link-cleaner:
    enable: true # Включение очистки старых ссылок
    time-cron-expression: 0 0 2 * * ? # Расписание очистки (каждый день в 2:00)
  database-access-type: jpa # Используемый тип доступа к базе данных (JPA)

management:
  endpoints:
    web:
      exposure:
        include: "info,health,prometheus" # Доступные эндпоинты для мониторинга
      base-path: / # Базовый путь для всех эндпоинтов
      path-mapping:
        prometheus: "metrics" # Путь для метрик Prometheus
  metrics:
    tags:
      application: "${spring.application.name}" # Тег метрик с названием приложения
  server:
    port: 8085 # Порт, на котором работает сервис

kafka:
  bootstrapAddress: localhost:9092 # Адрес Kafka брокера
  groupId: UTBGroupId # ID группы для Kafka консюмеров
  topic: message-update # Топик для отправки обновлений

clients:
  github:
    host: https://api.github.com # Адрес API GitHub
  stack:
    host: https://api.stackexchange.com/2.3 # Адрес API StackExchange
  bot:
    host: http://localhost:8090 # Адрес сервиса Bot
  strategy: constant # Стратегия повторных попыток
  configurations:
    retry:
      strategy: constant # Стратегия повторных попыток при ошибках
      max-attempts: 3 # Максимальное количество попыток
      interval: 2 # Интервал между попытками (в секундах)
      statuses: 500, 501 # Статусы HTTP, при которых будут выполняться повторные попытки

spring:
  application:
    name: scrapper # Название приложения
  liquibase:
    enabled: false # Отключение Liquibase
  datasource:
    url: jdbc:postgresql://localhost:5432/scrapper # URL базы данных
    username: postgres # Имя пользователя базы данных
    password: postgres # Пароль пользователя базы данных
    driver-class-name: org.postgresql.Driver # Драйвер базы данных
  cache:
    cache-names:
      - rate-limit-buckets # Название кэша для ограничения запросов
    caffeine:
      spec: maximumSize=100000,expireAfterAccess=3600s # Конфигурация кэша Caffeine

springdoc:
  swagger-ui:
    path: /swagger-ui # Путь к Swagger UI

server:
  port: 8080 # Порт, на котором работает скраппер

logging:
  config: classpath:log4j2-plain.xml # Конфигурация логирования
```
