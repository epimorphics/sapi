/******************************************************************
 * File:        SparqlQuery.java
 * Created by:  Dave Reynolds
 * Created on:  29 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.query.impl;

import com.epimorphics.simpleAPI.query.ListQuery;

/**
 * Implementation of query interface that just stores the query as 
 * a single string plus an explicit marker for whether this is an item
 * or a list query.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class SparqlSelectQuery extends SparqlQuery implements ListQuery {

    public SparqlSelectQuery(String query) {
        super(query);
    }
    
}
