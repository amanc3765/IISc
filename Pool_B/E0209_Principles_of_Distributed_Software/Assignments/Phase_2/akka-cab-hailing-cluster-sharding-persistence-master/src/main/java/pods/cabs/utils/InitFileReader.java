package pods.cabs.utils;

import java.io.File; 
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;


public class InitFileReader {
	
	public static String fileDirectory = "init/";

	public static class InitReadWrapper {
		public ArrayList<String> cabIDList;
		
		public InitReadWrapper(){
			cabIDList=new ArrayList<>();
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
		
		wrapperObj.cabIDList.sort(Comparator.naturalOrder());

		for (String cabID : wrapperObj.cabIDList) {
//			System.out.println(cabID);
			Logger.log("Cab : " + cabID);
		}
		// -----------------------------------------------------

		fileReader.close();
		Logger.log("Data Initialization Complete");
	}

}
