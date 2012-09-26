package com.attask.descriptiondashboard;

import org.apache.commons.lang.exception.ExceptionUtils;

/**
 * User: Joel Johnson
 * Date: 9/26/12
 * Time: 12:22 PM
 */
public class Logger {
	public static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(Dashboard.class.getCanonicalName());

	public static void fine(Throwable message) {
		fine(null, message);
	}

	public static void finer(Throwable message) {
		finer(null, message);
	}

	public static void finest(Throwable message) {
		finest(null, message);
	}

	public static void info(Throwable message) {
		info(null, message);
	}

	public static void warn(Throwable message) {
		warn(null, message);
	}

	public static void error(Throwable message) {
		error(null, message);
	}

	public static void fine(String message) {
		fine(message, null);
	}

	public static void finer(String message) {
		finer(message, null);
	}

	public static void finest(String message) {
		finest(message, null);
	}

	public static void info(String message) {
		info(message, null);
	}

	public static void warn(String message) {
		warn(message, null);
	}

	public static void error(String message) {
		error(message, null);
	}

	public static void fine(String message, Throwable t) {
		LOGGER.fine(fullMessage(message, t));
	}

	public static void finer(String message, Throwable t) {
		LOGGER.finer(fullMessage(message, t));
	}

	public static void finest(String message, Throwable t) {
		LOGGER.finest(fullMessage(message, t));
	}

	public static void info(String message, Throwable t) {
		LOGGER.info(fullMessage(message, t));
	}

	public static void warn(String message, Throwable t) {
		LOGGER.warning(fullMessage(message, t));
	}

	public static void error(String message, Throwable t) {
		LOGGER.severe(fullMessage(message, t));
	}

	private static String fullMessage(String message, Throwable t) {
		if(message == null) {
			message = "";
		}
		if(t == null) {
			return message;
		}
		return message + "\n" + ExceptionUtils.getStackTrace(t);
	}
}
