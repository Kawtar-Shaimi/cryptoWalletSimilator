package metier.model;

import metier.enums.CryptoType;
import java.math.BigDecimal;
import java.time.Instant;

public class EthereumWallet extends Wallet {

	public EthereumWallet(String address) {
		super(CryptoType.ETHEREUM, address);
	}
	
	// Constructeur pour la reconstruction depuis la base de données
	public EthereumWallet(String id, String address, BigDecimal balance, Instant createdAt) {
		super(id, CryptoType.ETHEREUM, address, balance, createdAt);
	}
}