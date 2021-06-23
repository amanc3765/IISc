package cab_hailing.wallet_service.db_init;

import java.io.File; 
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import cab_hailing.wallet_service.Logger;

@Component
public class DBInitFileReader {
	
	@Value("${db.init_file_dir}")
	public String fileDirectory;
	
	public List<Long> cabIDList = new ArrayList<Long>();
	public List<Long> custIDList = new ArrayList<Long>();
	public Long walletBalance;
	
	
	public void clearReadData() {
		this.cabIDList.clear();
		this.custIDList.clear();
		this.walletBalance=null;
	}
	
	public void readInitFile() throws IOException {
		clearReadData();
		
		File file = new File(fileDirectory + "IDs.txt");
		Scanner fileReader = new Scanner(file);

		// -----------------------------------------------------
		String data = fileReader.nextLine();
		if (data != null && data.equals("****")) {
			data = fileReader.nextLine();

			while (data != null && !data.equals("****")) {
				cabIDList.add(Long.parseLong(data));
				data = fileReader.nextLine();
			}
		}

		for (Long cabID : cabIDList) {
			System.out.println(cabID);
		}

		// -----------------------------------------------------
		if (data != null && data.equals("****")) {
			data = fileReader.nextLine();

			while (data != null && !data.equals("****")) {
				custIDList.add(Long.parseLong(data));
				data = fileReader.nextLine();
			}
		}

		for (Long custID : custIDList) {
			System.out.println(custID);
		}

		// -----------------------------------------------------

		if (data != null && data.equals("****")) {
			data = fileReader.nextLine();
			walletBalance = Long.parseLong(data);
		}

		Logger.log("Wallet Balance : "+walletBalance);

		// -----------------------------------------------------

		fileReader.close();
		Logger.log("Data Initialization Complete");
	}

}
