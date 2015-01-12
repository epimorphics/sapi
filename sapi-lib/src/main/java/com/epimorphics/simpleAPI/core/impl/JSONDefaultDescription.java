/******************************************************************
 * File:        JSONDefaultDescription.java
 * Created by:  Dave Reynolds
 * Created on:  12 Jan 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core.impl;

import com.epimorphics.simpleAPI.core.JSONMap;
import com.epimorphics.simpleAPI.core.JSONNodeDescription;

/**
 * Implementation of JSONNodeDescription suitable for use as a generic
 * fall back default in cases where there is no explicit mapping definition
 * for the value being rendered.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class JSONDefaultDescription implements JSONNodeDescription {
    protected boolean showLang = true;
    protected boolean multivalued = false;
    protected boolean filterable = true;
    
    public JSONDefaultDescription() {
    }
    
    public void setShowLang(boolean showLang) {
        this.showLang = showLang;
    }

    public void setMultivalued(boolean multivalued) {
        this.multivalued = multivalued;
    }
    
    public void setFilterable(boolean filterable) {
        this.filterable = filterable;
    }

    @Override
    public boolean isMultivalued() {
        return multivalued;
    }

    @Override
    public boolean showLangTag(String lang) {
        return showLang;
    }

    @Override
    public boolean isChild() {
        return false;
    }

    @Override
    public JSONNodeDescription getParent() {
        return null;
    }

    @Override
    public JSONMap getNestedMap() {
        return null;
    }

    @Override
    public boolean isFilterable() {
        return filterable;
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public boolean isParent() {
        return false;
    }

}
