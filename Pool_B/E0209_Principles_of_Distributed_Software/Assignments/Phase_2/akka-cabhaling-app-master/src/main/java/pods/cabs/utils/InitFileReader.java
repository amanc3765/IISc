package pods.cabs.utils;

import java.io.File; 
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;


public class InitFileReader {
	
	public static String fileDirectory = "init/";

	public static class InitReadWrapper {
		public ArrayList<String> cabIDList;
		public ArrayList<String> custIDList;
		public long walletBalance;
		
		public InitReadWrapper(){
			cabIDList=new ArrayList<>();
			custIDList=new ArrayList<>();
		}
	}
	
	public static void readInitFile(InitReadWrapper wrapperObj) throws IOException {
		
		File file = new File(fileDirectory + "IDs.txt");
		Scanner fileReader = new Scanner(file);

		// -----------------------------------------------------
		String data = fileReader.nextLine();
		if (data != null && data.equals("****")) {
			data = fileReader.nextLine();

			while (data != null && !data.equals("****")) {
				wrapperObj.cabIDList.add(String.valueOf(Long.parseLong(data)));
				data = fileReader.nextLine();
			}
		}

		for (String cabID : wrapperObj.cabIDList) {
//			System.out.println(cabID);
			Logger.log("Cab : " + cabID);
		}

		// -----------------------------------------------------
		if (data != null && data.equals("****")) {
			data = fileReader.nextLine();

			while (data != null && !data.equals("****")) {
				wrapperObj.custIDList.add(String.valueOf(Long.parseLong(data)));
				data = fileReader.nextLine();
			}
		}

		for (String custID : wrapperObj.custIDList) {
//			System.out.println(custID);
			Logger.log("Customer : " + custID);
		}

		// -----------------------------------------------------

		if (data != null && data.equals("****")) {
			data = fileReader.nextLine();
			wrapperObj.walletBalance = Long.parseLong(data);
		}

		Logger.log("Wallet Balance : "+wrapperObj.walletBalance);

		// -----------------------------------------------------

		fileReader.close();
		Logger.log("Data Initialization Complete");
	}

}
