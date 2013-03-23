package edu.upenn.cis455.xpathengine;
import org.w3c.dom.Document;

import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;

public class XPathEngineImpl implements XPathEngine {

	int track = 0;

	// Data structure to hold raw XPaths 
	String[] xpth;

	// Global list of head nodes
	ArrayList<XPathNode> headNodes = new ArrayList<XPathNode>();

	int curLevel = 0;
	char currentToken, previousToken;
	int charNum = 0, totalNum = 0;

	// HashMap for the waiting list
	HashMap<String, ArrayList<XPathNode>> waitList = new HashMap<String, ArrayList<XPathNode>>();

	// Handler class for SAX parser (extra credit)
	static class Handler extends DefaultHandler {
		// Hashmaps for the candidate list
		HashMap<String, ArrayList<XPathNode>> candList = new HashMap<String, ArrayList<XPathNode>>();
		
		int curLevel = 0;
		boolean[] queryMatches;
		boolean[] queryFails;
		Stack<String> nodeTrav;

		public Handler(ArrayList<XPathNode> headNodes){
			queryMatches = new boolean[headNodes.size()];
			queryFails = new boolean[headNodes.size()];
			nodeTrav = new Stack<String>();

			// Initialize queryMatches to all false
			for(int i=0; i<queryMatches.length; i++){
				queryMatches[i] = false;
			}

			// Initialize queryMatches to all false
			for(int i=0; i<queryFails.length; i++){
				queryFails[i] = true;
			}

			// Fill up the candidate list with head nodes of all the Xpaths
			for(XPathNode headNode : headNodes){
				if(candList.containsKey(headNode.nodeName)){
					ArrayList<XPathNode> temp = candList.get(headNode.nodeName);
					temp.add(headNode);
				}
				else{
					ArrayList<XPathNode> temp = new ArrayList<XPathNode>();
					temp.add(headNode);
					candList.put(headNode.nodeName, temp);
				}
			}
		}

		public void startElement(String uri, String localName, String qName, Attributes attributes) {

			boolean attTest = false;
			// Check if this element is already a hash key or not
			if(candList.containsKey(qName)){
				ArrayList<XPathNode> nodes = candList.get(qName);
				for(XPathNode node : nodes){
					if(node.level == curLevel){
						// Attribute test
						for(String attName : node.att.keySet()){
							if(!node.att.get(attName).equals(attributes.getValue(attName))){
								attTest = true;
							}
						}
						// If attribute test passes
						if(attTest == false){
							nodeTrav.push(node.nodeName);
							// check if it is last node in list
							if(!node.nextNodePointer.isEmpty()){
								// Copy all the next nodes into their respective candidate lists
								ArrayList<XPathNode> nextNodes = node.nextNodePointer;
								for(XPathNode nextNode : nextNodes){
									if(candList.containsKey(nextNode.nodeName)){
										ArrayList<XPathNode> temp = candList.get(nextNode.nodeName);
										temp.add(nextNode);
									}
									else{
										ArrayList<XPathNode> temp = new ArrayList<XPathNode>();
										temp.add(nextNode);
										candList.put(nextNode.nodeName, temp);
									}
								}

							}
							else{
								// If this node is the last in the query, the query  has MATCHED!
								queryMatches[node.QeuryID] = true;
							}
						}
					}
				}
			}
			curLevel++;
		}

		public void characters(char[] ch, int start, int length) {
			String nodeName = nodeTrav.peek();

			// Get the list of nodes based on element name and level
			ArrayList<XPathNode> nodes = candList.get(nodeName);
			String elemText = "";
			for(int i=start; i<(start+length); i++){
				elemText += ch[i];
			}
			// Traverse the nodes for text() and contains() tests
			for(XPathNode x : nodes){
				if(x.level == curLevel){
					// text() filter
					if(!x.textField.trim().isEmpty()){
						if(!elemText.equals(x.textField)) queryFails[x.QeuryID] = false;
					}

					// contains() filter
					for(int i=0; i<x.containsField.size(); i++){
						if(!elemText.matches(".*"+x.containsField.get(i)+".*")) queryFails[x.QeuryID] = false;
					}

				}

			}
		}	

		public void endElement(String uri, String localName, String qName) {
			// Check hashmap of candidate list for key corresponding to the element name
			// and then match level of the node to the curLevel and for all matches, remove the node from 
			// candidate list
			nodeTrav.pop();
			if(candList.containsKey(qName)){
				ArrayList<XPathNode> nodes = candList.get(qName);
				for(XPathNode node : nodes){
					if(node.level == curLevel){
						nodes.remove(node);
					}
				}
				candList.put(qName, nodes);
			}
			else{
				// There is a mismatch of tags in the XML document!
			}
			curLevel--;

		}
	}

	// Setter method for XML URI 
	String XmlUri = "";
	public void setXmlUri(String xml){
		XmlUri = xml;
	}

	public XPathEngineImpl() {
		// Do NOT add arguments to the constructor!!
	}

	public void setXPaths(String[] s) {
		xpth = s;
	}

	private boolean EvaluateTest(String Xpath){

		// Handle the case for text
		if(Xpath.matches("\\s*text()\\s*=\\s*\"\\s*.*\\s*\"\\s*"))
			return true;

		// Handle the case for contains
		if(Xpath.matches("\\s*contains[(]\\s*text[(][)]\\s*[,]\\s*[\"]\\s*.*\\s*[\"]\\s*[)]\\s*")){
			return true;
		}

		// Handle the case for @attname
		if(Xpath.matches("\\s*@\\w+\\s*=\\s*\"\\s*.*\\s*\""))
			return true;

		// Handle case for step
		if(Xpath.matches("\\s*\\w+\\s*.*") && EvaluateStep(Xpath)) return true;

		// If it doesnt conform to any of the cases, return false
		return false;
	}

	private boolean EvaluatePredicate(String Xpath){
		char curChar;
		int count = 0;
		String pred = "";
		for(int i=Xpath.indexOf('[')+1; i<Xpath.length(); i++){
			curChar = Xpath.charAt(i);
			pred += curChar;
			if(curChar == '['){
				count++;
			}
			if(curChar == ']'){
				count--;
				if(count == 0){
					if(!EvaluateTest(pred.substring(0,pred.length()-2))){
						return false;
					}
					else break;
				}

			}
		}
		return true;
	}

	private boolean EvaluateStep(String Xpath){
		// grammar for step:  nodename ([ test ])* (axis step)?

		// First check if Xpath starts with a nodename
		if(Xpath.matches("\\s*\\w+\\s*.*")){

			// check for predicates 
			if(Xpath.contains("[")){
				if(!EvaluatePredicate(Xpath)) return false;
			}

			// Check for axis-step
			if(Xpath.contains("/")){
				if(Xpath.indexOf('/') != Xpath.length()-1){
					if(!EvaluateStep(Xpath.substring(Xpath.indexOf('/')+1))) return false;
				}
			}

			return true;
		}
		return false;
	}

	public boolean isValid(int i) {
		// Current XPath
		String Xpath = xpth[i];

		// First char MUST be a '/'
		if(!Xpath.matches("\\s*[/].*")) return false;

		// After axis is a step
		if(!EvaluateStep(Xpath.substring(Xpath.indexOf('/')+1))) return false;

		return true;
	}

	public XPathNode ParseXPath(int QueryID){
		String Xpath = xpth[QueryID];
		Stack<XPathNode> stk = new Stack<XPathNode>();

		XPathNode curNode = null;
		XPathNode prevNode = null;
		XPathNode firstNode = null;

		boolean first = true;
		String curNodeName = "";
		char curChar;
		int nodeInd = 0;
		int ind = 1; 

		// traverse XPath char by char, finding nodes
		while(ind < Xpath.length()){
			curChar = Xpath.charAt(ind);

			if(curChar == '['){
				if(!curNodeName.trim().isEmpty()){
					curNode = new XPathNode(QueryID, nodeInd, curNodeName.trim());
					if(first){
						firstNode = curNode;
						first = false;
					}
					else{
						prevNode.updateNextNodePointer(curNode);					
					}
					prevNode = curNode;
				}
				ind++;

				// Handle attribute filter
				if(Xpath.substring(ind, Xpath.length()).matches("\\s*@\\w+\\s*=\\s*\"\\s*.*\\s*\".*")){

					// let us see how we can split this using a re parser
					Pattern attrPattern = Pattern.compile("\\s*@(\\w+)\\s*=\\s*\"(.*?)\"(.*)");
					Matcher m = attrPattern.matcher(Xpath.substring(ind, Xpath.length()));
					if(m.matches()){
						//System.out.println("match count " + m.groupCount());
						String attrname = m.group(1);
						String attrvalue = m.group(2);

						ind += m.start(3);
						curNode.att.put(attrname, attrvalue);
					}
				}

				// Handle text() filter
				if(Xpath.substring(ind, Xpath.length()).matches("\\s*text[(][)]\\s*=\\s*\"\\s*.*\\s*\".*")){

					// let us see how we can split this using a re parser
					Pattern attrPattern = Pattern.compile("\\s*text[(][)]\\s*=\\s*\"(.*?)\"(.*)");
					Matcher m = attrPattern.matcher(Xpath.substring(ind, Xpath.length()));
					if(m.matches()){
						String textField = m.group(1);
						ind += m.start(2);
						curNode.textField = textField;
					}
				}

				// Handle contains() filter
				if(Xpath.substring(ind, Xpath.length()).matches("\\s*contains[(]\\s*text[(][)]\\s*[,]\\s*\"\\s*.*\\s*\"\\s*[)].*")){

					// let us see how we can split this using a re parser
					Pattern attrPattern = Pattern.compile("\\s*contains[(]\\s*text[(][)]\\s*[,]\\s*\"(.*?)\"\\s*[)](.*)");
					Matcher m = attrPattern.matcher(Xpath.substring(ind, Xpath.length()));
					if(m.matches()){
						String containsField = m.group(1);
						ind += m.start(2);
						curNode.containsField.add(containsField);
					}
				}

				stk.push(curNode);
				nodeInd++;
				curNodeName = "";
				continue;
			}

			// end of a predicate
			if(curChar == ']'){
				if(!curNodeName.trim().isEmpty()){
					curNode = new XPathNode(QueryID, nodeInd, curNodeName.trim());
					prevNode.updateNextNodePointer(curNode);	
				}
				curNode = (XPathNode)stk.pop();
				nodeInd = curNode.level;
				curNodeName = "";
				ind++;
				continue;
			}

			if(curChar == '/'){	
				if(!curNodeName.trim().isEmpty()){
					curNode = new XPathNode(QueryID, nodeInd, curNodeName.trim());
					if(first){
						firstNode = curNode;
						first = false;
					}
					else{
						prevNode.updateNextNodePointer(curNode);					
					}
					prevNode = curNode;
				}
				nodeInd++;
				curNodeName = "";
				ind++;
				continue;
			}

			curNodeName += curChar;
			ind++;
		}

		if(!curNodeName.trim().isEmpty()){
			curNode = new XPathNode(QueryID, nodeInd, curNodeName.trim());
			prevNode.updateNextNodePointer(curNode);
		}

		return firstNode;
	}

	public boolean[] evaluate(Document d) { 
		// XPath parsing
		ArrayList<XPathNode> head = new ArrayList<XPathNode>();
		if(xpth.length > 0){
			for(int i=0; i<xpth.length; i++){
				head.add(ParseXPath(i));
			}
		}

		// handler for SAX parsing
		Handler h = new Handler(head);

		try{
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(XmlUri, h);
		}
		catch(Exception e){
			return h.queryMatches;
		}

		// AND operation on the 2 boolean arrays to make sure both the level and attr tests were passed during matching
		for(int i=0; i<h.queryFails.length; i++){
			h.queryMatches[i] = h.queryMatches[i] && h.queryFails[i];
		}

		// Return result!
		return h.queryMatches;
	}
}
