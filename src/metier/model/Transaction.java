package metier.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import metier.enums.FeePriority;
import metier.enums.TransactionStatus;

public class Transaction {

	private final String id;
	private final String fromAddress;
	private final String toAddress;
	private final BigDecimal amount;
	private final FeePriority feePriority;
	private final Instant createdAt;
	private BigDecimal feeAmount;
	private TransactionStatus status;
	private final String walletId;

	public Transaction(String fromAddress, String toAddress, BigDecimal amount, FeePriority feePriority, String walletId) {
		this.id = UUID.randomUUID().toString();
		this.fromAddress = fromAddress;
		this.toAddress = toAddress;
		this.amount = amount;
		this.feePriority = feePriority;
		this.createdAt = Instant.now();
		this.status = TransactionStatus.PENDING;
		this.walletId = walletId;
	}

	public String getId() { return id; }
	public String getFromAddress() { return fromAddress; }
	public String getToAddress() { return toAddress; }
	public BigDecimal getAmount() { return amount; }
	public FeePriority getFeePriority() { return feePriority; }
	public Instant getCreatedAt() { return createdAt; }
	public BigDecimal getFeeAmount() { return feeAmount; }
	public TransactionStatus getStatus() { return status; }
	public String getWalletId() { return walletId; }

	public void setFeeAmount(BigDecimal feeAmount) { this.feeAmount = feeAmount; }
	public void setStatus(TransactionStatus status) { this.status = status; }
}