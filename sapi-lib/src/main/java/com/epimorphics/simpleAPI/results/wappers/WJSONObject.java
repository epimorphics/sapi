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
public class WJSONObject extends HashMap<String, Object> implements Map<String, Object> {
    private static final long serialVersionUID = 2209592197821602773L;

    public WJSONObject() {
        super();
    }
    
    public WJSONObject(Resource resource) {
        super();
        put(ID_FIELD, resource.getURI());
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
    
    public String getURI() {
        return get(ID_FIELD).toString();
    }
    
    public boolean isLangString() {
        return false;
    }
    
    public boolean isTypedLiteral() {
        return false;
    }
    
    public List<String> getOrderedFields() {
        List<String> keys = new ArrayList<>(keySet());
        keys.remove(ID_FIELD);
        Collections.sort(keys);
        return keys;
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
    
    public Object getName() {
        return getLabel();
    }
}
