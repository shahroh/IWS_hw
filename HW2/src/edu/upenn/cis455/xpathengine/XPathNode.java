package edu.upenn.cis455.xpathengine;
import java.util.ArrayList;
import java.util.HashMap;

public class XPathNode {

	public int QeuryID;
	public int level;
	public String nodeName;
	public HashMap<String, String> att;
	public ArrayList<String> containsField;
	public String textField;
	
	public ArrayList<XPathNode> nextNodePointer;
	
	public void updateNextNodePointer(XPathNode nextNode){
		nextNodePointer.add(nextNode);
	}
	
	public XPathNode(int QueryID, int level, String nodeName) {
		this.QeuryID = QueryID;
		this.level = level;
		this.nodeName = nodeName;
		nextNodePointer = new ArrayList<XPathNode>();
		att = new HashMap<String, String>();
		containsField = new ArrayList<String>();
		
	}
}
