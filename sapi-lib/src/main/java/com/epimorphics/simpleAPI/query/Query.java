/******************************************************************
 * File:        Query.java
 * Created by:  Dave Reynolds
 * Created on:  28 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.query;

/**
 * Base abstraction for a query, might be a SPARQL or a noSQL query, might
 * be an item (describe) or list (select) query. 
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public interface Query {
    
    /**
     * Return true if the query describes a single item (resource) rather 
     * returning a set of matching bindings
     */
    public boolean isItemQuery();
    
    /**
     * Returns a 
     */
}