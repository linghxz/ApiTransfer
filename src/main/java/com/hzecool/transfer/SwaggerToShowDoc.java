package com.hzecool.transfer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.hzecool.dto.ShowDocReqDTO;
import com.hzecool.dto.SwaggerDetailDTO;
import com.hzecool.dto.SwaggerJsonDTO;
import com.hzecool.dto.SwaggerParametersDTO;
import com.hzecool.dto.SwaggerParametersSchemaDTO;
import com.hzecool.fdn.utils.StringUtils;
import com.hzecool.fdn.utils.converter.JsonUtils;
import com.hzecool.fdn.utils.net.HttpClientUtil;

public class SwaggerToShowDoc {
	private static Logger logger = LoggerFactory.getLogger(SwaggerToShowDoc.class);

	private static String url = "http://106.15.226.60:8080/showdoc/server/index.php?s=/api/item/updateByApi";
	
	private static Map<String, StringBuilder> defMap = new HashMap<String, StringBuilder>();
	private static Map<String, LinkedHashMap<String, Object>> defPropMap = new HashMap<String, LinkedHashMap<String, Object>>();
	
	// 1：商陆花一代，0：其他
	private static int docType = 0;
	
	public static void main(String[] args) {
		transfer("d470c2ab915b5d881acd0a8b3c9571f51352429348", "c77c90e31047a6c42486ccc32f5de673524562028", 
				"D:/swaggerJson/config_center_swagger.json", "D:/api.txt");
		
//		StringBuilder sb = new StringBuilder();
//		String s = "{\"codeorname\":\"款号名称查条件\",\"flag\":\"0表示不停用，1表示停用\","
//				+ "\"calss\":{\"name\":\"初一三班\",\"number\":\"301\"},\"rows\":[{\"id\":\"物理id\",\"type\":\"类型\"}]}";
//		JSONObject jo = JSONObject.parseObject(s);
//		for (Entry<String, Object> entry : jo.entrySet()) {
//			jsonDataHandleForOut(entry.getKey(), entry.getValue().toString(), 0, sb);
//		}
//		System.out.println(sb.toString());

	}
	
	public static void transfer(String apiKey, String apiToken, String filePath, String templatePath) {
		
		StringBuilder jsonDoc = new StringBuilder();
		StringBuilder jsonShowDoc = new StringBuilder();
		BufferedReader br = null;
		Map<String, String> replaceParams = new HashMap<String, String>();
		ShowDocReqDTO showDocReq = null;
		try {
			// 读取swagger
			File file = new File(filePath);
			if (file.exists()) {
				br = new BufferedReader(new FileReader(file));
				String s = null;
				while ((s = br.readLine()) != null) {
					jsonDoc.append(s);
				}
			}
			
			// 读取showDoc模板
			File fileShowDoc = new File(templatePath);
			if (file.exists()) {
				br = new BufferedReader(new FileReader(fileShowDoc));
				String s = null;
				while ((s = br.readLine()) != null) {
					jsonShowDoc.append(s);
					jsonShowDoc.append("\r\n");
				}
			}
			
			String jsonDocS = StringUtils.replace(jsonDoc.toString(), "$", "");
			SwaggerJsonDTO swaggerJson = JsonUtils.fromJson(jsonDocS, SwaggerJsonDTO.class);
			String content = swaggerJson.getPaths();
			LinkedHashMap<String,Object> contentJo = JSONObject.parseObject(content, new LinkedTypeReference(), Feature.OrderedField);
			// 解析definitions数据结构
			definitionsAnalysis(swaggerJson.getDefinitions());
			
			// 请求地址层 “/api.do?apiKey=ec-xboss-auth”
			for (Entry<String, Object> entry : contentJo.entrySet()) {
				// 请求地址
				String reqUrl = entry.getKey();
				String schemes = "://xxx";
				String reqMethod = "";
				SwaggerDetailDTO detail = null;
				
				LinkedHashMap<String, Object> jo1 = JSONObject.parseObject(entry.getValue().toString(), new LinkedTypeReference(), Feature.OrderedField);
				// 请求方式层 “post”
				for (Entry<String, Object> entry2 : jo1.entrySet()) {
					reqMethod = entry2.getKey();
					detail = JsonUtils.fromJson(entry2.getValue().toString(), SwaggerDetailDTO.class);
					
					break;
				}
				
				showDocReq = new ShowDocReqDTO();
				showDocReq.setApiKey(apiKey);
				showDocReq.setApiToken(apiToken);
				showDocReq.setPageTitle(detail.getSummary());
				String catName = "默认";
				if (detail.getTags() != null && detail.getTags().size() > 0) {
					catName = detail.getTags().get(0);
				}
				showDocReq.setCatName(catName);
				
				replaceParams.clear();
				schemes = detail.getSchemes().get(0) + schemes;
				replaceParams.put("INTERFACE_DESC", detail.getDescription());
				replaceParams.put("REQUEST_URL", schemes + reqUrl);
				replaceParams.put("REQUEST_METHOD", reqMethod);
				// 入参
				StringBuilder inParams = new StringBuilder();
				List<SwaggerParametersDTO> parameters = detail.getParameters();
				inParamsHandle(parameters, inParams);

				replaceParams.put("IN_PARAMS", inParams.toString());
				// 出参
				StringBuilder outJson = new StringBuilder(); 
				StringBuilder outParams = new StringBuilder();
				LinkedHashMap<String,Object> outP = detail.getResponses();
				for (Entry<String, Object> entry4 : outP.entrySet()) {
					LinkedHashMap<String,Object> outJo = JSONObject.parseObject(entry4.getValue().toString(), new LinkedTypeReference(), Feature.OrderedField);
					SwaggerParametersSchemaDTO schema = JsonUtils.fromJson(outJo.get("schema").toString(), SwaggerParametersSchemaDTO.class);
					
					if (schema == null) {
						continue;
					}
					outParamsHandle(schema, outParams);
					// 返回示例
					if (schema != null) {
						// 统一定义返回结构处理
						if (StringUtils.isNotBlank(schema.getRef())) {
							String[] refs = StringUtils.split(schema.getRef(), "/");
							outParams = defMap.get(refs[2]);
							
							JSONObject jsonObject = new JSONObject();
							outJsonAssemble(jsonObject, defPropMap.get(refs[2]));
							outJson.append(JsonFormart.formatJson(jsonObject.toString()));
						} else {
							if (StringUtils.isNotBlank(schema.getContent())) {
								outJson.append(schema.getContent());
							} else {
								JSONObject jsonObject = new JSONObject();
								outJsonAssemble(jsonObject, schema.getProperties());
								outJson.append(JsonFormart.formatJson(jsonObject.toString()));
							}
						}
					}
					
					break;
				}
				replaceParams.put("OUT_PARAMS", outParams.toString());
				replaceParams.put("OUT_JSON", outJson.toString());
				
				String pageContent = StringUtils.replaceParams(jsonShowDoc.toString(), replaceParams);
				
//				System.out.println(pageContent);
				showDocReq.setPageContent(pageContent);
				// 推送showDoc
				JSONObject json = JsonUtils.bean2JSONObject(showDocReq);
				Map<String, Object> reqParam = new JSONObject();
				reqParam = json;
				JSONObject retJo = HttpClientUtil.postFormRetJson(url, reqParam);
				if (!"0".equals(retJo.getString("error_code"))) {
					logger.error("{},{}",retJo.getString("error_code"), retJo.getString("error_message"));
				} else {
					System.out.println("success");
				}
				
			}
			System.out.println("结束！！！");
		} catch (Exception e) {
			logger.error("错误：", e);
//			System.err.println(e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					logger.error(e.getMessage());
				}
			}
		}
	}
	
	public static void inParamsHandle(List<SwaggerParametersDTO> inParams, StringBuilder builder) {
		for (SwaggerParametersDTO param : inParams) {
			String paramType = "string", memo = "无";
			
			if (param.getSchema() != null) {
				int jsonType = 0;
				if (isJsonArr(param.getSchema().getContent())) {
					jsonType = 1;
				}
				builder.append("|");
				builder.append(param.getName());
				builder.append("|");
				builder.append("object");
				builder.append("|");
				if (param.getRequired()) {
					builder.append("是");
				} else {
					builder.append("否");
				}
				builder.append("|");
				if (jsonType == 1) {
					builder.append("list结构数据");
				} else {
					builder.append("json结构数据");
				}
				builder.append("|");
				builder.append("\r\n");
				
				int level = 1;
				if (isJsonStr(param.getSchema().getContent())) {
					LinkedHashMap<String,Object> firstLevel = JSONObject.parseObject(param.getSchema().getContent(), new LinkedTypeReference(), Feature.OrderedField);
					
					for (Entry<String, Object> fl : firstLevel.entrySet()) {
						level = 1;
						jsonDataHandle(fl.getKey(), fl.getValue().toString(), level, builder);
						
					}
				} else if (isJsonArr(param.getSchema().getContent())) {
					JSONArray array = JSONObject.parseArray(param.getSchema().getContent());
					if (array != null && array.size() > 0) {
						JSONObject firstLevel = array.getJSONObject(0);
						for (Entry<String, Object> fl : firstLevel.entrySet()) {
							level = 1;
							jsonDataHandle(fl.getKey(), fl.getValue().toString(), level, builder);
							
						}
					}
					
				}
			} else {
				// 非json格式
				builder.append("|");
				builder.append(param.getName());
				builder.append("|");
				if (StringUtils.isNotBlank(param.getType())) {
					paramType = param.getType();
				}
				builder.append(paramType);
				builder.append("|");
				if (param.getRequired()) {
					builder.append("是");
				} else {
					builder.append("否");
				}
				builder.append("|");
				if (StringUtils.isNotBlank(param.getDescription())) {
					memo = param.getDescription();
				}
				builder.append(memo);
				builder.append("|");
				builder.append("\r\n");
			}
		}
		
//		return builder.toString();
	}
	
	public static void outParamsHandle(SwaggerParametersSchemaDTO parametersSchema, StringBuilder builder) {
		// 处理商陆花的自构json
		if (parametersSchema != null && parametersSchema.getProperties() != null) {
			for (Entry<String, Object> param : parametersSchema.getProperties().entrySet()) {
				jsonDataHandleForOutProp(param.getKey(), param.getValue().toString(), 0, builder);
			}
		} else if (parametersSchema != null && isJsonStr(parametersSchema.getContent())) {
			LinkedHashMap<String,Object> content = JSONObject.parseObject(parametersSchema.getContent(), new LinkedTypeReference(), Feature.OrderedField);
			for (Entry<String, Object> param : content.entrySet()) {
				String key = param.getKey();
				String val = param.getValue().toString();
				
				jsonDataHandleForOut(key, val, 0, builder);
				
			}
		}
	}
	
	
	private static boolean isJsonStr(String str) {
		boolean ret = false;
		try {
			if (StringUtils.isNotBlank(str)) {
				JSONObject.parseObject(str);
				ret = true;
			}
		} catch (Exception e) {
			ret = false;
		}
		return ret;
	}
	
	private static boolean isJsonArr(String data) {
		boolean ret = false;
		try {
			if (StringUtils.isNotBlank(data)) {
				JSONObject.parseArray(data);
				ret = true;
			}
		} catch (Exception e) {
			ret = false;
		}
		return ret;
	}
	
	private static String getHorizontal(int level) {
		String ret = "";
		for (int i = 0; i < level; i++) {
			ret += "- ";
		}
		
		return ret;
	}
	
	private static void jsonDataHandleForOut(String key, String data, int level, StringBuilder builder) {
		String paramType = "string", memo = "";
		if (isJsonStr(data)) {
			builder.append("|");
			builder.append(getHorizontal(level) + key);
			builder.append("|");
			builder.append("object");
			builder.append("|");
			builder.append("json结构数据");
			builder.append("|");
			builder.append("\r\n");
			LinkedHashMap<String,Object> jo = JSONObject.parseObject(data, new LinkedTypeReference(), Feature.OrderedField);
			++level;
			for (Entry<String, Object> entry : jo.entrySet()) {
				jsonDataHandleForOut(entry.getKey(), entry.getValue().toString(), level, builder);
			}
		}else if (isJsonArr(data)) {
			builder.append("|");
			builder.append(getHorizontal(level) + key);
			builder.append("|");
			builder.append("object");
			builder.append("|");
			builder.append("list数据结构");
			builder.append("|");
			builder.append("\r\n");
			JSONArray ja = JSONObject.parseArray(data);
			if (ja != null && ja.size() > 0) {
				if (isJsonStr(ja.getString(0))) {
					JSONObject jo = ja.getJSONObject(0);
					++level;
					for (Entry<String, Object> entry : jo.entrySet()) {
						jsonDataHandleForOut(entry.getKey(), entry.getValue().toString(), level, builder);
					}
				} else {
					System.out.println(ja.getString(0));
				}
			}
		} else {
			builder.append("|");
			builder.append(getHorizontal(level) + key);
			builder.append("|");
			// 商陆花一代特殊处理，别的项目要恢复
			if (docType == 0) {
				String[] vals = StringUtils.split(data, ",");
				if (vals.length > 1) {
					paramType = vals[0];
					memo = vals[1];
				} else if (vals.length > 0) {
					memo = vals[0];
				} else {
					memo = "无";
				}
			} else {
				memo = data;
			}
			builder.append(paramType);
			builder.append("|");
			builder.append(memo);
			builder.append("|");
			builder.append("\r\n");
		}
	}
	
	private static void jsonDataHandle(String key, String data, int level,StringBuilder builder) {
		String paramType = "string", memo = "";
		if (isJsonStr(data)) {
			LinkedHashMap<String,Object> jo = JSONObject.parseObject(data, new LinkedTypeReference(), Feature.OrderedField);
			builder.append("|");
			builder.append(getHorizontal(level) + key);
			builder.append("|");
			builder.append("object");
			builder.append("|");
			builder.append("|");
			builder.append("json结构数据");
			builder.append("|");
			builder.append("\r\n");
			
			++level;
			for (Entry<String, Object> entry : jo.entrySet()) {
				jsonDataHandle(entry.getKey(), entry.getValue().toString(), level, builder);
			}
		} else if (isJsonArr(data)) {
			builder.append("|");
			builder.append(getHorizontal(level) + key);
			builder.append("|");
			builder.append("object");
			builder.append("|");
			builder.append("|");
			builder.append("list数据结构");
			builder.append("|");
			builder.append("\r\n");
			JSONArray ja = JSONObject.parseArray(data);
			if (ja != null && ja.size() > 0) {
				if (isJsonStr(ja.getString(0))) {
					LinkedHashMap<String, Object> jo = JSONObject.parseObject(ja.getString(0), new LinkedTypeReference(), Feature.OrderedField);
					++level;
					for (Entry<String, Object> entry : jo.entrySet()) {
						jsonDataHandle(entry.getKey(), entry.getValue().toString(), level, builder);
					}
				} else {
					System.out.println(ja.getString(0));
				}
			}
		} else {
			builder.append("|");
			builder.append(getHorizontal(level) + key);
			builder.append("|");
			
			// 商陆花一代特殊处理，别的项目要恢复
			if (docType == 0) {
				String[] vals = StringUtils.split(data, ",");
				if (vals.length > 1) {
					paramType = vals[0];
					memo = vals[1];
				} else if (vals.length > 0) {
					memo = vals[0];
				}
			} else {
				memo = data;
			}
			
			builder.append(paramType);
			builder.append("|");
			builder.append("|");
			builder.append(memo);
			builder.append("|");
			builder.append("\r\n");
		}
	}
	
	private static void jsonDataHandleForOutProp(String key, String data, int level, StringBuilder builder) {
		LinkedHashMap<String,Object> dataJo = JSONObject.parseObject(data, new LinkedTypeReference(), Feature.OrderedField);
		if ("object".equals(dataJo.get("type").toString())) {
			builder.append("|");
			builder.append(getHorizontal(level) + key);
			builder.append("|");
			builder.append(dataJo.get("type").toString());
			builder.append("|");
			builder.append("json结构数据");
			builder.append("|");
			builder.append("\r\n");
			
			++level;
			LinkedHashMap<String,Object> prop = JSONObject.parseObject(dataJo.get("properties").toString(), new LinkedTypeReference(), Feature.OrderedField);
			for (Entry<String, Object> entryProp : prop.entrySet()) {
				jsonDataHandleForOutProp(entryProp.getKey(), entryProp.getValue().toString(), level, builder);
			}
		} else if ("array".equals(dataJo.get("type").toString())) {
			builder.append("|");
			builder.append(getHorizontal(level) + key);
			builder.append("|");
			builder.append("object");
			builder.append("|");
			builder.append("list结构数据");
			builder.append("|");
			builder.append("\r\n");
			
			++level;
			LinkedHashMap<String,Object> items = JSONObject.parseObject(dataJo.get("items").toString(), new LinkedTypeReference(), Feature.OrderedField);
			LinkedHashMap<String,Object> prop = JSONObject.parseObject(items.get("properties").toString(), new LinkedTypeReference(), Feature.OrderedField);
			for (Entry<String, Object> entryProp : prop.entrySet()) {
				jsonDataHandleForOutProp(entryProp.getKey(), entryProp.getValue().toString(), level, builder);
			}
		} else {
			
			String type = "string", memo = "无";
			if (dataJo.containsKey("format")) {
				if ("int32".equals(dataJo.get("format").toString())) {
					type = "int";
				} else if ("int64".equals(dataJo.get("format").toString())) {
					type = "long";
				} else {
					type = dataJo.get("type").toString();
				}
			}
//			if ("int32".equals(dataJo.get("format").toString())) {
//				type = "int";
//			} else if ("int64".equals(dataJo.get("format").toString())) {
//				type = "long";
//			} else {
//				type = dataJo.get("type").toString();
//			}
			
			if (StringUtils.isNotBlank(dataJo.get("description").toString())) {
				memo = dataJo.get("description").toString();
			}
			
			builder.append("|");
			builder.append(getHorizontal(level) + key);
			builder.append("|");
			builder.append(type);
			builder.append("|");
			builder.append(memo);
			builder.append("|");
			builder.append("\r\n");
		}
	}
	
	/**
	 * 
	 *  
	 * @author maozj  
	 * @param retJo
	 * @param data responses层-第一个结果层-properties层
	 */
	private static void outJsonAssemble(JSONObject retJo, LinkedHashMap<String,Object> data) {
		for (Entry<String, Object> entry : data.entrySet()) {
			String key = entry.getKey();
			String val = entry.getValue().toString();
			
			LinkedHashMap<String,Object> valJ = JSONObject.parseObject(val, new LinkedTypeReference(), Feature.OrderedField);
			String type = valJ.get("type").toString();
			if ("object".equals(type)) {
				LinkedHashMap<String,Object> jo = JSONObject.parseObject(valJ.get("properties").toString(), new LinkedTypeReference(), Feature.OrderedField);
				JSONObject jo1 = new JSONObject();
				outJsonAssemble(jo1, jo);
				retJo.put(key, jo1);
			} else if ("array".equals(type)) {
				JSONArray array = new JSONArray();
				LinkedHashMap<String,Object> jo = JSONObject.parseObject(valJ.get("items").toString(), new LinkedTypeReference(), Feature.OrderedField);
				JSONObject jo1 = new JSONObject();
				LinkedHashMap<String,Object> jot = JSONObject.parseObject(jo.get("properties").toString(), new LinkedTypeReference(), Feature.OrderedField);
				outJsonAssemble(jo1, jot);
				array.add(jo1);
				retJo.put(key, array);
			} else {
//				String format = valJ.get("format").toString();
				String format = null;
				if (valJ.containsKey("format")) {
					format = valJ.get("format").toString();
					if ("int32".equals(format)) {
						type = "int";
					} else if ("int64".equals(format)) {
						type = "long";
					}
				}
				retJo.put(key, type + ";" + valJ.get("description").toString());
			}
		}
	}
	
	private static void definitionsAnalysis(String definitions) {
		LinkedHashMap<String,Object> defJson = JSONObject.parseObject(definitions, new LinkedTypeReference(), Feature.OrderedField);
		for (Entry<String, Object> entry : defJson.entrySet()) {
			String key = entry.getKey();
			
			StringBuilder builder = new StringBuilder();
			
			JSONObject jsonO = JSONObject.parseObject(entry.getValue().toString());
			if ("object".equalsIgnoreCase(jsonO.getString("type"))) {
				LinkedHashMap<String,Object> jsonProp = JSONObject.parseObject(jsonO.getString("properties"), new LinkedTypeReference(), Feature.OrderedField);
				defPropMap.put(key, jsonProp);
				
				for (Entry<String, Object> entry2 : jsonProp.entrySet()) {
					jsonDataHandleForOutProp(entry2.getKey(), entry2.getValue().toString(), 0, builder);
				}
			}
			
			defMap.put(key, builder);
		}
	}
}
