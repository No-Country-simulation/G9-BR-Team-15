package com.energ_ia.api.infra.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErroPadraoDTO> tratarResponseStatusException(ResponseStatusException ex) {

        ErroPadraoDTO erro = new ErroPadraoDTO(
                LocalDateTime.now(),
                ex.getStatusCode().value(),
                ex.getReason()
        );

        return ResponseEntity.status(ex.getStatusCode()).body(erro);
    }

    public record ErroPadraoDTO(
            LocalDateTime timestamp,
            int status,
            String message
    ) {}
}