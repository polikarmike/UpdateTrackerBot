package edu.java.scrapper.service;

import edu.java.common.dto.requests.LinkUpdateRequest;
import org.springframework.stereotype.Service;

@Service
public interface NotificationService {
    void sendNotification(LinkUpdateRequest update);
}
