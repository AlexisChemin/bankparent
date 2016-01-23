package com.bankparent.rest;

import java.util.List;

import org.codegist.crest.annotate.Consumes;
import org.codegist.crest.annotate.FormParam;
import org.codegist.crest.annotate.POST;
import org.codegist.crest.annotate.Path;
import org.codegist.crest.annotate.Produces;

import com.bankparent.model.Account;
import com.bankparent.model.AccountSynchro;


@Consumes("application/json")
@Produces("application/json")
public interface BankParentAPI {

	
	@Path("/accounts")
    public List<Account> getAccounts();
	
	@POST
	@Path("/account/sync")
	public AccountSynchro synchronize(@FormParam("status") AccountSynchro accountSync);
}
