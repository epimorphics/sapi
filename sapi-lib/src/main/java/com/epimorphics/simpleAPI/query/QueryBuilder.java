/******************************************************************
 * File:        QueryBuilder.java
 * Created by:  Dave Reynolds
 * Created on:  30 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.query;

import java.util.Collection;

import org.apache.jena.rdf.model.RDFNode;

/**
 * Represents a generic query (might be SPARQL or noSQL) that can
 * be modified and extended.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public interface QueryBuilder {

    /**
     * Add an equality filter constraint to the query
     */
    public QueryBuilder filter(String shortname, RDFNode value);

    /**
     * Add a one-of filter constraint to the query
     */
    public QueryBuilder filter(String shortname, Collection<RDFNode> values);
    
    // TODO more generalized filters?
    
    /**
     * Add a sort directive to the query
     */
    public QueryBuilder sort(String shortname, boolean down);
    
    /**
     * Set a paging window on the query results
     */
    public QueryBuilder limit(long limit, long offset);
    
    /**
     * Bind a value to a variable within the query
     */
    public QueryBuilder bind(String varname, RDFNode value);
    
    /**
     * Finalize the query, in the case of SPARQL this will include
     * setting prefix bindings.
     */
    public Query build();
}
