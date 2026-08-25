package com.ada.transferscheduling.exception;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(String accountNumber) {
        super("Conta não encontrada para o número da conta: " + accountNumber);
    }
}
