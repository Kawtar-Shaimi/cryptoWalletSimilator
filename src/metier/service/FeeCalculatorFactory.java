package metier.service;

import metier.enums.CryptoType;

/**
 * Sélectionne le calculateur de frais selon le type de crypto.
 */
public final class FeeCalculatorFactory {

    private FeeCalculatorFactory() {}

    public static FeeCalculator forType(CryptoType type) {
        switch (type) {
            case BITCOIN:
                return new BitcoinFeeCalculator();
            case ETHEREUM:
                return new EthereumFeeCalculator();
            default:
                throw new IllegalArgumentException("Type de crypto non supporté: " + type);
        }
    }
}


