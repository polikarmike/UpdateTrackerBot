FROM openjdk:21

WORKDIR /app

ENV TELEGRAM_TOKEN=${TELEGRAM_TOKEN}

COPY target/bot.jar /app/bot.jar

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "/app/bot.jar"]
