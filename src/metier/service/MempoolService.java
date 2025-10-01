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
		pending.clear();
		for (int i = 0; i < count; i++) {
			Transaction t = new Transaction(anonymAddr(rnd), anonymAddr(rnd), new BigDecimal("0.01"), FeePriority.STANDARD, UUID.randomUUID().toString());
			BigDecimal fee = new BigDecimal(rnd.nextInt(90) + 1).multiply(new BigDecimal("0.00000010"));
			t.setFeeAmount(fee);
			pending.add(t);
		}
	}

	private String anonymAddr(Random rnd) {
		StringBuilder sb = new StringBuilder("0x");
		String hex = "0123456789abcdef";
		for (int i = 0; i < 8; i++) sb.append(hex.charAt(rnd.nextInt(hex.length())));
		return sb.toString();
	}
}


