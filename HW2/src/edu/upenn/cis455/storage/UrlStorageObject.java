package edu.upenn.cis455.storage;

import java.util.Date;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class UrlStorageObject {
	@PrimaryKey
	private String docUrl;
	
	private String docContent;
	private Date lastModifiedDate;
	private String contentType;
	
	public UrlStorageObject() {}

	public String GetDocUrl(){
		return docUrl;
	}
	
	public String GetDocContent(){
		return docContent;
	}
	
	public Date GetLastModifiedDate(){
		return lastModifiedDate;
	}

	public String GetContentType(){
		return contentType;
	}
	
	public void SetDocUrl(String newDocUrl){
		docUrl = newDocUrl;
	}
	
	public void SetDocContent(String newDocContent){
		docContent = newDocContent;
	}
	
	public void SetLastModifiedDate(Date lastModNew){
		lastModifiedDate = lastModNew;
	}
	
	public void SetContentType(String newContentType){
		contentType = newContentType;
	}
}
