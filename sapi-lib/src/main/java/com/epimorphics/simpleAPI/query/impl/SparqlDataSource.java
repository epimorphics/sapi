/******************************************************************
 * File:        SparqlDataSource.java
 * Created by:  Dave Reynolds
 * Created on:  28 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.query.impl;

import com.epimorphics.appbase.data.SparqlSource;
import com.epimorphics.simpleAPI.query.DataSource;
import com.epimorphics.simpleAPI.query.Query;
import com.epimorphics.simpleAPI.results.ResultStream;
import com.epimorphics.simpleAPI.results.ResultStreamSparqlSelect;
import com.epimorphics.simpleAPI.views.ViewMap;
import com.epimorphics.util.EpiException;

/**
 * Implementation of DataSource that is just a thin wrapper 
 * round a SPARQL source
 */
public class SparqlDataSource implements DataSource {
    protected SparqlSource source;
    
    public SparqlDataSource(){
    }
    
    public void setSource(SparqlSource source) {
        this.source = source;
    }

    @Override
    public ResultStream query(Query query, ViewMap view) {
        if (query instanceof SparqlQuery) {
            SparqlQuery sq = (SparqlQuery) query;
            if (sq.isSelect()) {
                return new ResultStreamSparqlSelect( source.streamableSelect( sq.getQuery() ), view );
            } else {
                // TODO implement describes
                return null;
            }
        } else {
            throw new EpiException("SPARQL source given non-SPARQL query");
        }
    }

}
