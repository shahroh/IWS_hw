package edu.upenn.cis455.servlet;

import edu.upenn.cis455.xpathengine.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.net.*;

public class XPathServlet extends HttpServlet {

	public void doGet(HttpServletRequest req, HttpServletResponse res)throws ServletException, IOException 
	{   
		String myForm = "<HTML><HEAD><TITLE>XFilter</TITLE></HEAD><BODY>"+
				"<form name=\"input\" action=\"xpath\" method=\"post\">"+
				"<h1> Full name: Rohan Shah SEAS login: shahroh"+
				" Welcome to my XFilter Engine!! </h1><br/>"+
				"<h3> enter the fields and hit submit button!</h3>"+
				"XPaths: "+ 
				"<br/><textarea rows=\"10\" cols=\"30\" name=\"xpath\">"+
				"</textarea>"+"<br/>"+
				"XML: <br/><input type=\"text\" name=\"xml\"><br/>"+
				"<input type=\"submit\" value=\"Submit\">"+
				"</form>";

		res.setContentType("text/html");
		PrintWriter out = res.getWriter();
		out.println(myForm);

	}

	public void doPost(HttpServletRequest req, HttpServletResponse res)throws ServletException, IOException 
	{       
		String xpath = req.getParameter("xpath");
		String xml = req.getParameter("xml");

		xpath = URLDecoder.decode(xpath, "utf-8");
		xml = URLDecoder.decode(xml, "utf-8");

		String[] xpaths = xpath.split("\r\n");
		
		// Instatiating the XPathEngine class
		XPathEngineImpl myEngine = new XPathEngineImpl();

		// Set the XPaths in the instance
		myEngine.setXPaths(xpaths);

		boolean anyInvalid = false;

		// Validating the input XPaths
		boolean[] isValidResult = new boolean[xpaths.length];
		for(int i=0; i<xpaths.length; i++){
			if((isValidResult[i] = myEngine.isValid(i)) == false){
				anyInvalid = true;
			}
		}

		// If any of the XPaths is invalid, we print a message giving the status of each XPath's validity
		if(anyInvalid){
			String invalidMsg = "<HTML><HEAD><TITLE>XFilter</TITLE></HEAD><BODY>"+
					"At least on of the entered XPaths is invalid.";
			String myForm = "<h1> is valid failure, STATUS: </h1>" + "<table>";
		
			for(int i=0; i<xpaths.length; i++){
				myForm += "<tr><td>"+xpaths[i]+"</td><td>"+isValidResult[i]+"</td></tr>";
			}
			myForm += "</table></BODY></HTML>";
			res.setContentType("text/html");
			PrintWriter out = res.getWriter();
			out.println(invalidMsg);
			out.println(myForm);
			return;
		}
		
		// Set the XML file URI in the XFilter engine, (SINCE IM USING SAX PARSER METHOD)
		myEngine.setXmlUri(xml);

		// Evaluate the XML against the Validated Xpaths
		boolean[] evaluateResult = myEngine.evaluate(null);

		String myForm = "<HTML><HEAD><TITLE>XFilter</TITLE></HEAD><BODY>"+
				"<h1> Results of My XFilter Engine's Matching: </h1>" + "<table>";
	
		for(int i=0; i<evaluateResult.length; i++){
			myForm += "<tr><td>"+xpaths[i]+"</td><td>"+evaluateResult[i]+"</td></tr>";
		}
		myForm += "</table></BODY></HTML>";
		res.setContentType("text/html");
		PrintWriter out = res.getWriter();
		out.println(myForm);
	}
}