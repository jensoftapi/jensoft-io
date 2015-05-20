package com.jensoft.catalog.server;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "views-page",namespace = "com.jensoft.web.catalog")
@XmlAccessorType(XmlAccessType.FIELD)
public class ViewsPage {

	
	@XmlElement(name = "view",type=ViewItem.class)
	private ArrayList<ViewItem> page;
	
	public ArrayList<ViewItem> getPage() {
		return page;
	}

	public void setPage(ArrayList<ViewItem> page) {
		this.page = page;
	}
	
}
