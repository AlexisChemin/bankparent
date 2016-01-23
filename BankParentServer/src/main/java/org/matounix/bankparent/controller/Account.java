package org.matounix.bankparent.controller;


public class Account {


	protected String id;
	protected String name;
	protected Integer version = 0;
	protected Double sum;
	

	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getSum() {
		return sum;
	}

	public void setSum(Double sum) {
		this.sum = sum;
	}
	

	public void increaseSum(Double toAdd) {
		this.sum += toAdd;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		if (version!=null) {
			this.version = version;
		}
	}
	
	public synchronized Integer increaseVersion() {
		version += 1;
		return version;
	}

	
	
	
	
}
