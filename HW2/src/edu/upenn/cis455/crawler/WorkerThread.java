package edu.upenn.cis455.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WorkerThread implements Runnable{

	/* 
	 * 1. Open client side socket (with the URL to visit)
	 * 2. Send HEAD request to that URL
	 * 3. Analyze response and depending on criteria decide whether to proceed or not
	 * 4. Criteria: content type, length, if-modified-since a particular time
	 * 5. If yes, then send GET request and get the HTML/XML 
	 * 6. Extract all URL's from the document
	 * 7. For now, print them!
	 */
	// NOT ACCOUNTING FOR: Relative URL, # (fragment), encoding/deciding (which is in accordance with HTTP),


	private Socket clientSoc;
	private int mPortNum;
	private PrintWriter out;
	private BufferedReader in;
	private String headRequest, getRequest, userAgentHeader="User-Agent: cis455crawler";
	private URL targetURL;
	private Frontier frontier;

	public WorkerThread() {
		frontier = Frontier.GetSingleton();
	}

	private void BadContentType(){
		return; 
	}

	private void BadContentLength(){
		return; 
	}

	private void SocketRequest(URL urlToTarget) throws UnknownHostException, IOException{
		// Create new client socket object, and in/out utils
		clientSoc = new Socket(targetURL.getHost(), mPortNum);
		out = new PrintWriter(clientSoc.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(clientSoc.getInputStream()));

		// Send the HEAD request to the destination server
		headRequest = "HEAD "+ targetURL.getPath()+" HTTP/1.1";
		
		out.println(headRequest);
		out.println(userAgentHeader);
		out.println("");

		String line = "";

		// Get the response to the HEAD request from the target server, line by line
		while((line = in.readLine()) != null){
			// Check the content type
			if(line.matches("Content-Type:.*")){
				if(!(line.matches("Content-Type:.*text/html.*") || line.matches("Content-Type:.*application/html.*") || line.matches("Content-Type:.*+xml.*"))){
					// This method should handle the case where the thread does not process this URL
					BadContentType();
				}
			}

			// Check for content length compliance
			if(line.matches("Content-Length:.*")){
				Pattern attrPattern = Pattern.compile("Content-Length:\\s*(.*)\\s*");
				Matcher m = attrPattern.matcher(line);
				int ContentSize;
				if(m.matches()){
					//System.out.println("match count " + m.groupCount());
					ContentSize = Integer.parseInt(m.group(0));
					if(ContentSize > Frontier.GetMaxContentLength()){
						BadContentLength();
					}
				}
			}

			// Check for the last modified date/time 
			// So that we only crawl it if it has been modified since the last time we crawled it
			
			
			
			
			System.out.println(line);
		}
	}

	@Override
	public void run() {

		try {
			while(true){
				// Wait on queue for next URL 
				targetURL = frontier.pollFromFrontier();

				// Once we have targetURL, call the method that opens the socket and  crawls the target!
				SocketRequest(targetURL);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}