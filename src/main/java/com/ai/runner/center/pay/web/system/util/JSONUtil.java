package com.ai.runner.center.pay.web.system.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public final class JSONUtil {
    private static final Logger LOG = LogManager.getLogger(XMLUtil.class);
    
    private JSONUtil() {
        
    }
    /**
     * json转换成map
     * @param json
     * @return
     * @author LiangMeng
     * @ApiDocMethod
     */
    public static Map<String,Object> parseToMap(JSONObject json){
        Iterator it = json.keys(); 
        Map<String,Object> map = new HashMap<String, Object>();
        while (it.hasNext())  
        {  
            String key = String.valueOf(it.next());  
            String value = (String) json.get(key);  
            map.put(key, value);  
        }  
        return map;
    }
}
