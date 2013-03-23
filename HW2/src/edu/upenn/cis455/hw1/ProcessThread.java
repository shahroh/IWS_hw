package edu.upenn.cis455.hw1;

import java.net.*;
import java.text.*;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;

public class ProcessThread implements Runnable {
	Socket socket;
	String mRootDir;
	String mPathToXml;
	BlockingQueue blockingQueue = BlockingQueue.GetSingleton();
	HashMap<String, String> ext2MIME = new HashMap<String, String>();
	private boolean servletFlag;

	public void setServletFlag(){
		this.servletFlag = true;
	}

	public boolean getServletFlag(){
		return this.servletFlag;
	}

	public String sendShutdownPage(){
		String msg = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">";
		msg += " <html> <h1> Rohan's Server is now SHUTTING DOWN. </h1><h2> Good bye for now</h2><html>";
		return msg;
	}

	public ProcessThread(String RootDir, String pathToServletXml) {
		this.mRootDir = RootDir;
		this.mPathToXml = pathToServletXml;

		// Fill up the hashmap for MIME type generation
		ext2MIME.put(".JPG", "image/jpg");
		ext2MIME.put(".jpg", "image/jpg");
		ext2MIME.put(".jpeg", "image/jpg");
		ext2MIME.put(".PNG", "image/png");
		ext2MIME.put(".png", "image/png");
		ext2MIME.put(".html", "text/html");
		ext2MIME.put(".gif", "image/gif");
		ext2MIME.put(".txt", "text/txt");
		ext2MIME.put(".htm", "text/htm");
	}

	public Date processIfModifiedSince(String[] toCheckLine){
		String myDateMod;
		String reqDate = " ";
		reqDate.trim();
		for(int j=1; j<(toCheckLine.length); j++){
			reqDate += toCheckLine[j]+" "; 
		}
		myDateMod = reqDate.trim();
		return this.getDate(myDateMod);

	}

	public String FormatDirectory(File file, String path){
		// This method is called when the URI points to a directory
		// It generates HTML file with URI to each of the sub-files and sub-folders

		String[] subFiles = file.list();
		String html = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">";
		for(int i=0;i<subFiles.length;i++){
			html += "<a href=" + path + "/" + subFiles[i] + ">" + subFiles[i] + "</a></br>";
		}
		return html;
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

	public String headerEssentials(int contentLength){
		return "HTTP/1.1 200 OK\r\n"+"Content-Type: text/html\r\n"+"Content-Length: "+"\r\n"+"Connection: close\r\n\r\n";
	}

	public void sendHeader(String absPath, OutputStream out, BufferedReader in){
		try{
			File path = new File(absPath);

			// Get MIME type
			int idx = path.getName().lastIndexOf(".");
			String ext;
			ext = path.getName().substring(idx);
			String mime = ext2MIME.get(ext);

			// Send header before the message body
			//System.out.println ("HTTP/1.1 200 OK\r\n");
			//System.out.println ("Content-Type: "+mime+"\r\n\r\n");
			//System.out.println (("Content-Length: "+path.length()+"\r\n").getBytes("UTF-8"));
			//System.out.println (("Connection: close\r\n\r\n").getBytes("UTF-8"));
			out.write("HTTP/1.1 200 OK\r\n".getBytes("UTF-8"));
			out.write(("Content-Type: "+mime+"\r\n").getBytes("UTF-8"));
			out.write(("Content-Length: "+path.length()+"\r\n").getBytes("UTF-8"));
			out.write(("Connection: close\r\n\r\n").getBytes("UTF-8"));
		}
		catch(IOException e){
			System.out.println("Exception caught in sendHeader: "+e.getMessage());
		}
	}

	public void processGET(String absPath, String[] relDirs, String parentPath, String[] splits, OutputStream out, BufferedReader in, Date toCompareMod, Date toCompareUnmod) throws AllThreadsTerminatedException{
		try{
			// Check if absolute path has been given
			if(relDirs[0].trim().equals("http:")){
				absPath = "";
			}

			// Traverse till the end of the path 
			for(int i=1;i<relDirs.length-1;i++){
				absPath = absPath+relDirs[i]+"/";
			}

			int i = relDirs.length-1;
			absPath = absPath+relDirs[i];

			absPath = absPath.trim();	

			// handle GET /shutdown
			if(parentPath.equals("/shutdown")){
				System.out.println("Calling thread ID: " + Thread.currentThread().getId());
				out.write(headerEssentials(0).getBytes("UTF-8"));
				out.write(this.sendShutdownPage().getBytes("UTF-8"));
				ThreadPool.interruptAllThreads(Thread.currentThread().getId());
				return;
			}

			//handle GET /control
			if(splits[1].equals("/control")){
				String controlDisplay = ThreadPool.getControlDisplay();
				out.write(headerEssentials(controlDisplay.length()).getBytes("UTF-8"));
				out.write(controlDisplay.getBytes("UTF-8"));
				out.write(ThreadPool.getLogDisplay().getBytes("UTF-8"));
				out.close();
				in.close();
				return;
			}

			// This block of code sends HTTP response header 
			File path = new File(absPath);
			long lastMod = path.lastModified();
			SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
			Date fileLastModified = null;
			try{
				fileLastModified = sdf.parse((sdf.format(lastMod)));
			}
			catch(ParseException e){

			}
			if(toCompareMod != null && fileLastModified.compareTo(toCompareMod)>0){
				out.write("HTTP/1.1 304 Not Modified\r\n".getBytes("UTF-8"));
				out.write(("Date: "+fileLastModified.toString()+"\r\n\r\n").getBytes("UTF-8"));
				return;
			}
			else if(toCompareUnmod != null && fileLastModified.compareTo(toCompareUnmod)<=0){
				out.write("HTTP/1.1 412 Precondition Failed\r\n\r\n".getBytes("UTF-8"));
				return;
			}

			if(path.isDirectory()){
				String html = this.FormatDirectory(path, parentPath);						
				out.write("HTTP/1.1 200 OK\r\n".getBytes("UTF-8"));
				out.write("Content-Type: text/html\r\n".getBytes("UTF-8"));
				out.write(("Content-Length: "+html.length()+"\r\n").getBytes("UTF-8"));
				out.write(("Connection: close\r\n\r\n").getBytes("UTF-8"));
				out.write(html.getBytes("UTF-8"));
				in.close();
				out.close();
				return;
			}

			FileInputStream fstream;

			try{
				fstream = new FileInputStream(absPath);
			}
			catch(Exception e){
				// Exception catch resulting in generation of Error 404 message
				ThreadPool.getLogger().info("File does not exist: "+absPath);
				System.out.println("Error 404: ");
				e.printStackTrace();
				String source = "<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\"><html><head><title>404 Not Found</title></head><body><h1>Not Found</h1><p>The requested URL "+ parentPath +" was not found on this server.</p><hr><address>Rohan Shah's Server at localhost Port </address></body></html>";
				out.write("HTTP/1.1 404 Not Found\r\n".getBytes("UTF-8"));
				out.write("Content-Type: text/html\r\n".getBytes("UTF-8"));
				out.write(("Content-Length: "+source.length()+"\r\n").getBytes("UTF-8"));out.write(("Connection: close\r\n\r\n").getBytes("UTF-8"));
				out.write(("Connection: close\r\n\r\n").getBytes("UTF-8"));
				out.write(source.getBytes("UTF-8"));
				out.close();
				in.close();
				return;
			}

			sendHeader(absPath, out, in);

			// File to bytes
			long length;
			while((length = fstream.available()) > 0){
				byte[] bytes = new byte[(int) length];
				fstream.read(bytes);
				out.write(bytes);
			}
			fstream.close();
		}
		catch(EndAllServletsException e){
			//ThreadPool.destroyServlets();
			throw new AllThreadsTerminatedException();
		}
		catch(IOException e){
			System.out.println("Thread ID: "+Thread.currentThread().getId());
			System.out.println("Exception caught in processGET: "+e.getMessage());
		}

	}

	public void processHEAD(String absPath, OutputStream out, BufferedReader in, Date toCompareUnmod){

		try{
			File path = new File(absPath);
			long lastMod = path.lastModified();
			Date date = new Date(lastMod);
			if(toCompareUnmod != null && !(date.after(toCompareUnmod))){

				out.write("HTTP/1.1 412 Precondition Failed\r\n\r\n".getBytes("UTF-8"));
				return;
			}

			sendHeader(absPath, out , in);
			in.close();
			out.close();
			return;
		}
		catch(IOException e){
			System.out.println("Exception caught in processHEAD: "+e.getMessage());
		}
	}

	public void processHTTP(Socket clientSocket, OutputStream out, BufferedReader in) throws InterruptedException, OneThreadLeftException{
		// In this method: HTTP request read and response generated
		try{
			// Create out and in objects to write and read to/from socket
			out = clientSocket.getOutputStream();
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			String[] inputLine = new String[32]; // According to Wikipedia, there are 31 defined HTTP request lines
			String relPath, absPath;
			int i = 1;
			String[] toCheckLine = new String[10];
			Date toCompareMod = new Date();
			Date toCompareUnmod = new Date();
			toCompareMod = null;
			toCompareUnmod = null;
			boolean noHostHeader = true;
			int contentLen = 0;

			// If there is an input from the socket, then process it:
			if(!(inputLine[0] = in.readLine()).equals(null)) {   
				while(!(inputLine[i] = in.readLine()).equals("")){
					System.out.println(inputLine[i]);
					toCheckLine = inputLine[i].split(" ");
					if((toCheckLine[0]).equals("If-Modified-Since:")){
						toCompareMod = processIfModifiedSince(toCheckLine);
					}
					else if((toCheckLine[0]).equals("If-Unmodified-Since:")){
						toCompareUnmod = processIfModifiedSince(toCheckLine);
					}

					// to handle Host: requirement for version 1.1
					if((toCheckLine[0]).equals("Host:")){
						noHostHeader = false;
					}

					// To get content-length for cleaner processing of POST request
					if((toCheckLine[0]).equals("Content-Length:")){
						contentLen = Integer.parseInt(toCheckLine[1].trim());
					}

					i++;
				}

				// Parse input line
				String[] splits = (inputLine[0]).split(" ",3);
				ThreadPool.registerURI(Thread.currentThread().getId(), splits[1]);
				String parentPath = splits[1]; 
				relPath = splits[1].replaceAll("/", ","); 
				String[] relDirs = relPath.split(",",10);
				String fileName = relDirs[relDirs.length-1];
				absPath = mRootDir+"/";

				// Check if the client has requested for a servlet to be run
				String requestType = splits[0];

				// In case of POST request
				String postBody = "";
				
				if(requestType.equals("POST")){
					char[] tempBuf = new char[contentLen];
					in.read(tempBuf,0,contentLen);
					postBody = new String(tempBuf);
					//System.out.println("Postbody before: "+postBody);
					//postBody = URLDecoder.decode(postBody, "utf-8");
					//System.out.println("Postbody after: "+postBody);

				}

				if(requestType.equals("GET") || requestType.equals("HEAD") || requestType.equals("POST")){
					String[] wrapperArgs = {mPathToXml, requestType, parentPath};
					try{
						if(TestHarness.servletWrapper(wrapperArgs, inputLine, out, postBody, absPath) != 0)
							return;
					}
					catch(Exception e){
						e.printStackTrace();
					}
				}

				// Check that request conforms to HTTP GET/HEAD request format before further processing
				if(splits[0].equals("GET") || splits[0].equals("HEAD")){


					/*
					// If HTTP/1.1, we want to check for Host header
					// This block verifies that, if it is version 1.1 then Host header must be there
					String[] version = splits[2].split("/");
					if(version[1].trim().equals("1.1")){
						if(noHostHeader == true){
							String source = "<html><body><h2>No Host: header received</h2>HTTP 1.1 requests must include the Host: header.</body></html>";
							out.write("HTTP/1.1 400 Bad Request\r\n".getBytes("UTF-8"));
							out.write("Content-Type: text/html\r\n".getBytes("UTF-8"));
							out.write(("Content-Length: "+source.length()+"\r\n").getBytes("UTF-8"));
							out.write(("Connection: close\r\n\r\n").getBytes("UTF-8"));
							out.write(source.getBytes("UTF-8"));
							out.close();
							in.close();
							return;
						}
					}
					 */


					// Check if the request is a GET request
					if(splits[0].equals("GET")){
						processGET(absPath, relDirs, parentPath, splits, out, in, toCompareMod, toCompareUnmod);
					}
					// If not, check if the request is a HEAD request
					else if(splits[0].equals("HEAD")){
						processHEAD(absPath, out, in, toCompareUnmod);
					}
					// If not GET or HEAD, the method is not implemented in this server
					else{
						out.write("HTTP/1.1 501 Not Implemented\r\n\r\n".getBytes("UTF-8"));
						out.write(("Connection: close\r\n\r\n").getBytes("UTF-8"));
					}

					// Clean up
					in.close();
					out.close();
				}
			}
		}
		catch(IOException e){
			System.out.println("Thread ID: "+ Thread.currentThread().getId());
			System.out.println("caught at buf in block, message: "+e.getMessage());
			return;
		}
		catch(AllThreadsTerminatedException AllTe){
			try{
				out.close();
				in.close();
			}
			catch(IOException IOE){

			}
			throw new OneThreadLeftException();
		}

	}

	public void run(){
		OutputStream out = null;
		BufferedReader in = null;

		while(true){
			try{
				this.servletFlag = false;
				this.socket = blockingQueue.dequeue();
				this.processHTTP(this.socket, out, in);
			}
			catch(InterruptedException e){
				System.out.println("Dying thread ID: " + Thread.currentThread().getId());

				ThreadPool.removeURI(Thread.currentThread().getId());
				return;
			}
			catch(OneThreadLeftException OTLe){
				ThreadPool.interrupt_flag = true;
				Socket echoSocket = null;
				PrintWriter toSocket = null;
				try {
					echoSocket = new Socket("localhost", ThreadPool.getPortNumber());
					toSocket = new PrintWriter(echoSocket.getOutputStream(), true);
					toSocket.println("Here's the Kick!");
				} catch (Exception e) {
				}
				System.out.println("Dying thread ID: " + Thread.currentThread().getId());
				ThreadPool.removeURI(Thread.currentThread().getId());
				return;
			}
		}
	}
}
