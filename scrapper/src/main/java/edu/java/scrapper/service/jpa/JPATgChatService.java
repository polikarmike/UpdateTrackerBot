package edu.java.scrapper.service.jpa;

import edu.java.scrapper.domain.repository.jpa.JPAChatRepository;
import edu.java.scrapper.domain.repository.jpa.JPALinkRepository;
import edu.java.scrapper.dto.entity.Chat;
import edu.java.scrapper.dto.entity.Link;
import edu.java.scrapper.exception.MissingChatException;
import edu.java.scrapper.exception.RepeatedRegistrationException;
import edu.java.scrapper.service.TgChatService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class JPATgChatService implements TgChatService {
    private final JPAChatRepository chatRepository;

    private final JPALinkRepository linkRepository;

    private static final String MISSING_CHAT_ERROR_MESSAGE = "Chat does not exist";
    private static final String REPEATED_CHAT_ADDITION_ERROR_MESSAGE = "Chat already exists";

    @Override
    @Transactional
    public void register(long tgChatId) {
        Optional<Chat> existingChat = chatRepository.findById(tgChatId);
        if (existingChat.isPresent()) {
            throw new RepeatedRegistrationException(REPEATED_CHAT_ADDITION_ERROR_MESSAGE);
        }

        chatRepository.save(new Chat(tgChatId));
    }

    @Override
    @Transactional
    public void unregister(long tgChatId) {
        Chat chat = chatRepository.findById(tgChatId)
            .orElseThrow(() -> new MissingChatException(MISSING_CHAT_ERROR_MESSAGE));

        List<Link> linksToRemove = new ArrayList<>(chat.getLinks());

        for (Link link : linksToRemove) {
            chat.getLinks().remove(link);
            link.getChats().remove(chat);
        }

        chatRepository.delete(chat);
    }
}
