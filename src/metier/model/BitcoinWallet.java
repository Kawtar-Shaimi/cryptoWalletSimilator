package metier.model;

import metier.enums.CryptoType;

public class BitcoinWallet extends Wallet {

	public BitcoinWallet(String address) {
		super(CryptoType.BITCOIN, address);
	}
}