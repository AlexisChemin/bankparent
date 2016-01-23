package com.bankparent.model;

import org.codehaus.jackson.annotate.JsonIgnore;


public class AccountExtended extends Account {

	@JsonIgnore
	protected long rowid;

	@JsonIgnore
	protected String image;
	
	public String getImage() {
		return image;
	}
	
	
	public void setImage(String image) {
		this.image = image;
	}
	
	public long getRowid() {
		return rowid;
	}
	
	
	public void setRowid(long rowid) {
		this.rowid = rowid;
	}
	
	
}
