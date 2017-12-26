package com.hzecool.dto;

import java.util.List;

public class SwaggerJsonDTO {

	private String swagger;
	
	private List<SwaggerTagDTO> tags;
	
	private List<String> schemes;
	
	private List<String> consumes;
	
	private List<String> produces;
	
	private String paths;
	
	private String definitions;

	public String getSwagger() {
		return swagger;
	}

	public void setSwagger(String swagger) {
		this.swagger = swagger;
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

	public String getPaths() {
		return paths;
	}

	public void setPaths(String paths) {
		this.paths = paths;
	}

	public String getDefinitions() {
		return definitions;
	}

	public void setDefinitions(String definitions) {
		this.definitions = definitions;
	}

	public List<SwaggerTagDTO> getTags() {
		return tags;
	}

	public void setTags(List<SwaggerTagDTO> tags) {
		this.tags = tags;
	}

}
