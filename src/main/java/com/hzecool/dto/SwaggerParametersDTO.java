package com.hzecool.dto;

import com.alibaba.fastjson.annotation.JSONField;

public class SwaggerParametersDTO {

	private String in;
	
	private String name;
	
	private String description;
	
	private Boolean required;
	
	private String type;
	
	@JSONField(name = "default")
	private String defaultT;
	
	private String format;
	
	private SwaggerParametersSchemaDTO schema;

	public String getIn() {
		return in;
	}

	public void setIn(String in) {
		this.in = in;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

	public SwaggerParametersSchemaDTO getSchema() {
		return schema;
	}

	public void setSchema(SwaggerParametersSchemaDTO schema) {
		this.schema = schema;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDefaultT() {
		return defaultT;
	}

	public void setDefaultT(String defaultT) {
		this.defaultT = defaultT;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}
	
}
