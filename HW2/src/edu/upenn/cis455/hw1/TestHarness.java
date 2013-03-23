
package edu.upenn.cis455.hw1;

import java.io.File;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class TestHarness {	
	static class Handler extends DefaultHandler {

		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			if (qName.compareTo("servlet-name") == 0) {
				if(m_state == 5)m_state = 31;
				else m_state = 1;
			} else if (qName.compareTo("servlet-class") == 0) {
				m_state = 2;
			} else if (qName.compareTo("context-param") == 0) {
				m_state = 3;
			} else if (qName.compareTo("init-param") == 0) {
				m_state = 4;
			} else if (qName.compareTo("param-name") == 0) {
				m_state = (m_state == 3) ? 10 : 20;
			} else if (qName.compareTo("param-value") == 0) {
				m_state = (m_state == 10) ? 11 : 21;
			} else if (qName.compareTo("servlet-mapping") == 0) {
				m_state = 5;
			}else if (qName.compareTo("url-pattern") == 0) {	
				if(m_state == 32 || m_state == 31)
					m_state = 30;
			}
		}

		public void characters(char[] ch, int start, int length) {
			String value = new String(ch, start, length);
			if (m_state == 1) {
				m_servletName = value;
				m_state = 0;
			} else if (m_state == 2) {
				m_servlets.put(m_servletName, value);
				m_state = 0;
			} else if (m_state == 10 || m_state == 20) {
				m_paramName = value;
			} else if (m_state == 11) {
				if (m_paramName == null) {
					System.err.println("Context parameter value '" + value + "' without name");
					System.exit(-1);
				}
				m_contextParams.put(m_paramName, value);
				m_paramName = null;
				m_state = 0;
			} else if (m_state == 21) {
				if (m_paramName == null) {
					System.err.println("Servlet parameter value '" + value + "' without name");
					System.exit(-1);
				}
				HashMap<String,String> p = m_servletParams.get(m_servletName);
				if (p == null) {
					p = new HashMap<String,String>();
					m_servletParams.put(m_servletName, p);
				}
				p.put(m_paramName, value);
				m_paramName = null;
				m_state = 0;
			} else if(m_state == 30){
				m_urlPattern = value;
				m_servletMapping.put(m_urlPattern, m_servletName);
				m_state = 0;

			} else if(m_state == 31){
				m_servletName = value;
				m_state = 32;
			}
		}

		private int m_state = 0;
		private String m_urlPattern;
		private String m_servletName;
		private String m_paramName;
		HashMap<String, String> m_servletMapping = new HashMap<String, String>();
		HashMap<String,String> m_servlets = new HashMap<String,String>();
		HashMap<String,String> m_contextParams = new HashMap<String,String>();
		HashMap<String,HashMap<String,String>> m_servletParams = new HashMap<String,HashMap<String,String>>();
	}

	private Handler parseWebdotxml(String webdotxml) throws Exception {
		Handler h = new Handler();
		File file = new File(webdotxml);
		if (file.exists() == false) {
			System.err.println("error: cannot find " + file.getPath());
			System.exit(-1);
		}
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		parser.parse(file, h);

		return h;
	}

	private static FakeContext createContext(Handler h) {
		FakeContext fc = new FakeContext();
		for (String param : h.m_contextParams.keySet()) {
			fc.setInitParam(param, h.m_contextParams.get(param));
		}
		return fc;
	}

	private static HashMap<String,HttpServlet> createServlets(Handler h, FakeContext fc) throws Exception {
		HashMap<String,HttpServlet> servlets = new HashMap<String,HttpServlet>();
		for (String servletName : h.m_servlets.keySet()) {
			FakeConfig config = new FakeConfig(servletName, fc);
			String className = h.m_servlets.get(servletName);
			Class servletClass = Class.forName(className);
			HttpServlet servlet = (HttpServlet) servletClass.newInstance();
			HashMap<String,String> servletParams = h.m_servletParams.get(servletName);
			if (servletParams != null) {
				for (String param : servletParams.keySet()) {
					config.setInitParam(param, servletParams.get(param));
				}
			}
			servlet.init(config);
			servlets.put(servletName, servlet);
		}
		return servlets;
	}

	private static void usage() {
		System.err.println("usage: java TestHarness <path to web.xml> " 
				+ "[<GET|POST> <servlet?params> ...]");
	}

	public static Handler h;
	public static FakeContext context;
	public static HashMap<String,HttpServlet> servlets;
	public static TestHarness testHarness;

	public static TestHarness getSingleton(String pathToXml){
		try{
			if(testHarness == null){

				return new TestHarness(pathToXml);
			}
			else
				return testHarness;
		}
		catch(Exception e){
			return null;
		}
	}

	private TestHarness(String pathToXml) throws Exception{

		h = parseWebdotxml(pathToXml);
		if(h != null){
			context = createContext(h);
			servlets = createServlets(h, context);
		}
	}

	public static void DestroyServlets(){
		for(String name : servlets.keySet()){
			System.out.println("Shtting servlet: "+servlets.get(name));
			servlets.get(name).destroy();
		}
	}

	public static synchronized int servletWrapper(String[] in, String[] headerLine, OutputStream out, String postBody, String absPath) throws Exception {

		if (in.length < 3 && in.length % 2 == 0) return 0;
		FakeSession fs = null;

		int i =1;
		FakeRequest request = new FakeRequest(fs);
		FakeResponse response = new FakeResponse();
		String[] strings = in[i+1].split("\\?|&|="); // strings has the uri to the servlet + the parameters

		// This block of code checks if the given URI is valid servlet URI
		// and then returns the servlet name
		if(h.m_servletMapping.containsKey(strings[0])){
			String servletName = h.m_servletMapping.get(strings[0]);

			HttpServlet servlet = servlets.get(servletName); 

			// check if the URI points to a servlet, if not- get out
			if (servlet == null) {
				System.out.println("2, Not a servlet!");
				return 0;
			}

			// For loop to get parameters from the string variable
			for (int j = 1; j < strings.length - 1; j += 2) {
				request.setParameter(strings[j], strings[j+1]);
			}

			if (in[i].compareTo("GET") == 0 || in[i].compareTo("POST") == 0) {
				// If method is get or post, set the method in the request object
				// and invoke the service method of the servlet object
				request.shareHeaderLines(headerLine);
				request.setMethod(in[i]);
				request.setAbsPath(absPath);
				// In case of POST
				if(in[i].compareTo("POST") == 0) request.setRequestBody(postBody);

				// Complete this method by passing the body after checking that it is a POST request
				// Pass the body from the server to TestHarness as another arg
				response.setFakeRequestInstance(request);
				response.setOutputStream(out);
				servlet.service(request, response);
				response.flushBuffer();
				servlet.destroy();
			} else {
				return 0;
			}

			fs = (FakeSession) request.getSession(false);
		}
		else{
			return 0;
		}
		return 1;
	}

}
