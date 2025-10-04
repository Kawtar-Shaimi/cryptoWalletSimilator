package metier.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import metier.enums.CryptoType;

public abstract class Wallet {

	private final String id;
	private final CryptoType cryptoType;
	private final String address;
	private BigDecimal balance;
	private final Instant createdAt;
    private final List<metier.model.Transaction> transactions;

	protected Wallet(CryptoType cryptoType, String address) {
		this.id = UUID.randomUUID().toString();
		this.cryptoType = cryptoType;
		this.address = address;
		this.balance = BigDecimal.ZERO;
		this.createdAt = Instant.now();
        this.transactions = new ArrayList<metier.model.Transaction>();
	}

	// Constructeur pour la reconstruction depuis la base de données
	protected Wallet(String id, CryptoType cryptoType, String address, BigDecimal balance, Instant createdAt) {
		this.id = id;
		this.cryptoType = cryptoType;
		this.address = address;
		this.balance = balance;
		this.createdAt = createdAt;
        this.transactions = new ArrayList<metier.model.Transaction>();
	}

	public String getId() { return id; }
	public CryptoType getCryptoType() { return cryptoType; }
	public String getAddress() { return address; }
	public BigDecimal getBalance() { return balance; }
	public Instant getCreatedAt() { return createdAt; }
    public List<metier.model.Transaction> getTransactions() { return transactions; }

	public void setBalance(BigDecimal balance) { this.balance = balance; }

    public void addTransaction(metier.model.Transaction tx) { this.transactions.add(tx); }
}


