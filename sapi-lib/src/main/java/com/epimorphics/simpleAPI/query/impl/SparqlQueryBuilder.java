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
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.util.FmtUtils;

import com.epimorphics.simpleAPI.query.Query;
import com.epimorphics.simpleAPI.query.QueryBuilder;
import com.epimorphics.util.PrefixUtils;

/**
 * Implements query as a SPARQL string. This version uses low level string hacking.
 * Assumes the query has text markers for where injects can occur (does that way so
 * as to support nested queries where the injections are not obvious).
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class SparqlQueryBuilder implements QueryBuilder {
    public static final String INJECT_MARKER = "#$INJECT$";
    public static final String FILTER_MARKER = "#$FILTER$";
    public static final String MODIFIER_MARKER = "#$MODIFIER$";
    public static final String GENERIC_TEMPLATE =
            "SELECT * WHERE {\n"
            + "    #$INJECT$\n"
            + "    #$FILTER$\n"
            + "}\n"
            + "#$MODIFIER$";
    
    protected String query;
    protected PrefixMapping prefixes;

    protected SparqlQueryBuilder(String query) {
        this.query = query;
    }

    protected SparqlQueryBuilder(String query, PrefixMapping prefixes) {
        this.query = query;
        this.prefixes = prefixes;
    }
    
    /**
     * Construct a query builder from a base query.
     */
    public static final QueryBuilder fromBaseQuery(String baseQuery) {
        return fromBaseQuery(baseQuery, null);
    }
    
    /**
     * Construct a query builder from a base query.
     */
    public static final QueryBuilder fromBaseQuery(String baseQuery, PrefixMapping prefixes) {
        return new SparqlQueryBuilder( GENERIC_TEMPLATE.replace(INJECT_MARKER, baseQuery + "\n    " + INJECT_MARKER), prefixes );
    }
    
    /**
     * Construct a query builder from a complete query template that must incluide the inject, filter and modification markers.
     */
    public static final QueryBuilder fromTemplate(String queryTemplate) {
        return new SparqlQueryBuilder( queryTemplate );
    }
    
    public void setPrefixes(PrefixMapping prefixes) {
        this.prefixes = prefixes;
    }
    
    /**
     * Insert an arbitrary sparql query string before the base query element.
     * Mostly used internally in the builder but public to support legacy apps.
     */
    public SparqlQueryBuilder inject(String s) {
        return new SparqlQueryBuilder(query.replace(INJECT_MARKER, INJECT_MARKER + "\n    " + s), prefixes);
    }
    
    /**
     * Insert an arbitrary sparql query string in the filter region of the query.
     * Mostly used internally in the builder but public to support legacy apps.
     */
    public SparqlQueryBuilder filter(String s) {
        return new SparqlQueryBuilder(query.replace(FILTER_MARKER, s + "\n    " + FILTER_MARKER), prefixes);
    }
    
    
    /**
     * Insert an arbitrary sparql query modifier in to the query.
     * Mostly used internally in the builder but public to support legacy apps.
     */
    protected SparqlQueryBuilder modifier(String s) {
        return new SparqlQueryBuilder(query.replace(MODIFIER_MARKER, s + "\n" + MODIFIER_MARKER), prefixes);
    }

    @Override
    public QueryBuilder filter(String shortname, RDFNode value) {
        return filter( String.format("FILTER (?%s = %s)\n", shortname, FmtUtils.stringForNode( value.asNode() ) ) );
    }

    @Override
    public QueryBuilder filter(String shortname, Collection<RDFNode> values) {
        StringBuffer filters = new StringBuffer();
        filters.append("FILTER (?");
        filters.append(shortname);
        filters.append(" IN (");
        for (RDFNode value : values) {
            filters.append(FmtUtils.stringForNode( value.asNode() ));
            filters.append(" ");
        }
        filters.append(") )");
        return filter( filters.toString() );
    }

    @Override
    public QueryBuilder sort(String shortname, boolean down) {
        return modifier( "ORDER BY " + String.format(down ? "DESC(?%s)" : "?%s", shortname) );
    }

    @Override
    public QueryBuilder limit(long limit, long offset) {
        return modifier( String.format("LIMIT %d OFFSET %d\n", limit, offset) );
    }

    @Override
    public QueryBuilder bind(String varname, RDFNode value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Query build() {
        return new SparqlQuery( PrefixUtils.expandQuery(query, prefixes) );
    }
}
