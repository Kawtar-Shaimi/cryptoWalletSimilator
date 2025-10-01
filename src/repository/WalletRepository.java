package repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import metier.model.Wallet;

public interface WalletRepository {
    void save(Wallet wallet);
    Optional<Wallet> findById(String id);
    Optional<Wallet> findByAddress(String address);
    void updateBalance(String id, BigDecimal newBalance);
    List<Wallet> findAll();
}


