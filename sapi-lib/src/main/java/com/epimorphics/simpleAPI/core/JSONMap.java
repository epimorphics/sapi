/******************************************************************
 * File:        JSONMap.java
 * Created by:  Dave Reynolds
 * Created on:  12 Jan 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.epimorphics.simpleAPI.core.impl.JSONDefaultDescription;
import com.epimorphics.simpleAPI.core.impl.JSONMapEntry;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.shared.PrefixMapping;

/**
 * Represents a mapping from SELECT variable values to a structured JSON representation.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class JSONMap {
    protected API api;
    protected JSONNodeDescription defaultDescription = new JSONDefaultDescription();
    protected List<JSONMapEntry> mapping = new ArrayList<>();
    protected Map<String, JSONMapEntry> entries;
    
    public JSONMap(API api) {
        this.api = api;
    }

    public List<JSONMapEntry> getMapping() {
        return mapping;
    }

    public void setMapping(List<JSONMapEntry> mapping) {
        this.mapping = mapping;
        entries = null;
    }
    
    public void addMapping(JSONMapEntry entry) {
        mapping.add( entry );
        entries = null;
    }
    
    protected void init() {
        if (entries == null) {
            entries = new HashMap<String, JSONMapEntry>( );
            initFromMap(this);
        }
    }
    
    protected void initFromMap(JSONMap jmap) {
        for (JSONMapEntry entry : jmap.getMapping()) {
            entries.put( entry.getJsonName(), entry );
            if (entry.isParent()) {
                initFromMap(entry.getNestedMap());
            }
        }
    }
    
    public JSONNodeDescription getEntry(String key) {
        init();
        if (entries != null) {
            JSONNodeDescription desc = entries.get(key);
            if (desc != null) {
                return desc;
            }
        }
        return defaultDescription;
    }
    
    public String keyFor(Property property) {
        // TODO cache expanding property URIs? Not clear this is really needed anyway, so defer.
        PrefixMapping pm = api.getApp().getPrefixes();
        for (JSONMapEntry entry : mapping) {
            String puri = pm.expandPrefix(entry.getProperty());
            if (puri.equals(property.getURI())) {
                return entry.getJsonName();
            }
        }
        return api.shortnameFor(property);
    }
    
    public String asQuery(String baseQuery) {
        StringBuffer buf = new StringBuffer();
        buf.append("SELECT * WHERE {\n");
        buf.append("    " + baseQuery + "\n");
        renderAsQuery(buf, "id");
        for (JSONMapEntry entry : mapping) {
            if (entry.isParent() && !entry.isOptional()) {
                JSONMap nested = entry.getNestedMap();
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
                if (map.isParent()) {
                    buf.append("    OPTIONAL {?" + var + " " + map.asQueryRow() + " .\n" );
                    JSONMap nested = map.getNestedMap();
                    nested.renderAsQuery(buf, map.getJsonName());
                    buf.append("    }\n" );
                    
                } else {
                    buf.append("    OPTIONAL {?" + var + " " + map.asQueryRow() + " .}\n" );
                }
            }
        }
    }
    
}
