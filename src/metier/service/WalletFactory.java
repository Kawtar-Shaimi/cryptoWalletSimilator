package metier.service;

import metier.enums.CryptoType;
import metier.model.BitcoinWallet;
import metier.model.EthereumWallet;
import metier.model.Wallet;
import util.AddressGenerator;

/**
 * Fabrique de wallets qui génère une adresse conforme au type demandé.
 */
public final class WalletFactory {

    private WalletFactory() {}

    public static Wallet createWallet(CryptoType type) {
        switch (type) {
            case BITCOIN:
                return new BitcoinWallet(AddressGenerator.generateBitcoinAddress());
            case ETHEREUM:
                return new EthereumWallet(AddressGenerator.generateEthereumAddress());
            default:
                throw new IllegalArgumentException("Type de crypto non supporté: " + type);
        }
    }
}


