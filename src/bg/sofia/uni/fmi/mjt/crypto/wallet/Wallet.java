package bg.sofia.uni.fmi.mjt.crypto.wallet;

import bg.sofia.uni.fmi.mjt.crypto.exception.InsufficientResourcesException;
import bg.sofia.uni.fmi.mjt.crypto.exception.NegativeValueException;

public interface Wallet {
    void deposit(double money) throws NegativeValueException;

    void withdraw(double money) throws NegativeValueException, InsufficientResourcesException;

    double getBalance();
}
