package com.jensoft.catalog.server;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "units-page",namespace = "com.jensoft.web.catalog")
@XmlAccessorType(XmlAccessType.FIELD)
public class UnitsPage {

	
	@XmlElement(name = "unit",type=UnitItem.class)
	private ArrayList<UnitItem> page;
	
	public ArrayList<UnitItem> getPage() {
		return page;
	}

	public void setPage(ArrayList<UnitItem> page) {
		this.page = page;
	}

}
