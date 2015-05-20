package com.jensoft.catalog.server;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "see")
@XmlAccessorType(XmlAccessType.FIELD)
public class SeeItem {
	
	@XmlElement(name = "see-class")
	private String seeClassName;
	
	@XmlElement(name = "see-package")
	private String seeClassPackage;

	/**
	 * @return the seeClassName
	 */
	public String getSeeClassName() {
		return seeClassName;
	}

	/**
	 * @param seeClassName the seeClassName to set
	 */
	public void setSeeClassName(String seeClassName) {
		this.seeClassName = seeClassName;
	}

	/**
	 * @return the seeClassPackage
	 */
	public String getSeeClassPackage() {
		return seeClassPackage;
	}

	/**
	 * @param seeClassPackage the seeClassPackage to set
	 */
	public void setSeeClassPackage(String seeClassPackage) {
		this.seeClassPackage = seeClassPackage;
	}
	
	
}
