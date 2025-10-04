package metier.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import metier.enums.FeePriority;
import metier.model.Transaction;

/**
 * Service de mempool en mémoire pour la simulation (liste en attente).
 */
public class MempoolService {

	private final List<Transaction> pending;

	public MempoolService() {
		this.pending = new ArrayList<>();
	}

	public void addTransaction(Transaction tx) {
		pending.add(tx);
	}

	public List<Transaction> getPendingSortedByFeeDesc() {
		return pending.stream()
				.filter(t -> t.getFeeAmount() != null)
				.sorted(Comparator.comparing(Transaction::getFeeAmount).reversed())
				.collect(Collectors.toList());
	}

	public int computePosition(Transaction myTx) {
		List<Transaction> sorted = getPendingSortedByFeeDesc();
		for (int i = 0; i < sorted.size(); i++) {
			if (sorted.get(i).getId().equals(myTx.getId())) {
				return i + 1; // position 1-based
			}
		}
		return -1; // non trouvé
	}

	/**
	 * Retourne les informations de debug du mempool
	 * @return DebugInfo contenant toutes les données pour l'affichage
	 */
	public DebugInfo getDebugInfo() {
		List<TransactionSummary> summaries = new ArrayList<>();
		for (int i = 0; i < pending.size(); i++) {
			Transaction t = pending.get(i);
			summaries.add(new TransactionSummary(
				i + 1, 
				t.getId(), 
				t.getFeeAmount(), 
				t.getFromAddress()
			));
		}
		return new DebugInfo(pending.size(), summaries);
	}
	
	/**
	 * Classe pour encapsuler les informations de debug
	 */
	public static class DebugInfo {
		private final int totalTransactions;
		private final List<TransactionSummary> transactions;
		
		public DebugInfo(int totalTransactions, List<TransactionSummary> transactions) {
			this.totalTransactions = totalTransactions;
			this.transactions = transactions;
		}
		
		public int getTotalTransactions() { return totalTransactions; }
		public List<TransactionSummary> getTransactions() { return transactions; }
	}
	
	/**
	 * Classe pour résumer une transaction pour le debug
	 */
	public static class TransactionSummary {
		private final int position;
		private final String id;
		private final BigDecimal feeAmount;
		private final String fromAddress;
		
		public TransactionSummary(int position, String id, BigDecimal feeAmount, String fromAddress) {
			this.position = position;
			this.id = id;
			this.feeAmount = feeAmount;
			this.fromAddress = fromAddress;
		}
		
		public int getPosition() { return position; }
		public String getId() { return id; }
		public BigDecimal getFeeAmount() { return feeAmount; }
		public String getFromAddress() { return fromAddress; }
	}

	/**
	 * Calcule la position hypothétique si une transaction avait un certain fee,
	 * sans modifier le contenu actuel du mempool.
	 */
	public int computeHypotheticalPosition(java.math.BigDecimal hypotheticalFee) {
		List<Transaction> sorted = getPendingSortedByFeeDesc();
		int pos = 1;
		for (Transaction t : sorted) {
			if (t.getFeeAmount() != null && t.getFeeAmount().compareTo(hypotheticalFee) > 0) {
				pos++;
			}
		}
		return pos;
	}

	public Duration estimateConfirmationTime(Transaction myTx) {
		int pos = computePosition(myTx);
		if (pos < 0) return Duration.ZERO;
		return Duration.ofMinutes(pos * 10L);
	}

	public void generateRandomPending(int count) {
		Random rnd = new Random();
		// Sauvegarde les transactions existantes de l'utilisateur
		List<Transaction> userTransactions = pending.stream()
			.filter(t -> !isRandomTransaction(t))
			.collect(Collectors.toList());
		
		pending.clear();
		
		// Restaure les transactions de l'utilisateur
		pending.addAll(userTransactions);
		
		// Ajoute les transactions aléatoires
		for (int i = 0; i < count; i++) {
			Transaction t = new Transaction(anonymAddr(rnd), anonymAddr(rnd), new BigDecimal("0.01"), FeePriority.STANDARD, UUID.randomUUID().toString());
			BigDecimal fee = new BigDecimal(rnd.nextInt(90) + 1).multiply(new BigDecimal("0.00000010"));
			t.setFeeAmount(fee);
			pending.add(t);
		}
	}
	
	/**
	 * Vérifie si une transaction est générée aléatoirement (adresse courte)
	 */
	private boolean isRandomTransaction(Transaction t) {
		// Les transactions aléatoires ont des adresses courtes (10 caractères + 0x)
		return t.getFromAddress().length() <= 12 && t.getToAddress().length() <= 12;
	}

	private String anonymAddr(Random rnd) {
		StringBuilder sb = new StringBuilder("0x");
		String hex = "0123456789abcdef";
		for (int i = 0; i < 8; i++) sb.append(hex.charAt(rnd.nextInt(hex.length())));
		return sb.toString();
	}
}


