package com.jensoft.catalog.server;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "view")
@XmlType(propOrder = { "className", "classPackage", "description", "type",
		"unit", "sees"})
public class ViewItem {

	private String className;
	private String classPackage;
	private String unit;
	private String description;
	private String type;

	private ArrayList<SeeItem> sees;

	public ViewItem() {
	}


	@XmlElement(name = "class")
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	@XmlElement(name = "package")
	public String getClassPackage() {
		return classPackage;
	}

	public void setClassPackage(String classPackage) {
		this.classPackage = classPackage;
	}

	@XmlElement(name = "unit")
	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	@XmlElement(name = "description")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@XmlElement(name = "type")
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the sees
	 */
	@XmlElement(name = "see", type = SeeItem.class)
	public ArrayList<SeeItem> getSees() {
		return sees;
	}

	/**
	 * @param sees
	 *            the sees to set
	 */
	public void setSees(ArrayList<SeeItem> sees) {
		this.sees = sees;
	}


}
