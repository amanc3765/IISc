package cab_hailing.cab_service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
	static boolean logEnabled = true;
	private static final ThreadLocal<DateFormat> dateFormatter = ThreadLocal
			.withInitial(() -> new SimpleDateFormat("HH:mm:ss"));

	public static void log(String logString) {
		if (logEnabled) {
			System.out.println("[" + dateFormatter.get().format(new Date()) + "] CAB : LOG : " + logString);
		}
	}
	
	public static void logReset(String logString) {
		System.out.println("\n\n\n-----------------RESET CALLED--------------------\n");
		log(logString);
	}
	
	public static void logErr(String logString) {
		System.out.println("-----------------UNEXPECTED ERROR--------------------");
		log(logString);
	}
}
