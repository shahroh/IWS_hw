package edu.upenn.cis455.storage;

import java.io.File;
import java.util.Date;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.Transaction;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

public class BerkDBWrapper {

	static BerkDBWrapper bdb;

	// Initialization
	public static PrimaryIndex<String, UrlStorageObject> UrlInd;
	private static String myEnvPath = "./";
	private static String storeName = "exampleStore";

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

		// Primary indices
		UrlInd = myStore.getPrimaryIndex(String.class, UrlStorageObject.class);
		
		openEnv();
	}

	// Function to instantiate singleton class
	public static BerkDBWrapper GetSingleton(String envPath, String NameOfStore){
		if(bdb == null){
			bdb = new BerkDBWrapper(envPath, NameOfStore);
		}
		return bdb;
	}

	// URL to document content
	public void UrlToDoc(String docUrl, String docContent, Date lastModDate){
		Transaction trans = myEnv.beginTransaction(null, null);
		UrlStorageObject obj = new UrlStorageObject();
		
		obj.SetDocUrl(docUrl);
		obj.SetDocContent(docContent);
		obj.SetLastModifiedDate(lastModDate);
		 
		UrlInd.put(trans,obj);
		trans.commit();
	}

}
