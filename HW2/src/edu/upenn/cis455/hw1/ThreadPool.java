package edu.upenn.cis455.hw1;

import java.util.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import java.io.*;
// This singleton class creates a pool of threads when instantiated. 

public class ThreadPool{

	private static int portNum;
	private static int thread_pool_size;
	private String rootDir;
	private String pathToServletXml;
	private ProcessThread runnable;
	private static ThreadPool threadPool;
	private static ArrayList<Thread> myThread;
	public static boolean interrupt_flag;
	private static int lastThreadIndex = 0;
	private static HashMap<Integer, String> uriReg = new HashMap<Integer, String>();
	private static HashMap<String, FakeSession> sessionMap = new HashMap<String, FakeSession>();
	private static HashMap<Integer, Integer> servletFg = new HashMap<Integer, Integer>();
	private static final Logger errLog = Logger.getLogger("errLog");

	public static Logger getLogger(){
		return errLog;
	}

	public static String getLogDisplay(){

		try{
			String html;
			String line = null;
			BufferedReader buf = new BufferedReader(new FileReader("./logs/info.log"));
			html = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">";
			html += "<h1> Log File Data (from ./logs/info.log):  </h1></br>";
			while((line = buf.readLine()) != null){
				html += "<h2> \r\n"+line+ "</h2></br>";			
			}
			buf.close();
			return html;
		}
		catch(Exception e){
			return null;
		}
	}

	// Constructor takes as arg, the thread pool size and queue size
	private ThreadPool(int in_thread_pool_size, String rootDir, int in_portNum, String pathToServletXml) {
		PropertyConfigurator.configure("log4j.properties");
		this.rootDir = rootDir;
		this.pathToServletXml = pathToServletXml;
		interrupt_flag = false;
		portNum = in_portNum;
		myThread = new ArrayList<Thread>();
		runnable = new ProcessThread(this.rootDir, this.pathToServletXml);
		thread_pool_size = in_thread_pool_size;
		for(int i=0; i<thread_pool_size; i++){
			myThread.add(i, new Thread(runnable));
			myThread.get(i).start();
			uriReg.put((int)myThread.get(i).getId(), "waiting");
		}
		InitializeServletFg();
	}

	public static void InitializeServletFg(){
		for(int i=0; i<thread_pool_size; i++){
			servletFg.put((int)myThread.get(i).getId(), 0);
		}
	}

	public static void GetSingleton(int in_thread_pool_size, String rootDir, int portNum, String pathToServletXml){
		if(threadPool == null){
			threadPool = new ThreadPool(in_thread_pool_size, rootDir, portNum, pathToServletXml);
		}
		return;
	}

	public static void setServletFg(int threadID){
		servletFg.put(threadID, 1);
	}

	public static int getServletFg(int threadID){
		return servletFg.get(threadID);
	}

	public static FakeSession RetrieveSession(String ID){
		return sessionMap.get(ID);
	}

	public static boolean SessionExists(String ID){
		return sessionMap.containsKey(ID);
	}

	public static void insertSessionInstance(String UID, FakeSession session){
		sessionMap.put(UID, session);
	}

	//public static void interruptAllThreads(long callingThreadId) throws AllThreadsTerminatedException{
	public static void interruptAllThreads(long callingThreadId) throws EndAllServletsException{
		if(threadPool != null){
			for(int i=0; i<thread_pool_size; i++){
				Thread thisThread = myThread.get(i);
				if(thisThread.getId() != callingThreadId){
					//if(thisThread.getS))
					thisThread.interrupt();
					while(thisThread.getState() != Thread.State.valueOf("TERMINATED"));
				}
				else{
					System.out.println("last thread ind is set here to: "+i);
					lastThreadIndex = i;
				}
			}
		}
		//throw new AllThreadsTerminatedException();
		throw new EndAllServletsException();

	}

	public static void isLastThreadTerminated(){
		System.out.println("Last thread index: " + lastThreadIndex);
		while(myThread.get(lastThreadIndex).getState() != Thread.State.valueOf("TERMINATED"));
		return;
	}

	// handle GET /control 
	public static String getControlDisplay(){
		String html;
		Thread thread;
		html = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">";
		html += "<h1> Rohan Shah (shahroh) </h1></br>";

		for(int i=0; i<thread_pool_size; i++){
			thread = myThread.get(i);
			html += "<h2> thread name: " + thread.getName() + "     status: " + thread.getState() + "     URI: " + uriReg.get((int)thread.getId())+ "</h2></br>";
		}

		return html;
	}

	// get port number
	public static int getPortNumber(){
		return portNum;
	}

	// hash the thread ID to its URI 
	public static void registerURI(long threadID, String uri){
		uriReg.put((int)threadID, uri);
	}

	public static void removeURI(long threadID){
		uriReg.remove((int)threadID);
	}
}
