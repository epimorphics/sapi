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
import com.epimorphics.simpleAPI.endpoints.impl.EndpointSpecBase;
import com.epimorphics.simpleAPI.query.ListQueryBuilder;
import com.epimorphics.simpleAPI.query.impl.SparqlQueryBuilder;

/**
 * Support simple free text search.
 */
// TODO add endpoint configuration to control application/customization of this processor
public class SearchRequestProcessor implements RequestProcessor {
    public static final String P_SEARCH = "search";
    
    protected boolean filterDistinct = false;
    
    /**
     * Set to true to de-duplicate text search results (the text search run run as a subsquery with a DISTINCT modifier)
     */
    public void setFilterDistinct(boolean filterDistict) {
        this.filterDistinct = filterDistict;
    }
    
    @Override
    public ListQueryBuilder process(Request request, ListQueryBuilder builder, EndpointSpec spec) {
        if (spec instanceof EndpointSpecBase) {
            EndpointSpecBase specbase = (EndpointSpecBase) spec;
            if ( request.hasAvailableParameter(P_SEARCH) && builder instanceof SparqlQueryBuilder && specbase.getTextSearchRoot() != null) {
                String lucene = request.getAsLuceneQuery(P_SEARCH);
                request.consume(P_SEARCH);
                String root = specbase.getTextSearchRoot();
                String search = 
                        filterDistinct 
                                ? String.format("{ SELECT DISTINCT ?%s WHERE {?%s  <http://jena.apache.org/text#query> '%s'} }", root, root, lucene)
                                : String.format("?%s  <http://jena.apache.org/text#query> '%s'.", root, lucene);
                return ((SparqlQueryBuilder)builder).inject( search );
            }
        }
        return builder;
    }

}
