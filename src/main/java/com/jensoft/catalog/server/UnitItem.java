package com.jensoft.catalog.server;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "unit")
@XmlAccessorType(XmlAccessType.FIELD)
public class UnitItem {

	@XmlElement(name = "name")
	private String unitName;
	
	 @XmlElement(name = "info")
	private String unitInfo;
	
	 @XmlElement(name = "views-count")
	private int unitViews;
	
	@XmlElement(name = "view",type=ViewItem.class)
	private ArrayList<ViewItem> charts;
	
	
	
	public UnitItem() {
	}


	public String getUnitName() {
		return unitName;
	}

	
	public void setUnitName(String name) {
		this.unitName = name;
	}

	
	public String getUnitInfo() {
		return unitInfo;
	}

	
	public void setUnitInfo(String info) {
		this.unitInfo = info;
	}


	public int getUnitViews() {
		return unitViews;
	}

	/**
	 * @param unitViews the unitViews to set
	 */
	public void setUnitViews(int unitViews) {
		this.unitViews = unitViews;
	}


	public ArrayList<ViewItem> getCharts() {
		return charts;
	}


	public void setCharts(ArrayList<ViewItem> charts) {
		this.charts = charts;
	}
	
	
}
