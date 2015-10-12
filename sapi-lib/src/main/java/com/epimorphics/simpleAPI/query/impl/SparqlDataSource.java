/******************************************************************
 * File:        SparqlDataSource.java
 * Created by:  Dave Reynolds
 * Created on:  28 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.query.impl;

import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

import com.epimorphics.appbase.data.SparqlSource;
import com.epimorphics.simpleAPI.query.DataSource;
import com.epimorphics.simpleAPI.query.ItemQuery;
import com.epimorphics.simpleAPI.query.ListQuery;
import com.epimorphics.simpleAPI.requests.Call;
import com.epimorphics.simpleAPI.results.RDFResult;
import com.epimorphics.simpleAPI.results.Result;
import com.epimorphics.simpleAPI.results.ResultStream;
import com.epimorphics.simpleAPI.results.ResultStreamSparqlSelect;
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
    public Result query(ItemQuery query, Call call) {
        if (query instanceof SparqlQuery) {
            SparqlQuery sq = (SparqlQuery) query;
            Graph graph = source.describe(sq.getQuery());
            Model model = ModelFactory.createModelForGraph(graph);
            Resource root = model.getResource( call.getRequest().getRequestedURI() );
            return new RDFResult(root, call);
        } else {
            throw new EpiException("SPARQL source given non-SPARQL query");
        }
    }

    @Override
    public ResultStream query(ListQuery query, Call call) {
        if (query instanceof SparqlQuery) {
            SparqlQuery sq = (SparqlQuery) query;
            return new ResultStreamSparqlSelect( source.streamableSelect( sq.getQuery() ), call );
        } else {
            throw new EpiException("SPARQL source given non-SPARQL query");
        }
    }

}
