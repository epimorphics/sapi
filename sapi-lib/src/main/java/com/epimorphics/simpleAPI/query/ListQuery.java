/******************************************************************
 * File:        ListQuery.java
 * Created by:  Dave Reynolds
 * Created on:  28 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.query;

import java.util.Collection;

import org.apache.jena.rdf.model.RDFNode;

/**
 * Base abstraction for a list query, might be implemented as a SPARQL query or a noSQL query.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public interface ListQuery extends Query {

    /**
     * Add an equality filter constraint to the query
     */
    public void addFilter(String shortname, RDFNode value);

    /**
     * Add a one-of filter constraint to the query
     */
    public void addFilter(String shortname, Collection<RDFNode> values);
    
    /**
     * Add a sort directive to the query
     */
    public void addSort(String shortname, boolean down);
    
    /**
     * Set a paging window on the query results
     */
    public void addLimit(long limit, long offset);
}
