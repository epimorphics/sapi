/******************************************************************
 * File:        JSONExplicitDescription.java
 * Created by:  Dave Reynolds
 * Created on:  12 Jan 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core.impl;

import com.epimorphics.rdfutil.RDFUtil;
import com.epimorphics.simpleAPI.core.JSONMap;
import com.epimorphics.simpleAPI.core.JSONNodeDescription;
import com.epimorphics.util.NameUtils;

/**
 * Implementation of JSONNodeDescription for use by explicitly
 * specified mappings.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class JSONMapEntry extends JSONDefaultDescription implements JSONNodeDescription {
    protected String jsonname;
    protected String property;
    protected boolean optional = false;
    protected String typeURI;
    protected String comment;
    protected JSONMap nested = null;
    protected JSONNodeDescription parent = null;
    
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
    
    public void setNested(JSONMap nested) {
        this.nested = nested;
    }
    
    public void setParent(JSONNodeDescription parent) {
        this.parent = parent;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }
    
    public void setType(String typeURI) {
        this.typeURI = typeURI;
    }
    
    public String getJsonName() {
        return jsonname;
    }

    public String getProperty() {
        return property;
    }
    
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public boolean isChild() {
        return parent != null;
    }

    @Override
    public boolean isParent() {
        return nested != null;
    }

    @Override
    public JSONNodeDescription getParent() {
        return parent;
    }

    @Override
    public JSONMap getNestedMap() {
        return nested;
    }

    @Override
    public String getType() {
        return typeURI;
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
