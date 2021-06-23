package pods.cabs.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
	static boolean logEnabled = true;

	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";

	private static final ThreadLocal<DateFormat> dateFormatter = ThreadLocal
			.withInitial(() -> new SimpleDateFormat("HH:mm:ss"));

	public static void log(String logString) {
		if (logEnabled) {
			System.out.println("[" + dateFormatter.get().format(new Date()) + "] LOG: " + logString);
		}
	}

	public static void logReset(String logString) {
		System.out.println("\n\n\n-----------------RESET CALLED--------------------\n");
		log(logString);
	}

	public static void logErr(String logString) {
//		System.out.println(ANSI_RED + "-----------------UNEXPECTED ERROR--------------------" + ANSI_RESET);
		log(ANSI_RED + logString + ANSI_RESET);
	}
	
	public static void logTestSuccess(String logString) {
		log(ANSI_GREEN + "TEST : " + logString + ANSI_RESET);
	}
	
	public static void logTestFail(String logString) {
		log(ANSI_RED + "TEST : " + logString + ANSI_RESET);
	}
}