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
    
    /**
     * Construct a list query
     */
    public SparqlQuery(String query) {
        this.query = query;
    }
    
    public String getQuery() {
    	System.err.println(">> QUERY WAS:\n" + query);
    	new RuntimeException().printStackTrace();
//    	return
//    		"PREFIX eg: <http://localhost/example/>" 
//    		+ " PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"
//    		+ " PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
//    		+ " PREFIX eg: <http://localhost/example/>"
//    		+ " PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"
//    		+ " PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
//    		+ " SELECT * WHERE {"
//    		+ "   ?id a eg:Itest . "
//    		+ "   ?id rdfs:label ?label . "
//    		+ "   ?id skos:notation ?notation . "
//    		+ "   OPTIONAL { ?id eg:child ?child . OPTIONAL { ?child skos:notation ?child_cnotation . }}"
//    		+ "} "
//    		+ "ORDER BY ASC(?id)";
         return query;
    }
    
    @Override
    public String toString() {
        return query;
    }
    
}
