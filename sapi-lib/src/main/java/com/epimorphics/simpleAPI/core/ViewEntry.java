/******************************************************************
 * File:        ViewEntry.java
 * Created by:  Dave Reynolds
 * Created on:  27 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core;

import com.epimorphics.rdfutil.RDFUtil;
import com.epimorphics.util.NameUtils;

/**
 * Represents view information for a single property within the context of some ViewMap
 */
public class ViewEntry {
    protected String jsonname;
    protected String property;
    protected boolean optional = false;
    protected String typeURI;
    protected String comment;
    protected ViewTree nested = null;
    
    public ViewEntry(String jsonname, String property) {
        this.jsonname = jsonname == null ? makeJsonName(property) : jsonname;
        this.property = property;
    }
    
    public ViewEntry(String property) {
        this.jsonname = makeJsonName(property);
        this.property = property;
    }

    static String makeJsonName(String property) {
        String name = RDFUtil.getLocalname(property);
        return name.isEmpty() ?  NameUtils.splitAfterLast(property, ":") : name;
    }
    
    public void setNested(ViewTree nested) {
        this.nested = nested;
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

    public boolean isNested() {
        return nested != null;
    }
    
    public ViewTree getNested() {
        return nested;
    }

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
