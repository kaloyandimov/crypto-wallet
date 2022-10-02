package bg.sofia.uni.fmi.mjt.crypto.user;

import bg.sofia.uni.fmi.mjt.crypto.wallet.CryptoWallet;

public interface CryptoUser extends User {
    CryptoWallet getWallet();
}
