/******************************************************************
 * File:        ViewEntry.java
 * Created by:  Dave Reynolds
 * Created on:  27 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.views;

import com.epimorphics.rdfutil.RDFUtil;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.util.NameUtils;

/**
 * Represents view information for a single property within the context of some ViewMap
 */
public class ViewEntry {
    protected String jsonname;
    protected String property;
    protected boolean optional = false;
    protected boolean multivalued = false;
    protected boolean filterable = true;
    protected boolean showLang = API.getShowLangTag();
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
    
    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public boolean isMultivalued() {
        return multivalued;
    }

    public void setMultivalued(boolean multivalued) {
        this.multivalued = multivalued;
    }

    public boolean isFilterable() {
        return filterable;
    }

    public void setFilterable(boolean filterable) {
        this.filterable = filterable;
    }

    public boolean isShowLang() {
        return showLang;
    }

    public void setShowLang(boolean showLang) {
        this.showLang = showLang;
    }

    public String getTypeURI() {
        return typeURI;
    }

    public void setTypeURI(String typeURI) {
        this.typeURI = typeURI;
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

    public void setNested(ViewTree nested) {
        this.nested = nested;
    }
    
    public String getJsonName() {
        return jsonname;
    }

    public String asQueryRow() {
        if (property.startsWith("http:") || property.startsWith("https:")) {
            return "<" + property + "> ?" + jsonname;
        } else {
            return property + " ?" + jsonname;
        }
    }
    
    @Override
    public String toString() {
        return print(new StringBuffer(), "").toString();
    }
    
    protected StringBuffer print(StringBuffer buf, String indent) {
        buf.append(indent);
        buf.append(jsonname);
        buf.append("(" + property + ")");
        if (filterable) buf.append(" filterable");
        if (optional) buf.append(" optional");
        if (multivalued) buf.append(" multi");
        if (showLang) buf.append(" showLang");
        if (comment != null) buf.append(" '" + comment + "'");
        if (isNested()) {
            buf.append("\n");
            nested.print(buf, indent + "  ");
        }
        return buf;
    }
    
}
