package test.edu.upenn.cis455;
import edu.upenn.cis455.xpathengine.*;

import junit.framework.TestCase;

public class TestIsValid extends TestCase {

	
	public void testSimpleString(){

		String[] Xpaths = new String[20];

		// test strings
		Xpaths[0] = "/web-app/b";
		Xpaths[1] = "/foo/bar[@att=\"123\"]";
		Xpaths[2] = "/xyz/abc[contains(text(),\"someSubstring\")]" ;
		Xpaths[3] = "/a/b/c[text()=\"theEntireText\"]";
		Xpaths[4] = "/blah[anotherElement]";
		Xpaths[5] = "/this/that[something/else]" ;
		Xpaths[6] = "/d/e/f[foo[text()=\"something\"]][bar] ";
		Xpaths[7] = "/a/b/c[text() =   \"whiteSpacesShouldNotMatter\"]";

		// Instatiate XPathEngineImpl
		XPathEngineImpl eng = new XPathEngineImpl();

		eng.setXPaths(Xpaths);

		// run isValid, answer should be true
		assertTrue("simple string", eng.isValid(0));
	}
	

	public void testString2(){

		String[] Xpaths = new String[20];

		// test strings
		Xpaths[0] = "/foo/bar/xyz";
		Xpaths[1] = "/foo/bar[@att=\"123\"]";
		Xpaths[2] = "/xyz/abc[contains(text(),\"someSubstring\")]" ;
		Xpaths[3] = "/a/b/c[text()=\"theEntireText\"]";
		Xpaths[4] = "/blah[anotherElement]";
		Xpaths[5] = "/this/that[something/else]" ;
		Xpaths[6] = "/d/e/f[foo[text()=\"something\"]][bar] ";
		Xpaths[7] = "/a/b/c[text() =   \"whiteSpacesShouldNotMatter\"]";

		// Instatiate XPathEngineImpl
		XPathEngineImpl eng = new XPathEngineImpl();

		eng.setXPaths(Xpaths);

		// run isValid, answer should be true
		assertTrue("simple string", eng.isValid(1));
	}
	
	public void testString3(){

		String[] Xpaths = new String[20];

		// test strings
		Xpaths[0] = "/foo/bar/xyz";
		Xpaths[1] = "/foo/bar[@att=\"123\"]";
		Xpaths[2] = "/xyz/abc[contains(text(),\"someSubstring\")]" ;
		Xpaths[3] = "/a/b/c[text()=\"theEntireText\"]";
		Xpaths[4] = "/blah[anotherElement]";
		Xpaths[5] = "/this/that[something/else]" ;
		Xpaths[6] = "/d/e/f[foo[text()=\"something\"]][bar] ";
		Xpaths[7] = "/a/b/c[text() =   \"whiteSpacesShouldNotMatter\"]";

		// Instatiate XPathEngineImpl
		XPathEngineImpl eng = new XPathEngineImpl();

		eng.setXPaths(Xpaths);

		// run isValid, answer should be true
		assertTrue("simple string", eng.isValid(2));
	}
	
	public void testString4(){

		String[] Xpaths = new String[20];

		// test strings
		Xpaths[0] = "/foo/bar/xyz";
		Xpaths[1] = "/foo/bar[@att=\"123\"]";
		Xpaths[2] = "/xyz/abc[contains(text(),\"someSubstring\")]" ;
		Xpaths[3] = "/a/b/c[text()=\"theEntireText\"]";
		Xpaths[4] = "/blah[anotherElement]";
		Xpaths[5] = "/this/that[something/else]" ;
		Xpaths[6] = "/d/e/f[foo[text()=\"something\"]][bar] ";
		Xpaths[7] = "/a/b/c[text() =   \"whiteSpacesShouldNotMatter\"]";

		// Instatiate XPathEngineImpl
		XPathEngineImpl eng = new XPathEngineImpl();

		eng.setXPaths(Xpaths);

		// run isValid, answer should be true
		assertTrue("simple string", eng.isValid(3));
	}
	
	public void testString5(){

		String[] Xpaths = new String[20];

		// test strings
		Xpaths[0] = "/foo/bar/xyz";
		Xpaths[1] = "/foo/bar[@att=\"123\"]";
		Xpaths[2] = "/xyz/abc[contains(text(),\"someSubstring\")]" ;
		Xpaths[3] = "/a/b/c[text()=\"theEntireText\"]";
		Xpaths[4] = "/blah[anotherElement]";
		Xpaths[5] = "/this/that[something/else]" ;
		Xpaths[6] = "/d/e/f[foo[text()=\"something\"]][bar] ";
		Xpaths[7] = "/a/b/c[text() =   \"whiteSpacesShouldNotMatter\"]";

		// Instatiate XPathEngineImpl
		XPathEngineImpl eng = new XPathEngineImpl();

		eng.setXPaths(Xpaths);

		// run isValid, answer should be true
		assertTrue("simple string", eng.isValid(4));
	}
	
	public void testString6(){

		String[] Xpaths = new String[20];

		// test strings
		Xpaths[0] = "/foo/bar/xyz";
		Xpaths[1] = "/foo/bar[@att=\"123\"]";
		Xpaths[2] = "/xyz/abc[contains(text(),\"someSubstring\")]" ;
		Xpaths[3] = "/a/b/c[text()=\"theEntireText\"]";
		Xpaths[4] = "/blah[anotherElement]";
		Xpaths[5] = "/this/that[something/else]" ;
		Xpaths[6] = "/d/e/f[foo[text()=\"something\"]][bar] ";
		Xpaths[7] = "/a/b/c[text() =   \"whiteSpacesShouldNotMatter\"]";

		// Instatiate XPathEngineImpl
		XPathEngineImpl eng = new XPathEngineImpl();

		eng.setXPaths(Xpaths);

		// run isValid, answer should be true
		assertTrue("simple string 5", eng.isValid(5));
	}
	
	public void testString7(){

		String[] Xpaths = new String[20];

		// test strings
		Xpaths[0] = "/foo/bar/xyz";
		Xpaths[1] = "/foo/bar[@att=\"123\"]";
		Xpaths[2] = "/xyz/abc[contains(text(),\"someSubstring\")]" ;
		Xpaths[3] = "/a/b/c[text()=\"theEntireText\"]";
		Xpaths[4] = "/blah[anotherElement]";
		Xpaths[5] = "/this/that[something/else]" ;
		Xpaths[6] = "/d/e/f[foo[text()=\"something\"]][bar] ";
		Xpaths[7] = "/a/b/c[text() =   \"whiteSpacesShouldNotMatter\"]";

		// Instatiate XPathEngineImpl
		XPathEngineImpl eng = new XPathEngineImpl();

		eng.setXPaths(Xpaths);

		// run isValid, answer should be true
		assertTrue("simple string 6", eng.isValid(6));
	}
	
	public void testString8(){

		String[] Xpaths = new String[20];

		// test strings
		Xpaths[0] = "/foo/bar/xyz";
		Xpaths[1] = "/foo/bar[@att=\"123\"]";
		Xpaths[2] = "/xyz/abc[contains(text(),\"someSubstring\")]" ;
		Xpaths[3] = "/a/b/c[text()=\"theEntireText\"]";
		Xpaths[4] = "/blah[anotherElement]";
		Xpaths[5] = "/this/that[something/else]" ;
		Xpaths[6] = "/d/e/f[foo[text()=\"something\"]][bar] ";
		Xpaths[7] = "/a/b/c[text() =   \"whiteSpacesShouldNotMatter\"]";

		// Instatiate XPathEngineImpl
		XPathEngineImpl eng = new XPathEngineImpl();

		eng.setXPaths(Xpaths);

		// run isValid, answer should be true
		assertTrue("simple string 7", eng.isValid(7));
	}
	
}
