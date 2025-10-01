package ui;

import java.util.Scanner;
import java.util.logging.Logger;
import util.Database;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import metier.enums.CryptoType;
import metier.enums.FeePriority;
import metier.model.Transaction;
import metier.model.Wallet;
import metier.service.FeeCalculator;
import metier.service.FeeCalculatorFactory;
import metier.service.MempoolService;
import metier.service.WalletFactory;
import repository.TransactionRepository;
import repository.WalletRepository;
import repository.jdbc.JdbcTransactionRepository;
import repository.jdbc.JdbcWalletRepository;
import util.ValidationUtils;

/**
 * Couche de présentation: point d'entrée console et squelette du menu.
 * System.out.println est utilisé uniquement pour l'IHM.
 */
public class ConsoleApp {

	private static final Logger LOGGER = Logger.getLogger(ConsoleApp.class.getName());
	private static final WalletRepository walletRepo = new JdbcWalletRepository();
	private static final TransactionRepository txRepo = new JdbcTransactionRepository();
	private static final MempoolService mempool = new MempoolService();
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
					createTransaction(scanner);
					break;
				case "3":
					showMyPosition();
					break;
				case "4":
					compareFees(scanner);
					break;
				case "5":
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
		System.out.println("2. Creer une nouvelle transaction");
		System.out.println("3. Voir ma position dans le mempool");
		System.out.println("4. Comparer les 3 niveaux de frais");
		System.out.println("5. Consulter l'etat actuel du mempool");
		System.out.println("0. Quitter");
		System.out.print("Votre choix: ");
	}

	private static void createWallet(Scanner scanner) {
		System.out.println("Type de wallet (1=BITCOIN, 2=ETHEREUM): ");
		String t = scanner.nextLine();
		CryptoType type = "1".equals(t) ? CryptoType.BITCOIN : CryptoType.ETHEREUM;
		Wallet w = WalletFactory.createWallet(type);
		walletRepo.save(w);
		System.out.println("Wallet créé: id=" + w.getId() + ", type=" + w.getCryptoType() + ", address=" + w.getAddress());
		LOGGER.info("Wallet créé avec succès et persisté: " + w.getId());
	}

	private static void createTransaction(Scanner scanner) {
		System.out.print("ID du wallet source: ");
		String walletId = scanner.nextLine();
		Wallet w = walletRepo.findById(walletId).orElse(null);
		if (w == null) {
			System.out.println("Wallet introuvable");
			return;
		}
		System.out.print("Adresse destination: ");
		String to = scanner.nextLine();
		if (w.getCryptoType() == CryptoType.ETHEREUM && !ValidationUtils.isValidEthereumAddress(to)) {
			System.out.println("Adresse ETH invalide");
			return;
		}
		if (w.getCryptoType() == CryptoType.BITCOIN && !ValidationUtils.isValidBitcoinAddress(to)) {
			System.out.println("Adresse BTC invalide");
			return;
		}
		System.out.print("Montant (>0): ");
		String amtStr = scanner.nextLine();
		BigDecimal amount;
		try { amount = new BigDecimal(amtStr); } catch (Exception e) { System.out.println("Montant invalide"); return; }
		if (!ValidationUtils.isPositive(amount)) { System.out.println("Montant doit être > 0"); return; }
		System.out.print("Priorité (1=ECONOMIQUE, 2=STANDARD, 3=RAPIDE): ");
		String p = scanner.nextLine();
		FeePriority pr = "1".equals(p) ? FeePriority.ECONOMIQUE : ("3".equals(p) ? FeePriority.RAPIDE : FeePriority.STANDARD);

		Transaction tx = new Transaction(w.getAddress(), to, amount, pr, w.getId());
		FeeCalculator calc = FeeCalculatorFactory.forType(w.getCryptoType());
		BigDecimal fee = calc.calculateFee(tx, w, pr);
		tx.setFeeAmount(fee);
		txRepo.save(tx);
		mempool.addTransaction(tx);
		lastCreatedTx = tx;
		System.out.println("Transaction créée: id=" + tx.getId() + ", fee=" + fee);
		LOGGER.info("Transaction PENDING persistée: id=" + tx.getId());
	}

	private static void showMyPosition() {
		if (lastCreatedTx == null) {
			System.out.println("Aucune transaction récente. Créez d'abord une transaction.");
			return;
		}
		int pos = mempool.computePosition(lastCreatedTx);
		List<Transaction> sorted = mempool.getPendingSortedByFeeDesc();
		Duration eta = mempool.estimateConfirmationTime(lastCreatedTx);
		System.out.println("Votre transaction est en position " + pos + " sur " + sorted.size());
		System.out.println("Temps estimé: " + eta.toMinutes() + " minutes");
	}

	private static void compareFees(Scanner scanner) {
		if (lastCreatedTx == null) {
			System.out.println("Aucune transaction récente. Créez d'abord une transaction.");
			return;
		}
		Wallet w = walletRepo.findById(lastCreatedTx.getWalletId()).orElse(null);
		if (w == null) { System.out.println("Wallet introuvable"); return; }
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
		mempool.generateRandomPending(15);
		List<Transaction> sorted = mempool.getPendingSortedByFeeDesc();
		System.out.println("\n=== ÉTAT DU MEMPOOL ===");
		System.out.println("Transactions en attente : " + sorted.size());
		for (Transaction t : sorted) {
			String tag = (lastCreatedTx != null && t.getId().equals(lastCreatedTx.getId())) ? ">>> VOTRE TX: " : "";
			System.out.println(tag + t.getFromAddress() + " -> " + t.getToAddress() + " | frais=" + t.getFeeAmount());
		}
	}
}


