package com.hzecool.dto;

import java.util.LinkedHashMap;
import java.util.List;

public class SwaggerDetailDTO {

	private List<String> tags;
	
	private String summary;
	
	private String description;
	
	private List<String> schemes;
	
	private List<String> consumes;
	
	private List<String> produces;
	
	private List<SwaggerParametersDTO> parameters;
	
	private LinkedHashMap<String, Object> responses;
	
	private Integer sortWeight;

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<String> getSchemes() {
		return schemes;
	}

	public void setSchemes(List<String> schemes) {
		this.schemes = schemes;
	}

	public List<String> getConsumes() {
		return consumes;
	}

	public void setConsumes(List<String> consumes) {
		this.consumes = consumes;
	}

	public List<String> getProduces() {
		return produces;
	}

	public void setProduces(List<String> produces) {
		this.produces = produces;
	}

	public List<SwaggerParametersDTO> getParameters() {
		return parameters;
	}

	public void setParameters(List<SwaggerParametersDTO> parameters) {
		this.parameters = parameters;
	}

	public LinkedHashMap<String, Object> getResponses() {
		return responses;
	}

	public void setResponses(LinkedHashMap<String, Object> responses) {
		this.responses = responses;
	}

	public Integer getSortWeight() {
		return sortWeight;
	}

	public void setSortWeight(Integer sortWeight) {
		this.sortWeight = sortWeight;
	}
}
