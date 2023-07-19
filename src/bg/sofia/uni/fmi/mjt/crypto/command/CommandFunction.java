package bg.sofia.uni.fmi.mjt.crypto.command;

import bg.sofia.uni.fmi.mjt.crypto.user.CryptoUser;

@FunctionalInterface
interface CommandFunction {
    String apply(CryptoUser user, String[] args) throws Exception;
}
