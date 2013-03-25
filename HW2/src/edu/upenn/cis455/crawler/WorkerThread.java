package edu.upenn.cis455.crawler;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
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
	private BufferedReader in;
	private String headRequest, getRequest, hostHeader, userAgentHeader="User-Agent: cis455crawler";
	private Frontier frontier;

	public WorkerThread() {
		frontier = Frontier.GetSingleton();
	}

	private void BadContentType() throws Exception{
		throw new Exception("bad content type");
	}

	private void BadContentLength() throws Exception{
		return; 
	}

	private void SendHeadRequest(PrintWriter out, URL targetUrl){
		headRequest = "HEAD "+ targetUrl.getPath()+" HTTP/1.1";
		hostHeader = "Host: "+ targetUrl.getHost();
		out.println(headRequest);
		out.println(userAgentHeader);
		out.println(hostHeader);
		out.println("\r");
	}

	private void CheckContentType(String line) throws Exception{
		if(line.matches("Content-Type:.*")){
			if(!(line.matches("Content-Type:.*text/html.*") || line.matches("Content-Type:.*application/html.*") || line.matches("Content-Type:.*+xml.*"))){
				// This method should handle the case where the thread does not process this URL
				BadContentType();
			}
		}
	}

	private void CheckContentLength(String line) throws Exception{
		if(line.matches("Content-Length:.*")){
			Pattern attrPattern = Pattern.compile("Content-Length:\\s*(.*)\\s*");
			Matcher m = attrPattern.matcher(line);
			int ContentSize;
			if(m.matches()){
				//System.out.println("match count " + m.groupCount());
				System.out.println("COntent size: "+m.group(1).trim());
				ContentSize = Integer.parseInt(m.group(1).trim());
				if(ContentSize > Frontier.GetMaxContentLength()){
					BadContentLength();
				}
			}
		}
	}

	public Date getDate(String myDateMod){
		SimpleDateFormat fmt1 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
		SimpleDateFormat fmt2 = new SimpleDateFormat("EEEEEEE, dd-MMM-yy HH:mm:ss z");
		SimpleDateFormat fmt3 = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");

		fmt1.setTimeZone(TimeZone.getTimeZone("GMT"));
		fmt2.setTimeZone(TimeZone.getTimeZone("GMT"));
		fmt3.setTimeZone(TimeZone.getTimeZone("GMT"));

		try{
			return fmt1.parse(myDateMod);
		}
		catch(ParseException e){
		}
		try{
			return fmt2.parse(myDateMod);
		}
		catch(ParseException e){
		}
		try{
			return fmt3.parse(myDateMod);
		}
		catch(Exception e){
		}
		return null;
	}

	private void LastModifedError()throws Exception{
		return;
	}

	private void CheckLastModifiedTime(String line) throws Exception{
		if(line.matches("Last-Modified-Since:.*")){
			Pattern attrPattern = Pattern.compile("Last-Modified-Since:\\s*(.*)\\s*");
			Matcher m = attrPattern.matcher(line);
			Date date;
			if(m.matches()){
				date = this.getDate(m.group(1));
				System.out.println("date: " + date);
				/*
				TODO :
				if(date.after(lastTouched)){
					LastModifiedError();
				}
				 */
			}
		}
	}

	private void SendGetRequest(URL targetURL, PrintWriter out){
		headRequest = "GET "+ targetURL.getPath()+" HTTP/1.1";
		hostHeader = "Host: "+ targetURL.getHost();
		out.println(headRequest);
		out.println(hostHeader);
		out.println(userAgentHeader);
		out.println("");
	}

	private void ProcessResponseToHead(BufferedReader in) throws Exception{
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

	static Pattern hrefPattern = Pattern.compile(".*?href\\s*=\\s*\"(.*?)\"(.*)");

	private void ProcessHref(String pattern) throws Exception{
		// Put the newly crawled URL into the frontier queue
		if(pattern.matches("http://(.*)")){
			frontier.addToFrontier(new URL(pattern));
		}
	}

	private void ProcessResponseToGet(BufferedReader in) throws Exception{
		String line = "";
		String pattern = "";
		int i = 0;

		while((line = in.readLine()) != null){
			Matcher m = hrefPattern.matcher(line);
			while(m.matches()){
				pattern = m.group(1);
				ProcessHref(pattern);
				i = m.start(2);
				line = line.substring(i);
				m = hrefPattern.matcher(line);
			}
		}

	}

	private boolean IsUrlInteresting(URL targetUrl){

		// returns true if response from HEAD meets our parameters of traversal
		// (Content type, content length, last modified)
		try{
			clientSoc = new Socket(targetUrl.getHost(), targetUrl.getPort());
			PrintWriter out = new PrintWriter(clientSoc.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clientSoc.getInputStream()));

			// Send the HEAD request to the destination server
			SendHeadRequest(out, targetUrl);

			// Get the response to the HEAD request from the target server, line by line
			ProcessResponseToHead(in);

		}
		catch(Exception e){
			return false;
		}
		return true;
	}

	private void ProcessGet(URL targetUrl){
		// Get raw content of URL, traverse line by line, get the href, and store it in data store
		try{
			clientSoc = new Socket(targetUrl.getHost(), targetUrl.getPort());
			PrintWriter out = new PrintWriter(clientSoc.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clientSoc.getInputStream()));

			// Send GET request to the destination server
			SendGetRequest(targetUrl, out);

			// Process the response from the server
			ProcessResponseToGet(in);
		}
		catch(Exception e){

		}
	}

	// Called when an item is picked from the queue
	private void ProcessHtmlUrl(URL targetURL) throws UnknownHostException, IOException{
		if(IsUrlInteresting(targetURL)){

			ProcessGet(targetURL);


		}
	}

	@Override
	public void run() {

		URL targetURL;

		try {
			while(true){
				System.out.println("Thread started");
				// Wait on queue for next URL 
				targetURL = frontier.pollFromFrontier();

				// Once we have targetURL, call the method that opens the socket and  crawls the target!
				ProcessHtmlUrl(targetURL);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}