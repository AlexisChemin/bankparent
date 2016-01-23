package org.matounix.bankparent.controller;

import java.util.List;

public class AccountSynchro {
	
	protected Account account;
	protected List<Operation> operations;
	public Account getAccount() {
		return account;
	}
	public void setAccount(Account account) {
		this.account = account;
	}
	public List<Operation> getOperations() {
		return operations;
	}
	public void setOperations(List<Operation> operations) {
		this.operations = operations;
	}
	
	

}
