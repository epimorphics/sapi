/******************************************************************
 * File:        JSONMapEntry.java
 * Created by:  Dave Reynolds
 * Created on:  5 Jan 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core;

import java.util.ArrayList;
import java.util.List;

import com.epimorphics.rdfutil.RDFUtil;
import com.epimorphics.util.NameUtils;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Specifies one entry in an RDF to JSON mapping.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class JSONMapEntry {
    protected String jsonName;
    protected String property;
    protected boolean optional = false;
    protected boolean multivalued = false;    
    protected List<JSONMapEntry> nested = null;

    public JSONMapEntry(String jsonName, String property) {
        this.jsonName = jsonName;
        this.property = property;
    }
        
    public JSONMapEntry(String property) {
        this(makeJsonName(property), property);
    }
    
    public JSONMapEntry(Resource property) {
        jsonName = RDFUtil.getLocalname(property);
        this.property = property.getURI();
    }
    
    static String makeJsonName(String property) {
        String name = RDFUtil.getLocalname(property);
        return name.isEmpty() ?  NameUtils.splitAfterLast(property, ":") : name;
    }
    
    public void addNested(JSONMapEntry entry) {
        if (nested == null) {
            nested = new ArrayList<JSONMapEntry>();
        }
        nested.add(entry);
    }

    public String getJsonName() {
        return jsonName;
    }

    public String getProperty() {
        return property;
    }

    public boolean isOptional() {
        return optional;
    }
    
    
    public boolean isMultivalued() {
        return multivalued;
    }

    public void setMultivalued(boolean multivalued) {
        this.multivalued = multivalued;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public void setNested( List<JSONMapEntry> entries ) {
        this.nested = entries;
    }
    
    public List<JSONMapEntry> getNested() {
        return nested;
    }
    
    public boolean hasNested() {
        return nested != null && ! nested.isEmpty();
    }
    
    public String asQueryRow() {
        if (property.startsWith("http:") || property.startsWith("https:")) {
            return "<" + property + "> ?" + jsonName;
        } else {
            return property + " ?" + jsonName;
        }
    }
 
}
