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
import static com.sleepycat.persist.model.DeleteAction.NULLIFY;
import static com.sleepycat.persist.model.Relationship.ONE_TO_ONE;
import static com.sleepycat.persist.model.Relationship.ONE_TO_MANY;
import static com.sleepycat.persist.model.Relationship.MANY_TO_ONE;
import static com.sleepycat.persist.model.Relationship.MANY_TO_MANY;

@Entity
public class ChannelEntity {
	
	@PrimaryKey
	private String channelID;

	@SecondaryKey(relate=MANY_TO_MANY, relatedEntity = XPathEntity.class,
			onRelatedEntityDelete=NULLIFY)
	
	Set<String> matched_xpaths = new HashSet<String>();
	
	public ChannelEntity(String ID) {
		channelID = ID;
	}
	
	public String GetChannelID(){
		return channelID;
	}
	
	public void SetXPath(String newXPath){
		matched_xpaths.add(newXPath);
	}
	
}


