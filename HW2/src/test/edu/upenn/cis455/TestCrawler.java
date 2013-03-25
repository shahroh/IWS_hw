package test.edu.upenn.cis455;
import edu.upenn.cis455.crawler.XPathCrawler;
import edu.upenn.cis455.xpathengine.*;

import edu.upenn.cis455.xpathengine.XPathEngineImpl;
import junit.framework.TestCase;

public class TestCrawler extends TestCase{

	public void testUrlExtraction(){
		try{
			XPathCrawler.InitializeCrawl("http://stackoverflow.com/", "/", 400);
		}
		catch(Exception e){

		}
	}

}
