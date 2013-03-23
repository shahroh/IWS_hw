package test.edu.upenn.cis455;
import edu.upenn.cis455.xpathengine.*;
import edu.upenn.cis455.xpathengine.XPathEngineImpl;
import junit.framework.TestCase;

public class TestEvaluate extends TestCase {

	// Instantiate object of type XPathEngineImpl
	XPathEngineImpl eng = new XPathEngineImpl();

	public void testXPathNodeCreation(){
		// Create nodes, link them, and test the functionality and structure
		XPathNode node1 = new XPathNode(0, 0, "node1");
		XPathNode node2 = new XPathNode(0, 1, "node2");
		XPathNode node3 = new XPathNode(2, 1, "node3");
		XPathNode node4 = new XPathNode(2, 0, "node4");

		// Link the nodes 
		node1.updateNextNodePointer(node2);
		node3.updateNextNodePointer(node4);

		// Test whether the link has been made
		assertTrue("Testing if node1 points to node2", node1.nextNodePointer.get(0).nodeName.equals(node2.nodeName));
		assertTrue("Testing if node3 points to node4", node3.nextNodePointer.get(0).nodeName.equals(node4.nodeName));

	}

	public void testXPathParsingWithNodeCreation(){
		String[] xpth = new String[10];

		// Initialize XPaths 
		xpth[0] = "/web-app/b";
		//xpth[1] = "/web-app/b";
		xpth[1] = "/foo/bar/";
		xpth[2] = "/xyz/abc[contains(text(),\"someSubstring\")]";
		xpth[3] = "/a/b/c[text()=\"theEntireText\"]";
		xpth[4] = "/blah[anotherElement]";

		eng.setXPaths(xpth);

		// Parse the XPath
		XPathNode headNode1 = eng.ParseXPath(0);
		XPathNode headNode2 = eng.ParseXPath(1);
		XPathNode headNode3 = eng.ParseXPath(2);
		XPathNode headNode4 = eng.ParseXPath(3);
		XPathNode headNode5 = eng.ParseXPath(4);

		assertTrue("Testing the parsing of the XPaths function", "web-app".equals(headNode1.nodeName));
		assertTrue("Testing the parsing of the XPaths function", "bar".equals(headNode2.nextNodePointer.get(0).nodeName));
		assertTrue("Testing the parsing of the XPaths function", "xyz".equals(headNode3.nodeName));
		assertTrue("Testing the parsing of the XPaths function", "a".equals(headNode4.nodeName));
		assertTrue("Testing the parsing of the XPaths function", "blah".equals(headNode5.nodeName));

	}

	public void testXFilterEngine(){
		String[] xpth = new String[2];

		// Initialize XPaths 
		xpth[0] = "/web-app[context-param[param-name]]";
		xpth[1] = "/web-app/b";

		eng.setXPaths(xpth);
		boolean[] expected_result = new boolean[2];
		expected_result[0] = true;
		expected_result[1] = false;
		
		boolean[] res = eng.evaluate(null);
		assertTrue("Check the evaluation of 2 valid XPaths", res[0] == expected_result[0]);
		assertTrue("Check the evaluation of 2 valid XPaths", res[1] == expected_result[1]);

	}
}


