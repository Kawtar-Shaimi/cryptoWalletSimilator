package metier.model;

import metier.enums.CryptoType;
import java.math.BigDecimal;
import java.time.Instant;

public class BitcoinWallet extends Wallet {

	public BitcoinWallet(String address) {
		super(CryptoType.BITCOIN, address);
	}
	
	// Constructeur pour la reconstruction depuis la base de données
	public BitcoinWallet(String id, String address, BigDecimal balance, Instant createdAt) {
		super(id, CryptoType.BITCOIN, address, balance, createdAt);
	}
}