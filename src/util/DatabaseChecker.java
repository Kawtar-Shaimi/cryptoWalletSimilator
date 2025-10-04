package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import config.Database;

/**
 * Utilitaire pour vérifier le contenu de la base de données
 */
public class DatabaseChecker {
    
    public static void main(String[] args) {
        System.out.println("=== VÉRIFICATION BASE DE DONNÉES ===");
        
        Database.getInstance().init();
        
        System.out.println("\n[1] Contenu table WALLETS:");
        checkWallets();
        
        System.out.println("\n[2] Contenu table TRANSACTIONS:");
        checkTransactions();
        
        System.out.println("\n=== FIN VÉRIFICATION ===");
    }
    
    private static void checkWallets() {
        String sql = "SELECT id, type, address, balance, created_at FROM wallets ORDER BY created_at DESC";
        
        try (Connection c = Database.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            int count = 0;
            while (rs.next()) {
                count++;
                System.out.println(String.format("%d. ID: %s", count, rs.getString("id")));
                System.out.println(String.format("   Type: %s", rs.getString("type")));
                System.out.println(String.format("   Adresse: %s", rs.getString("address")));
                System.out.println(String.format("   Balance: %s", rs.getBigDecimal("balance")));
                System.out.println(String.format("   Créé: %s", rs.getTimestamp("created_at")));
                System.out.println();
            }
            
            if (count == 0) {
                System.out.println("❌ Aucun wallet trouvé en base de données");
            } else {
                System.out.println("✅ " + count + " wallet(s) trouvé(s)");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la vérification des wallets: " + e.getMessage());
        }
    }
    
    private static void checkTransactions() {
        String sql = "SELECT id, from_address, to_address, amount, fee_amount, status, wallet_id FROM transactions ORDER BY created_at DESC";
        
        try (Connection c = Database.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            int count = 0;
            while (rs.next()) {
                count++;
                System.out.println(String.format("%d. ID: %s", count, rs.getString("id")));
                System.out.println(String.format("   De: %s", rs.getString("from_address")));
                System.out.println(String.format("   Vers: %s", rs.getString("to_address")));
                System.out.println(String.format("   Montant: %s", rs.getBigDecimal("amount")));
                System.out.println(String.format("   Frais: %s", rs.getBigDecimal("fee_amount")));
                System.out.println(String.format("   Statut: %s", rs.getString("status")));
                System.out.println(String.format("   Wallet: %s", rs.getString("wallet_id")));
                System.out.println();
            }
            
            if (count == 0) {
                System.out.println("ℹ️ Aucune transaction trouvée");
            } else {
                System.out.println("✅ " + count + " transaction(s) trouvée(s)");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la vérification des transactions: " + e.getMessage());
        }
    }
}