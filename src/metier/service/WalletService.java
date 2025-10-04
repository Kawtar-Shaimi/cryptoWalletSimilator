package metier.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import metier.enums.CryptoType;
import metier.enums.FeePriority;
import metier.model.Transaction;
import metier.model.Wallet;
import repository.TransactionRepository;
import repository.WalletRepository;
import util.LoggerProvider;
import util.ValidationUtils;

/**
 * Service pour la gestion des wallets - Couche métier pure
 */
public class WalletService {
    
    private static final Logger LOGGER = LoggerProvider.getLogger(WalletService.class.getName());
    
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;
    
    public WalletService(WalletRepository walletRepository, TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.transactionService = new TransactionService(walletRepository);
    }
    
    /**
     * Résultat d'une opération de service
     */
    public static class ServiceResult<T> {
        private final boolean success;
        private final T data;
        private final String message;
        
        private ServiceResult(boolean success, T data, String message) {
            this.success = success;
            this.data = data;
            this.message = message;
        }
        
        public static <T> ServiceResult<T> success(T data, String message) {
            return new ServiceResult<>(true, data, message);
        }
        
        public static <T> ServiceResult<T> failure(String message) {
            return new ServiceResult<>(false, null, message);
        }
        
        public boolean isSuccess() { return success; }
        public T getData() { return data; }
        public String getMessage() { return message; }
    }
    
    /**
     * Crée un wallet en validant le type
     */
    public ServiceResult<Wallet> createWallet(String typeInput) {
        try {
            CryptoType type = parseWalletType(typeInput);
            Wallet wallet = WalletFactory.createWallet(type);
            walletRepository.save(wallet);
            
            String message = String.format("Wallet créé: id=%s, type=%s, address=%s, balance=%s", 
                wallet.getId(), wallet.getCryptoType(), wallet.getAddress(), wallet.getBalance());
            
            LOGGER.info("Wallet créé avec succès: " + wallet.getId());
            return ServiceResult.success(wallet, message);
            
        } catch (IllegalArgumentException e) {
            return ServiceResult.failure("Type de wallet invalide. Utilisez 1 pour BITCOIN ou 2 pour ETHEREUM.");
        } catch (Exception e) {
            LOGGER.severe("Erreur lors de la création du wallet: " + e.getMessage());
            return ServiceResult.failure("Erreur lors de la création du wallet: " + e.getMessage());
        }
    }
    
    /**
     * Trouve un wallet par ID
     */
    public ServiceResult<Wallet> findWallet(String walletId) {
        if (walletId == null || walletId.trim().isEmpty()) {
            return ServiceResult.failure("ID du wallet requis");
        }
        
        Optional<Wallet> wallet = walletRepository.findById(walletId.trim());
        if (wallet.isPresent()) {
            return ServiceResult.success(wallet.get(), "Wallet trouvé");
        } else {
            return ServiceResult.failure("Wallet introuvable");
        }
    }
    
    /**
     * Ajoute des fonds à un wallet
     */
    public ServiceResult<Wallet> addFunds(String walletId, String amountStr) {
        // Validation des paramètres
        ServiceResult<Wallet> walletResult = findWallet(walletId);
        if (!walletResult.isSuccess()) {
            return walletResult;
        }
        
        BigDecimal amount;
        try {
            amount = new BigDecimal(amountStr);
        } catch (Exception e) {
            return ServiceResult.failure("Montant invalide");
        }
        
        if (!ValidationUtils.isPositive(amount)) {
            return ServiceResult.failure("Montant doit être > 0");
        }
        
        // Logique métier
        Wallet wallet = walletResult.getData();
        BigDecimal oldBalance = wallet.getBalance();
        transactionService.addFunds(wallet, amount);
        
        String message = String.format("Fonds ajoutés avec succès. Ancienne balance: %s, Nouvelle balance: %s", 
            oldBalance, wallet.getBalance());
        
        return ServiceResult.success(wallet, message);
    }
    
    /**
     * Crée une transaction en validant tous les paramètres
     */
    public ServiceResult<Transaction> createTransaction(String walletId, String toAddress, 
                                                       String amountStr, String priorityInput) {
        // Validation du wallet
        ServiceResult<Wallet> walletResult = findWallet(walletId);
        if (!walletResult.isSuccess()) {
            return ServiceResult.failure(walletResult.getMessage());
        }
        
        Wallet wallet = walletResult.getData();
        
        // Validation de l'adresse de destination
        if (!isValidAddress(wallet.getCryptoType(), toAddress)) {
            String cryptoName = wallet.getCryptoType() == CryptoType.ETHEREUM ? "ETH" : "BTC";
            return ServiceResult.failure("Adresse " + cryptoName + " invalide");
        }
        
        // Validation du montant
        BigDecimal amount;
        try {
            amount = new BigDecimal(amountStr);
        } catch (Exception e) {
            return ServiceResult.failure("Montant invalide");
        }
        
        if (!ValidationUtils.isPositive(amount)) {
            return ServiceResult.failure("Montant doit être > 0");
        }
        
        // Validation de la priorité
        FeePriority priority;
        try {
            priority = parsePriority(priorityInput);
        } catch (IllegalArgumentException e) {
            return ServiceResult.failure("Priorité invalide. Utilisez 1=ECONOMIQUE, 2=STANDARD, 3=RAPIDE");
        }
        
        // Création de la transaction
        TransactionService.TransactionResult result = transactionService.createTransaction(wallet, toAddress, amount, priority);
        
        if (result.isSuccess()) {
            Transaction transaction = result.getTransaction();
            transactionRepository.save(transaction);
            return ServiceResult.success(transaction, result.getMessage());
        } else {
            return ServiceResult.failure(result.getMessage());
        }
    }
    
    /**
     * Compare les frais pour différentes priorités
     */
    public ServiceResult<List<FeeComparison>> compareFees(String walletId, String toAddress, String amountStr) {
        // Validation des paramètres (réutilise la logique existante)
        ServiceResult<Wallet> walletResult = findWallet(walletId);
        if (!walletResult.isSuccess()) {
            return ServiceResult.failure(walletResult.getMessage());
        }
        
        // Logique de comparaison des frais...
        // (à implémenter selon les besoins)
        return ServiceResult.failure("Fonctionnalité à implémenter");
    }
    
    // Méthodes utilitaires privées
    private CryptoType parseWalletType(String typeInput) {
        if ("1".equals(typeInput)) {
            return CryptoType.BITCOIN;
        } else if ("2".equals(typeInput)) {
            return CryptoType.ETHEREUM;
        } else {
            throw new IllegalArgumentException("Type invalide: " + typeInput);
        }
    }
    
    private FeePriority parsePriority(String priorityInput) {
        switch (priorityInput) {
            case "1": return FeePriority.ECONOMIQUE;
            case "2": return FeePriority.STANDARD;
            case "3": return FeePriority.RAPIDE;
            default: throw new IllegalArgumentException("Priorité invalide: " + priorityInput);
        }
    }
    
    private boolean isValidAddress(CryptoType cryptoType, String address) {
        switch (cryptoType) {
            case ETHEREUM:
                return ValidationUtils.isValidEthereumAddress(address);
            case BITCOIN:
                return ValidationUtils.isValidBitcoinAddress(address);
            default:
                return false;
        }
    }
    
    /**
     * Classe pour les résultats de comparaison de frais
     */
    public static class FeeComparison {
        private final FeePriority priority;
        private final BigDecimal fee;
        private final int position;
        private final long estimatedMinutes;
        
        public FeeComparison(FeePriority priority, BigDecimal fee, int position, long estimatedMinutes) {
            this.priority = priority;
            this.fee = fee;
            this.position = position;
            this.estimatedMinutes = estimatedMinutes;
        }
        
        // Getters
        public FeePriority getPriority() { return priority; }
        public BigDecimal getFee() { return fee; }
        public int getPosition() { return position; }
        public long getEstimatedMinutes() { return estimatedMinutes; }
    }
}