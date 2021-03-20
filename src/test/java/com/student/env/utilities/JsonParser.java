package com.student.env.utilities;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.hc.client5.http.HttpHostConnectException;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.simpleflatmapper.csv.CloseableCsvReader;
import org.simpleflatmapper.csv.CsvParser;
import org.simpleflatmapper.csv.CsvParser.DSL;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;
import com.steadystate.css.util.ThrowCssExceptionErrorHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class JsonParser {

	private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());
	private static String response;
	private static int statuCode;
	public final int SUCCESS = 200;
	public final static int FAILURE = 500;
	public final static String bReturnAsString = "ReturnAsString";
	public final static String bReturnAsJson = "ReturnAsJson";
	public static String value = "";

	public static String getValue(String jsonObj, String key) {

		String[] pChild = key.split("\\.");
		JSONObject record = new JSONObject(jsonObj);

		if (key.contains(".")) {
			JSONObject temp = record.getJSONObject(pChild[0]);

			for (int depth = 1; depth < pChild.length; depth++) {
				temp = temp.getJSONObject(pChild[depth]);
			}
			value = String.valueOf(temp.get(pChild[pChild.length]));
		} else
			value = String.valueOf(record.get(key));

		return value;
	}

	public static String getValue(JSONObject record, String key) {
		String[] pChild = key.split("\\.");

		if (key.contains(".")) {
			JSONObject temp = record.getJSONObject(pChild[0]);

			for (int depth = 1; depth < pChild.length - 1; depth++) {
				temp = temp.getJSONObject(pChild[depth]);
			}
			value = String.valueOf(temp.get(pChild[pChild.length - 1]));
		} else
			value = String.valueOf(record.get(key));

		return value;

	}

	public static JSONObject getJsonNode(JSONObject record, String key) {
		String[] pChild = key.split("\\.");
		JSONObject temp = record.getJSONObject(pChild[0]);
		for (int depth = 1; depth < pChild.length; depth++) {
			temp = temp.getJSONObject(pChild[depth]);
		}

		return temp;
	}

	public static List<JSONObject> getArrayElements(JSONArray jsonObj, String key, String value) {

		List<JSONObject> jsonObjectList = new ArrayList<JSONObject>();
		for (int count = 0; count < jsonObj.length(); count++) {
			String jsonValue = String.valueOf(jsonObj.getJSONObject(count).get(key));
			if (jsonValue.equalsIgnoreCase(value)) {
				LOGGER.info("key " + key + "value : " + value);
				jsonObjectList.add(jsonObj.getJSONObject(count));
			}
		}

		return jsonObjectList;
	}

	public static JSONObject getArrayElementForNestedElement(JSONArray jsonObj, String key, String nestedKey,
			String value) {
		for (int count = 0; count < jsonObj.length(); count++) {
			JSONObject jsonobj = (JSONObject) jsonObj.getJSONObject(count).get(key);
			String jsonValue = String.valueOf(jsonobj.get(nestedKey));
			if (jsonValue.equalsIgnoreCase(value)) {
				return (JSONObject) jsonObj.getJSONObject(count).get(key);
			}
		}
		return null;
	}

	public static JSONObject getArrayElement(JSONObject obj, HashMap<String, String> keyValue) throws Throwable {

		ObjectMapper oMapper = new ObjectMapper();
		JsonNode jNodes = oMapper.readTree(obj.toString());

		for (JsonNode jn : jNodes) {
			JSONObject jObj = new JSONObject(jn.toString());
			boolean bFound = true;
			for (Map.Entry<String, String> entry : keyValue.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();

				String jsonVal = JsonParser.getValue(jObj, key);
				if (!jsonVal.equals(value)) {
					bFound = false;
					break;
				}
			}
			if (bFound) {
				return jObj;
			}
		}

		return null;
	}

	public static JsonNode getArrayElementsUsingObjectMapper(JsonNode jsonObj, String key, String value) {

		for (int count = 0; count < jsonObj.size(); count++) {
			String jsonVal = jsonObj.get(count).get(key).textValue();
			if (jsonVal.equalsIgnoreCase(value)) {
				return jsonObj.get(count);
			}
		}
		return null;
	}

	public static JSONObject getArrayElementFromFile(String filePath, String key, String value) throws Throwable {
		ObjectMapper oMapper = new ObjectMapper();
		JsonNode jNodes = oMapper.readTree(new File(filePath));

		for (JsonNode jn : jNodes) {
			JSONObject jObj = new JSONObject(jn.toString());

			try {
				String jsonValue = JsonParser.getValue(jObj, key);
				if (jsonValue.equals(value)) {
					LOGGER.info("Found ...");
					return jObj;
				}
			} catch (Exception e) {
				e.getMessage();
			}
		}

		return null;
	}

	public static JSONObject getArrayElementFromFiles(String filePath, String key, String value) throws Throwable {
		ObjectMapper oMapper = new ObjectMapper();
		JsonNode jNodes = oMapper.readTree(new File(filePath));

		try {
			for (JsonNode jn : jNodes) {
				List<JsonNode> parentJsonForMultipleKeyPresent = oMapper.readTree(jn.toString()).findParents(key);
				if (parentJsonForMultipleKeyPresent.size() > 1) {
					for (JsonNode j : parentJsonForMultipleKeyPresent) {
						if (j.findValue(key).asText().equalsIgnoreCase(value)) {
							JSONObject jObj = new JSONObject(j.toString());
							return jObj;
						}
					}
				} else if (jn.findValue(key).asText().equalsIgnoreCase(value)) {
					JSONObject jObj = new JSONObject(jn.toString());
					return jObj;
				}
			}
		} catch (Exception e) {
			e.getMessage();
		}

		return null;
	}

	public static void convertCSVToJsonFile(String csvFilePath, String jsonFilePath, char seperator) throws Throwable {

		File csvData = new File(csvFilePath);
		OutputStream out = new FileOutputStream(jsonFilePath);

		DSL test = CsvParser.separator(seperator);
		CloseableCsvReader reader = test.reader(csvData);
		JsonFactory jsonFactory = new JsonFactory();

		Iterator<String[]> it = reader.iterator();
		String[] headers = it.next();

		try {
			JsonGenerator jsonGen = jsonFactory.createGenerator(out);
			jsonGen.writeStartArray();
			while (it.hasNext()) {
				jsonGen.writeStartObject();
				String[] values = it.next();
				int nbCells = Math.min(values.length, headers.length);

				for (int i = 0; i < nbCells; i++) {
					jsonGen.writeFieldName(headers[i]);
					jsonGen.writeString(values[i]);
				}
				jsonGen.writeEndObject();
			}
			jsonGen.writeEndArray();
		} catch (Exception e) {
			e.printStackTrace();
		}

		reader.close();
		out.close();
	}

	public static JSONObject getJSONFromXML(String xmlFilePath) throws IOException {

		String sContent = "";
		String sCurrentLine = "";

		BufferedReader br = new BufferedReader(new FileReader(xmlFilePath));
		while ((sContent = br.readLine()) != null) {
			sContent += sCurrentLine;
		}
		br.close();
		return XML.toJSONObject(sContent);
	}

	public static JSONObject getJSONFromXML(String xmlFilePath, String rootNode) throws IOException {

		return getJSONFromXML(xmlFilePath).getJSONObject(rootNode);
	}

	public static boolean verifyKeyExistsInJSONFile(String jsonFileName, String lookUpKey, String lookUpValue)
			throws Exception {

		JsonFactory jFact = new JsonFactory();
		File jFile = new File(jsonFileName);

		try {
			com.fasterxml.jackson.core.JsonParser parser = jFact.createJsonParser(jFile);
			while (!parser.isClosed()) {
				parser.nextToken();
				String curToken = parser.getCurrentName();
				if (lookUpKey.equalsIgnoreCase(curToken)) {
					// get next token
					parser.nextTextValue(); // move the cursor to value
					String stVal = parser.getText();
					if (stVal.equals(lookUpValue)) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public static String getValuefromJsonObj(JSONObject jsonObj, String key) throws Exception {
		ObjectMapper oMapper = new ObjectMapper();
		JsonNode jNodes = oMapper.readTree(jsonObj.toString());

		return jNodes.findValue(key).asText("none");
	}

	public static List<String> getValuesfromJsonObj(JSONObject jsonObj, String key) throws Exception {
		ObjectMapper oMapper = new ObjectMapper();
		JsonNode jNodes = oMapper.readTree(jsonObj.toString());
		List<String> findValAsText = jNodes.findValuesAsText(key);
		// trim all elements in the list

		return findValAsText.stream().map(String::trim).collect(Collectors.toList());
	}

	public static Object getJSONElement(Object json, String elementKey, String elementValue) {
		Object returnJSON = null;
		try {
			if (json instanceof JSONArray) {
				JSONArray jsonArray = (JSONArray) json;
				for (int i = 0; i < jsonArray.length(); i++) {
					returnJSON = getJSONElement(jsonArray.get(i), elementKey, elementValue);
					if (returnJSON != null) {
						return returnJSON;
					}
				}
			} else if (json instanceof JSONObject) {
				JSONObject jSONObject = (JSONObject) json;
				@SuppressWarnings("rawtypes")
				Iterator keyItr = jSONObject.keys();
				while (keyItr.hasNext()) {
					String key = keyItr.next().toString();
					Object value = jSONObject.get(key);
					if (key.equals(elementKey) && (elementValue.equals(value.toString()))) {
						return returnJSON;
					}
					if (value instanceof JSONArray || value instanceof JSONObject) {
						returnJSON = getJSONElement(value, elementKey, elementValue);
						if (returnJSON != null) {

							return returnJSON;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return returnJSON;
	}

	public static Object getJSONPropElement(Object jsonPropResp, String elementKey, String elementValue) {
		Object returnJSON = null;
		JSONObject jsonObject = (JSONObject) jsonPropResp;

		@SuppressWarnings("rawtypes")
		Iterator keyItr = jsonObject.keys();
		while (keyItr.hasNext()) {
			String key = keyItr.next().toString();
			Object value = jsonObject.get(key);
			if (key.equals(elementKey) && (elementValue.equals(value.toString()))) {
				returnJSON = jsonObject;
				return returnJSON;
			}

		}

		return returnJSON;
	}

	public String getSwaggerResponseFromUrl(String getStudentExamPublishUri) throws InterruptedException, Exception {
		TimeOutUtil.sleepFor(20000);
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(30 * 100).build();
		HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
		HttpGet request = new HttpGet(getStudentExamPublishUri);

		try {
			HttpResponse httpResponse = client.execute(request);
			statuCode = httpResponse.getStatusLine().getStatusCode();
			if (statuCode == 200) {
				BufferedReader bufferedReader = new BufferedReader(
						new InputStreamReader(httpResponse.getEntity().getContent()));
				StringBuffer serviceResponse = new StringBuffer();
				String line = "";
				while ((line = bufferedReader.readLine()) != null) {
					serviceResponse.append(line);
				}
				response = serviceResponse.toString();
				if (response.equals("[]") || response.equals("{}")) {
					response = null;
				}
			} else {
				throw new AssertionError("encountered status code as : " + statuCode);
			}
		} catch (HttpHostConnectException e) {
			statuCode = FAILURE;
			response = null;
			e.printStackTrace();
		} catch (Exception e) {
			statuCode = FAILURE;
			response = null;
			e.printStackTrace();
		}

		return response;
	}

	public static JSONObject convertJsonObjectToArrayEmbedded(JSONObject jObj) throws Exception {

		// this is an incompleted method but can be found in a better way
		if (jObj.has("embedded")) {

		}
		return jObj;
	}

	public static Map<String, Object> convertToMap(JSONObject object) throws JSONException {
		Map<String, Object> map = new HashMap<>();
		Iterator<String> keysItr = object.keys();
		while (keysItr.hasNext()) {
			String key = keysItr.next();
			Object val = object.get(key);
			if (val instanceof JSONArray) {
				val = convertToList((JSONArray) val);
			} else if (val instanceof JSONObject) {
				val = convertToMap((JSONObject) val);
			}
			map.put(key, val);
		}

		return map;
	}

	public static List<Object> convertToList(JSONArray array) throws JSONException {
		List<Object> list = new ArrayList<>();
		for (int i = 0; i < array.length(); i++) {
			Object val = array.get(i);
			if (val instanceof JSONArray) {
				val = convertToList((JSONArray) val);
			} else if (val instanceof JSONObject) {
				val = convertToMap((JSONObject) val);
			}
			list.add(val);
		}

		return list;
	}

	public static boolean compareJSON(Object expected, Object actual) {

		if (!expected.getClass().equals(actual.getClass())) {
			return false;
		}
		if (expected instanceof JSONObject) {
			JSONObject expectedJSON = (JSONObject) expected;
			JSONObject actualJSON = (JSONObject) actual;
			if (expectedJSON.length() != actualJSON.length()) {
				return false;
			}
			for (Object key : expectedJSON.keySet()) {
				if (!compareJSON(expectedJSON.get(key.toString()), actualJSON.get(key.toString()))) {
					return false;
				}
			}
		} else if (expected instanceof JSONArray) {
			JSONArray expJ = (JSONArray) expected;
			JSONArray actualJ = (JSONArray) actual;
			if (expJ.length() != actualJ.length()) {
				return false;
			}
			for (int i = 0; i < expJ.length(); i++) {
				int j;
				for (j = 0; j < actualJ.length(); j++) {
					if (compareJSON(expJ.get(i), actualJ.get(j))) {
						break;
					}
				}
				if (j == actualJ.length()) {
					return false;
				}
				actualJ.remove(j);
			}
		} else if (!expected.toString().equals(actual.toString())) {
			return false;
		}

		return true;
	}

	public static boolean getIsActive(JSONObject json, String attributePath, String value) {

		String attributeArray[] = splitString(attributePath);
		String keyActive = "isActive";
		Set<String> keys = new HashSet<String>(json.keySet());
		for (String key : keys) {
			if (key.toLowerCase().contains(keyActive)) {
				keyActive = key;
				break;
			}
		}
		Boolean isActive = new Boolean(json.get(keyActive).toString());
		return checkActive(json, attributeArray, value, 0, isActive, keyActive);
	}

	public static boolean findAttribute(Object obj, Map<String, String> value) {
		if (validateValue(value, obj)) {
			return true;
		}
		if (obj instanceof JSONObject) {
			JSONObject jsonObject = (JSONObject) obj;
			for (Object key : jsonObject.keySet()) {
				if (findAttribute(jsonObject.get(key.toString()), value)) {
					return true;
				}
			}
		} else if (obj instanceof JSONArray) {
			JSONArray jsonArray = (JSONArray) obj;
			for (int i = 0; i < jsonArray.length(); i++) {
				if (findAttribute(jsonArray.get(i), value)) {
					return true;
				}
			}
		}

		return false;
	}

	public static boolean checkAttributeEquality(JSONObject json, String attributePath,
			List<Map<String, Object>> value) {

		String attributeArray[] = splitString(attributePath);
		return checkAttribute(json, attributeArray, value, 0);
	}

	@Deprecated
	@SuppressWarnings("unchecked")
	public static boolean compareObjects(Object expectedValue, Object actualValue) {
		if (actualValue instanceof JSONArray) {
			JSONArray actualValueArray = (JSONArray) actualValue;
			if (expectedValue instanceof JSONArray) {
				for (int i = 0; i < actualValueArray.length(); i++) {
					if (compareObjects(expectedValue, actualValueArray.get(i))) {
						return true;
					}
				}
				return false;

			} else if (expectedValue instanceof List) {
				List<Map<String, Object>> expectedValueList = (List<Map<String, Object>>) expectedValue;
				for (Map<String, Object> expectedValueMap : expectedValueList) {
					if (!compareObjects(expectedValueMap, actualValueArray)) {
						return false;
					}
				}
				return true;
			}
		} else if (actualValue instanceof JSONObject) {
			JSONObject actualValueObject = (JSONObject) actualValue;
			if (expectedValue instanceof Map) {
				Map<String, Object> expectedValurMap = (Map<String, Object>) expectedValue;
				for (String key : expectedValurMap.keySet()) {
					if (!actualValueObject.has(key)
							|| !compareObjects(expectedValurMap.get(key), actualValueObject.get(key))) {
						return false;
					}
				}
				return true;
			}
		} else {
			return actualValue.toString().equals(expectedValue.toString());
		}
		return false;
	}

	private static boolean checkActive(Object obj, String[] attributes, String value, int index, boolean isActive,
			String keyActive) {

		if (attributes.length == index) {
			if (attributes.length == 0 || value.equals(obj.toString())) {
				return isActive;

			} else {
				throw new IllegalArgumentException();
			}
		}

		if (obj instanceof JSONObject) {
			JSONObject jsonObject = (JSONObject) obj;
			try {
				if (jsonObject.has(keyActive)) {
					isActive = jsonObject.getBoolean(keyActive);
				}

				if (jsonObject.has(attributes[index])) {
					return checkActive(jsonObject.get(attributes[index]), attributes, value, index + 1, isActive,
							keyActive);
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}

		if (obj instanceof JSONArray) {
			JSONArray jsonArray = (JSONArray) obj;
			for (int i = 0; i < jsonArray.length(); i++) {
				try {
					return checkActive(jsonArray.get(i), attributes, value, index, isActive, keyActive);
				} catch (IllegalArgumentException e) {
					e.getMessage();
				}
			}
		}
		throw new IllegalArgumentException();
	}

	public static String[] splitString(String string) {
		String split[] = null;
		if (string == null || string.length() == 0) {
			split = new String[] {};
		} else {
			split = string.split("\\.");
		}

		return split;
	}

	private static boolean checkAttribute(Object obj, String[] attributres, Object value, int index) {
		if (attributres.length == index) {
			return validateValue(value, obj);
		}

		if (obj instanceof JSONObject) {
			JSONObject jsonObject = (JSONObject) obj;
			if (attributres[index].equals("*")) {
				for (Object key : jsonObject.keySet()) {
					if (checkAttribute(jsonObject.get(key.toString()), attributres, value, index + 1)) {
						return true;
					}
				}
			} else if (jsonObject.has(attributres[index])) {
				if (checkAttribute(jsonObject.get(attributres[index]), attributres, value, index + 1)) {
					return true;
				}
			}
		}

		if (obj instanceof JSONArray) {
			JSONArray jsonArray = (JSONArray) obj;
			for (int i = 0; i < jsonArray.length(); i++) {
				if (checkAttribute(jsonArray.get(i), attributres, value, index)) {
					return true;
				}
			}
		}

		return false;
	}

	@SuppressWarnings("unchecked")
	private static boolean validateValue(Object expectedValue, Object actualValue) {
		if (actualValue instanceof JSONArray) {
			JSONArray actualValueArray = (JSONArray) actualValue;
			if (expectedValue instanceof Map) {
				for (int i = 0; i < actualValueArray.length(); i++) {
					if (validateValue(expectedValue, actualValueArray.get(i))) {
						return true;
					}
				}
				return false;
			} else if (expectedValue instanceof List) {
				List<Map<String, Object>> expectedValueList = (List<Map<String, Object>>) expectedValue;
				for (Map<String, Object> expectedValueMap : expectedValueList) {
					if (!validateValue(expectedValueMap, actualValueArray)) {
						return false;
					}
				}
				return true;
			}
		} else if (actualValue instanceof JSONObject) {
			JSONObject actualValueObject = (JSONObject) actualValue;
			if (expectedValue instanceof Map) {
				Map<String, Object> expectedValueMap = (Map<String, Object>) expectedValue;
				for (String key : expectedValueMap.keySet()) {
					if (!actualValueObject.has(key)
							|| !validateValue(expectedValueMap.get(key), actualValueObject.get(key))) {
						return false;
					}
				}
				return true;
			}
		} else {
			if (expectedValue instanceof Set) {
				Set<String> expectedValueSet = (Set<String>) expectedValue;
				return expectedValueSet.contains(actualValue.toString());
			} else {
				return actualValue.toString().equals(expectedValue.toString());
			}
		}
		return false;
	}

	public static List<JSONObject> getArrayElementsFromLargeJSON(String jsonFileName, String lookUpKey,
			String lookUpValue) throws Exception {
		JsonFactory jsonFactory = new JsonFactory();
		ObjectMapper objMapper;
		File jsnFile = new File(jsonFileName);
		boolean foundObject = false;
		ObjectNode newObjectNode = null;
		JSONObject jsonObj = null;
		int foundobjectCount = 0;
		List<JSONObject> jsnObjects = new ArrayList<>();
		try {
			com.fasterxml.jackson.core.JsonParser parser = jsonFactory.createParser(jsnFile);
			while (parser.nextToken() != com.fasterxml.jackson.core.JsonToken.END_ARRAY) {

				com.fasterxml.jackson.core.JsonToken currentToken = parser.getCurrentToken();
				String currentFieldName = parser.getCurrentName();

				if (currentToken == com.fasterxml.jackson.core.JsonToken.START_OBJECT) {
					objMapper = new ObjectMapper();
					newObjectNode = objMapper.createObjectNode(); // create new
																	// node

				} else if (currentToken == com.fasterxml.jackson.core.JsonToken.END_OBJECT) {
					if (foundObject) {
						jsonObj = new JSONObject(newObjectNode.toString());
						jsnObjects.add(jsonObj);
						foundObject = false;
						foundobjectCount++;
					}
				} else {
					if (currentFieldName != null) {
						parser.nextToken(); // move to next foeld value
						String currentFieldValue = parser.getText();
						if (currentFieldValue != null) {
							newObjectNode.put(currentFieldName, currentFieldValue);
						}
						// check if it matches with lokUp key and value
						if (lookUpKey.equalsIgnoreCase(currentFieldName)
								&& lookUpValue.equalsIgnoreCase(currentFieldValue)) {
							foundObject = true;
						}
					}
				}

			}
			if (!foundObject && foundobjectCount == 0) {
				LOGGER.error(String.format("did not find json with lookUp %s and vale : %s", lookUpKey, lookUpValue));
				return null;
			}

		} catch (Exception e) {
			e.getMessage();
		}

		return jsnObjects;
	}

}
