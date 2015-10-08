/******************************************************************
 * File:        DescribeQueryBuilder.java
 * Created by:  Dave Reynolds
 * Created on:  8 Oct 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.query.impl;

import java.util.Collection;

import javax.ws.rs.core.Response.Status;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.shared.PrefixMapping;

import com.epimorphics.appbase.webapi.WebApiException;
import com.epimorphics.rdfutil.QueryUtil;
import com.epimorphics.simpleAPI.query.Query;
import com.epimorphics.simpleAPI.query.QueryBuilder;
import com.epimorphics.util.PrefixUtils;

public class DescribeQueryBuilder implements QueryBuilder {
    protected String query;
    protected PrefixMapping prefixes;
    
    public DescribeQueryBuilder(String query) {
        this.query = query;
    }
    
    public DescribeQueryBuilder(String query, PrefixMapping prefixes) {
        this.prefixes = prefixes;
        this.query = query;
    }

    @Override
    public QueryBuilder filter(String shortname, RDFNode value) {
        throw new WebApiException(Status.BAD_REQUEST, "Cannot filter an Item query");
    }

    @Override
    public QueryBuilder filter(String shortname, Collection<RDFNode> values) {
        throw new WebApiException(Status.BAD_REQUEST, "Cannot filter an Item query");
    }

    @Override
    public QueryBuilder sort(String shortname, boolean down) {
        throw new WebApiException(Status.BAD_REQUEST, "Cannot sort an Item query");
    }

    @Override
    public QueryBuilder limit(long limit, long offset) {
        throw new WebApiException(Status.BAD_REQUEST, "Cannot limit an Item query");
    }

    @Override
    public QueryBuilder bind(String varname, RDFNode value) {
        return new DescribeQueryBuilder( bindQueryParam(query, varname, value) );
    }

    @Override
    public Query build() {
        return new SparqlQuery( prefixes == null ? query : PrefixUtils.expandQuery(query, prefixes), true);
    }
    
    // TODO move this to somewhere more logical and reusable
    /**
     * Bind a variable in a query by syntactic substitution
     */
    public static String bindQueryParam(String query, String var, Object value) {
        String subs = QueryUtil.asSPARQLValue( value ).replace("\\", "\\\\");
        // Two step substitute so don't use regex when substituting value (which might have regex special characters)
        String bound = query.replaceAll("\\?" + var + "\\b", MARKER);
        bound = bound.replace(MARKER, subs);
        return bound;
    }
    protected static final String MARKER="?ILLEGAL-VAR";
}
