package com.example.frontend.utils;

import static java.lang.Math.min;

import org.json.JSONException;
import org.json.JSONObject;

public class KeyValue2Object {
    /**
     * convert key-value pair to JSONObject.
     * @param key: key arrays
     * @param value: value arrays
     * @return
     */
    public static JSONObject keyValue2Object(String[] key, String value[]) {
        JSONObject object = new JSONObject();
        try {
            int len_key = key.length, len_value = value.length;
            int len = min(len_key, len_value);
            for (int i = 0; i < len; ++i) {
                object.put(key[i], value[i]);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
//        return new JSONObject("{" + key + ": " + value + "}");
    }
}
