package ru.iu3.myapp;

public interface TransactionEvents {
    String enterPin(int ptc, String amount);
    void transactionResult(boolean result);
}
