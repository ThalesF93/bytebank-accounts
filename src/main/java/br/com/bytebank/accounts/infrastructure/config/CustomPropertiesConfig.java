package br.com.bytebank.accounts.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bytebank.accounts.generation")
public record CustomPropertiesConfig(

        int accountNumberMin,
        int accountNumberRange,
        int agencyMin,
        int agencyRange
) {
}
