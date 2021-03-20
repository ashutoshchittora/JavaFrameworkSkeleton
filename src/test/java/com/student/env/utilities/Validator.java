package com.student.env.utilities;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import cucumber.api.DataTable;
import org.testng.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Validator {
	private static Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());

	public static void compareJsonObject(DataTable dataTable, JSONObject source, JSONObject target) {

		for (Map<String, String> row : dataTable.asMaps(String.class, String.class)) {
			String sExpected = JsonParser.getValue(source, row.get("SourceStudent"));
			String sActual = JsonParser.getValue(target, row.get("TargetStudent"));

			String sMsg = String.format("Values mismatched for %s, Expected -> %s, Actual -> %s",
					row.get("SourceStudent"), sExpected, sActual);
			assertEquals(sExpected, sActual, sMsg);
		}
	}

	public static void compareNestedJsonObjects(List<Map<String, String>> dataTable, JSONObject source,
			JSONObject target, boolean bSearchMultVal) throws Exception {

		for(Map<String,String> row : dataTable){
			List<String> expJsonKeyValList = getJSONValue(source,row.get("SourceStudent"),bSearchMultVal);
			String sExpected = "";
			String sActual = "";
			if(expJsonKeyValList.size()){
				sExpected = expJsonKeyValList.get(0);
			}else{
				throw new AssertionError("No matching source attribute key found"+row.get("SourceStudent")+" in source JSON object");
			}
			List<String>  actualJsonValList = getJSONValue(target,row.get("TargetStudent"), bSearchMultVal);
			if(actualJsonValList.size() > 0){
				sActual = actualJsonValList.get(0);				
			}else {
				throw new AssertionError("No matching target attribute key found"+row.get("SourceStudent")+" in target JSON object");
			}
			String sMsg = String.format("Values mismatched for %s, Expected -> %s, Actual -> %s",
					row.get("SourceStudent"), sExpected, sActual);
			assertEquals(sExpected, sActual, sMsg);
		
		}
		
		public static List<String> getJSONValue(JSONObject source , String jsonKey, boolean bMultVal) throws Exception {
			
			ObjectMapper oMapper = new ObjectMapper();
			JsonNode jsonNodes = oMapper.readTree(source.toString());
			
			List<String> jsonValues = new ArrayList<String>();
			if(bMultVal){
				List<JsonNodes> jsonKeyBValList = jsonNodes.findValues(jsonKey);
				for(JsonNode jN : jsonKeyBValList){
					jsonValues.add(jN.asText());
				}
			} else{
				JsonNode jsonKeyVal = jsonNodes.findValues(jsonKey);
				jsonValues.add(jsonNodes.asText());
			}
			
			return jsonValues;
		}	
		
	}

	public static void compareAndValidateJSON(JSONObject sourceObj, JSONObject targetObj, boolean bStrictVerify) {
		// using JsonAssert lib , one can identify the differecens at any nested
		// level of JSON content
		// skyscreamer.JsonAssert
		// set JSONcompareMode to Linnient and you can comapre json irrespective
		// of order it is in.
		if (!bStrictVerify) {
			JSONAssert.assertEquals(sourceObj, targetObj, JSONCompareMode.LENIENT);
		} else {
			JSONAssert.assertEquals(sourceObj, targetObj, JSONCompareMode.STRICT);
		}
		LOGGER.info("VALIDATION success ....");
	}

}
