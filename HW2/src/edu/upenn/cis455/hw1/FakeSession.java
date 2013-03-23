package edu.upenn.cis455.hw1;
import java.sql.Date;
import java.util.*;
import java.util.Enumeration;
import java.util.Properties;
import java.text.*;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

class FakeSession implements HttpSession {

	private Properties m_props = new Properties();
	private boolean m_valid = true;
	private int max_time_int = 0;

	private static int newID = 0;
	private int myID;
	private DateFormat dateFormat = new SimpleDateFormat("yyyy/mm/dd HH:mm:ss");
	private Calendar cal; 
	private long creationTime;
	public synchronized int generateNewID(){
		return (++newID);
	}

	public FakeSession(){
		super();
		myID = generateNewID();
		ThreadPool.insertSessionInstance(String.valueOf(myID), this);

		// to get current time 
		cal = Calendar.getInstance();
		this.creationTime = Long.valueOf(dateFormat.format(cal.getTime()));
	}

	public long getCreationTime() {
		return creationTime;
	}

	public String getId() {
		return String.valueOf(myID);
	}

	public long getLastAccessedTime() {
		return 0;
	}

	public ServletContext getServletContext() {
		return null;
	}

	public void setMaxInactiveInterval(int arg0) {
		this.max_time_int = arg0;
	}

	public int getMaxInactiveInterval() {
		return this.max_time_int>=0?this.max_time_int:-1;
	}

	public HttpSessionContext getSessionContext() {
		return null;
	}

	public Object getAttribute(String arg0) {
		return m_props.get(arg0);
	}

	public Object getValue(String arg0) {
		return m_props.get(arg0);
	}

	public Enumeration getAttributeNames() {
		return m_props.keys();
	}

	// Deprecated
	public String[] getValueNames() {
		return null;
	}

	public void setAttribute(String arg0, Object arg1) {
		m_props.put(arg0, arg1);
	}

	public void putValue(String arg0, Object arg1) {
		m_props.put(arg0, arg1);
	}

	public void removeAttribute(String arg0) {
		m_props.remove(arg0);
	}

	public void removeValue(String arg0) {
		m_props.remove(arg0);
	}

	public void invalidate() {
		m_valid = false;
	}

	public boolean isNew() {
		return false;
	}

	boolean isValid() {
		return m_valid;
	}
}
