package com.bankparent.test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.test.AndroidTestCase;

import com.bankparent.model.Account;

public class RestClientTest extends AndroidTestCase {


	static String endpoint = 
			"http://bankparent.appspot.com";
			// "http://localhost:8889"; 
	
	
	public void testRestTemplate() {
		// Create a new RestTemplate instance
		RestTemplate restTemplate = new RestTemplate();

		// Add the String message converter
		restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

		// Make the HTTP GET request, marshaling the response to a String
		String result = restTemplate.getForObject(endpoint + "/accounts", String.class, "Android");
		System.out.println("accounts result : " + result);
	}
	
	
	public void testRestTemplateJacksonMapper() {

		// Create a new RestTemplate instance
		RestTemplate restTemplate = new RestTemplate();

		
		MappingJacksonHttpMessageConverter jsonConverter = new MappingJacksonHttpMessageConverter();

		List<MediaType> supportedMediaTypes = new ArrayList<MediaType>();
		supportedMediaTypes.add(MediaType.APPLICATION_JSON);
		jsonConverter.setSupportedMediaTypes(supportedMediaTypes);

		List<HttpMessageConverter<?>> listHttpMessageConverters = restTemplate.getMessageConverters();
		listHttpMessageConverters.add(jsonConverter);
		restTemplate.setMessageConverters(listHttpMessageConverters);
		
		Account[] accounts = restTemplate.getForObject(endpoint + "/accounts", Account[].class);
		

		System.out.println("accounts result : " + accounts.length);
	}
	
	public void testEndpoint() throws Exception {
		HttpClient httpclient = new DefaultHttpClient();
		
		HttpGet request = new HttpGet(endpoint + "/accounts");
		request.addHeader("Accept", "application/json");
		
		
		HttpResponse response = httpclient.execute(request);
		
		HttpEntity entity = response.getEntity();
		InputStream instream = entity.getContent();
		
		StringBuilder sb = new StringBuilder();
		BufferedReader r = new BufferedReader(new InputStreamReader(instream));
		for (String line = r.readLine(); line != null; line = r.readLine()) {
			sb.append(line);
		}
		String jaxrsmessage = sb.toString();
		instream.close();

		System.out.println("accounts result : " + jaxrsmessage);
	}
	
}
