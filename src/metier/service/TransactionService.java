package metier.service;

import java.math.BigDecimal;
import java.util.logging.Logger;

import metier.enums.FeePriority;
import metier.model.Transaction;
import metier.model.Wallet;
import repository.WalletRepository;
import util.LoggerProvider;

/**
 * Service pour gérer les transactions avec validation de balance
 */
public class TransactionService {
    
    private static final Logger LOGGER = LoggerProvider.getLogger(TransactionService.class.getName());
    
    private final WalletRepository walletRepository;
    
    public TransactionService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }
    
    /**
     * Crée une transaction en vérifiant que le wallet a suffisamment de fonds
     * 
     * @param wallet Le wallet source
     * @param toAddress L'adresse de destination
     * @param amount Le montant à envoyer
     * @param priority La priorité de la transaction
     * @return La transaction créée
     * @throws InsufficientFundsException Si le wallet n'a pas assez de fonds
     */
    public TransactionResult createTransaction(Wallet wallet, String toAddress, 
                                             BigDecimal amount, FeePriority priority) {
        
        // 1. Calculer les frais
        Transaction tempTx = new Transaction(wallet.getAddress(), toAddress, amount, priority, wallet.getId());
        FeeCalculator calc = FeeCalculatorFactory.forType(wallet.getCryptoType());
        BigDecimal fee = calc.calculateFee(tempTx, wallet, priority);
        
        // 2. Calculer le montant total nécessaire (montant + frais)
        BigDecimal totalRequired = amount.add(fee);
        
        // 3. Vérifier si le wallet a suffisamment de fonds
        if (wallet.getBalance().compareTo(totalRequired) < 0) {
            String message = String.format(
                "Fonds insuffisants. Balance: %s, Requis: %s (montant: %s + frais: %s)",
                wallet.getBalance(), totalRequired, amount, fee
            );
            LOGGER.warning(message);
            return TransactionResult.failure(message);
        }
        
        // 4. Créer la transaction
        Transaction transaction = new Transaction(wallet.getAddress(), toAddress, amount, priority, wallet.getId());
        transaction.setFeeAmount(fee);
        
        // 5. Déduire le montant + frais de la balance
        BigDecimal newBalance = wallet.getBalance().subtract(totalRequired);
        wallet.setBalance(newBalance);
        
        // 6. Sauvegarder le wallet avec la nouvelle balance
        walletRepository.save(wallet);
        
        LOGGER.info(String.format(
            "Transaction créée avec succès. Montant: %s, Frais: %s, Nouvelle balance: %s",
            amount, fee, newBalance
        ));
        
        return TransactionResult.success(transaction, String.format(
            "Transaction créée avec succès. Montant: %s, Frais: %s, Nouvelle balance: %s",
            amount, fee, newBalance
        ));
    }
    
    /**
     * Ajoute des fonds à un wallet (simulation de réception de crypto)
     */
    public void addFunds(Wallet wallet, BigDecimal amount) {
        BigDecimal newBalance = wallet.getBalance().add(amount);
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);
        
        LOGGER.info(String.format(
            "Fonds ajoutés au wallet %s. Montant: %s, Nouvelle balance: %s",
            wallet.getId(), amount, newBalance
        ));
    }
    
    /**
     * Classe pour encapsuler le résultat d'une transaction
     */
    public static class TransactionResult {
        private final boolean success;
        private final Transaction transaction;
        private final String message;
        
        private TransactionResult(boolean success, Transaction transaction, String message) {
            this.success = success;
            this.transaction = transaction;
            this.message = message;
        }
        
        public static TransactionResult success(Transaction transaction, String message) {
            return new TransactionResult(true, transaction, message);
        }
        
        public static TransactionResult failure(String message) {
            return new TransactionResult(false, null, message);
        }
        
        public boolean isSuccess() { return success; }
        public Transaction getTransaction() { return transaction; }
        public String getMessage() { return message; }
    }
}