package com.ada.transferscheduling.exception;

public class TransferNotFoundException extends RuntimeException {

    public TransferNotFoundException(Long id) {
        super("Transfer not found for id: " + id);
    }
}
