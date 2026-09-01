package com.hokyozu.kyofuse.chat.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MessageInfoResponse(
        UUID messageId,
        Instant createdAt,
        List<MessageReceiptItemResponse> receipts
) {
}
