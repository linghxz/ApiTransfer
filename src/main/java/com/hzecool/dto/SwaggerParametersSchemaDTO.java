package com.hzecool.dto;

import java.util.LinkedHashMap;

public class SwaggerParametersSchemaDTO {

	private String type;
	
	private String format;
	
	private String content;
	
	private String title;
	
	private LinkedHashMap<String, Object> properties;
	
	private String ref;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public LinkedHashMap<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(LinkedHashMap<String, Object> properties) {
		this.properties = properties;
	}

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}
}
