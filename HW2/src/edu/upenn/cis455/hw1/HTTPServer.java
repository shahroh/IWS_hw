
package edu.upenn.cis455.hw1;

import org.apache.commons.*;
import org.apache.log4j.*;
import java.net.*;
import java.io.*;

public class HTTPServer {

	private String mRootDir;
	private int mPort; 
	private static String mPathToXml;
	private static int thread_pool_sz = 100;
	private static ServerSocket serverSocket;
	private static Socket clientSocket;
	private static TestHarness th;
	
	
	public HTTPServer(String rootDir, int portNum, String pathToServletXml) {
		// Constructor for initializing port number and root dir
		this.mPort = portNum;
		this.mRootDir = rootDir;
		this.mPathToXml = pathToServletXml;
		System.out.println("this.mPort = " + this.mPort);
		System.out.println("this.mRootDir = " + this.mRootDir);
		System.out.println("this.mPathToXml = " + this.mPathToXml);
	}	

	public void serve(){	

		// Open socket object on the specified mPort
		serverSocket = null;
		try {
			serverSocket = new ServerSocket(mPort);
		} 
		catch (IOException e) {
			System.out.println("Could not listen on port: " + mPort);
			System.exit(-1);
		}

		// Object clientSocket is used for accepting inputs from socket
		clientSocket = null;

		// Create an instance of the singleton classes: BlockingQueue and ThreadPool
		BlockingQueue blockingQueue = BlockingQueue.GetSingleton();
		ThreadPool.GetSingleton(thread_pool_sz, mRootDir, mPort, mPathToXml);

		while(!ThreadPool.interrupt_flag){
			try {
				clientSocket = serverSocket.accept();
				if(ThreadPool.interrupt_flag)
					break;
				blockingQueue.addToQueue(clientSocket);
			} 
			catch (IOException e) {
				// Exception evaluating socket communication
				System.out.println("Accept failed: " + mPort);
			}
		}
		return;
	}

	public static void main(String[] args) {

		ClassLoader cl = ClassLoader.getSystemClassLoader();
		URL[] urls = ((URLClassLoader)cl).getURLs();
		
		if(args.length == 3){

			int portNum = Integer.parseInt(args[0]);
			String rootDir = new String(args[1]);
			String pathToServletXml = new String(args[2]);
			
			// Call constructor to initialize port number and root dir of server
			HTTPServer serve_obj = new HTTPServer(rootDir, portNum, pathToServletXml);

			// Servlet Initializations 
			try{
			th = TestHarness.getSingleton(pathToServletXml);
			}
			catch(Exception e){
				System.out.println("Could not initialize servlet");
			}
			// Main processing happens here:
			serve_obj.serve();
		}
		else{
			// In case no args are specified, we output full name and SEAS login
			System.out.println("Rohan Shah (shahroh)");
			System.exit(-1);
		}

		ThreadPool.isLastThreadTerminated();
		try{
			clientSocket.close();
			serverSocket.close();
		}
		catch(IOException eIO){
			System.out.println("couldnt close sockets: "+eIO.getStackTrace());
		}
		TestHarness.DestroyServlets();
		System.out.println("Shutting down..");
		System.exit(-1);
	}

	
}
