package com.uroria.fastconfig.internal.utils;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import lombok.experimental.UtilityClass;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@UtilityClass
public class JsonUtils {

    public Map<String, Object> jsonToMap(JSONObject jsonObject) throws JSONException {
        Map<String, Object> retMap = new Object2ObjectLinkedOpenHashMap<>();

        if (jsonObject != JSONObject.NULL) {
            retMap = toMap(jsonObject);
        }
        return retMap;
    }

    public JSONObject getJsonFromMap(Map<String, Object> map) throws JSONException {
        JSONObject jsonData = new JSONObject();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                value = getJsonFromMap((Map<String, Object>) value);
            }
            jsonData.put(key, value);
        }
        return jsonData;
    }

    public Map<String, Object> toMap(JSONObject jsonObject) throws JSONException {
        final Map<String, Object> map = new Object2ObjectLinkedOpenHashMap<>();

        Iterator<String> keysItr = jsonObject.keys();
        keysItr.forEachRemaining(key -> map.put(key, getValue(jsonObject.get(key))));
        return map;
    }

    public List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ObjectArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            list.add(getValue(array.get(i)));
        }
        return list;
    }

    private Object getValue(Object obj) {
        if (obj instanceof JSONArray) {
            return toList((JSONArray) obj);
        } else if (obj instanceof JSONObject) {
            return toMap((JSONObject) obj);
        } else {
            return obj;
        }
    }
}
