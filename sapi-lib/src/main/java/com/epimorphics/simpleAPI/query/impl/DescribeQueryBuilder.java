/******************************************************************
 * File:        DescribeQueryBuilder.java
 * Created by:  Dave Reynolds
 * Created on:  8 Oct 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.query.impl;

import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.shared.PrefixMapping;

import com.epimorphics.simpleAPI.query.ItemQuery;
import com.epimorphics.simpleAPI.query.QueryBuilder;
import com.epimorphics.sparql.graphpatterns.Bind;
import com.epimorphics.sparql.query.QueryShape;
import com.epimorphics.sparql.templates.Settings;
import com.epimorphics.sparql.terms.IsExpr;
import com.epimorphics.sparql.terms.Literal;
import com.epimorphics.sparql.terms.URI;
import com.epimorphics.sparql.terms.Var;
import com.epimorphics.util.PrefixUtils;

public class DescribeQueryBuilder implements QueryBuilder {
    protected QueryShape query;
    protected PrefixMapping prefixes;
    
    public DescribeQueryBuilder(QueryShape query) {
        this.query = query;
    }
    
    public DescribeQueryBuilder(QueryShape query, PrefixMapping prefixes) {
        this.prefixes = prefixes;
        this.query = query;
    }

    @Override public QueryBuilder bind(String varname, RDFNode value) {
        return new DescribeQueryBuilder( bindQueryParam(query, varname, value), prefixes );
    }

    @Override public ItemQuery build() {
        Settings s = new Settings();
        Set<Entry<String, String>> es = prefixes.getNsPrefixMap().entrySet();
        for (Map.Entry<String, String> e: es) {
            s.setPrefix(e.getKey(), e.getValue());
        }
    	String queryString = query.toSparqlDescribe(s);
        return new SparqlDescribeQuery
        	( prefixes == null 
        	? queryString 
        	: PrefixUtils.expandQuery(queryString, prefixes) )
        	;
    }
    
    // TODO move this to somewhere more logical and reusable
    /**
     * Bind a variable in a query by syntactic substitution
     */
    public static QueryShape bindQueryParam(QueryShape query, String var, Object value) {
        return query.copy().addPreBinding(new Bind(asTerm(value), new Var(var)));
    }
    
    private static IsExpr asTerm(Object value) {
    	if (value instanceof RDFNode) 
    		return new URI(((RDFNode) value).asNode().getURI());
    	if (value instanceof org.apache.jena.rdf.model.Literal) {
    		Node n = ((RDFNode) value).asNode();
    		URI type = new URI(n.getLiteralDatatypeURI());
    		return new Literal(n.getLiteralLexicalForm(), type, n.getLiteralLanguage());
    	}
    	throw new IllegalArgumentException("as Term: " + value.toString());
	}
    
	protected static final String MARKER="?ILLEGAL-VAR";
}
