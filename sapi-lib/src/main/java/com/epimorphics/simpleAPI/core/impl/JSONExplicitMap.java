/******************************************************************
 * File:        JSONExplicitMap.java
 * Created by:  Dave Reynolds
 * Created on:  6 Jan 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.core.JSONMap;
import com.epimorphics.simpleAPI.core.JSONNodePolicy;
import com.hp.hpl.jena.rdf.model.Property;

/**
 * JSON map where the properties to be serialized are explicitly enumerated,
 * e.g. via a json/yaml specification.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class JSONExplicitMap extends JSONPlainMap implements JSONMap {
    protected List<JSONMapEntry> mapping = new ArrayList<JSONMapEntry>();
    protected List<String> keys;
    protected Map<String, JSONMapEntry> entries;
    
    public JSONExplicitMap(API api) {
        super(api);
    }

    public List<JSONMapEntry> getMapping() {
        return mapping;
    }

    public void setMapping(List<JSONMapEntry> mapping) {
        this.mapping = mapping;
        keys = null;
        entries = null;
    }
    
    public void addMapping(JSONMapEntry entry) {
        mapping.add( entry );
        keys = null;
        entries = null;
    }
    
    protected void init() {
        if (keys == null) {
            keys = new ArrayList<String>( mapping.size() );
            entries = new HashMap<String, JSONMapEntry>( mapping.size() );
            for (JSONMapEntry entry : mapping) {
                keys.add( entry.getJsonname() );
                entries.put( entry.getJsonname(), entry );
            }
        }
    }
    
    @Override
    public JSONNodePolicy policyFor(String key) {
        init();
        return entries.get(key);
    }
    
    @Override
    public String keyFor(Property property) {
        for (JSONMapEntry entry : mapping) {
            // TODO this isn't really needed but won't work unless we expand the qnames in the map entries
            if (entry.getProperty().equals(property.getURI())) {
                return entry.getJsonname();
            }
        }
        return super.keyFor(property);
    }

    @Override
    public List<String> listKeys() {
        init();
        return keys;
    }
}
