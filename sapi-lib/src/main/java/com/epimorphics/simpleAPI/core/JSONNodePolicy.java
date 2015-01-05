/******************************************************************
 * File:        JSONNodePolicy.java
 * Created by:  Dave Reynolds
 * Created on:  5 Jan 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core;

/**
 * Capture policy for how some node in an RDF description should be rendered to JSON.
 * 
 */
public interface JSONNodePolicy {

    /**
     * Returns true if the node should always be rendered as a multi-valued array, even if only one value is present
     */
    public boolean isMultivalued();

    /**
     * Returns true if values of the given language should be shown with explicit language tags
     */
    public boolean showLangTag(String lang);
    
    /**
     * Returns true if node should be shown with nested resource description
     */
    public boolean isNested();
    
    /**
     * Returns a optional map to use for rendering the nested value.
     * Returns null is the parent map should be used or nesting is not allowed. 
     */
    public JSONMap getNestedMap();
    
}
