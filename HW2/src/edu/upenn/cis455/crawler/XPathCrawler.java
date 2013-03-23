package edu.upenn.cis455.crawler;

import java.net.MalformedURLException;
import java.net.URL;

import edu.upenn.cis455.storage.*;

public class XPathCrawler {
	
	static int maxNumFiles;
	static String firstWebPage;
	static String BDBStore;
	static int maxDocSize;
	
	private static void InitializeCrawl(String firstDestination, String BDBStore, int maxDocSize) throws MalformedURLException{
		URL firstDest = new URL(firstDestination);
		
		// Get singleton frontier object for the crawler
		Frontier myFrontier = Frontier.GetSingleton(maxDocSize);
		
		// Get singleton threadpool object (wherein each worker is initialized and is waiting on the queue)
		WorkerPool workerPool = WorkerPool.GetSingleton();
		
		
		
		
	}

	public static void main(String args[])
	{
		// Read in the command line arguments:
		if(args.length >= 3){
			try{
			// URL of webpage at which to start
			firstWebPage = new String(args[0]);

			// Directory containing BDB Data store
			BDBStore = new String(args[1]);

			// Max size (in MB) of a document to be retrieved from web server
			maxDocSize = Integer.parseInt(args[2]);

			// Optional 4th argument
			if(args.length == 4){
				// Number of files to retireve before stopping
				maxNumFiles = Integer.parseInt(args[3]);
			}
			}
			catch(Exception e){
				System.out.println("Exception: "+e.getMessage());
			}
		}
		else{
			// In case no args are specified, we output full name and SEAS login
			System.out.println("Rohan Shah (shahroh)");
			System.exit(-1);
		}

	}

}
