package repository.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import metier.enums.FeePriority;
import metier.enums.TransactionStatus;
import metier.model.Transaction;
import config.Database;
import util.LoggerProvider;

public class JdbcTransactionRepository implements repository.TransactionRepository {

	@Override
	public void save(Transaction tx) {
		String sql = "INSERT INTO transactions(id, from_address, to_address, amount, fee_priority, fee_amount, status, created_at, wallet_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (Connection c = Database.getInstance().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, tx.getId());
			ps.setString(2, tx.getFromAddress());
			ps.setString(3, tx.getToAddress());
			ps.setBigDecimal(4, tx.getAmount());
			ps.setString(5, tx.getFeePriority().name());
			ps.setBigDecimal(6, tx.getFeeAmount());
			ps.setString(7, tx.getStatus().name());
			ps.setTimestamp(8, new java.sql.Timestamp(tx.getCreatedAt().toEpochMilli()));
			ps.setString(9, tx.getWalletId());
			ps.executeUpdate();
		} catch (SQLException e) {
			LoggerProvider.getLogger(getClass().getName()).severe(e.getMessage());
		}
	}

	@Override
	public Optional<Transaction> findById(String id) {
		String sql = "SELECT id, from_address, to_address, amount, fee_priority, fee_amount, status, created_at, wallet_id FROM transactions WHERE id = ?";
		try (Connection c = Database.getInstance().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					Transaction t = mapRow(rs);
					return Optional.of(t);
				}
			}
		} catch (SQLException e) {
			LoggerProvider.getLogger(getClass().getName()).severe(e.getMessage());
		}
		return Optional.empty();
	}

	@Override
	public List<Transaction> findPending() {
		String sql = "SELECT id, from_address, to_address, amount, fee_priority, fee_amount, status, created_at, wallet_id FROM transactions WHERE status = 'PENDING'";
		List<Transaction> list = new ArrayList<>();
		try (Connection c = Database.getInstance().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql);
			 ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				list.add(mapRow(rs));
			}
		} catch (SQLException e) {
			LoggerProvider.getLogger(getClass().getName()).severe(e.getMessage());
		}
		return list;
	}

	@Override
	public List<Transaction> findByWalletId(String walletId) {
		String sql = "SELECT id, from_address, to_address, amount, fee_priority, fee_amount, status, created_at, wallet_id FROM transactions WHERE wallet_id = ?";
		List<Transaction> list = new ArrayList<>();
		try (Connection c = Database.getInstance().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, walletId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(mapRow(rs));
				}
			}
		} catch (SQLException e) {
			LoggerProvider.getLogger(getClass().getName()).severe(e.getMessage());
		}
		return list;
	}

	@Override
	public void updateStatus(String id, TransactionStatus status) {
		String sql = "UPDATE transactions SET status = ? WHERE id = ?";
		try (Connection c = Database.getInstance().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, status.name());
			ps.setString(2, id);
			ps.executeUpdate();
		} catch (SQLException e) {
			LoggerProvider.getLogger(getClass().getName()).severe(e.getMessage());
		}
	}

	private Transaction mapRow(ResultSet rs) throws SQLException {
		Transaction t = new Transaction(
			rs.getString("from_address"),
			rs.getString("to_address"),
			rs.getBigDecimal("amount"),
			FeePriority.valueOf(rs.getString("fee_priority")),
			rs.getString("wallet_id")
		);
		t.setFeeAmount(rs.getBigDecimal("fee_amount"));
		t.setStatus(TransactionStatus.valueOf(rs.getString("status")));
		return t;
	}
}


