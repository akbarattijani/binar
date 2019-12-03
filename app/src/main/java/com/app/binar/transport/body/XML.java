package com.app.binar.transport.body;

import java.util.HashMap;
import java.util.Map;

/**
 * @author AKBAR <akbar.attijani@gmail.com>
 */

public class XML {
    private Map<String, Object> params;

    public XML() {
        params = new HashMap<>();
    }

    public XML add(String key, String value) {
        params.put(key, value);
        return this;
    }

    public XML add(String key, XML value) {
        params.put(key, value);
        return this;
    }

    public XML add(String key, int value) {
        params.put(key, value);
        return this;
    }

    public XML add(String key, float value) {
        params.put(key, value);
        return this;
    }

    public XML add(String key, boolean value) {
        params.put(key, value);
        return this;
    }

    public XML add(String key, double value) {
        params.put(key, value);
        return this;
    }

    public XML add(String key, char value) {
        params.put(key, value);
        return this;
    }

    public Map<String, Object> getParams() {
        return params;
    }
}
