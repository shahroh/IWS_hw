package edu.upenn.cis455.crawler;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.tidy.Tidy;


public class Expt {

	public Expt() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws MalformedURLException {
		URL mDestURL = new URL("http://stackoverflow.com/");
		Socket clientSoc;
		int mPortNum = 80;
		PrintWriter out;
		BufferedReader in;
		String headRequest, getRequest, hostName;

		try {
			System.out.println("entered try");
			// Create new client socket object, and in/out utils
//			clientSoc = new Socket(mDestURL.getHost(), mPortNum);
			clientSoc = new Socket(mDestURL.getHost(), mPortNum);
			out = new PrintWriter(clientSoc.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clientSoc.getInputStream()));

			// Send the HEAD request to the destination server
			headRequest = "GET "+ mDestURL.getPath()+" HTTP/1.1";
			hostName = "Host: "+ mDestURL.getHost();
			out.println(headRequest);
			out.println(hostName);
			out.println("");

			int count  = 0;

			StringBuffer response = new StringBuffer();
			String line = "";
			while((line = in.readLine()) != null){
				System.out.println(line);
				response.append(line);
			}

			Tidy jTidy = new Tidy();
			//Converting String to InputStream
			Document JTidyDOM = jTidy.parseDOM(new ByteArrayInputStream(response.toString().getBytes()), null);
			NodeList nodesWithA = JTidyDOM.getElementsByTagName("a");
			System.out.println("Extracting Links from the Current Page:");
			String fullHost = "";
			for(int i=0; i<nodesWithA.getLength(); i++){
				NamedNodeMap attributeNodeA = nodesWithA.item(i).getAttributes();
				Node link = attributeNodeA.getNamedItem("href");
				System.out.println("Link: "+link);
			}
			/*
			String line = "";
			while((line = in.readLine()) != null){

				Tidy jTidy = new Tidy();
				//Converting String to InputStream
				Document JTidyDOM = jTidy.parseDOM(new ByteArrayInputStream(fileContent.toString().getBytes()), null);
				NodeList nodesWithA = JTidyDOM.getElementsByTagName("a");
				System.out.println("Extracting Links from the Current Page:");
				String fullHost = "";
				for(int i=0; i<nodesWithA.getLength(); i++){
					NamedNodeMap attributeNodeA = nodesWithA.item(i).getAttributes();
					Node link = attributeNodeA.getNamedItem("href");

					if(line.matches(".*href\\s*=\\s*\".*\".*")){
						Pattern hrefPattern = Pattern.compile(".*(href\\s*=\\s*\"(.*?)\").*");

						//Pattern hrefPattern = Pattern.compile("\\s*(href\\s*=\\s*).*");
						Matcher m = hrefPattern.matcher(line);
						if(m.matches()){
							//for(int i =0; i<m.groupCount(); i++){
							System.out.println(count++ + ": " + m.group(0));

							//}
						}
					}
				}

			}
			 */
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
