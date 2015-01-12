/******************************************************************
 * File:        JSONPlainMap.java
 * Created by:  Dave Reynolds
 * Created on:  6 Jan 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core.impl;

import java.util.List;

import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.core.JSONOldMap;
import com.epimorphics.simpleAPI.core.JSONNodePolicy;
import com.hp.hpl.jena.rdf.model.Property;

/**
 * JSONMap implementation that just uses default policy of serializing
 * properties using localnames (or any global mapping in the API). 
 * Defaults to allowing nesting and language tags. 
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class JSONOldPlainMap implements JSONOldMap {
    protected API api;
    protected DefaultPolicy defaultPolicy = new DefaultPolicy();
    
    public JSONOldPlainMap(API api) {
        this.api = api;
    }
    
    @Override
    public JSONNodePolicy policyFor(String key) {
        return defaultPolicy;
    }

    @Override
    public String keyFor(Property property) {
        return api.shortnameFor(property);
    }

    @Override
    public List<String> listKeys() {
        return null;
    }
    

    public void setAllowNesting(boolean allowNesting) {
        defaultPolicy.setAllowNesting(allowNesting);
    }
    
    public void setShowLangTags(boolean showLangTags) {
        defaultPolicy.setShowLangTags(showLangTags);
    }

    public class DefaultPolicy implements JSONNodePolicy {
        private boolean allowNesting = true;
        private boolean showLangTags = true;

        public void setAllowNesting(boolean allowNesting) {
            this.allowNesting = allowNesting;
        }
        
        public void setShowLangTags(boolean showLangTags) {
            this.showLangTags = showLangTags;
        }
        
        @Override
        public boolean isMultivalued() {
            return false;
        }

        @Override
        public boolean showLangTag(String lang) {
            return showLangTags;
        }

        @Override
        public boolean isNested() {
            return allowNesting;
        }

        @Override
        public JSONOldMap getNestedMap() {
            return null;
        }
        
    }
}
