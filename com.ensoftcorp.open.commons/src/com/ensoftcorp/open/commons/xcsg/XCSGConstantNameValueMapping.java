package com.ensoftcorp.open.commons.xcsg;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.log.Log;

public class XCSGConstantNameValueMapping {

	private static Map<String,String> xcsgConstantNameToValueMap = getConstantNameValueMap();
	
	/**
	 * Reflectively recovers the XCSG constant names and values
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private static Map<String, String> getConstantNameValueMap() {
		Map<String,String> result = new HashMap<String,String>();
		// need to use reflection on XCSG class and its sub interfaces to get constant names to their raw value mapping
		// example: "XCSG.ControlFlow_Node" maps to "XCSG.ControlFlow (Node)"
		Class[] classes = new Class[]{XCSG.class, XCSG.Provisional.class, XCSG.C.Provisional.class, XCSG.Java.class, XCSG.Jimple.class, XCSG.C.class, XCSG.CPP.class};
		for(Class c : classes){
			Field[] declaredFields = XCSG.class.getDeclaredFields();
			for (Field field : declaredFields) {
			    if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) && java.lang.reflect.Modifier.isPublic(field.getModifiers())) {
			    	if(field.getType() == String.class){
			    		try {
			    			String value = (String) field.get(null);
			    			String prefix = "";
			    			if(c == XCSG.class){
			    				prefix = "XCSG.";
			    			} else {
			    				prefix = "XCSG." + c.getSimpleName() + ".";
			    			}
				    		result.put(prefix + field.getName(), value);
			    		} catch (Exception e){
			    			Log.warning("Unable to parse XCSG value for field: " + field.toGenericString());
			    		}
			    	}
			    }
			}
		}
		
		return result;
	}
	
	/**
	 * Returns a copy of the XCSG constant name to value mapping
	 * Example: "XCSG.ControlFlow_Node" maps to "XCSG.ControlFlow (Node)"
	 * @return
	 */
	public static Map<String,String> getXCSGConstantNameToValueMap(){
		HashMap<String,String> mapping = new HashMap<String,String>();
		mapping.putAll(xcsgConstantNameToValueMap);
		return mapping;
	}
}
