package com.mongodb.tradecraft.service_test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceWorker implements Runnable {

	Logger logger;
	HttpClient client;
	int threadId;
	int nCalls = 1000;
	String url = null;
	Document example;
	int maxCustomers = 1000000;
	int maxProducts = 1234;
	String nextReview = "";
	String lastProduct = "";
	boolean useGet;

	StringEntity entity;

	Map<String, List<Long>> times = new HashMap<String, List<Long>>();

	List<Long> GetTimes(String op) {
		return times.get(op);
	}

	ServiceWorker(int threadid, String url, int ncalls) {
		logger = LoggerFactory.getLogger(ServiceWorker.class);
		client = HttpClientBuilder.create().build();
		this.threadId = threadid;
		this.url = url;
		this.nCalls = ncalls;
		this.useGet = false;
		
		if( url.startsWith("GET:")) {
			useGet=true;
			this.url = url.substring(4);
		}
		// If you want dynamic posts - put an example here then move the
		// entity code into postTest and play with the values before converting to a String
		// If it's static we do it here for performance

		String sample = "{a:1}";
		example = Document.parse(sample);

		// Move into TestPost if example is dynamic
		entity = new StringEntity(example.toJson(), "UTF-8");
		entity.setContentType("application/json");
	

		times.put("POST", new ArrayList<Long>());
		//times.put("GET", new ArrayList<Long>());
		//times.put("MORE", new ArrayList<Long>());
	}

	void TestGet() {
		HttpGet get = new HttpGet(url);
		get.setHeader("User-Agent", "ServiceTest");
		HttpResponse response;
		try {
			long startTime = System.currentTimeMillis();
			response = client.execute(get);
			String rval = EntityUtils.toString(response.getEntity(), "UTF-8");
			response.getEntity().getContent().close();
			long endTime = System.currentTimeMillis();
			long duration = endTime - startTime;
			times.get("POST").add(duration);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}

	void TestPost() {
		HttpPost post = new HttpPost(url);
		post.setEntity(entity);

		

		post.setHeader("User-Agent", "ServiceTest");
		HttpResponse response;
		try {
			long startTime = System.currentTimeMillis();
			
			response = client.execute(post);
			String rval = EntityUtils.toString(response.getEntity(), "UTF-8");
			response.getEntity().getContent().close();
			long endTime = System.currentTimeMillis();
			long duration = endTime - startTime;
			times.get("POST").add(duration);

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}

	
	public void run() {

		for (int c = 0; c < nCalls; c++) {
			if(useGet) {
				TestGet();
			} else {
				TestPost();
			}
		}
	}

}
