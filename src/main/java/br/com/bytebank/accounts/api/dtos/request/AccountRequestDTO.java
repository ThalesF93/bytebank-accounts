package br.com.bytebank.accounts.api.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "DTO used to open account")
public record AccountRequestDTO(

        @Schema(description = "Customer ID")
        @NotNull
        UUID customerId
) {
}
