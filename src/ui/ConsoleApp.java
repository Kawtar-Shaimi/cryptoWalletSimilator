package ui;

import java.util.Scanner;
import java.util.logging.Logger;
import config.Database;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;


import metier.enums.FeePriority;
import metier.model.Transaction;
import metier.model.Wallet;
import metier.service.FeeCalculator;
import metier.service.FeeCalculatorFactory;
import metier.service.MempoolService;

import metier.service.WalletService;

import repository.TransactionRepository;
import repository.WalletRepository;
import repository.jdbc.JdbcTransactionRepository;
import repository.jdbc.JdbcWalletRepository;


/**
 * Couche de présentation: point d'entrée console et squelette du menu.
 * System.out.println est utilisé uniquement pour l'IHM.
 */
public class ConsoleApp {

	private static final Logger LOGGER = Logger.getLogger(ConsoleApp.class.getName());
	private static final WalletRepository walletRepo = new JdbcWalletRepository();
	private static final TransactionRepository txRepo = new JdbcTransactionRepository();
	private static final MempoolService mempool = new MempoolService();

	private static final WalletService walletService = new WalletService(walletRepo, txRepo);
	private static Transaction lastCreatedTx;

	public static void main(String[] args) {
		Database.getInstance().init();
		Scanner scanner = new Scanner(System.in);
		boolean running = true;
		while (running) {
			printMenu();
			String choice = scanner.nextLine();
			switch (choice) {
				case "1":
					createWallet(scanner);
					break;
				case "2":
					addFunds(scanner);
					break;
				case "3":
					checkBalance(scanner);
					break;
				case "4":
					createTransaction(scanner);
					break;
				case "5":
					showMyPosition();
					break;
				case "6":
					compareFees(scanner);
					break;
				case "7":
					showMempool();
					break;
				case "0":
					running = false;
					System.out.println("Au revoir.");
					break;
				default:
					System.out.println("Choix invalide. Réessayez.");
			}
		}
		scanner.close();
		LOGGER.info("Application terminée");
	}

	private static void printMenu() {
		System.out.println();
		System.out.println("=== Crypto Wallet Simulator ===");
		System.out.println("1. Creer un wallet crypto");
		System.out.println("2. Ajouter des fonds à un wallet");
		System.out.println("3. Consulter la balance d'un wallet");
		System.out.println("4. Creer une nouvelle transaction");
		System.out.println("5. Voir ma position dans le mempool");
		System.out.println("6. Comparer les 3 niveaux de frais");
		System.out.println("7. Consulter l'etat actuel du mempool");
		System.out.println("0. Quitter");
		System.out.print("Votre choix: ");
	}

	private static void createWallet(Scanner scanner) {
		System.out.println("Type de wallet (1=BITCOIN, 2=ETHEREUM): ");
		String typeInput = scanner.nextLine();
		
		WalletService.ServiceResult<Wallet> result = walletService.createWallet(typeInput);
		
		if (result.isSuccess()) {
			System.out.println(result.getMessage());
			LOGGER.info("Wallet créé avec succès et persisté: " + result.getData().getId());
		} else {
			System.out.println("Erreur: " + result.getMessage());
		}
	}

	private static void addFunds(Scanner scanner) {
		System.out.print("ID du wallet: ");
		String walletId = scanner.nextLine();
		
		// Vérifier d'abord si le wallet existe et afficher sa balance
		WalletService.ServiceResult<Wallet> walletResult = walletService.findWallet(walletId);
		if (!walletResult.isSuccess()) {
			System.out.println("Erreur: " + walletResult.getMessage());
			return;
		}
		
		Wallet wallet = walletResult.getData();
		System.out.println("Balance actuelle: " + wallet.getBalance());
		
		System.out.print("Montant à ajouter (>0): ");
		String amountStr = scanner.nextLine();
		
		WalletService.ServiceResult<Wallet> result = walletService.addFunds(walletId, amountStr);
		
		if (result.isSuccess()) {
			System.out.println(result.getMessage());
		} else {
			System.out.println("Erreur: " + result.getMessage());
		}
	}

	private static void checkBalance(Scanner scanner) {
		System.out.print("ID du wallet: ");
		String walletId = scanner.nextLine();
		
		WalletService.ServiceResult<Wallet> result = walletService.findWallet(walletId);
		
		if (result.isSuccess()) {
			Wallet wallet = result.getData();
			System.out.println("=== Informations du Wallet ===");
			System.out.println("ID: " + wallet.getId());
			System.out.println("Type: " + wallet.getCryptoType());
			System.out.println("Adresse: " + wallet.getAddress());
			System.out.println("Balance: " + wallet.getBalance());
			System.out.println("Créé le: " + wallet.getCreatedAt());
		} else {
			System.out.println("Erreur: " + result.getMessage());
		}
	}

	private static void createTransaction(Scanner scanner) {
		System.out.print("ID du wallet source: ");
		String walletId = scanner.nextLine();
		
		// Vérifier le wallet et afficher la balance
		WalletService.ServiceResult<Wallet> walletResult = walletService.findWallet(walletId);
		if (!walletResult.isSuccess()) {
			System.out.println("Erreur: " + walletResult.getMessage());
			return;
		}
		
		Wallet wallet = walletResult.getData();
		System.out.println("Balance actuelle: " + wallet.getBalance());
		
		System.out.print("Adresse destination: ");
		String toAddress = scanner.nextLine();
		
		System.out.print("Montant (>0): ");
		String amountStr = scanner.nextLine();
		
		System.out.print("Priorité (1=ECONOMIQUE, 2=STANDARD, 3=RAPIDE): ");
		String priorityInput = scanner.nextLine();

		// Utiliser le WalletService pour créer la transaction
		WalletService.ServiceResult<Transaction> result = walletService.createTransaction(walletId, toAddress, amountStr, priorityInput);
		
		if (result.isSuccess()) {
			Transaction tx = result.getData();
			mempool.addTransaction(tx);
			lastCreatedTx = tx;
			
			// Recharger le wallet depuis la base pour vérifier la mise à jour
			WalletService.ServiceResult<Wallet> updatedWalletResult = walletService.findWallet(walletId);
			if (updatedWalletResult.isSuccess()) {
				System.out.println(result.getMessage());
				System.out.println("Balance mise à jour en base: " + updatedWalletResult.getData().getBalance());
			} else {
				System.out.println(result.getMessage());
			}
			LOGGER.info("Transaction PENDING persistée: id=" + tx.getId());
		} else {
			System.out.println("Erreur: " + result.getMessage());
		}
	}

	private static void showMyPosition() {
		if (lastCreatedTx == null) {
			System.out.println("Aucune transaction récente. Créez d'abord une transaction.");
			return;
		}
		
		// Debug temporaire
		System.out.println("ID de votre transaction: " + lastCreatedTx.getId());
		
		// Affichage des informations de debug du mempool
		MempoolService.DebugInfo debugInfo = mempool.getDebugInfo();
		System.out.println("=== DEBUG MEMPOOL ===");
		System.out.println("Nombre total de transactions: " + debugInfo.getTotalTransactions());
		for (MempoolService.TransactionSummary summary : debugInfo.getTransactions()) {
			System.out.println(String.format("%d. ID: %s | Fee: %s | From: %s", 
				summary.getPosition(), summary.getId(), summary.getFeeAmount(), summary.getFromAddress()));
		}
		System.out.println("======================");
		
		int pos = mempool.computePosition(lastCreatedTx);
		List<Transaction> sorted = mempool.getPendingSortedByFeeDesc();
		Duration eta = mempool.estimateConfirmationTime(lastCreatedTx);
		
		if (pos == -1) {
			System.out.println("Transaction non trouvée dans le mempool. ID: " + lastCreatedTx.getId());
			System.out.println("Frais de votre transaction: " + lastCreatedTx.getFeeAmount());
		} else {
			System.out.println("Votre transaction est en position " + pos + " sur " + sorted.size());
			System.out.println("Temps estimé: " + eta.toMinutes() + " minutes");
			System.out.println("ID de votre transaction: " + lastCreatedTx.getId());
			System.out.println("Frais de votre transaction: " + lastCreatedTx.getFeeAmount());
		}
	}

	private static void compareFees(Scanner scanner) {
		if (lastCreatedTx == null) {
			System.out.println("Aucune transaction récente. Créez d'abord une transaction.");
			return;
		}
		
		WalletService.ServiceResult<Wallet> result = walletService.findWallet(lastCreatedTx.getWalletId());
		if (!result.isSuccess()) {
			System.out.println("Erreur: " + result.getMessage());
			return;
		}
		
		Wallet w = result.getData();
		FeeCalculator calc = FeeCalculatorFactory.forType(w.getCryptoType());

		System.out.println("\n+----------------------+--------------+----------+----------+");
		System.out.println("| Niveau               | Frais        | Position | ETA(min) |");
		System.out.println("+----------------------+--------------+----------+----------+");
		for (FeePriority pr : new FeePriority[]{FeePriority.ECONOMIQUE, FeePriority.STANDARD, FeePriority.RAPIDE}) {
			Transaction t = new Transaction(lastCreatedTx.getFromAddress(), lastCreatedTx.getToAddress(), lastCreatedTx.getAmount(), pr, w.getId());
			BigDecimal fee = calc.calculateFee(t, w, pr);
			int position = mempool.computeHypotheticalPosition(fee);
			long eta = position * 10L;
			String line = String.format("| %-20s | %-12s | %8d | %8d |", pr, fee, position, eta);
			System.out.println(line);
		}
		System.out.println("+----------------------+--------------+----------+----------+\n");
	}

	private static void showMempool() {
		// Génère des transactions aléatoires sans supprimer les transactions utilisateur
		mempool.generateRandomPending(15);
		List<Transaction> sorted = mempool.getPendingSortedByFeeDesc();
		System.out.println("\n=== ÉTAT DU MEMPOOL ===");
		System.out.println("Transactions en attente : " + sorted.size());
		
		boolean userTxFound = false;
		for (int i = 0; i < sorted.size(); i++) {
			Transaction t = sorted.get(i);
			boolean isUserTx = (lastCreatedTx != null && t.getId().equals(lastCreatedTx.getId()));
			if (isUserTx) {
				userTxFound = true;
				System.out.println(String.format("%d. >>> VOTRE TRANSACTION (Position %d) <<<", 
					i + 1, i + 1));
				System.out.println("    " + t.getFromAddress() + " -> " + t.getToAddress() + " | frais=" + t.getFeeAmount());
			} else {
				System.out.println(String.format("%d. %s -> %s | frais=%s", 
					i + 1, t.getFromAddress(), t.getToAddress(), t.getFeeAmount()));
			}
		}
		
		if (lastCreatedTx != null && !userTxFound) {
			System.out.println("\n[WARNING] Votre transaction n'apparait pas dans le mempool actuel.");
			System.out.println("ID de votre transaction : " + lastCreatedTx.getId());
		}
	}
}


