package edu.upenn.cis455.storage;

public class BerkDBWrapper {

	static BerkDBWrapper bdb;
	
	private BerkDBWrapper() {
		
	}

	// Function to instantiate singleton class
	public static BerkDBWrapper GetSingleton(){
		if(bdb == null){
			bdb = new BerkDBWrapper();
		}
		return bdb;
	}
	
	public void UrlToDoc(){
		
	}
	
}
