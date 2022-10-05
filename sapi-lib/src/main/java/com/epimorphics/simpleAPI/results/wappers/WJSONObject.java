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
import java.util.Set;

import org.apache.jena.rdf.model.Resource;

import com.epimorphics.rdfutil.RDFUtil;
import com.epimorphics.simpleAPI.core.API;

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
    
    public boolean hasKey(String key) {
        return properties.containsKey(key);
    }
    
    public void put(String key, Object value) {
    	if(value!=null)
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
    
    public boolean hasValue(String key, Object expected) {
        Object value = properties.get(key);
        if (value instanceof WJSONArray) {
            ((WJSONArray)value).hasValue(expected);
        }
        return expected.equals(value);
    }

    public boolean hasResourceValue(String key, String expected) {
        expected = API.get().getPrefixes().expandPrefix(expected);
        Object value = properties.get(key);
        if (value instanceof WJSONObject) {
            return expected.equals( ((WJSONObject)value).getURI() );
        } else if (value instanceof WJSONArray) {
            return ((WJSONArray)value).hasResourceValue(expected);
        }
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
            } else if (value instanceof WJSONArray) {
                return ((WJSONArray)value).getFromPath(rest);
            } else {
                return null;
            }
        } else {
            return get(path);
        }
    }
    
    /**
     * Return flattened string representation of the values(s) from a tree path
     */
    public String getStringFromPath(String path) {
        Object value = getFromPath(path);
        if (value instanceof Set<?>) {
            Set<?> values = (Set<?>)value;
            List<String> strings = new ArrayList<>( values.size() );
            for ( Object v : values ) {
                strings.add( v.toString() );
            }
            Collections.sort(strings);
            StringBuffer flat = new StringBuffer();
            boolean started = false;
            for (String vs : strings) {
                if (started) {
                    flat.append(", ");
                } else {
                    started = true;
                }
                flat.append( vs );
            }
            return flat.toString();
        } else if (value != null) {
            return value.toString();
        } else {
            return null;
        }
    }
    
    public Object getLabel() {
        Object label = null;
        for (String lf : LABEL_FIELDS) {
            label = get(lf);
            if (label != null) return label;
        }
        String uri = getURI();
        if (uri != null) {
            label = RDFUtil.getLocalname(uri);
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
    
    @Override
    public boolean equals(Object other) {
        return other instanceof WJSONObject && this.properties.equals(((WJSONObject)other).properties);
    }
    
    @Override
    public int hashCode() {
        return properties.hashCode();
    }
    
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        boolean started = false;
        for (String key : properties.keySet()) {
            if (started) {
                buf.append(", ");
            } else {
                started = true;
            }
            buf.append(key);
            buf.append(": ");
            buf.append( properties.get(key).toString() );
        }
        buf.append("}");
        return buf.toString();
    }
}
