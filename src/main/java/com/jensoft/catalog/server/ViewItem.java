package com.jensoft.catalog.server;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "view")
@XmlType(propOrder = { "className", "classPackage", "description", "type",
		"unit", "sees", "x2d" ,"captchas"})
public class ViewItem {

	private String className;
	private String classPackage;
	private String unit;
	private String description;
	private String type;
	private String x2d;

	private ArrayList<SeeItem> sees;
	private ArrayList<CaptchaItem> captchas;

	public ViewItem() {
	}

	@XmlElement(name = "x2d")
	public String getX2d() {
		return x2d;
	}

	/**
	 * @param x2d
	 *            the x2d to set
	 */
	public void setX2d(String x2d) {
		this.x2d = x2d;
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

	@XmlElement(name = "captcha", type = CaptchaItem.class)
	public ArrayList<CaptchaItem> getCaptchas() {
		return captchas;
	}

	/**
	 * @param captchas the captchas to set
	 */
	public void setCaptchas(ArrayList<CaptchaItem> captchas) {
		this.captchas = captchas;
	}
	
	

}
