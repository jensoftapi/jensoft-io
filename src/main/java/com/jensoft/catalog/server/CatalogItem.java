package com.jensoft.catalog.server;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "catalog",namespace = "com.jensoft.web.catalog")
@XmlAccessorType(XmlAccessType.FIELD)
public class CatalogItem {

	@XmlElement
	private String name;
	@XmlElement
	private String group;
	@XmlElement
	private String artifact;
	@XmlElement
	private String version;
	@XmlElement
	private String description;
	@XmlElement
	private String core;
	
	@XmlElement(name="count-charts")
	private int countCharts;
	
	@XmlElement(name="count-units")
	private int countUnits;
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the group
	 */
	public String getGroup() {
		return group;
	}
	/**
	 * @param group the group to set
	 */
	public void setGroup(String group) {
		this.group = group;
	}
	/**
	 * @return the artifact
	 */
	public String getArtifact() {
		return artifact;
	}
	/**
	 * @param artifact the artifact to set
	 */
	public void setArtifact(String artifact) {
		this.artifact = artifact;
	}
	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}
	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}
	
	
	
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the core
	 */
	public String getCore() {
		return core;
	}
	/**
	 * @param core the core to set
	 */
	public void setCore(String core) {
		this.core = core;
	}
	
	
	
	/**
	 * @return the countCharts
	 */
	public int getCountCharts() {
		return countCharts;
	}
	/**
	 * @param countCharts the countCharts to set
	 */
	public void setCountCharts(int countCharts) {
		this.countCharts = countCharts;
	}
	/**
	 * @return the countUnits
	 */
	public int getCountUnits() {
		return countUnits;
	}
	/**
	 * @param countUnits the countUnits to set
	 */
	public void setCountUnits(int countUnits) {
		this.countUnits = countUnits;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CatalogItem [name=" + name + ", group=" + group + ", artifact="
				+ artifact + ", version=" + version + ", description="
				+ description + ", core=" + core + ", countCharts="
				+ countCharts + ", countUnits=" + countUnits + "]";
	}
	
	
	
	
}
