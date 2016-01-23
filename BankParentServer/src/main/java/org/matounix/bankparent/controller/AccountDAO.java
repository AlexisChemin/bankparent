package org.matounix.bankparent.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Transaction;

public class AccountDAO {
	

	Logger log = LoggerFactory.getLogger(AccountDAO.class);
	
	Map<String, Account> accounts = new HashMap<String, Account>();
	Map<String, List<Operation>> operations = new HashMap<String, List<Operation>>();
	
	public AccountDAO() {
	}


	
	public List<Account> getAccounts() {
		
		List<Account> accounts = new ArrayList<Account>();
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		// The Query interface assembles a query
		Query q = new Query("Account");

		PreparedQuery pq = datastore.prepare(q);

		for (Entity entity : pq.asIterable()) {
			accounts.add( toAccount(entity));
		}
		return accounts;
	}
	
	public Account getAccount(String accountId) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Key accountKey = KeyFactory.createKey("Account", accountId);

		try {
			Entity entity = datastore.get(accountKey);
			return  toAccount(entity);
		} catch (EntityNotFoundException e) {
			// log.error("Account '"+accountId+"' not found ", e);
			return null;
		}
		
	}
	
	public List<Operation> getOperations(String accountId) {
		
		List<Operation> operations = new ArrayList<Operation>();
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		// The Query interface assembles a query
		//Query q = new Query("Operation");
		Key accountKey = KeyFactory.createKey("account", accountId);
		Query q = new Query("Operation", accountKey)
		                    .setAncestor(accountKey);		
		
		//q.setFilter(FilterOperator.EQUAL.of("account",accountId)); 
		
		PreparedQuery pq = datastore.prepare(q);

		for (Entity entity : pq.asIterable()) {
			Operation operation = toOperation(entity);
			operations.add( operation);
			log.info("getOperations '{}' operation : {}", accountId, operation );
		}
		return operations;
	}
	
	public List<Operation> getOperationBetween(String accountId, Integer ltLow, Integer ltHigh) {
		
		log.info("Getting Account '{}' operations matching  : {} <= version <= {}", accountId, ltLow, ltHigh );
		List<Operation> operations = new ArrayList<Operation>();
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		// The Query interface assembles a query
		Key accountKey = KeyFactory.createKey("account", accountId);
		//Query q = new Query("Operation");
		Query q = new Query("Operation", accountKey)
		                    .setAncestor(accountKey);

		
//		Filter accountFilter = FilterOperator.EQUAL.of("account",accountId);
		Filter upperFilter = FilterOperator.LESS_THAN_OR_EQUAL.of("version", ltHigh);
		Filter lowerFilter = FilterOperator.GREATER_THAN_OR_EQUAL.of("version", ltLow);

		//q.setFilter(CompositeFilterOperator.and(accountFilter, upperFilter, lowerFilter)); 
		q.setFilter(CompositeFilterOperator.and( upperFilter, lowerFilter));
		
		PreparedQuery pq = datastore.prepare(q);

		for (Entity entity : pq.asIterable()) {
			Operation operation = toOperation(entity);
			operations.add( operation);
			log.info("getOperationBetween '{}' operation : {}", accountId, operation );
		}
		log.info("Getting Account '{}', # operations matching :", operations.size() );
		
		return operations;
	}
	
	
	protected static Entity toEntity(Account account) {		
		String id = account.getId();	
		String name = account.getName();
		Double sum = account.getSum();
		Integer version = account.getVersion();
		
		Entity entity = new Entity("Account", id);
		if (name!=null) {
			entity.setProperty("name", name);
		}
		if (sum!=null) {
			entity.setProperty("sum", sum);
		}
		if (version!=null) {
			entity.setProperty("version", version);
		}
		return entity;
	}


	protected static Account toAccount(Entity entity) {
		Account account = new Account();
		account.setId(entity.getKey().getName());
		account.setName(entity.getProperty("name").toString());
		account.setSum(Double.valueOf(entity.getProperty("sum").toString()));
		account.setVersion( new Integer((entity.getProperty("version").toString())) );
		return account;
	}
	

	protected static Operation toOperation(Entity entity) {
		Operation operation = new Operation();
		operation.setId(entity.getKey().getName());
		Object uid = entity.getProperty("uid");
		if (uid!=null) {
			operation.setId(uid.toString());
		}
		Object label = entity.getProperty("label");
		if (label!=null) {
			operation.setLabel(label.toString());
		}

		Object logicalTime = entity.getProperty("version");
		if (logicalTime!=null) {
			operation.setVersion( new Integer((logicalTime.toString())) );
		}

		Object nature = entity.getProperty("nature");
		if (nature!=null) {
			operation.setNature(nature.toString());
		}

		Object canceled = entity.getProperty("canceled");
		if (canceled!=null) {
			operation.setCanceled(new Boolean(canceled.toString()));
		}
		
		Object amount = entity.getProperty("amount");
		if (amount!=null) {
			operation.setAmount(Double.valueOf(amount.toString()));
		}
		
		Object creationTimestamp = entity.getProperty("creationTimestamp");
		if (creationTimestamp!=null) {
			operation.setCreationTimestamp(Long.valueOf(creationTimestamp.toString()));
		}
		
		return operation;
	}

	
	
	protected static Entity toEntity(String accountId, Operation operation) {		
//		Entity entity = new Entity("Operation", operation.getId());
		Key accountKey = KeyFactory.createKey("account", accountId);

		Entity entity = new Entity("Operation", accountKey);

		entity.setProperty("uid",  operation.getId());
		entity.setProperty("label", operation.getLabel());
		entity.setProperty("version", operation.getVersion());
		entity.setProperty("nature", operation.getNature());
		entity.setProperty("amount", operation.getAmount());
		entity.setProperty("creationTimestamp", operation.getCreationTimestamp());
		entity.setProperty("canceled", operation.getCanceled());
		return entity;
	}

	public void updateOperations(String accountId, Integer version, List<Operation> operations) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Transaction t = datastore.beginTransaction();
		if (operations != null) {

			for (Operation operation : operations) {
				operation.setVersion(version);
				Entity operationEntity = toEntity(accountId, operation);
				log.info("Account : {}, recording op {} ", accountId, operation);
				datastore.put(t, operationEntity);
			}
		}		
		t.commit();
	}
	
	public Account updateAccount(Account account) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Transaction t = datastore.beginTransaction();
		Entity accountEntity = toEntity(account);

		datastore.put(t, accountEntity);
		t.commit();
		return account;
	}

}
