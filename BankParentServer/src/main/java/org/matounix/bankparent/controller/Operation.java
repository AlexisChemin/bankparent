package org.matounix.bankparent.controller;

import java.util.Date;
import java.util.UUID;

public class Operation {


	protected String id;
	protected String label;
	protected String nature;
	protected Integer version;
	protected Boolean canceled = false;
	protected Double amount;
	protected Long creationTimestamp;
	
	public static final String NATURE_ADDITION = "add";

	
	public Operation() {
		UUID uuid = UUID.randomUUID();
		id = uuid.toString();
	}

	
	
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getNature() {
		return nature;
	}

	public void setNature(String nature) {
		this.nature = nature;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	
	public Boolean getCanceled() {
		return canceled;
	}

	public void setCanceled(Boolean canceled) {
		this.canceled = canceled;
	}
	
	protected boolean isAddition() {
		return NATURE_ADDITION.equalsIgnoreCase(nature);
	}
	
	public Long getCreationTimestamp() {
		return creationTimestamp;
	}
	
	public void setCreationTimestamp(Long creationTimestamp) {
		this.creationTimestamp = creationTimestamp;
	}
	
	public String toString() {
		return (canceled?"-CANCELED- ":"") + label + " " + nature 
				+ " Q:" + amount + " V:" + version + " ID:" + id + " Creation:" + creationTimestamp;
	}
	
	
	
}
