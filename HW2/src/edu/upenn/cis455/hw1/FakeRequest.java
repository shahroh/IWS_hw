package edu.upenn.cis455.hw1;
import java.io.BufferedReader;
import java.io.*;
import java.lang.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.*;

class FakeRequest implements HttpServletRequest {

	private HashMap<String, ArrayList<String>> m_params = new HashMap<String, ArrayList<String>>();
	private Properties m_props = new Properties();
	private FakeSession m_session = null;
	private String m_method;
	private String m_queryString = null;
	private String curCharEnc = null;
	private String[] m_headerLine;

	// This is an arraylist that holds the character encodings that are legal
	private static ArrayList<String>  charEnc = new ArrayList<String>();

	// Hashmap to store incoming headers and their values
	HashMap<String, ArrayList<String>> requestHeaders;

	private Cookie[] cookieList;

	FakeRequest() {
		// Initialize the array list that holds the legal char encodings
		InitCharEnc();

		this.requestHeaders = new HashMap<String, ArrayList<String>>();
	}

	public void shareHeaderLines(String[] headerLine){
		m_headerLine = headerLine;
		HashTheHeaders();
		HashRequestParams();
		FindSessionsCookie();
		
	}

	private void HashRequestParams(){
		String paramName, paramVal;
		if(this.m_headerLine[0].split(" ")[1].contains("?")){
			String[] params = this.m_headerLine[0].split(" ")[1].replace("?", ",").split(",")[1].split("&");
			for(String param: params){
				paramName = param.split("=")[0];
				paramVal = param.split("=")[1];
				setParameter(paramName, paramVal);
			}
		}
	}

	private void HashPostBodyParams(String postBody){
		String[] params = postBody.split("&");
		System.out.println(postBody);
		if(params != null){
			String paramName, paramVal;
			for(String param : params){
				paramName = param.split("=")[0];
				paramVal = param.split("=")[1].replace("+", " ");
				setParameter(paramName, paramVal);
			}
		}
	}

	private void HashTheHeaders(){
		String headerName, headerVal;
		int i = 1;
		while(this.m_headerLine[i] != null){
			if(this.m_headerLine[i].contains(":")){
				//System.out.println(this.m_headerLine[i]);
				headerName = this.m_headerLine[i].split(":")[0].toLowerCase();
				headerVal = this.m_headerLine[i].split(":")[1];
				for(int j=2; j<this.m_headerLine[i].split(":").length; j++){
					headerVal += this.m_headerLine[i].split(":")[j];
				}

				ArrayList<String> tempList;

				// Hash the name,val pair up
				if(this.requestHeaders.containsKey(headerName)){
					tempList = this.requestHeaders.get(headerName);
					tempList.add(headerVal);
					this.requestHeaders.put(headerName, tempList);
				}
				else{
					tempList = new ArrayList<String>();
					tempList.add(headerVal);
					this.requestHeaders.put(headerName, tempList);
				}

			}
			i++;
		}
	}

	private void InitCharEnc(){
		charEnc.add("ISO-8859-1");
		charEnc.add("US-ASCII");
		charEnc.add("UTF-8");
		charEnc.add("UTF-16");
	}

	FakeRequest(FakeSession session) {
		m_session = session;

		// Initialize the array list that holds the legal char encodings
		InitCharEnc();

		this.requestHeaders = new HashMap<String, ArrayList<String>>();
	}

	public String getAuthType() {
		return BASIC_AUTH;
	}

	public String getUriFromHeaderLine(){
		return this.m_headerLine[0].trim().split(" ")[1];
	}

	// Assumption: the Cookie header is never passed more than once per request
	public Cookie[] getCookies() {
		if(this.requestHeaders.containsKey("cookie")){
			ArrayList<Cookie> cookieArray = new ArrayList<Cookie>();
			boolean endOfLine;
			for(int i=0; i<this.requestHeaders.get("cookie").size(); i++){
				String afterColon = this.requestHeaders.get("cookie").get(i).trim();
				String[] nameValPairs = afterColon.split(";");
				boolean addVer = false;
				String verNum = null;
				String attName;
				String attVal;
				int j = 0;
				if(nameValPairs[0].split("=")[0].trim().equals("$Version")){
					addVer = true;
					verNum = nameValPairs[0].split("=")[1].trim();
					j++;
				}
				while(j<nameValPairs.length){
					// Process the name-val pairs (and possible domain and path atts) for the cookie header
					attName = nameValPairs[j].split("=")[0].trim();
					attVal = nameValPairs[j].split("=")[1].trim();

					Cookie thisCookie = new Cookie(attName, attVal);
					if(addVer && verNum != null)
						thisCookie.setVersion(Integer.parseInt(verNum));
					if(j+1<nameValPairs.length){
						if(nameValPairs[j+1].split("=")[0].trim().equalsIgnoreCase("domain")){
							thisCookie.setDomain(nameValPairs[j+1].split("=")[1].trim());
							j += 2;
						}
					}
					if(j+2<nameValPairs.length){
						if(nameValPairs[j+2].split("=")[0].trim().equalsIgnoreCase("path")){
							thisCookie.setPath(nameValPairs[j+2].split("=")[1].trim());
							j += 3;
						}
					}
					else
						j += 1;
					cookieArray.add(thisCookie);
				}		
			}
			return cookieArray.toArray(new Cookie[cookieArray.size()]);
		}
		return null;
	}
	public void FindSessionsCookie(){
		Cookie[] cookies = getCookies();
		if(cookies != null){
			for(int i=0; i<cookies.length; i++){
				if(cookies[i].getName().equals("IWSsession") && ThreadPool.SessionExists(cookies[i].getValue())){
					m_session = ThreadPool.RetrieveSession(cookies[i].getValue());
					break;
				}
			}
		}
	}

	public long getDateHeader(String arg0) throws IllegalArgumentException {
		// first check if header of the specified name was even in the request
		if(!this.requestHeaders.containsKey(arg0.toLowerCase()))
			return -1;
		// else, convert the date to the long format
		else{
			Date myDate = new Date();
			DateFormat df = new SimpleDateFormat();
			try{
				myDate = df.parse(this.requestHeaders.get(arg0.toLowerCase()).get(0));
				return myDate.getTime();
			}
			catch(Exception e){
				System.out.println("Parse Exception, at getDateHeader in FakeRequest");
				return 0;
			}
		}
	}

	public String getHeader(String arg0) {
		if(this.requestHeaders.containsKey(arg0.toLowerCase())){
			return this.requestHeaders.get(arg0).get(0);
		}
		return null;
	}

	public boolean isRequestedSessionIdFromUrl(){
		return false;
	}
	
	public Enumeration<String> getHeaders(String arg0) {
		Vector<String> out = new Vector<String>();

		for(int i=0; i<this.requestHeaders.get(arg0).size(); i++){
			out.add(i,this.requestHeaders.get(arg0).get(i));
		}
		return out.elements();
	}

	public Enumeration<String> getHeaderNames() {
		return (new Vector<String>(this.requestHeaders.keySet())).elements();
	}

	public int getIntHeader(String arg0) throws NumberFormatException{
		return this.requestHeaders.containsKey(arg0.toLowerCase())?Integer.valueOf(this.requestHeaders.get(arg0).get(0)):-1;
	}

	public String getMethod() {
		return m_method;
	}

	public String getPathInfo() {
		return null;
	}

	public String getPathTranslated() {
		return null;
	}

	// Assumption: the context path is always the URI up to the last file since I am assuming that that will always
	// be the servlet name and the path up to that will always be the context path
	public String getContextPath() {
		String[] contextPath = getUriFromHeaderLine().trim().split("/");
		String out = "";
		for(int i=0; i<contextPath.length-2; i++){
			out += contextPath[i]+"/";
		}
		out += contextPath[contextPath.length-2];
		return out;
	}

	public void setQueryString(String queryString){
		this.m_queryString = queryString;
		return;
	}

	public String getQueryString() {
		return m_queryString;
	}

	public String getRemoteUser() {
		return null;
	}

	public boolean isUserInRole(String arg0) {
		return false;
	}

	public Principal getUserPrincipal() {
		return null;
	}

	public String getRequestedSessionId() {
		return null;
	}

	public String getRequestURI() {
		String[] out;
		String output = "";
		if(this.m_headerLine[0].split(" ")[1].contains("//")){
			out = this.m_headerLine[0].split(" ")[1].split("//")[1].split("/");
			for(int i=1; i<out.length-1; i++){
				output += out[i];
			}
			output += out[out.length-1].split("?|=| ")[0];
		}
		else{
			out = this.m_headerLine[0].split(" ")[1].split("/");
			for(int i=0; i<out.length-1; i++){
				output += out[i];
			}
			output += out[out.length-1].split("?|=| ")[0];
		}
		return output;
	}

	public StringBuffer getRequestURL() {
		return new StringBuffer(this.m_headerLine[0]);
	}

	public String getServletPath() {
		return null;
	}

	public HttpSession getSession(boolean arg0) {
		if (arg0) {
			if (! hasSession()) {
				m_session = new FakeSession();
			}
		} else {
			if (! hasSession()) {
				m_session = null;
			}
		}
		return m_session;
	}

	public HttpSession getSession() {
		return getSession(true);
	}

	public boolean isRequestedSessionIdValid() {
		return m_session.isValid();
	}

	// Assumption, since cookie is the only session mechanism Im implementing
	public boolean isRequestedSessionIdFromCookie() {
		return true;
	}

	// Assumption, since cookie is the only session mechanism Im implementing
	public boolean isRequestedSessionIdFromURL() {
		return false;
	}

	public Object getAttribute(String arg0) {
		return m_props.get(arg0);
	}

	public Enumeration getAttributeNames() {
		return m_props.keys();
	}

	public String getCharacterEncoding() {
		return this.curCharEnc==null?"ISO-8859-1":this.curCharEnc;
	}

	public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {
		this.curCharEnc = charEnc.contains(arg0)?arg0:null;
	}

	// Assuming its okay for content length to be null
	public int getContentLength() {
		return getMethod().equals("POST")?postBody.length():null;
	}

	public String getContentType() {
		// In case of GET 
		return null;
	}

	// Not required as per the hw doc
	public ServletInputStream getInputStream() throws IOException {
		return null;
	}

	public String getParameter(String arg0) {
		if(this.m_params.containsKey(arg0))
			return this.m_params.get(arg0).get(0);
		return null;
	}

	public Enumeration<String> getParameterNames() {
		Vector<String> v = new Vector<String>();
		for(String s: this.m_params.keySet()){
			v.add(s);
		}
		return v.elements();
	}

	public String[] getParameterValues(String arg0) {
		if(this.m_params.containsKey(arg0)){
			ArrayList<String> out = new ArrayList<String>();
			for(int i=0; i<this.m_params.size(); i++){
				out.add(i, this.m_params.get(arg0).get(i));
			}
			return out.toArray(new String[20]);
		}
		else{
			return null;
		}
	}

	public Map getParameterMap() {
		return this.m_params;
	}

	public String getProtocol() {
		return m_headerLine[0].split(" ")[2];
	}

	public String getScheme() {
		return "http";
	}

	public String getServerName() {
		return this.requestHeaders.get("host").get(0).split(":")[0];
	}

	public int getServerPort() {
		return Integer.parseInt(this.requestHeaders.get("host").get(0).split(":")[1]);
	}

	private String postBody = null;

	// Method to set the body of the request (postBody var)
	public void setRequestBody(String postBody){
		this.postBody = postBody;
		System.out.println("postBody:   ");
		System.out.println(postBody);
		this.HashPostBodyParams(postBody);
		
	}

	private String getRequestBody(){
		if(getMethod() == "POST"){
			return this.postBody;
		}
		return null;
	}

	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new StringReader(getRequestBody()));
	}

	public String getRemoteAddr() {
		return null;
	}

	public String getRemoteHost() {
		return null;
	}

	public void setAttribute(String arg0, Object arg1) {
		m_props.put(arg0, arg1);
	}

	public void removeAttribute(String arg0) {
		m_props.remove(arg0);
	}

	public Locale getLocale() {
		return null;
	}

	public Enumeration getLocales() {
		return null;
	}

	// my server is Unsecure Always
	public boolean isSecure() {
		return false;
	}

	public RequestDispatcher getRequestDispatcher(String arg0) {
		return null;
	}

	private String absPath;
	
	public void setAbsPath(String absPath){
		this.absPath = absPath;
	}
	
	public String getRealPath(String arg0) {
		return absPath+"/"+arg0;
	}

	public int getRemotePort() {
		return 0;
	}

	public String getLocalName() {
		return null;
	}

	public String getLocalAddr() {
		return null;
	}

	public int getLocalPort() {
		return 0;
	}

	void setMethod(String method) {
		this.m_method = method;
	}

	void setParameter(String key, String value) {
		ArrayList<String> temp = new ArrayList<String>();
		if(this.m_params.containsKey(key)){
			temp = this.m_params.get(key);
		}
		temp.add(value);
		this.m_params.put(key, temp);
	}

	void clearParameters() {
		m_params.clear();
	}

	boolean hasSession() {
		return ((m_session != null) && m_session.isValid());
	}
}
