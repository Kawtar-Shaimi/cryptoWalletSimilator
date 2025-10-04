package repository.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import metier.enums.CryptoType;
import metier.model.BitcoinWallet;
import metier.model.EthereumWallet;
import metier.model.Wallet;
import config.Database;
import util.LoggerProvider;

public class JdbcWalletRepository implements repository.WalletRepository {

	@Override
	public void save(Wallet wallet) {
		if (findById(wallet.getId()).isPresent()) {
			updateWallet(wallet);
		} else {
			insertWallet(wallet);
		}
	}
	
	private void insertWallet(Wallet wallet) {
		String sql = "INSERT INTO wallets(id, type, address, balance, created_at) VALUES (?, ?, ?, ?, ?)";
		try (Connection c = Database.getInstance().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, wallet.getId());
			ps.setString(2, wallet.getCryptoType().name());
			ps.setString(3, wallet.getAddress());
			ps.setBigDecimal(4, wallet.getBalance());
			ps.setTimestamp(5, new java.sql.Timestamp(wallet.getCreatedAt().toEpochMilli()));
			ps.executeUpdate();
		} catch (SQLException e) {
			LoggerProvider.getLogger(getClass().getName()).severe(e.getMessage());
		}
	}
	
	private void updateWallet(Wallet wallet) {
		String sql = "UPDATE wallets SET balance = ? WHERE id = ?";
		try (Connection c = Database.getInstance().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setBigDecimal(1, wallet.getBalance());
			ps.setString(2, wallet.getId());
			int rowsUpdated = ps.executeUpdate();
			if (rowsUpdated > 0) {
				LoggerProvider.getLogger(getClass().getName()).info("Balance mise à jour pour wallet: " + wallet.getId() + " -> " + wallet.getBalance());
			}
		} catch (SQLException e) {
			LoggerProvider.getLogger(getClass().getName()).severe(e.getMessage());
		}
	}

	@Override
	public Optional<Wallet> findById(String id) {
		String sql = "SELECT id, type, address, balance, created_at FROM wallets WHERE id = ?";
		try (Connection c = Database.getInstance().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					String walletId = rs.getString("id");
					CryptoType type = CryptoType.valueOf(rs.getString("type"));
					String address = rs.getString("address");
					BigDecimal balance = rs.getBigDecimal("balance");
					java.time.Instant createdAt = rs.getTimestamp("created_at").toInstant();
					
					Wallet w = type == CryptoType.BITCOIN ? 
						new BitcoinWallet(walletId, address, balance, createdAt) : 
						new EthereumWallet(walletId, address, balance, createdAt);
					
					return Optional.of(w);
				}
			}
		} catch (SQLException e) {
			LoggerProvider.getLogger(getClass().getName()).severe(e.getMessage());
		}
		return Optional.empty();
	}

	@Override
	public Optional<Wallet> findByAddress(String address) {
		String sql = "SELECT id, type, address, balance, created_at FROM wallets WHERE address = ?";
		try (Connection c = Database.getInstance().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, address);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					String walletId = rs.getString("id");
					CryptoType type = CryptoType.valueOf(rs.getString("type"));
					String addr = rs.getString("address");
					BigDecimal balance = rs.getBigDecimal("balance");
					java.time.Instant createdAt = rs.getTimestamp("created_at").toInstant();
					
					Wallet w = type == CryptoType.BITCOIN ? 
						new BitcoinWallet(walletId, addr, balance, createdAt) : 
						new EthereumWallet(walletId, addr, balance, createdAt);
					
					return Optional.of(w);
				}
			}
		} catch (SQLException e) {
			LoggerProvider.getLogger(getClass().getName()).severe(e.getMessage());
		}
		return Optional.empty();
	}

	@Override
	public void updateBalance(String id, BigDecimal newBalance) {
		String sql = "UPDATE wallets SET balance = ? WHERE id = ?";
		try (Connection c = Database.getInstance().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setBigDecimal(1, newBalance);
			ps.setString(2, id);
			ps.executeUpdate();
		} catch (SQLException e) {
			LoggerProvider.getLogger(getClass().getName()).severe(e.getMessage());
		}
	}

	@Override
	public List<Wallet> findAll() {
		String sql = "SELECT id, type, address, balance, created_at FROM wallets";
		List<Wallet> list = new ArrayList<>();
		try (Connection c = Database.getInstance().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql);
			 ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				String walletId = rs.getString("id");
				CryptoType type = CryptoType.valueOf(rs.getString("type"));
				String address = rs.getString("address");
				BigDecimal balance = rs.getBigDecimal("balance");
				java.time.Instant createdAt = rs.getTimestamp("created_at").toInstant();
				
				Wallet w = type == CryptoType.BITCOIN ? 
					new BitcoinWallet(walletId, address, balance, createdAt) : 
					new EthereumWallet(walletId, address, balance, createdAt);
				
				list.add(w);
			}
		} catch (SQLException e) {
			LoggerProvider.getLogger(getClass().getName()).severe(e.getMessage());
		}
		return list;
	}
}