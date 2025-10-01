package metier.service;

import java.math.BigDecimal;

import metier.enums.FeePriority;
import metier.model.Transaction;
import metier.model.Wallet;


public class BitcoinFeeCalculator implements FeeCalculator {

	@Override
	public BigDecimal calculateFee(Transaction tx, Wallet wallet, FeePriority priority) {
		int estimatedSizeBytes = 250; // taille moyenne simplifiée
		int satPerByte;
		switch (priority) {
			case ECONOMIQUE: satPerByte = 5; break;
			case STANDARD: satPerByte = 20; break;
			case RAPIDE: satPerByte = 60; break;
			default: satPerByte = 10; break;
		}
		long sats = (long) estimatedSizeBytes * satPerByte;
		// 1 BTC = 100_000_000 sats
		return new BigDecimal(sats).divide(new BigDecimal(100_000_000));
	}
}


