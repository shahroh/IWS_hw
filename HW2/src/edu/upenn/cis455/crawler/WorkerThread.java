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

import com.sleepycat.je.Transaction;

import edu.upenn.cis455.storage.BerkDBWrapper;
import edu.upenn.cis455.storage.UrlStorageObject;

public class WorkerThread implements Runnable{

	private String headRequest, getRequest, hostHeader, userAgentHeader="User-Agent: cis455crawler";
	private Frontier frontier;
	private WorkerPool workerPool;

	public WorkerThread() {
		frontier = Frontier.GetSingleton();

	}

	private void BadContentType() throws Exception{
		throw new Exception("bad content type");
	}

	private void LastModifiedError() throws Exception{
		throw new Exception("last modified error");
	}

	private void BadContentLength() throws Exception{
		return; 
	}

	private void SendHeadRequest(PrintWriter out, URL targetUrl, Socket clientSoc){
		// System.out.println("SendHeadRequest");
		headRequest = "HEAD "+ targetUrl.getPath()+" HTTP/1.1";
		int portNum = (targetUrl.getPort() == -1)?80:targetUrl.getPort();
		hostHeader = "Host: "+ portNum;
		out.println(headRequest);
		out.println(userAgentHeader);
		out.println(hostHeader);
		out.println("\r");
		//out.println("");
		out.flush();
	}

	private void CheckContentType(String line) throws Exception{
		// System.out.println("CheckContentType");
		if(line.matches("Content-Type:.*")){
			if((line.matches("Content-Type:.*text/html.*"))){ 
				SetContentType("text/html");
			}
			else if(line.matches("Content-Type:.*text/xml.*")){
				SetContentType("text/xml");
			}
			else if(line.matches("Content-Type:.*application/html.*")){
				SetContentType("application/xml");

			}
			else if(line.matches("Content-Type:.*+xml.*")){
				SetContentType("+xml");

			}
			else{
				// This method should handle the case where the thread does not process this URL
				BadContentType();
			}
		}
	}

	public void SetContentType(String newContentType){
		contentType = newContentType;
	}

	private void CheckContentLength(String line) throws Exception{
		// System.out.println("CheckContentLength");
		if(line.matches("Content-Length:.*")){
			Pattern attrPattern = Pattern.compile("Content-Length:\\s*(.*)\\s*");
			Matcher m = attrPattern.matcher(line);
			int ContentSize;
			if(m.matches()){
				//System.out.println("match count " + m.groupCount());
				// System.out.println("COntent size: "+m.group(1).trim());
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

	private void CheckLastModifiedTime(String line, URL targetUrl) throws Exception{
		// System.out.println("CheckLastModTime");
		if(line.matches("Last-Modified:.*")){
			Pattern attrPattern = Pattern.compile("Last-Modified:\\s*(.*)\\s*");
			Matcher m = attrPattern.matcher(line);
			Date dateLastMod;

			if(m.matches()){
				dateLastMod = this.getDate(m.group(1));

				// Check if the last modified date of the URL object in the data store 
				// comes BEFORE the date_now variable. If not, process it again. If yes, skip it.


				// Compare date_now with date from store
				BerkDBWrapper bdb = BerkDBWrapper.GetSingleton();
				if(!bdb.compareDate(targetUrl, dateLastMod)){
					LastModifiedError();
				}

			}
		}
	}

	private void SendGetRequest(URL targetURL, PrintWriter out, Socket clientSoc){
		//System.out.println("SendGetRequest");
		headRequest = "GET "+ targetURL.getPath()+" HTTP/1.1";
		hostHeader = "Host: "+ targetURL.getHost();
		out.println(headRequest);
		out.println(hostHeader);
		out.println(userAgentHeader);
		out.println("");
	}

	private void CheckForErrorInResponse(String line) throws Exception{
		// Raise exception if the response is not HTTP 200
		if(line.startsWith("HTTP")){
			if(!line.contains("200")){
				throw new Exception();
			}
		}
	}

	private void ProcessResponseToHead(BufferedReader in, Socket clientSoc, URL targetUrl) throws Exception{
		// System.out.println("ProcessResponseToHead");
		String line = "";
		while((line = in.readLine()) != null){
			// Check for 404 Error in first line 
			CheckForErrorInResponse(line);

			// Check the content type
			CheckContentType(line);

			// Check for content length compliance
			CheckContentLength(line);

			// Check for the last modified date/time 
			// So that we only crawl it if it has been modified since the last time we crawled it
			CheckLastModifiedTime(line, targetUrl);

			//System.out.println("headline: "	+ line);
		}
	}

	static Pattern hrefPattern = Pattern.compile(".*?href\\s*=\\s*\"(.*?)\"(.*)");
	static Pattern relHrefPattern = Pattern.compile("(.*\\/).*");

	private void ProcessHref(String pattern, URL targetUrl) throws Exception{
		//System.out.println("ProcessHref line: "+pattern);
		// Put the newly crawled URL into the frontier queue
		String fullUrl = "";

		if(pattern.matches("http://(.*)")){
			frontier.addToFrontier(new URL(pattern));
		}
		else if(pattern.matches("/(.*)")){
			int portNum = (targetUrl.getPort() == -1)?80:targetUrl.getPort();
			fullUrl += "http://" + targetUrl.getHost() + ":" + portNum + pattern;
			frontier.addToFrontier(new URL(fullUrl));
		}
		else if(pattern.matches("([A-Za-z0-9].*)")){
			String path = targetUrl.getPath().substring(0, targetUrl.getPath().lastIndexOf("/")+1);
			int portNum = (targetUrl.getPort() == -1)?80:targetUrl.getPort();
			fullUrl += "http://" + targetUrl.getHost() + ":" + portNum + path + pattern;
			frontier.addToFrontier(new URL(fullUrl));
		}
	}

	private void ProcessResponseToGet(URL targetUrl, BufferedReader in, Socket clientSoc) throws Exception{
		// System.out.println("ProcessResponseToGet");
		String line = "";
		String pattern = "";
		int i = 0;
		String content = "";

		while((line = in.readLine()) != null){
			content += line;
			//System.out.println("getLine: "+line);

			// Only if it is an html file, we extract links
			if(GetContentType() == "text/html"){
				Matcher m = hrefPattern.matcher(line);
				while(m.matches()){
					pattern = m.group(1);

					ProcessHref(pattern, targetUrl);
					i = m.start(2);
					line = line.substring(i);
					m = hrefPattern.matcher(line);
				}
			}
		}

		// Store the link and its content to the database
		StoreToDatabase(targetUrl, content, new Date(), GetContentType());

	}

	private String contentType;

	public String GetContentType(){
		return contentType;
	}

	private void StoreToDatabase(URL targetUrl, String content, Date lastModDate, String contentType){
		// Method to store the link and its content to the database
		BerkDBWrapper bdb = BerkDBWrapper.GetSingleton();
		bdb.UrlToDoc(targetUrl.toString(), content, lastModDate, contentType);
	}

	private boolean IsUrlInteresting(URL targetUrl){
		//System.out.println("IsUrlInteresting");
		// returns true if response from HEAD meets our parameters of traversal
		// (Content type, content length, last modified)
		try{
			// System.out.println("IsUrlInteresting2");
			// System.out.println(targetUrl.getHost());
			//int portNum = targetUrl.getPort()>0?targetUrl.getPort():80;
			Socket clientSoc = new Socket(targetUrl.getHost(), 80);
			// System.out.println("IsUrlInteresting3");
			PrintWriter out = new PrintWriter(clientSoc.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSoc.getInputStream()));

			// Send the HEAD request to the destination server
			SendHeadRequest(out, targetUrl, clientSoc);

			// Get the response to the HEAD request from the target server, line by line
			ProcessResponseToHead(in, clientSoc, targetUrl);

			clientSoc.close();
		}
		catch(Exception e){
			System.out.println("Exception in isUrlInteresting");
			//e.printStackTrace();
			return false;
		}
		return true;
	}

	private void ProcessGet(URL targetUrl){
		//System.out.println("Inside processGet");
		// Get raw content of URL, traverse line by line, get the href, and store it in data store
		try{
			Socket clientSoc = new Socket(targetUrl.getHost(), 80);
			PrintWriter out = new PrintWriter(clientSoc.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSoc.getInputStream()));

			//System.out.println("Just before sendGetRequest");
			// Send GET request to the destination server
			SendGetRequest(targetUrl, out, clientSoc);

			// Process the response from the server
			ProcessResponseToGet(targetUrl, in, clientSoc);

			clientSoc.close();
		}
		catch(Exception e){
			//System.out.println("Exception in ProcessGet: "+e.getMessage());
			//e.printStackTrace();
		}
	}

	// Called when an item is picked from the queue
	private void ProcessHtmlUrl(URL targetURL) throws UnknownHostException, IOException{

		if(IsUrlInteresting(targetURL)){
			//System.out.println("IsUrlInteresting was passed. Entering ProcessGet");
			ProcessGet(targetURL);
		}
		else{
			System.out.println("IsUrlInteresting was not passed. Exiting");
		}
	}

	@Override
	public void run() {

		URL targetURL;

		try {
			while(true){
				//System.out.println("Thread started");
				// Wait on queue for next URL 
				targetURL = frontier.pollFromFrontier();
				System.out.println(targetURL);
				// Once we have targetURL, call the method that opens the socket and  crawls the target!
				ProcessHtmlUrl(targetURL);

			}
		} catch (Exception e) {
			//e.printStackTrace();
		}

	}
}