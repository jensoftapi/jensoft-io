package com.jensoft.catalog.server;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "new-book",namespace = "com.jensoft.web.catalog")
@XmlAccessorType(XmlAccessType.FIELD)
public class NewBook {

	
	@XmlElement(name = "new",type=NewItem.class)
	private ArrayList<NewItem> page;
	
	public ArrayList<NewItem> getPage() {
		return page;
	}

	public void setPage(ArrayList<NewItem> page) {
		this.page = page;
	}
	
}
