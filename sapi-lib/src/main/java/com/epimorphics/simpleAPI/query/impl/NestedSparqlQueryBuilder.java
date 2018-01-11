/******************************************************************
 * File:        NestedSparqlQueryBuilder.java
 * Created by:  Dave Reynolds
 * Created on:  20 May 2016
 * 
 * (c) Copyright 2016, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.query.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.shared.PrefixMapping;

import com.epimorphics.simpleAPI.core.ConfigConstants;
import com.epimorphics.simpleAPI.query.ListQuery;
import com.epimorphics.simpleAPI.query.ListQueryBuilder;
import com.epimorphics.simpleAPI.query.QueryBuilder;
import com.epimorphics.simpleAPI.views.ViewMap;
import com.epimorphics.simpleAPI.views.ViewPath;
import com.epimorphics.sparql.graphpatterns.And;
import com.epimorphics.sparql.graphpatterns.GraphPattern;
import com.epimorphics.sparql.graphpatterns.GraphPatternText;
import com.epimorphics.sparql.graphpatterns.Optional;
import com.epimorphics.sparql.query.As;
import com.epimorphics.sparql.query.Order;
import com.epimorphics.sparql.query.QueryShape;
import com.epimorphics.sparql.templates.Settings;
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
    protected List<String> additionalProjectionVars = null;
    
    public NestedSparqlQueryBuilder(QueryShape innerQuery, QueryShape outerQuery, PrefixMapping prefixes) {
        super(innerQuery, prefixes);
        this.outerQuery = outerQuery;
    }
    
    public NestedSparqlQueryBuilder(QueryShape innerQuery, QueryShape outerQuery, PrefixMapping prefixes, List<String> additionalProjectVars) {
        super(innerQuery, prefixes);
        this.outerQuery = outerQuery;
        this.additionalProjectionVars = additionalProjectVars;
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
    public ListQueryBuilder filter(ViewPath path, ViewMap map, RDFNode value) {
        return pathAndFilter(path, map, filterPattern(path.asVariableName(), value));
    }

    @Override
    public ListQueryBuilder filter(ViewPath path, ViewMap map, Collection<RDFNode> values) {
        return pathAndFilter(path, map, filterPattern(path.asVariableName(), values));
    }
    
    @Override
    public ListQueryBuilder filterExists(ViewPath path, ViewMap map, boolean mustExist) {
        GraphPattern pathPattern = new Optional( map.patternForPath(path) );
        GraphPattern merged = new And( pathPattern, filterExistsPattern( path.asVariableName(), mustExist ) );
        return updateQuery( query.copy().addLaterPattern(merged) );
    }
    
    public void setAdditionalProjectionVars(List<String> vars) {
        additionalProjectionVars = vars;
    }
        
    public ListQueryBuilder pathAndFilter(ViewPath path, ViewMap map, GraphPattern filter) {
        GraphPattern pathPattern = map.patternForPath(path);
        GraphPattern merged = new And( pathPattern, filter );
        return updateQuery( query.copy().addLaterPattern(merged) );
    }
    
    @Override public ListQueryBuilder sort(ViewPath path, ViewMap map, boolean down) {
        Order sc = (down ? Order.DESC : Order.ASC);
        QueryShape q = query.copy().addOrder(sc, new Var(path.asVariableName()));
        if ( ! path.isEmpty() ) {
            GraphPattern pathPattern = map.patternForPath(path);
            q.addLaterPattern(pathPattern);
        }
        return updateQuery( q );
    }
    
    /**
     * Insert an arbitrary sparql query string in the filter position of the outer query
     */
    public SparqlQueryBuilder filterOuter(String s) {
        return filterOuter( new GraphPatternText(s) );
    }
    
    /**
     * Insert an arbitrary sparql query in the filter position of the outer query
     */
    public SparqlQueryBuilder filterOuter(GraphPattern pattern) {
        return new NestedSparqlQueryBuilder(query, outerQuery.addLaterPattern(pattern), prefixes, additionalProjectionVars);
    }
    
    @Override
    protected SparqlQueryBuilder updateQuery(QueryShape q) {
        return new NestedSparqlQueryBuilder(q, outerQuery, prefixes, additionalProjectionVars);
    }

    @Override public ListQuery build() {
        QueryShape innerQS = query.copy();
        innerQS.addProjection( new Var(ConfigConstants.ROOT_VAR) );
        if (additionalProjectionVars != null) {
            for (String var : additionalProjectionVars) {
                innerQS.addProjection( new Var(var) );
            }
        }
        String inner = "{ " + innerQS.toSparqlSelect( new Settings() ) + "}";
        
        QueryShape outerQS = outerQuery.copy();
        outerQS.addEarlyPattern( new GraphPatternText(inner) );
        
        Settings s = new Settings();
        Set<Entry<String, String>> es = prefixes.getNsPrefixMap().entrySet();
        for (Map.Entry<String, String> e: es) {
            s.setPrefix(e.getKey(), e.getValue());
        }
        String queryString = outerQS.toSparqlSelect(s);
        String expanded = PrefixUtils.expandQuery(queryString, prefixes);
        return new SparqlSelectQuery( expanded );
    }
    
    /**
     * Experimental facility to allow one query builder to be used to create
     * an inner select for use in another query
     * @param projection the variable to project out
     * @return
     */
    public String buildQueryBodyProjecting(String projection, boolean removeLimits) {
        QueryShape qs = outerQuery.copy();
        if (removeLimits){
            qs.setLimit(-1);
            qs.setOffset(-1);
        }
        qs.addProjection(new As(new Var(ConfigConstants.ROOT_VAR), new Var(projection)));
        return qs.toSparqlSelect(new Settings());
    }    
}
