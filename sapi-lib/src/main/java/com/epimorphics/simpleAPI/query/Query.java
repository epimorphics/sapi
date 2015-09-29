/******************************************************************
 * File:        Query.java
 * Created by:  Dave Reynolds
 * Created on:  28 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.query;

import com.epimorphics.simpleAPI.endpoints.EndpointSpec;

/**
 * Base abstraction for a query, might be a SPARQL or a noSQL query, might
 * be an item (describe) or list (select) query. 
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public interface Query {
    
    // TODO support for variable binding
    
    /**
     * Return a finalized query after all filter/modifiers have been added.
     * May use information (e.g. prefix declarations) from the endpoint specification
     */
    public Query finalize(EndpointSpec spec);

}
