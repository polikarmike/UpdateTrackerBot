package edu.java.bot.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.java.bot.exception.BadRequestException;
import edu.java.bot.service.LinkUpdateService;
import edu.java.common.dto.requests.LinkUpdateRequest;
import edu.java.common.dto.responses.ApiErrorResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaUpdateListener {

    private final LinkUpdateService linkUpdateService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.dlq}")
    private String dlqTopic;

    @KafkaListener(topics = "${kafka.topic}")
    public void listen(String update) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            LinkUpdateRequest updates = objectMapper.readValue(update, LinkUpdateRequest.class);
            linkUpdateService.processLinkUpdate(updates);
        } catch (IOException e) {
            kafkaTemplate.send(dlqTopic, update);
            List<String> stackTraceList = Arrays.stream(e.getStackTrace())
                .map(StackTraceElement::toString)
                .toList();
            throw new BadRequestException(
                new ApiErrorResponse(
                    "Scrapper Exception",
                    HttpStatus.BAD_REQUEST.toString(),
                    e.getClass().getSimpleName(),
                    e.getMessage(),
                    stackTraceList));
        }
    }
}
