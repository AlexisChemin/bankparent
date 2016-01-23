package org.matounix.bankparent.controller;

import java.text.ParseException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class BankController {
	
	static String decimalPattern = "#0.00";
	
	Logger logger = LoggerFactory.getLogger(BankController.class);
	
	AccountDAO accountDAO = new AccountDAO();

	
	@RequestMapping(value = "/accounts", method = RequestMethod.GET)
	public @ResponseBody List<Account> getAccountList() {
		logger.info("getAccountList");
		java.util.logging.Logger logging = java.util.logging.Logger.getLogger(BankController.class.getName());
		logging.info("Get Accounts");
		return accountDAO.getAccounts();
 }

	
	@RequestMapping(value="/account/{accountId}", method = RequestMethod.GET)
	public @ResponseBody Account getAccount(@PathVariable String accountId) {

		logger.info("getAccount, accountId  {}", accountId);
		return accountDAO.getAccount(accountId);
 
	}
	
	@RequestMapping(value="/account/{accountId}/operations", method = RequestMethod.GET)
	public @ResponseBody List<Operation> getOperations(@PathVariable String accountId) {

		logger.info("getOperations, account name  {}", accountId);
		return accountDAO.getOperations(accountId);
	}
	
	
	@RequestMapping(value="/account/sync", method = RequestMethod.POST)
	public @ResponseBody AccountSynchro sync(@RequestBody AccountSynchro synchro) {
		logger.info("sync, account");

		AccountSynchro result = new AccountSynchro();
		List<Operation> sendbackOperations = null; // operations to send back
		
		Account account = synchro.getAccount();
		String accountId = account.getId();
		Integer accountVersion = account.getVersion();
		Integer localVersion = accountVersion;
		
		if (accountVersion==null) {
			accountVersion = 0;
		}
		// get our local version of account
		Account localAccount = accountDAO.getAccount(accountId);
		if (localAccount!= null) {

			// are-we up to date from this version of account
			localVersion = localAccount.getVersion();
			
			logger.info("client version : {}, local version : {}", accountVersion, localVersion);
			
			if (localVersion >= accountVersion) {
				logger.info("Client is out of date ");
				// upgrade account version up to local version
				account.setVersion(localVersion);
			}

			localAccount = updateAccount(account, synchro.getOperations());

		}
		else {
			// (localAccount==null)
			logger.info("Importing new account");
			localAccount = storeAccountAndOperations(account, synchro.getOperations());
		}

		// returns all new operations 
		sendbackOperations = accountDAO.getOperationBetween(accountId, accountVersion, localVersion);
		
		
		result.setOperations(sendbackOperations);
		result.setAccount(localAccount);
		
		return result;

	}


	/**
	 * Update account if  needed (i.e if operations)
	 * @param account
	 * @param operations
	 * @return
	 */
	protected Account updateAccount(Account account, List<Operation> operations) {
		
		if (operations == null || operations.size() <=0) { 
			// change what ?
			return accountDAO.getAccount(account.getId()); 
		}
		
		return storeAccountAndOperations(account, operations);
	}


	protected Account storeAccountAndOperations(Account account,
			List<Operation> operations) {
		Integer version = account.getVersion();
		if (version<=0) {
			version = 0;
			account.setVersion(version);
		}

		String accountId = account.getId();
		
		
		accountDAO.updateOperations(accountId, version , operations);

		// increase version
		version = account.increaseVersion();		
		logger.info("Account {} has its version increased to {} ", accountId, version);
		
		// compute account 'sum'
		applyOperations(account);
		
		return accountDAO.updateAccount(account);
	}


	private void applyOperations(Account account) {
		account.setSum(0.0);
		List<Operation> operations = accountDAO.getOperations(account.getId());
		for(Operation operation : operations) {
			try {
				logger.error("apply Operation {}", operation );
				executeOperationOn(operation, account);
			} catch (ParseException e) {
				logger.error("Can't execute Operation {}", operation );
				e.printStackTrace();
			}
		}
	}


	private void executeOperationOn(Operation operation, Account account) throws ParseException {

		if (operation.getCanceled()) {
			logger.info("NOT applying operation {}", operation );
			return;
		}
		logger.info("applying operation {}", operation );
		Double amount = operation.getAmount();
		if (operation.isAddition() && amount!=null) {
//			DecimalFormat df = new DecimalFormat(decimalPattern);
//			double accountValue = Double.valueOf(account.getSum());
//			accountValue += amount;
			account.increaseSum(amount);
		}		
	}
}
