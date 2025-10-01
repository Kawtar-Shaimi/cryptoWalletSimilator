package repository;

import java.util.List;
import java.util.Optional;

import metier.enums.TransactionStatus;
import metier.model.Transaction;

public interface TransactionRepository {
    void save(Transaction tx);
    Optional<Transaction> findById(String id);
    List<Transaction> findPending();
    List<Transaction> findByWalletId(String walletId);
    void updateStatus(String id, TransactionStatus status);
}


