package com.ada.transferscheduling.exception;

public class TransferNotFoundException extends RuntimeException {

    public TransferNotFoundException(Long id) {
        super("Transferência não encontrada para o id: " + id);
    }
}
