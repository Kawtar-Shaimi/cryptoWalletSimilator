package metier.service;

import java.math.BigDecimal;

import metier.enums.FeePriority;
import metier.model.Transaction;
import metier.model.Wallet;

/**
 * Interface pour calculer les frais selon le type de crypto.
 */
public interface FeeCalculator {
	BigDecimal calculateFee(Transaction tx, Wallet wallet, FeePriority priority);
}


