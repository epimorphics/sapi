/******************************************************************
 * File:        JSONNodeDescription.java
 * Created by:  Dave Reynolds
 * Created on:  12 Jan 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core;

/**
 * Provides information on how a result value should be rendered,
 * including support for nested trees of result values.
 * Can be implemented from an explicit configurable mapping 
 * or a default policy for all nodes.
 *  
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public interface JSONNodeDescription {

    /**
     * Returns true if the node should always be rendered as a multi-valued array, even if only one value is present
     */
    public boolean isMultivalued();

    /**
     * Returns true if values of the given language should be shown with explicit language tags
     */
    public boolean showLangTag(String lang);
 
    /**
     * Returns true if the node is a child of some parent node in a nested tree structure
     */
    public boolean isChild();
    
    /**
     * Returns the descriptions of the parent node in a nested tree structure, or null
     * if this is not a child node.
     */
    public JSONNodeDescription getParent();
    
    /**
     * Returns true if the node is a parent of some nested tree structure
     */
    public boolean isParent();
    
    /**
     * Return a nested map definition, or null if this node has no children
     */
    public JSONMap getNestedMap();
    
    /**
     * Return true if this node is allowed to be used in filters
     */
    public boolean isFilterable();
    
    /**
     * Return true if the node is optional within the corresponding query
     */
    public boolean isOptional();

    /**
     * Return the URI of the datatype for filter values for this node.
     * May be null (in which case the caller may need to infer a type).
     */
    public String getType();
}
