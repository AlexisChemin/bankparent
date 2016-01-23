package com.bankparent.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.bankparent.model.Account;
import com.bankparent.model.AccountSynchro;

public class BankParentAPIRestTemplate implements BankParentAPI {
	
	private String endpoint;
	private RestTemplate restTemplate;

	public BankParentAPIRestTemplate(String endpoint) {
		this.endpoint = endpoint;
		

		SimpleClientHttpRequestFactory clientFactory = new SimpleClientHttpRequestFactory();
		clientFactory.setConnectTimeout(60000);
		// Create a new RestTemplate instance
		restTemplate = new RestTemplate(clientFactory);

		
		MappingJacksonHttpMessageConverter jsonConverter = new MappingJacksonHttpMessageConverter();

		List<MediaType> supportedMediaTypes = new ArrayList<MediaType>();
		supportedMediaTypes.add(MediaType.APPLICATION_JSON);
		jsonConverter.setSupportedMediaTypes(supportedMediaTypes);

		List<HttpMessageConverter<?>> listHttpMessageConverters = restTemplate.getMessageConverters();
		listHttpMessageConverters.add(jsonConverter);
		restTemplate.setMessageConverters(listHttpMessageConverters);		
	}

	@Override
	public List<Account> getAccounts() {
		Account[] accounts = restTemplate.getForObject(endpoint + "/accounts", Account[].class);
		return Arrays.asList(accounts);
	}

	@Override
	public AccountSynchro synchronize(AccountSynchro accountSync) {
		ResponseEntity<AccountSynchro> result = restTemplate.postForEntity(endpoint + "/account/sync", accountSync, AccountSynchro.class);
		return result.getBody();
	}

}
