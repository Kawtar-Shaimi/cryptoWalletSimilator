package metier.model;

import metier.enums.CryptoType;

public class EthereumWallet extends Wallet {

	public EthereumWallet(String address) {
		super(CryptoType.ETHEREUM, address);
	}
}