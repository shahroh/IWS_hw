package edu.upenn.cis455.storage;

import java.util.HashSet;
import java.util.Set;

import com.sleepycat.persist.model.DeleteAction;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;
import com.sleepycat.persist.model.Relationship.*;
import com.sleepycat.persist.model.*;
import com.sleepycat.persist.model.Relationship;
import static com.sleepycat.persist.model.DeleteAction.NULLIFY;
import static com.sleepycat.persist.model.Relationship.ONE_TO_ONE;
import static com.sleepycat.persist.model.Relationship.ONE_TO_MANY;
import static com.sleepycat.persist.model.Relationship.MANY_TO_ONE;
import static com.sleepycat.persist.model.Relationship.MANY_TO_MANY;

@Entity
public class XPathEntity {
	
	@PrimaryKey
	private String XPath;

	@SecondaryKey(relate=MANY_TO_MANY, relatedEntity = UrlStorageObject.class,
			onRelatedEntityDelete=NULLIFY)
	
	Set<String> matched_urls = new HashSet<String>();
	
	public XPathEntity(String str) {
		XPath = str;
	}
	
	public String GetXPath(){
		return XPath;
	}
	
	public void SetUrl(String newUrl){
		matched_urls.add(newUrl);
	}
	
}


