package com.example;

import static org.junit.Assert.assertTrue;

import java.io.Console;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import pods.cabs.Cab;
import pods.cabs.Globals;
import pods.cabs.RideService;
import pods.cabs.Wallet;
import pods.cabs.utils.Logger;

public class TestInterface {
	
	public TestKitJunitResource testKit;

	public TestInterface(TestKitJunitResource testKit) {
		super();
		this.testKit = testKit;

	}
	
	void resetAllCabs() {
		TestProbe<Cab.NumRidesReponse> testProbe = testKit.createTestProbe();
		for (ActorRef<Cab.Command> cabActor : Globals.cabs.values()) {
			cabActor.tell(new Cab.Reset(testProbe.getRef()));
			Cab.NumRidesReponse response = testProbe.receiveMessage();
		}
	}
	
	void resetAllWallets() {
		TestProbe<Wallet.ResponseBalance> testProbe = testKit.createTestProbe();
		for (ActorRef<Wallet.Command> walletActor : Globals.wallets.values()) {
			walletActor.tell(new Wallet.Reset(testProbe.getRef()));
			Wallet.ResponseBalance response = testProbe.receiveMessage();
		}
	}
	
	void resetAll() {
		
		resetAllCabs();
		resetAllWallets();
	}
	
	void startNewTest(String testName) {
		 
		sleep();
		sleep();
		System.out.println(Logger.ANSI_PURPLE + "\n\n----------Starting New Test Case - " + testName + " ----------------\n" + Logger.ANSI_RESET); 
		sleep();

		resetAll();
		System.out.println("\n\n");
	}
 
	void walletAdd(String custId, long amountToAdd) {
		Globals.wallets.get(custId).tell(new Wallet.AddBalance(amountToAdd));
		Logger.logTestSuccess("Added " + amountToAdd + " to wallet-"+custId);
	}
	
	void walletDeduct(String custId, long amountToDeduct, long expectedBalance) {
		TestProbe<Wallet.ResponseBalance> testProbe = testKit.createTestProbe();
		Globals.wallets.get(custId).tell(new Wallet.DeductBalance(amountToDeduct, testProbe.getRef()));
		testProbe.expectMessage(new Wallet.ResponseBalance(expectedBalance));
		
		Logger.logTestSuccess("Deducted " + amountToDeduct + " from wallet-"+custId);
	}
	
	long walletGetBalanceTest(String custId, long amountToDeduct, long expectedBalance, boolean doTest) {
		TestProbe<Wallet.ResponseBalance> testProbe = testKit.createTestProbe();
		Globals.wallets.get(custId).tell(new Wallet.DeductBalance(amountToDeduct, testProbe.getRef()));
		
		if(doTest) {
			Wallet.ResponseBalance walletResponse =  testProbe.receiveMessage();
			testProbe.expectMessage(new Wallet.ResponseBalance(expectedBalance));
			return expectedBalance;
		}
		Wallet.ResponseBalance walletResponse =  testProbe.receiveMessage();
		return walletResponse.balance;
	}
	
	
	void cabSignIn(String cabId, long initialPos) {
		Globals.cabs.get(cabId).tell(new Cab.SignIn(initialPos));
	}
	
	void cabSignOut(String cabId) {
		Globals.cabs.get(cabId).tell(new Cab.SignOut());
	}
	
	RideService.RideResponse requestRide(String custId, long srcPos, long destPos) {
		TestProbe<RideService.RideResponse> fufillRideTestProbe = testKit.createTestProbe();
		
		Random rand = new Random();
		int randRideServiceId = rand.nextInt(Globals.N_RIDE_SERVICE_INSTANCES);
		
		Globals.rideService[randRideServiceId].tell(new RideService.RequestRide(custId, srcPos, destPos, fufillRideTestProbe.getRef()));
		RideService.RideResponse rideResponse =  fufillRideTestProbe.receiveMessage();
		
		Logger.logTestSuccess("Received Response : rideId: " +rideResponse.rideId + ", cabId: "+rideResponse.cabId);
		
		return rideResponse;
	}
	
	void rideEnded(String cabId, long rideId) {
		Globals.cabs.get(cabId).tell(new Cab.RideEnded(rideId));
	}
	
	void sleep() {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
