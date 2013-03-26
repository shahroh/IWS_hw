package edu.upenn.cis455.storage;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.Transaction;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.StoreConfig;

public class BerkDBWrapper {

	static BerkDBWrapper bdb;

	// Initialization
	public static PrimaryIndex<String, UrlStorageObject> UrlInd;
	public static PrimaryIndex<String, XPathEntity> XPathInd;
	public static SecondaryIndex<String, String, XPathEntity> XPathToUrlInd;
	public static PrimaryIndex<String, ChannelEntity> ChannelInd;
	public static SecondaryIndex<String, String, ChannelEntity> ChannelToXPathInd;

	private String myEnvPath;
	private String storeName;

	// Handles
	public EntityStore myStore = null;
	public Environment myEnv = null;

	private void openEnv() throws DatabaseException {
		// Set up the environment.
		EnvironmentConfig myEnvConfig = new EnvironmentConfig();
		myEnvConfig.setAllowCreate(true);
		myEnvConfig.setTransactional(true);
		//  Environment handles are free-threaded by default in JE,
		// so we do not have to do anything to cause the
		// environment handle to be free-threaded.

		// Set up the entity store
		StoreConfig myStoreConfig = new StoreConfig();
		myStoreConfig.setAllowCreate(true);
		myStoreConfig.setTransactional(true);

		// Open the environment 
		myEnv = new Environment(new File(myEnvPath), myEnvConfig);

		// Open the store
		System.out.println("entityStore!!");
		myStore = new EntityStore(myEnv, storeName, myStoreConfig);

	}

	private void closeEnv() {
		System.out.println("Closing env and store");
		if (myStore != null ) {
			try {
				myStore.close();
			} catch (DatabaseException e) {
				System.err.println("closeEnv: myStore: " + 
						e.toString());
				e.printStackTrace();
			}
		}

		if (myEnv != null ) {
			try {
				myEnv.close();
			} catch (DatabaseException e) {
				System.err.println("closeEnv: " + e.toString());
				e.printStackTrace();
			}
		}
	}

	private BerkDBWrapper(String envPath, String NameOfStore) {
		myEnvPath = envPath;
		storeName = NameOfStore;

		openEnv();

		// Primary indices
		UrlInd = myStore.getPrimaryIndex(String.class, UrlStorageObject.class);
		XPathInd = myStore.getPrimaryIndex(String.class, XPathEntity.class);

	}

	// Function to instantiate singleton class
	public static BerkDBWrapper GetSingleton(String envPath, String NameOfStore){
		if(bdb == null){
			bdb = new BerkDBWrapper(envPath, NameOfStore);
		}
		return bdb;
	}

	// Function to instantiate singleton class without args
	public static BerkDBWrapper GetSingleton(){
		return bdb;
	}

	// URL to document content
	public void UrlToDoc(String docUrl, String docContent, Date lastModDate, String contentType){
		Transaction trans = myEnv.beginTransaction(null, null);
		UrlStorageObject obj = null;
		if(!UrlInd.contains(docUrl)){
			obj = new UrlStorageObject();
			obj.SetDocUrl(docUrl);
		}
		else{
			obj = UrlInd.get(docUrl);
		}
		obj.SetDocContent(docContent);
		obj.SetLastModifiedDate(lastModDate);
		obj.SetContentType(contentType);

		UrlInd.put(trans,obj);
		trans.commit();
	}

	public boolean compareDate(URL targetUrl, Date dateLastMod){
		Transaction trans = myEnv.beginTransaction(null, null);
		if(UrlInd.contains(targetUrl.toString())){
			UrlStorageObject obj = UrlInd.get(targetUrl.toString());
			Date lastTouched = obj.GetLastModifiedDate();

			// compare with the present date
			if(dateLastMod.after(lastTouched)){
				return true;
			}
			return false;

		}
		else{
			return true;
		}

	}

	// XPath to URL
	public void XPathToUrl(String XPath, URL newUrl){
		Transaction trans = myEnv.beginTransaction(null, null);
		XPathEntity obj = null;
		if(!UrlInd.contains(XPath)){
			obj = new XPathEntity(XPath);
		}
		else{
			obj = XPathInd.get(XPath);
		}

		obj.SetUrl(newUrl.toString());

		XPathInd.put(trans,obj);
		trans.commit();
	}

	// Channel to XPath
	public void ChannelToXPath(int channelID, String XPath){
		Transaction trans = myEnv.beginTransaction(null, null);
		ChannelEntity obj = null;
		if(!ChannelInd.contains(Integer.toString(channelID))){
			obj = new ChannelEntity(Integer.toString(channelID));
		}
		else{
			obj = ChannelInd.get(Integer.toString(channelID));
		}

		obj.SetXPath(XPath);

		ChannelInd.put(trans,obj);
		trans.commit();
	}

	// Get all DocUrls which are xml files
	public ArrayList<UrlStorageObject> GetAllXmlDocs(){
		Transaction trans = myEnv.beginTransaction(null, null);
		UrlStorageObject obj = null;

		EntityCursor<UrlStorageObject> cursor = UrlInd.entities();
		ArrayList<UrlStorageObject> xmlDocs = new ArrayList<UrlStorageObject>();

		for(UrlStorageObject o : cursor){
			if(!o.GetContentType().equals("text/html")){
				xmlDocs.add(o);
			}
		}
		return xmlDocs;	
	}
}
