/******************************************************************
 * File:        WJSONObject.java
 * Created by:  Dave Reynolds
 * Created on:  1 Nov 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.results.wappers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.Resource;

import com.epimorphics.rdfutil.RDFUtil;

import static com.epimorphics.simpleAPI.writers.JsonWriterUtil.*;

/**
 * A wrapped version of a JSON Object generated from an API Result.
 * Provides a representation and helper methods to support scripted HTML rendering e.g. via Velocity.
 */
public class WJSONObject {
    protected Map<String, Object> properties = new HashMap<>();
    
    public WJSONObject() {
    }
    
    public WJSONObject(Resource resource) {
        put(ID_FIELD, resource.getURI());
    }
    
    public Object get(String key) {
        return properties.get(key);
    }
    
    public void put(String key, Object value) {
        properties.put(key, value);
    }
    
    public boolean isObject() {
        return true;
    }
    
    public boolean isArray() {
        return false;
    }
    
    public boolean isResource() {
        return true;
    }
    
    public boolean isAnon() {
        return getURI() == null;
    }
    
    public String getURI() {
        Object id = get(ID_FIELD);
        return id == null ? null : id.toString();
    }
    
    public Object getFirst(String key) {
        Object value = get(key);
        if (value instanceof WJSONArray) {
            return ((WJSONArray)value).get(0);
        } else {
            return value;
        }
    }
    
    public boolean isLangString() {
        return false;
    }
    
    public boolean isTypedLiteral() {
        return false;
    }
    
    /**
     * Return all the field names in the object, in sorted order
     */
    public List<String> listProperties() {
        List<String> keys = new ArrayList<>(properties.keySet());
        keys.remove(ID_FIELD);
        Collections.sort(keys);
        return keys;
    }

    /**
     * Return all the field names in tree starting from this object,
     * using "p.q.r" notation for the paths
     */
    public List<String> getTreePaths() {
        List<String> paths = new ArrayList<>();
        descendTree(this, paths, "");
        return paths;
    }

    protected void descendTree(WJSONObject object, List<String> paths, String prefix) {
        for (String key : object.listProperties()) {
            String path = prefix.isEmpty() ? key : prefix + "." + key;
            paths.add( path );
            Object value = object.get(key);
            if (value instanceof WJSONObject) {
                descendTree((WJSONObject)value, paths, path);
            }
        }
    }
    
    /**
     * Return the value indicated by a tree path
     */
    public Object getFromPath(String path) {
        int index = path.indexOf(".");
        if (index != -1) {
            String p = path.substring(0, index);
            String rest = path.substring(index+1);
            Object value = get(p);
            if (value instanceof WJSONObject) {
                return ((WJSONObject)value).getFromPath(rest);
            } else {
                return null;
            }
        } else {
            return get(path);
        }
    }
    
    public Object getLabel() {
        Object label = get(LABEL_FIELD);
        if (label == null) {
           String uri = getURI();
           if (uri != null) {
               label = RDFUtil.getLocalname(uri);
           }
        }
        if (label == null) {
            label = "[]";
        }
        return label;
    }
    
    public String getLocalName() {
        String uri = getURI();
        if (uri != null) {
            return RDFUtil.getLocalname( uri );
        } 
        return null;
    }
    
    public Object getName() {
        return getLabel();
    }
}
