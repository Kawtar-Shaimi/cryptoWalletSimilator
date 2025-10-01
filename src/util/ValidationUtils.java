package util;

import java.math.BigDecimal;
import java.util.Optional;

public final class ValidationUtils {

	private ValidationUtils() {}

	public static boolean isPositive(BigDecimal value) {
		return value != null && value.compareTo(BigDecimal.ZERO) > 0;
	}

	public static boolean isValidEthereumAddress(String address) {
		return address != null && address.matches("0x[0-9a-fA-F]{40}");
	}

	public static boolean isValidBitcoinAddress(String address) {
		return address != null && (address.startsWith("1") || address.startsWith("3") || address.startsWith("bc1"));
	}

	public static <T> Optional<T> ofNullable(T value) {
		return Optional.ofNullable(value);
	}
}


