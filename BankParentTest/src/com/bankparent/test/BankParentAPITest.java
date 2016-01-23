package com.bankparent.test;

import java.util.ArrayList;
import java.util.List;

import android.test.AndroidTestCase;

import com.bankparent.model.Account;
import com.bankparent.model.AccountSynchro;
import com.bankparent.model.Operation;
import com.bankparent.rest.BankParentAPIRestTemplate;

public class BankParentAPITest extends AndroidTestCase {


	static String endpoint = 
			//"http://bankparent.appspot.com";
			"http://192.168.2.35:8889"; 
	
	
	public void testAccountSync() {

		BankParentAPIRestTemplate api = new BankParentAPIRestTemplate(endpoint);
		List<Account> accounts = api.getAccounts();
		int size = accounts.size();
		
		AccountSynchro accountSync = new AccountSynchro();
		Account account = new Account();
		accountSync.setAccount(account);
		List<Operation> operations = new ArrayList<Operation>();
		accountSync.setOperations(operations );
		account.setId("violette");
		account.setName("Violette");
		account.setSum(0.0);
		account.setVersion(1);
		
		Operation operation = new Operation();
		operation.setLabel("Anniversaire");
		operation.setNature("add");
		operation.setAmount(21.50);
		operations.add(operation );
		
		
		accountSync = api.synchronize(accountSync );

		accounts = api.getAccounts();
//		assertTrue(accounts.size() == size + 1);
		 
	}
	
	
	
}
