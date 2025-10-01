package util;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Fournit des instances de Logger configurées.
 */
public class LoggerProvider {

	public static Logger getLogger(String name) {
		Logger logger = Logger.getLogger(name);
		logger.setUseParentHandlers(false);
		ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Level.INFO);
		handler.setFormatter(new SimpleFormatter());
		if (logger.getHandlers().length == 0) {
			logger.addHandler(handler);
		}
		logger.setLevel(Level.INFO);
		return logger;
	}
}


