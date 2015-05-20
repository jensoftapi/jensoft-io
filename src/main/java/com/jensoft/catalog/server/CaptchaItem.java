package com.jensoft.catalog.server;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "captcha")
@XmlAccessorType(XmlAccessType.FIELD)
public class CaptchaItem {
	
	@XmlElement(name = "question")
	private String question;

	@XmlElement(name = "response")
	private String anwser;

	@XmlElement(name = "image")
	private String url;

	public CaptchaItem() {
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getAnwser() {
		return anwser;
	}

	public void setAnwser(String anwser) {
		this.anwser = anwser;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	
}
