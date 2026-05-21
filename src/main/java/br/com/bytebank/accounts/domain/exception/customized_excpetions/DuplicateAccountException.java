package br.com.bytebank.accounts.domain.exception.customized_excpetions;

import br.com.bytebank.accounts.domain.exception.default_exception.DefaultException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class DuplicateAccountException extends DefaultException {
    public DuplicateAccountException(String accountNumber) {
        super("DUPLICATE_ACCOUNT","Account with number = " + accountNumber + " already exists" , HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
