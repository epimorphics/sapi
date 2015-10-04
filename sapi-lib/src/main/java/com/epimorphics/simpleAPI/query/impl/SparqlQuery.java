/******************************************************************
 * File:        SparqlQuery.java
 * Created by:  Dave Reynolds
 * Created on:  29 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.query.impl;

import com.epimorphics.simpleAPI.query.Query;

/**
 * Implementation of query interface that just stores the query as 
 * a single string plus an explicit marker for whether this is an item
 * or a list query.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class SparqlQuery implements Query {
    protected String query;
    protected boolean isItem = false;
    
    /**
     * Construct a list query
     */
    public SparqlQuery(String query) {
        this.query = query;
    }
    
    /**
     * Construct a list or item query
     */
    public SparqlQuery(String query, boolean isItem) {
        this.query = query;
        this.isItem = isItem;
    }
    
    public String getQuery() {
        return query;
    }
    
    @Override
    public boolean isItemQuery() {
        return isItem;
    }
    
    @Override
    public String toString() {
        return query;
    }
    
}
