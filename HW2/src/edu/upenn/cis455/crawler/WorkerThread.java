package edu.upenn.cis455.crawler;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

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
	private int mPortNum = 80;
	private PrintWriter out;
	private BufferedReader in;
	private String headRequest, getRequest, hostHeader, userAgentHeader="User-Agent: cis455crawler";
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

	private void SendHeadRequest(){
		headRequest = "HEAD "+ targetURL.getPath()+" HTTP/1.1";
		hostHeader = "Host: "+ targetURL.getHost();
		out.println(headRequest);
		out.println(userAgentHeader);
		out.println(hostHeader);
		out.println("");
	}

	private void CheckContentType(String line){
		if(line.matches("Content-Type:.*")){
			if(!(line.matches("Content-Type:.*text/html.*") || line.matches("Content-Type:.*application/html.*") || line.matches("Content-Type:.*+xml.*"))){
				// This method should handle the case where the thread does not process this URL
				BadContentType();
			}
		}
	}
	
	private void CheckContentLength(String line){
		if(line.matches("Content-Length:.*")){
			Pattern attrPattern = Pattern.compile("Content-Length:\\s*(.*)\\s*");
			Matcher m = attrPattern.matcher(line);
			int ContentSize;
			if(m.matches()){
				//System.out.println("match count " + m.groupCount());
				System.out.println("COntent size: "+m.group(0).split(":")[1].trim());
				ContentSize = Integer.parseInt(m.group(0).split(":")[1].trim());
				if(ContentSize > Frontier.GetMaxContentLength()){
					BadContentLength();
				}
			}
		}
	}
	
	private void CheckLastModifiedTime(String line){
		return;
	}
	
	private void SendGetRequest(){
		headRequest = "GET "+ targetURL.getPath()+" HTTP/1.1";
		hostHeader = "Host: "+ targetURL.getHost();
		out.println(headRequest);
		out.println(hostHeader);
		out.println(userAgentHeader);
		out.println("");
	}
	
	private void ProcessResponseToHead(BufferedReader in) throws IOException{
		String line = "";
		while((line = in.readLine()) != null){
			// Check the content type
			CheckContentType(line);
			
			// Check for content length compliance
			CheckContentLength(line);

			// Check for the last modified date/time 
			// So that we only crawl it if it has been modified since the last time we crawled it
			CheckLastModifiedTime(line);

			System.out.println("headline: "	+ line);
		}
	}
	
	private void ProcessResponseToGet(BufferedReader in) throws IOException{
		String line = "";
		//StringBuffer response = new StringBuffer();
		
		System.out.println("entering loop");
		while((line = in.readLine()) != null){
			System.out.println(line);
		}
		System.out.println("exited loop");
		/*
		while((line = in.readLine()) != null){
			response.append(line);
			System.out.println(line);
		}
		
		Tidy jtidyObj = new Tidy();
		
		Document jtidydom = jtidyObj.parseDOM(new ByteArrayInputStream(response.toString().getBytes()), null);
		NodeList a_tag = jtidydom.getElementsByTagName("a");
		System.out.println("Extracting Links from the Current Page:");
		String fullHost = "";
		for(int i=0; i<a_tag.getLength(); i++){			
			System.out.println("prob");
			NamedNodeMap att = a_tag.item(i).getAttributes();
			Node myLink = att.getNamedItem("href");
			System.out.println(myLink);
			frontier.addToFrontier(new URL(myLink.getNodeValue()));
		}
		*/
	}
	
	private void SocketRequest(URL urlToTarget) throws UnknownHostException, IOException{
		// Create new client socket object, and in/out utils
		System.out.println("targetURL: "+targetURL.getHost());
		clientSoc = new Socket(targetURL.getHost(), mPortNum);
		out = new PrintWriter(clientSoc.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(clientSoc.getInputStream()));

		// Send the HEAD request to the destination server
		SendHeadRequest();

		// Get the response to the HEAD request from the target server, line by line
		ProcessResponseToHead(in);
		
		// Send GET request to the destination server
		SendGetRequest();
		
		// Process the response from the server
		in = new BufferedReader(new InputStreamReader(clientSoc.getInputStream()));
		ProcessResponseToGet(in);
	}

	@Override
	public void run() {

		try {
			while(true){
				System.out.println("Thread started");
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