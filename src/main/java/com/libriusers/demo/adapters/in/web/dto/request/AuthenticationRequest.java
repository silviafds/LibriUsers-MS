package com.libriusers.demo.adapters.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthenticationRequest(
        @Schema(
                description = "User email address",
                example = "john.doe@email.com"
        )
        String login,

        @Schema(
                description = "User password",
                example = "securePassword123"
        )
        String password
) {}
