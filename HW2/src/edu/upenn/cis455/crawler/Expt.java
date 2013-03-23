package edu.upenn.cis455.crawler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;

public class Expt {

	public Expt() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws MalformedURLException {
		URL mDestURL = new URL("http://www.rrap-software.com/");
		Socket clientSoc;
		int mPortNum = 80;
		PrintWriter out;
		BufferedReader in;
		String headRequest, getRequest;

		try {

			// Create new client socket object, and in/out utils
			clientSoc = new Socket(mDestURL.getHost(), mPortNum);
			out = new PrintWriter(clientSoc.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clientSoc.getInputStream()));

			// Send the HEAD request to the destination server
			headRequest = "HEAD "+ mDestURL.getPath()+" HTTP/1.1";
			out.println(headRequest);
			out.println("");

			String line = "";
			while((line = in.readLine()) != null){
				//if(line.matches("Content-Length:.*")){
					//if(line.matches("Content-Length:.*text/html.*")){
						System.out.println(line);
					//}
				//}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
