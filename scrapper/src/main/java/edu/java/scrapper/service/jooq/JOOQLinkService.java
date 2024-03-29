package edu.java.scrapper.service.jooq;

import edu.java.scrapper.domain.repository.jooq.JOOQChatLinkRepository;
import edu.java.scrapper.domain.repository.jooq.JOOQChatRepository;
import edu.java.scrapper.domain.repository.jooq.JOOQLinkRepository;
import edu.java.scrapper.dto.entity.Chat;
import edu.java.scrapper.dto.entity.Link;
import edu.java.scrapper.exception.InvalidLinkException;
import edu.java.scrapper.exception.LinkNotFoundException;
import edu.java.scrapper.exception.MissingChatException;
import edu.java.scrapper.exception.RepeatedLinkAdditionException;
import edu.java.scrapper.service.LinkService;
import edu.java.scrapper.utils.linkverifier.LinkVerifier;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JOOQLinkService implements LinkService {
    private final JOOQLinkRepository linkRepository;
    private final JOOQChatRepository chatRepository;
    private final JOOQChatLinkRepository chatLinkRepository;
    private final LinkVerifier linkVerifier;
    private static final String MISSING_CHAT_ERROR_MESSAGE = "Chat does not exist";
    private static final String INVALID_LINK_ERROR_MESSAGE = "Invalid link: ";
    private static final String REPEATED_LINK_ADDITION_ERROR_MESSAGE = "Link already exists in the chat";
    private static final String NO_LINK_ERROR_MESSAGE = "Link does not exist";
    private static final String LINK_NOT_FOUND_ERROR_MESSAGE = "Link not found in the chat";


    @Override
    public Link add(long tgChatId, URI uri) {
        if (!linkVerifier.checkLink(uri)) {
            throw new InvalidLinkException(INVALID_LINK_ERROR_MESSAGE + uri);
        }

        Chat chat = chatRepository.getById(tgChatId)
            .orElseThrow(() -> new MissingChatException(MISSING_CHAT_ERROR_MESSAGE));

        Link link = getOrCreate(uri);

        long chatId = chat.getId();
        long linkId = link.getId();

        if (chatLinkRepository.exists(chatId, linkId)) {
            throw new RepeatedLinkAdditionException(REPEATED_LINK_ADDITION_ERROR_MESSAGE);
        }

        chatLinkRepository.add(chatId, linkId);
        return link;
    }

    @Override
    public Link remove(long tgChatId, URI url) {
        Chat chat = chatRepository.getById(tgChatId)
            .orElseThrow(() -> new MissingChatException(MISSING_CHAT_ERROR_MESSAGE));

        Link existingLink = linkRepository.getLinkByUri(url)
            .orElseThrow(() -> new LinkNotFoundException(NO_LINK_ERROR_MESSAGE));

        long chatId = chat.getId();
        long linkId = existingLink.getId();

        if (!chatLinkRepository.exists(chatId, linkId)) {
            throw new LinkNotFoundException(LINK_NOT_FOUND_ERROR_MESSAGE);
        }

        chatLinkRepository.remove(chatId, linkId);

        return existingLink;
    }

    @Override
    public Link remove(long tgChatId, long id) {
        Chat chat = chatRepository.getById(tgChatId)
            .orElseThrow(() -> new MissingChatException(MISSING_CHAT_ERROR_MESSAGE));

        Link existingLink = linkRepository.getLinkById(id)
            .orElseThrow(() -> new LinkNotFoundException(NO_LINK_ERROR_MESSAGE));

        long chatId = chat.getId();
        long linkId = existingLink.getId();

        if (!chatLinkRepository.exists(chatId, linkId)) {
            throw new LinkNotFoundException(LINK_NOT_FOUND_ERROR_MESSAGE);
        }

        chatLinkRepository.remove(chatId, linkId);

        return existingLink;
    }

    @Override
    public Collection<Link> listAll(long tgChatId) {
        Chat chat = chatRepository.getById(tgChatId)
            .orElseThrow(() -> new MissingChatException(MISSING_CHAT_ERROR_MESSAGE));

        List<Long> linkIds = chatLinkRepository.getLinkIdsByChatId(chat.getId());
        Collection<Link> links = new ArrayList<>();

        for (Long linkId : linkIds) {
            Optional<Link> linkOptional = linkRepository.getLinkById(linkId);
            linkOptional.ifPresent(links::add);
        }

        return links;
    }

    @Override
    public int cleanUpUnusedLink() {
        return chatLinkRepository.cleanupUnusedLinks();
    }

    @Override
    public List<Link> findOldestLinks(int batchSize) {
        return linkRepository.findOldestLinks(batchSize);
    }

    @Override
    public List<Long> getChatIdsByLinkId(Long id) {
        return chatLinkRepository.getChatIdsByLinkId(id);
    }

    @Override
    public void updateLastUpdatedTime(long id) {
        linkRepository.updateLastUpdatedTime(id);
    }

    public Link getOrCreate(URI uri) {
        return linkRepository.getLinkByUri(uri)
            .orElseGet(() -> linkRepository.add(uri));
    }
}
