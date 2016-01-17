/******************************************************************
 * File:        SearchRequestProcessor.java
 * Created by:  Dave Reynolds
 * Created on:  17 Jan 2016
 * 
 * (c) Copyright 2016, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.requests;

import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.query.ListQueryBuilder;
import com.epimorphics.simpleAPI.query.impl.SparqlQueryBuilder;

/**
 * Support simple free text search.
 */
// TODO add endpoint configuration to control application/customization of this processor
public class SearchRequestProcessor implements RequestProcessor {
    public static final String P_SEARCH = "search";
    
    @Override
    public ListQueryBuilder process(Request request, ListQueryBuilder builder, EndpointSpec spec) {
        if ( request.hasAvailableParameter(P_SEARCH) && builder instanceof SparqlQueryBuilder ) {
            String lucene = request.getAsLuceneQuery(P_SEARCH);
            request.consume(P_SEARCH);
            return ((SparqlQueryBuilder)builder).inject("?id  <http://jena.apache.org/text#query> '" + lucene + "'.");
        }
        // TODO Auto-generated method stub
        return builder;
    }

}
