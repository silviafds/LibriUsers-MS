package com.libriusers.demo.adapters.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {

    @Schema(
            description = "User full name",
            example = "John Doe",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String name;

    @Schema(
            description = "User email address",
            example = "john.doe@email.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

    @Schema(
            description = "User password (min 6 characters)",
            example = "securePassword123",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 6
    )
    private String password;

    @Schema(
            description = "User role in the system",
            example = "USER",
            allowableValues = {"USER", "ADMIN", "MODERATOR"},
            defaultValue = "USER"
    )
    private String role;

}
