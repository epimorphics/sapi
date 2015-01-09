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
                keys.add( entry.getJsonName() );
                entries.put( entry.getJsonName(), entry );
            }
        }
    }
    
    @Override
    public JSONNodePolicy policyFor(String key) {
        init();
        if (entries != null) {
            JSONNodePolicy policy = entries.get(key);
            if (policy != null) {
                return policy;
            }
        }
        return defaultPolicy;
    }
    
    @Override
    public String keyFor(Property property) {
        for (JSONMapEntry entry : mapping) {
            // TODO this isn't really needed but won't work unless we expand the qnames in the map entries
            if (entry.getProperty().equals(property.getURI())) {
                return entry.getJsonName();
            }
        }
        return super.keyFor(property);
    }

    @Override
    public List<String> listKeys() {
        init();
        return keys;
    }
    
    public String asQuery(String baseQuery) {
        StringBuffer buf = new StringBuffer();
        buf.append("SELECT * WHERE {\n");
        buf.append("    " + baseQuery + "\n");
        renderAsQuery(buf, "id");
        for (JSONMapEntry entry : mapping) {
            if (entry.isNested() && entry.isOptional()) {
                JSONExplicitMap nested = (JSONExplicitMap)entry.getNestedMap();
                nested.renderAsQuery(buf, entry.getJsonName());
            }
        }
        // TODO hook for filters
        buf.append("}\n");
        return buf.toString();        
    }
    
    protected void renderAsQuery(StringBuffer buf, String var) {
        boolean started = false;
        for (JSONMapEntry map : mapping) {
            if (!map.isOptional()) {
                if (!started){
                    started = true;
                    buf.append("    ?" + var + "\n");                    
                }
                buf.append("        " + map.asQueryRow() + " ;\n");
            }
        }
        if (started) buf.append("    .\n");
        for (JSONMapEntry map : mapping) {
            if (map.isOptional()) {
                if (map.isNested()) {
                    buf.append("    OPTIONAL {?" + var + " " + map.asQueryRow() + " .\n" );
                    JSONExplicitMap nested = (JSONExplicitMap) map.getNestedMap();
                    nested.renderAsQuery(buf, map.getJsonName());
                    buf.append("    }\n" );
                    
                } else {
                    buf.append("    OPTIONAL {?" + var + " " + map.asQueryRow() + " .}\n" );
                }
            }
        }
    }
}
