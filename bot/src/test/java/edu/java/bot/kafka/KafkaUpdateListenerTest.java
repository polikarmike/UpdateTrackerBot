package edu.java.bot.kafka;

import edu.java.bot.exception.BadRequestException;
import edu.java.bot.service.LinkUpdateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;
import java.io.IOException;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

public class KafkaUpdateListenerTest {
    @Mock
    private LinkUpdateService linkUpdateService;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private KafkaUpdateListener kafkaUpdateListener;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @DisplayName("Прием некорректного ответа")
    public void testListenInvalidResponse() throws IOException {
        String update = "update";

        assertThrows(BadRequestException.class, () -> kafkaUpdateListener.listen(update));

        verify(kafkaTemplate).send(any(), eq(update));
    }

    @Test
    @DisplayName("Прием корректного ответа")
    public void testListenValidResponse() throws IOException {
        String update = """
        {
            "id": 1,
            "url": "http://example.com",
            "description": "Example description",
            "tgChatIds": [123456789, 987654321]
        }
        """;

        assertDoesNotThrow(() -> kafkaUpdateListener.listen(update));

        verify(linkUpdateService).processLinkUpdate(any());
    }
}
