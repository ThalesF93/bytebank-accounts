package br.com.bytebank.accounts.domain.exception.customized_excpetions;

import br.com.bytebank.accounts.domain.exception.default_exception.DefaultException;
import org.springframework.http.HttpStatus;

public class ServiceUnavailableException extends DefaultException {

    public ServiceUnavailableException(String message) {
            super("SERVICE_UNAVAILABLE", message, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
