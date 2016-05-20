/******************************************************************
 * File:        NestedSparqlQueryBuilder.java
 * Created by:  Dave Reynolds
 * Created on:  20 May 2016
 * 
 * (c) Copyright 2016, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.query.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.shared.PrefixMapping;

import com.epimorphics.simpleAPI.core.ConfigConstants;
import com.epimorphics.simpleAPI.query.ListQuery;
import com.epimorphics.simpleAPI.query.ListQueryBuilder;
import com.epimorphics.simpleAPI.query.QueryBuilder;
import com.epimorphics.sparql.exprs.Call;
import com.epimorphics.sparql.exprs.Infix;
import com.epimorphics.sparql.exprs.Op;
import com.epimorphics.sparql.geo.GeoQuery;
import com.epimorphics.sparql.graphpatterns.Basic;
import com.epimorphics.sparql.graphpatterns.Bind;
import com.epimorphics.sparql.graphpatterns.GraphPattern;
import com.epimorphics.sparql.graphpatterns.GraphPatternText;
import com.epimorphics.sparql.query.As;
import com.epimorphics.sparql.query.Order;
import com.epimorphics.sparql.query.QueryShape;
import com.epimorphics.sparql.templates.Settings;
import com.epimorphics.sparql.terms.Filter;
import com.epimorphics.sparql.terms.IsExpr;
import com.epimorphics.sparql.terms.TermUtils;
import com.epimorphics.sparql.terms.Var;
import com.epimorphics.util.PrefixUtils;

/**
 * Version of query builder that puts the base query, filters and modifiers into
 * an inner select (for accurate LIMIT/OFFSET handling) and puts the view projection
 * into an outer query.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class NestedSparqlQueryBuilder extends SparqlQueryBuilder {
    protected QueryShape outerQuery;
    
    public NestedSparqlQueryBuilder(QueryShape innerQuery, QueryShape outerQuery, PrefixMapping prefixes) {
        super(innerQuery, prefixes);
        this.outerQuery = outerQuery;
    }
    
    /**
     * Construct a nested query builder from a base query and view mapping
     */
    public static final QueryBuilder fromBaseQuery(QueryShape baseQuery, GraphPattern viewPattern, PrefixMapping prefixes) {
        QueryShape query = new QueryShape();
        query.addLaterPattern( viewPattern );
        return new NestedSparqlQueryBuilder( baseQuery, query, prefixes );
    }
    
    @Override
    protected SparqlQueryBuilder updateQuery(QueryShape q) {
        return new NestedSparqlQueryBuilder(q, outerQuery, prefixes);
    }

    @Override public ListQuery build() {
        // TODO change this to use inner/outer query arrangement
        Settings s = new Settings();
        Set<Entry<String, String>> es = prefixes.getNsPrefixMap().entrySet();
        for (Map.Entry<String, String> e: es) {
            s.setPrefix(e.getKey(), e.getValue());
        }
        String queryString = query.toSparqlSelect(s);
        String expanded = PrefixUtils.expandQuery(queryString, prefixes);
        return new SparqlSelectQuery( expanded );
    }
    
    /**
     * Experimental facility to allow one query builder to be used to create
     * an inner select for use in another query
     * @param projection the variable to project out
     * @return
     */
    
    // TODO drop from here?
    public String buildQueryBodyProjecting(String projection, boolean removeLimits) {
        QueryShape qs = query.copy();
        if (removeLimits){
            qs.setLimit(-1);
            qs.setOffset(-1);
        }
        qs.addProjection(new As(new Var(ConfigConstants.ROOT_VAR), new Var(projection)));
        return qs.toSparqlSelect(new Settings());
    }    
}
