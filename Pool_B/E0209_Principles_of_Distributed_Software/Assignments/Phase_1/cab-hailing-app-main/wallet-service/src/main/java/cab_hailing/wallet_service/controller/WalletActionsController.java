package cab_hailing.wallet_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cab_hailing.wallet_service.Logger;
import cab_hailing.wallet_service.db_init.DBInitializer;
import cab_hailing.wallet_service.service.WalletActionsService;


@RestController
public class WalletActionsController {
	
	@Autowired
	WalletActionsService walletActionsService;
	
	@Autowired
	DBInitializer dbInitializer;
	
	@GetMapping("getBalance")
	public long getBalance(@RequestParam long custId) {
		return walletActionsService.getBalance(custId);
	}
	
	@GetMapping("deductAmount")
	public boolean deductAmount(@RequestParam long custId, @RequestParam long amount) {
		
		return walletActionsService.deductAmount(custId, amount);
	}
	
	@GetMapping("addAmount")
	public boolean addAmount(@RequestParam long custId, @RequestParam long amount) {
		return walletActionsService.addAmount(custId, amount);
	}
	
	@GetMapping("reset")
	public void reset() {
		Logger.logReset("Received reset request");
		dbInitializer.resetAndLoadAllTables();
		Logger.log("Reset : All tables reloaded");
	}
	
}
