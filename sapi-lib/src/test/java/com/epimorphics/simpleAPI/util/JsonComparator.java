/******************************************************************
 * File:        JsonComparator.java
 * Created by:  Dave Reynolds
 * Created on:  15 Dec 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.util;

import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;

/**
 * Support for comparing two JSON results with order independence in arrays.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class JsonComparator {

    public static boolean equal(JsonValue expected, JsonValue actual) {
        if (expected.isObject()) {
            if (!actual.isObject()) {
                return false;
            } else {
                return equal(expected.getAsObject(), actual.getAsObject());
            }
        } else if (expected.isArray()) {
            if (!actual.isArray()) {
                return false;
            } else {
                return equal(expected.getAsArray(), actual.getAsArray());
            }
        } else {
            return expected.equals(actual);
        }
    }

    public static boolean equal(JsonObject expected, JsonObject actual) {
        if (expected.entrySet().size() != actual.entrySet().size()) return false;
        for (Entry<String, JsonValue> entry : expected.entrySet()) {
            String key = entry.getKey();
            if (!equal(entry.getValue(), actual.get(key))) return false;
        }
        return true;
    }

    public static boolean equal(JsonArray expected, JsonArray actual) {
        if (expected.size() != actual.size()) return false;
        for (Iterator<JsonValue> i = expected.iterator(); i.hasNext();) {
            JsonValue e = i.next();
            boolean found = false;
            for (Iterator<JsonValue> j = actual.iterator(); j.hasNext();) {
                JsonValue a = j.next();
                if (equal(e, a)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }
}
