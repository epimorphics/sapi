/******************************************************************
 * File:        JSONMapEntry.java
 * Created by:  Dave Reynolds
 * Created on:  6 Jan 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core.impl;

import com.epimorphics.rdfutil.RDFUtil;
import com.epimorphics.simpleAPI.core.JSONMap;
import com.epimorphics.simpleAPI.core.JSONNodePolicy;
import com.epimorphics.util.NameUtils;

/**
 * Specifies the handling of a single property within an explicit json map.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class JSONMapEntry implements JSONNodePolicy {
    protected boolean multivalued = false;
    protected boolean optional = false;
    protected boolean showLangTag = true;
    protected JSONMap nested = null;
    protected String jsonname;
    protected String property;
    
    public JSONMapEntry(String jsonname, String property) {
        this.jsonname = jsonname == null ? makeJsonName(property) : jsonname;
        this.property = property;
    }
    
    public JSONMapEntry(String property) {
        this.jsonname = makeJsonName(property);
        this.property = property;
    }

    static String makeJsonName(String property) {
        String name = RDFUtil.getLocalname(property);
        return name.isEmpty() ?  NameUtils.splitAfterLast(property, ":") : name;
    }
    
    public String getJsonName() {
        return jsonname;
    }

    public String getProperty() {
        return property;
    }

    public void setMultivalued(boolean multi) {
        this.multivalued = multi;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public void setShowLangTag(boolean showLangTag) {
        this.showLangTag = showLangTag;
    }

    public void setNested(JSONMap nested) {
        this.nested = nested;
    }

    @Override
    public boolean isMultivalued() {
        return multivalued;
    }

    @Override
    public boolean showLangTag(String lang) {
        return showLangTag;
    }

    @Override
    public boolean isNested() {
        return nested != null;
    }

    @Override
    public JSONMap getNestedMap() {
        return nested;
    }
    
    public boolean isOptional() {
        return optional;
    }
    
    public String asQueryRow() {
        if (property.startsWith("http:") || property.startsWith("https:")) {
            return "<" + property + "> ?" + jsonname;
        } else {
            return property + " ?" + jsonname;
        }
    }
}
