package com.mongodb.tradecraft.service_test;

import java.util.logging.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ServiceTest {
	static final String version = "0.0.1";
	static Logger logger;

	public static void main(String[] args) {
		LogManager.getLogManager().reset();

		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();

		logger = LoggerFactory.getLogger(ServiceTest.class);
		logger.info(version);
		boolean load=false;
		long nThreads = 4;
		long nCalls = 1000;
		String url = "http://localhost:5000/reviews";

		if (args.length > 0) {
			url = new String(args[0]);
		} else {
			System.out.println(
					"Usage: java -jar ServiceTest.jar [GET_]<url> [threads (4)] [times to call (1000)]");
			return;
		}

		if (args.length > 1) {
			nThreads = Integer.parseInt(args[1]);
		}

		if (args.length > 2) {
			nCalls = Integer.parseInt(args[2]);
		}

		ExecutorService simexec = Executors.newFixedThreadPool((int)nThreads);
		long start = System.currentTimeMillis();
		ArrayList<ServiceWorker> workers = new ArrayList<ServiceWorker>();
		for (int i = 0; i < nThreads; i++) {
			ServiceWorker s = new ServiceWorker(i, url, (int) nCalls);
			workers.add(s);
			simexec.execute(s);
		}

		simexec.shutdown();

		try {
			simexec.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
			long end = System.currentTimeMillis();
			System.out.println("Time Taken " + (end - start) + " ms");
			System.out.println("Calls/s " + (nCalls*nThreads*1000) / (end - start));
			// Report info from our workers

			List<String> ops = Arrays.asList("POST");
			System.out.println("\tcalls\tmean\t95th\tworst\n");
			for (String op : ops) {
				List<Long> alltimes = new ArrayList<Long>();
				for (ServiceWorker s : workers) {
					alltimes.addAll(s.GetTimes(op));

				}
				Collections.sort(alltimes);
				long totalops = alltimes.size();
				long totaltime = 0;
				for(Long t : alltimes) {
					totaltime = totaltime + t;
				}
				long worstop = alltimes.size()>0?alltimes.get(alltimes.size()-1):0;
				long centile = alltimes.size()>0?alltimes.get(alltimes.size()*3/4):0;
				System.out.printf("%s\t%d\t%d\t%d\t%d\n",op,totalops,
						totalops>0?totaltime/totalops:0,centile,worstop);
			}
		
			simexec.shutdown();
		} catch (InterruptedException e) {
			logger.error(e.getMessage());

		}
	}

}
