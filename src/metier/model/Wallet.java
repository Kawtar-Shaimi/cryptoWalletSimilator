package metier.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;


public abstract class Wallet {

	private final String id;
	private final String address;
	private BigDecimal balance;
	private final Instant createdAt;

  protected Wallet(String address) {
		this.id = UUID.randomUUID().toString();
		this.address = address;
		this.balance = BigDecimal.ZERO;
		this.createdAt = Instant.now();
	}
  
	public String getId() { return id; }
	public String getAddress() { return address; }
	public BigDecimal getBalance() { return balance; }
	public Instant getCreatedAt() { return createdAt; }
	public void setBalance(BigDecimal balance) { this.balance = balance; }