package metier.service;

import java.math.BigDecimal;

import metier.enums.FeePriority;
import metier.model.Transaction;
import metier.model.Wallet;

/**
 * Ethereum: gasLimit * gasPrice (en gwei) -> converti en ETH fictif.
 */
public class EthereumFeeCalculator implements FeeCalculator {

	@Override
	public BigDecimal calculateFee(Transaction tx, Wallet wallet, FeePriority priority) {
		long gasLimit = 21_000; // simple transfert
		long gasPriceGwei;
		switch (priority) {
			case ECONOMIQUE: gasPriceGwei = 5; break;
			case STANDARD: gasPriceGwei = 20; break;
			case RAPIDE: gasPriceGwei = 60; break;
			default: gasPriceGwei = 10; break;
		}
		// 1 gwei = 1e-9 ETH
		BigDecimal gas = new BigDecimal(gasLimit);
		BigDecimal priceInEth = new BigDecimal(gasPriceGwei).multiply(new BigDecimal("1e-9"));
		return gas.multiply(priceInEth);
	}
}


