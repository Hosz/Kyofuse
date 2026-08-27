package com.hokyozu.kyofuse.auth.dto.response;

import java.util.List;

public record TotpConfirmResponse(
        List<String> recoveryCodes
) {
}
