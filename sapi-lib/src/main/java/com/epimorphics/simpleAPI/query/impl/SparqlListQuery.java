/******************************************************************
 * File:        SparqlListQuery.java
 * Created by:  Dave Reynolds
 * Created on:  29 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.query.impl;

import java.util.Collection;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.util.FmtUtils;

import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.query.ListQuery;
import com.epimorphics.simpleAPI.query.Query;
import com.epimorphics.util.PrefixUtils;

/**
 * Implements query as a SPARQL string. This version uses low level string hacking.
 * Assumes the query has text markers for where injects can occur.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class SparqlListQuery implements ListQuery {
    protected String baseQuery = "";
    protected StringBuffer filters = new StringBuffer();
    protected StringBuffer modifiers = new StringBuffer();
    
    public SparqlListQuery() {
    }
    
    public SparqlListQuery(String baseQuery) {
        this.baseQuery = baseQuery;
    }
    
    @Override
    public void addFilter(String shortname, RDFNode value) {
        filters.append( String.format("FILTER (?%s = %s)\n", shortname, FmtUtils.stringForNode( value.asNode() ) ) );
    }

    @Override
    public void addFilter(String shortname, Collection<RDFNode> values) {
        filters.append("FILTER (?");
        filters.append(shortname);
        filters.append(" IN (");
        for (RDFNode value : values) {
            filters.append(FmtUtils.stringForNode( value.asNode() ));
            filters.append(" ");
        }
        filters.append(") )\n");
    }

    @Override
    public void addSort(String shortname, boolean down) {
        modifiers.append("ORDER BY ");
        modifiers.append( String.format(down ? "DESC(?%s)" : "?%s", shortname) );
        modifiers.append("\n");
    }

    @Override
    public void addLimit(long limit, long offset) {
        modifiers.append( String.format("LIMIT %d OFFSET %d\n", limit, offset) );
    }

    @Override
    public Query finalize(EndpointSpec spec) {
        String queryStr = "SELECT * WHERE {\n    " + baseQuery + "\n    " + filters + "\n}\n" + modifiers;
        queryStr = PrefixUtils.expandQuery(queryStr, spec.getPrefixes());
        SparqlQuery query = new SparqlQuery(queryStr);
        query.setSelect(true);
        return query;
    }
}
