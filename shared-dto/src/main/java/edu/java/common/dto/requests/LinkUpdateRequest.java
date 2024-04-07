package edu.java.common.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;

public record LinkUpdateRequest(
    @NotNull Long id,
    @NotNull URI url,
    @NotBlank String description,
    @NotNull List<Long> tgChatIds
) {
    public String toJson() {
        return String.format("{\"id\":%d,\"url\":\"%s\",\"description\":\"%s\",\"tgChatIds\":%s}",
            id(), url().toString(), description(), tgChatIds().toString());
    }
}
