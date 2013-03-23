package edu.upenn.cis455.hw1;
import java.io.IOException;

import java.io.*;
import java.util.*;
import java.io.PrintWriter;
import java.util.Locale;
import java.lang.*;
import javax.servlet.http.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class FakeResponse implements HttpServletResponse {

	private boolean commit = false;
	private String m_contentType = null;
	private int m_contentLength = 0;
	private int m_bufferSize = 0;

	private HttpSession ses;
	private StringWriter buf;
	private ArrayList<Cookie> cookieList;
	private int currentStatus = 200;
	private String curCharEnc = "UTF-8";
	private OutputStream out = null;
	
	private FakeRequest req;

	// HashMap to hold headers and a map to their values
	HashMap<String, String> responseHeaders;

	// This is an arraylist that holds the character encodings that are legal
	private static ArrayList<String>  charEnc = new ArrayList<String>();

	public FakeResponse(){
		InitCharEnc();
		buf = new StringWriter();
		this.cookieList = new ArrayList<Cookie>();
		this.responseHeaders = new HashMap<String, String>();
	}

	public void setOutputStream(OutputStream out){
		this.out = out;
	}

	public void setFakeRequestInstance(FakeRequest req){
		this.req = req;
	}
	
	public void addCookie(Cookie arg0) {
		// If cookie already present, replace it with the latest one
		for(int i=0; i<this.cookieList.size(); i++){
			if(this.cookieList.get(i).getName().equalsIgnoreCase(arg0.getName())){
				cookieList.remove(i);
			}
		}
		cookieList.add(arg0);
	}

	// This method is called just before flushing buffer, for Set-Cookie header
	public String[] makeCookieHeader(){
		if(this.cookieList.size() != 0){
			ArrayList<String> outArray = new ArrayList<String>();
			for(int i=0; i<this.cookieList.size(); i++){
				String out = "Set-Cookie: ";
				Cookie thisCookie = this.cookieList.get(i);
				out += thisCookie.getName()+"="+thisCookie.getValue();
				if(thisCookie.getMaxAge() != -1) {
					out += "; Max-Age="+ (thisCookie.getMaxAge());
				}
				if(thisCookie.getSecure() != false) out += "; Secure";
				if(thisCookie.getDomain() != null) out += "; Domain="+thisCookie.getDomain();
				if(thisCookie.getPath() != null) out += "; Path="+thisCookie.getPath();
				out += "; HttpOnly";
				outArray.add(out);
			}
			return outArray.toArray(new String[outArray.size()]);
		}
		else
			return null;
	}

	public boolean containsHeader(String arg0) {
		return this.responseHeaders.containsKey(arg0);
	}

	// TO DO (need more info on session)
	public String encodeURL(String arg0) {
		return arg0;
	}

	// TO DO (need more info on session)
	public String encodeRedirectURL(String arg0) {
		return arg0;
	}

	// Deprecated
	public String encodeUrl(String arg0) {
		return null;
	}

	// Deprecated
	public String encodeRedirectUrl(String arg0) {
		return null;
	}

	public void sendError(int arg0, String arg1) throws IOException {

	}

	// TO DO
	public void sendError(int arg0) throws IOException {

	}

	// TO DO
	public void sendRedirect(String arg0) throws IOException {
		System.out.println("[DEBUG] redirect to " + arg0 + " requested");
		System.out.println("[DEBUG] stack trace: ");
		Exception e = new Exception();
		StackTraceElement[] frames = e.getStackTrace();
		for (int i = 0; i < frames.length; i++) {
			System.out.print("[DEBUG]   ");
		}
	}

	// Assumption: Difference between this and addHeader is only that here we replace value if 
	// header already exists, and in the case of addHeader we have multiple values for same header
	// Also assuming that I don't have to specifically "remove" elements in case value already exists for
	// that header
	public void setDateHeader(String arg0, long arg1) {
		this.responseHeaders.put(arg0, String.valueOf(arg1));
	}

	// Assumption: for multiple date values corresponding to the same date, there is a blank space between 
	// consecutive dates
	public void addDateHeader(String arg0, long arg1) {
		if(this.responseHeaders.containsKey(arg0)){
			this.responseHeaders.put(arg0, this.responseHeaders.get(arg0)+" "+String.valueOf(arg1));
		}
		else{
			this.responseHeaders.put(arg0, String.valueOf(arg1));
		}
	}

	// Assumption: Difference between this and addHeader is only that here we replace value if 
	// header already exists, and in the case of addHeader we have multiple values for same header
	// Also assuming that I don't have to specifically "remove" elements in case value already exists for
	// that header
	public void setHeader(String arg0, String arg1) {
		this.responseHeaders.put(arg0, arg1);
	}

	// Assumption: for multiple date values corresponding to the same date, there is a blank space between 
	// consecutive dates
	public void addHeader(String arg0, String arg1) {
		if(this.responseHeaders.containsKey(arg0)){
			this.responseHeaders.put(arg0, this.responseHeaders.get(arg0)+" "+arg1);
		}
		else{
			this.responseHeaders.put(arg0, arg1);
		}
	}

	// Assumption: Difference between this and addHeader is only that here we replace value if 
	// header already exists, and in the case of addHeader we have multiple values for same header
	// Also assuming that I don't have to specifically "remove" elements in case value already exists for
	// that header
	public void setIntHeader(String arg0, int arg1) {
		this.responseHeaders.put(arg0, String.valueOf(arg1));
	}

	// Assumption: for multiple date values corresponding to the same date, there is a blank space between 
	// consecutive dates
	public void addIntHeader(String arg0, int arg1) {
		if(this.responseHeaders.containsKey(arg0)){
			this.responseHeaders.put(arg0, this.responseHeaders.get(arg0)+" "+String.valueOf(arg1));
		}
		else{
			this.responseHeaders.put(arg0, String.valueOf(arg1));
		}
	}

	public void setStatus(int arg0) {
		this.currentStatus = (arg0<600 && arg0>99)?arg0:200;
	}

	// Deprecated
	public void setStatus(int arg0, String arg1) {

	}

	public String getCharacterEncoding() {
		return "ISO-8859-1";
	}

	public String getContentType() {
		return this.m_contentType==null?"text/html":this.m_contentType;
	}

	public ServletOutputStream getOutputStream() throws IOException {
		return null;
	}

	public PrintWriter getWriter() throws IOException {
		return new PrintWriter(buf, false);
	}

	public void setCharacterEncoding(String arg0) {
		if(charEnc.contains(arg0))
			this.curCharEnc = arg0;
	}

	private void InitCharEnc(){
		charEnc.add("ISO-8859-1");
		charEnc.add("US-ASCII");
		charEnc.add("UTF-8");
		charEnc.add("UTF-16");
	}

	public void setContentLength(int arg0) {
		this.m_contentLength = arg0;
	}

	public void setContentType(String arg0) {
		this.m_contentType = arg0; 
	}

	public void setBufferSize(int arg0) {
		this.m_bufferSize = arg0;
	}

	public int getBufferSize() {
		return this.m_bufferSize;
	}

	// Takes the header data structure and generates string for the output stream
	public String generateHeaderString(){
		String out = "";
		Set set = this.responseHeaders.keySet();
		Iterator iter = set.iterator();
		while(iter.hasNext()){
			String s = iter.next().toString();
			if(s != "cookie"){
				out += "\r\n"+s+": "+this.responseHeaders.get(s);
			}
		}

		String[] listOfCookies = makeCookieHeader();
		if(listOfCookies != null){
			for(int j=0; j<listOfCookies.length; j++){
				out += "\r\n"+listOfCookies[j];
			}
		
		}
		return out;
	}

	public String generateFirstLine(){
		return "HTTP/1.1 "+Integer.toString(this.currentStatus);
	}

	// Create the extra cookie to maintain the session
	public void createSessionCookie(HttpSession ses){
		Cookie sesCookie = new Cookie("IWSsession", String.valueOf(ses.getId()));
		this.cookieList.add(sesCookie);
	}
	
	// Flush the buffer
	public void flushBuffer() throws IOException {
		if(out != null){
			// Is there a session in place?
			ses = req.getSession(false);
			if(ses != null){
				createSessionCookie(ses);
			}
			
			out.write(generateFirstLine().getBytes(this.curCharEnc));
			out.write(generateHeaderString().getBytes(this.curCharEnc));
			out.write(("\r\n\r\n"+buf.toString()).getBytes(this.curCharEnc));
		}
		out.close();
	}

	// Reset buffer by invoking new instance of StringWriter
	public void resetBuffer() {
		buf = new StringWriter();
	}

	// Check if it is committed
	public boolean isCommitted() {
		return commit;
	}

	// Resets buffer, status, and headers
	public void reset() throws IllegalStateException {
		resetBuffer();
		this.currentStatus = 200;
		this.responseHeaders = new HashMap<String, String>();
	}

	// No need to implement locales in any direction
	public void setLocale(Locale arg0) {
	}

	public Locale getLocale() {
		return null;
	}

}
