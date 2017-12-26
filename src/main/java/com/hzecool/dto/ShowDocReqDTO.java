package com.hzecool.dto;

import com.alibaba.fastjson.annotation.JSONField;

public class ShowDocReqDTO {

	@JSONField(name = "api_key")
	private String apiKey; 
	
	@JSONField(name = "api_token")
	private String apiToken; 
	
	@JSONField(name = "cat_name")
	private String catName; 
	
	@JSONField(name = "page_title")
	private String pageTitle; 
	
	@JSONField(name = "page_content")
	private String pageContent;

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getApiToken() {
		return apiToken;
	}

	public void setApiToken(String apiToken) {
		this.apiToken = apiToken;
	}

	public String getCatName() {
		return catName;
	}

	public void setCatName(String catName) {
		this.catName = catName;
	}

	public String getPageTitle() {
		return pageTitle;
	}

	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}

	public String getPageContent() {
		return pageContent;
	}

	public void setPageContent(String pageContent) {
		this.pageContent = pageContent;
	} 
	
	
}
