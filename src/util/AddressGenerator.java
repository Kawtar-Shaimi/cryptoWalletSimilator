package util;

import java.security.SecureRandom;

public final class AddressGenerator {

    private static final String HEX = "0123456789abcdef";
    private static final String BASE58 = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
    private static final SecureRandom RANDOM = new SecureRandom();

    private AddressGenerator() {}

    public static String generateEthereumAddress() {
        StringBuilder sb = new StringBuilder("0x");
        for (int i = 0; i < 40; i++) {
            sb.append(HEX.charAt(RANDOM.nextInt(HEX.length())));
        }
        return sb.toString();
    }

    public static String generateBitcoinAddress() {
        // Choix simple entre formats courants: 1..., 3..., bc1...
        int variant = RANDOM.nextInt(3);
        if (variant == 2) {
            // Bech32 simplifié: bc1 + 30 chars base58 (factice)
            StringBuilder sb = new StringBuilder("bc1");
            for (int i = 0; i < 30; i++) {
                sb.append(BASE58.charAt(RANDOM.nextInt(BASE58.length())));
            }
            return sb.toString();
        }
        char prefix = (variant == 0) ? '1' : '3';
        StringBuilder sb = new StringBuilder().append(prefix);
        for (int i = 0; i < 33; i++) {
            sb.append(BASE58.charAt(RANDOM.nextInt(BASE58.length())));
        }
        return sb.toString();
    }
}


