/******************************************************************
 * File:        SparqlQuery.java
 * Created by:  Dave Reynolds
 * Created on:  29 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.query.impl;

import com.epimorphics.simpleAPI.query.ItemQuery;

/**
 * Implementation of query interface that just stores the query as 
 * a single string plus an explicit marker for whether this is an item
 * or a list query.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class SparqlDescribeQuery extends SparqlQuery implements ItemQuery {

    public SparqlDescribeQuery(String query) {
        super(query);
    }
    
}
