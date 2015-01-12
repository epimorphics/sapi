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
import com.epimorphics.simpleAPI.core.JSONOldMap;
import com.epimorphics.simpleAPI.core.JSONNodePolicy;
import com.hp.hpl.jena.rdf.model.Property;

/**
 * JSON map where the properties to be serialized are explicitly enumerated,
 * e.g. via a json/yaml specification.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class JSONOldExplicitMap extends JSONOldPlainMap implements JSONOldMap {
    protected List<JSONOldMapEntry> mapping = new ArrayList<JSONOldMapEntry>();
    protected List<String> keys;
    protected Map<String, JSONOldMapEntry> entries;
    
    public JSONOldExplicitMap(API api) {
        super(api);
    }

    public List<JSONOldMapEntry> getMapping() {
        return mapping;
    }

    public void setMapping(List<JSONOldMapEntry> mapping) {
        this.mapping = mapping;
        keys = null;
        entries = null;
    }
    
    public void addMapping(JSONOldMapEntry entry) {
        mapping.add( entry );
        keys = null;
        entries = null;
    }
    
    protected void init() {
        if (keys == null) {
            keys = new ArrayList<String>( mapping.size() );
            entries = new HashMap<String, JSONOldMapEntry>( mapping.size() );
            for (JSONOldMapEntry entry : mapping) {
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
        for (JSONOldMapEntry entry : mapping) {
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
        for (JSONOldMapEntry entry : mapping) {
            if (entry.isNested() && !entry.isOptional()) {
                JSONOldExplicitMap nested = (JSONOldExplicitMap)entry.getNestedMap();
                nested.renderAsQuery(buf, entry.getJsonName());
            }
        }
        buf.append("    #$FILTER$\n");
        buf.append("}\n");
        buf.append("    #$MODIFIER$\n");
        return buf.toString();        
    }
    
    protected void renderAsQuery(StringBuffer buf, String var) {
        boolean started = false;
        for (JSONOldMapEntry map : mapping) {
            if (!map.isOptional()) {
                if (!started){
                    started = true;
                    buf.append("    ?" + var + "\n");                    
                }
                buf.append("        " + map.asQueryRow() + " ;\n");
            }
        }
        if (started) buf.append("    .\n");
        for (JSONOldMapEntry map : mapping) {
            if (map.isOptional()) {
                if (map.isNested()) {
                    buf.append("    OPTIONAL {?" + var + " " + map.asQueryRow() + " .\n" );
                    JSONOldExplicitMap nested = (JSONOldExplicitMap) map.getNestedMap();
                    nested.renderAsQuery(buf, map.getJsonName());
                    buf.append("    }\n" );
                    
                } else {
                    buf.append("    OPTIONAL {?" + var + " " + map.asQueryRow() + " .}\n" );
                }
            }
        }
    }
}
