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

import com.epimorphics.simpleAPI.query.ListQuery;
import com.epimorphics.simpleAPI.query.ListQueryBuilder;
import com.epimorphics.simpleAPI.query.QueryBuilder;
import com.epimorphics.sparql.query.Query;
import com.epimorphics.util.PrefixUtils;

/**
 * Implements query as a SPARQL string. This version uses low level string hacking.
 * Assumes the query has text markers for where injects can occur (done that way so
 * as to support nested queries where the injections are not obvious).
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class SparqlQueryBuilder implements ListQueryBuilder {
	
    public static final String INJECT_MARKER = "#$INJECT$";
    public static final String FILTER_MARKER = "#$FILTER$";
    public static final String MODIFIER_MARKER = "#$MODIFIER$";
    public static final String SORT_MARKER = "#$SORT$";
    public static final String SORT_X_MARKER = "#$SORTX$";
    public static final String GENERIC_TEMPLATE =
            "SELECT * WHERE {\n"
            + "    #$INJECT$\n"
            + "    #$FILTER$\n"
            + "}\n"
            + "#$SORT$\n"
            + "#$MODIFIER$\n";
    
    protected Query query;
    protected PrefixMapping prefixes;

    protected SparqlQueryBuilder(Query query) {
        this.query = query;
    }

    protected SparqlQueryBuilder(Query query, PrefixMapping prefixes) {
        this.query = query;
        this.prefixes = prefixes;
    }
    
    /**
     * Construct a query builder from a base query.
     */
    public static final QueryBuilder fromBaseQuery(Query baseQuery) {
        return fromBaseQuery(baseQuery, null);
    }
    
    /**
     * Construct a query builder from a base query.
     */
    public static final QueryBuilder fromBaseQuery(Query baseQuery, PrefixMapping prefixes) {
        return new SparqlQueryBuilder( baseQuery, prefixes );
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
    public ListQueryBuilder filter(String shortname, RDFNode value) {
        return filter( String.format("FILTER (?%s = %s)\n", shortname, FmtUtils.stringForNode( value.asNode() ) ) );
    }

    @Override
    public ListQueryBuilder filter(String shortname, Collection<RDFNode> values) {
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
    public ListQueryBuilder sort(String shortname, boolean down) {
        String sort = String.format(down ? "DESC(?%s)" : "?%s", shortname);
        if (query.contains(SORT_X_MARKER)) {
            return new SparqlQueryBuilder(query.replace(SORT_X_MARKER, sort + " " + SORT_X_MARKER), prefixes);
        } else {
            return new SparqlQueryBuilder(query.replace(SORT_MARKER, "ORDER BY " + sort + " " + SORT_X_MARKER), prefixes);
        }
    }

    @Override
    public ListQueryBuilder limit(long limit, long offset) {
        return modifier( String.format("LIMIT %d OFFSET %d\n", limit, offset) );
    }

    @Override
    public QueryBuilder bind(String varname, RDFNode value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ListQuery build() {
        return new SparqlSelectQuery( PrefixUtils.expandQuery(query.replaceAll("#\\$[A-Z]*\\$", ""), prefixes) );
    }
}
