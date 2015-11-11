/******************************************************************
 * File:        SparqlListQuery.java
 * Created by:  Dave Reynolds
 * Created on:  29 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.query.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.util.FmtUtils;

import com.epimorphics.simpleAPI.query.ListQuery;
import com.epimorphics.simpleAPI.query.ListQueryBuilder;
import com.epimorphics.simpleAPI.query.QueryBuilder;
import com.epimorphics.sparql.exprs.Call;
import com.epimorphics.sparql.exprs.Infix;
import com.epimorphics.sparql.exprs.Op;
import com.epimorphics.sparql.graphpatterns.Basic;
import com.epimorphics.sparql.graphpatterns.Bind;
import com.epimorphics.sparql.graphpatterns.GraphPattern;
import com.epimorphics.sparql.graphpatterns.GraphPatternText;
import com.epimorphics.sparql.query.Order;
import com.epimorphics.sparql.query.Query;
import com.epimorphics.sparql.templates.Settings;
import com.epimorphics.sparql.terms.Filter;
import com.epimorphics.sparql.terms.IsExpr;
import com.epimorphics.sparql.terms.TermUtils;
import com.epimorphics.sparql.terms.Var;
import com.epimorphics.util.PrefixUtils;

/**
 * Implements query as a SPARQL string. This version uses low level string hacking.
 * Assumes the query has text markers for where injects can occur (done that way so
 * as to support nested queries where the injections are not obvious).
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class SparqlQueryBuilder implements ListQueryBuilder {
	
	static int counter = 0;
	
	int index = ++counter;
	
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
    
    protected PrefixMapping prefixes = PrefixMapping.Factory.create();

//    protected SparqlQueryBuilder(Query query) {
//        this.query = query;
//    }

    protected SparqlQueryBuilder(Query query, PrefixMapping prefixes) {
    	System.err.println(">> SpqralQueryBuilder #" + index + " with " + prefixes.getNsPrefixMap().size() + " prefixes.");
        this.query = query;
        setPrefixes(prefixes);
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
    	Query q = new Query().setTemplate(queryTemplate);
        return new SparqlQueryBuilder( q, PrefixMapping.Factory.create() );
    }
    
    public void setPrefixes(PrefixMapping prefixes) {
    	if (prefixes == null) {
    		// DEBUG
    		System.err.println(">> Setting prefixes to null; instead set to empty map.");
    		new RuntimeException().printStackTrace();
    		prefixes = PrefixMapping.Factory.create();
    	}
    	System.err.println(">> setPrefixes [" + prefixes.getNsPrefixMap().size() + "] on #" + index);
        this.prefixes = prefixes;
    }
    
    /**
     * Insert an arbitrary sparql query string before the base query element.
     * Mostly used internally in the builder but public to support legacy apps.
     */
    public SparqlQueryBuilder inject(String s) {
    	Query q = query.copy().addEarlyPattern(new GraphPatternText(s));
        return new SparqlQueryBuilder(q, prefixes);
    }
    
    /**
     * Insert an arbitrary sparql query string in the filter region of the query.
     * Mostly used internally in the builder but public to support legacy apps.
     */
    public SparqlQueryBuilder filter(String s) {
    	Query q = query.copy().addLaterPattern(new GraphPatternText(s));
        return new SparqlQueryBuilder(q, prefixes);
    }
    
    /**
     * Insert an arbitrary sparql query modifier in to the query.
     * Mostly used internally in the builder but public to support legacy apps.
     */
    protected SparqlQueryBuilder modifier(String s) {
    	Query q = query.copy().addRawModifier(s);
        return new SparqlQueryBuilder(q, prefixes);
    }
	
	@Override public ListQueryBuilder filter(String shortname, RDFNode value) {
		Var var = new Var(shortname);
		IsExpr val = TermUtils.nodeToTerm(value);
		Filter eq = new Filter(new Infix(var, Op.opEq, val));
		Basic basic = new Basic(eq);		
		return new SparqlQueryBuilder(query.copy().addEarlyPattern(basic), prefixes);
	}

	@Override public ListQueryBuilder filter(String shortname,	Collection<RDFNode> values) {
		if (values.size() == 1) {
			return filter(shortname, values.iterator().next());
		} else {
			Var var = new Var(shortname);
			List<IsExpr> operands = new ArrayList<IsExpr>();
			for (RDFNode value: values) operands.add(TermUtils.nodeToTerm(value));
			IsExpr oneOf = new Infix(var, Op.opIn, new Call(Op.Tuple, operands));
			return new SparqlQueryBuilder(query.copy().addEarlyPattern(new Basic(new Filter(oneOf))), prefixes);
		}
	}
	
	@Override public ListQueryBuilder sort(String shortname, boolean down) {
		Order sc = (down ? Order.DESC : Order.ASC);
		return new SparqlQueryBuilder(query.copy().addOrder(sc, new Var(shortname)), prefixes);
	}

	@Override public ListQueryBuilder limit(long limit, long offset) {
		Query q = new Query();
		q.setLimit(limit);
		q.setOffset(offset);
		return new SparqlQueryBuilder(q, prefixes);
	}

	@Override public ListQueryBuilder bind(String varname, RDFNode value) {
		final Var var = new Var(varname);
		final IsExpr val = TermUtils.nodeToTerm(value);
		Query q = query.copy().addEarlyPattern((GraphPattern) new Bind(val, var));
		return new SparqlQueryBuilder(q, prefixes);
	}

    @Override public ListQuery build() {
    	Settings s = new Settings();
    	Set<Entry<String, String>> es = prefixes.getNsPrefixMap().entrySet();
    	System.err.println(">> BUILD [" + es.size() + " prefixes] on #" + index + " ...");
		for (Map.Entry<String, String> e: es) {
    		System.err.println(">> prefix: " + e.getKey() + ", " + e.getValue());
    		s.setPrefix(e.getKey(), e.getValue());
    	}
		String queryString = query.toSparqlSelect(s);
        String expanded = PrefixUtils.expandQuery(queryString, prefixes);
        System.err.println(">> expanded: " + expanded);
    	System.err.println(">> BUILT.");
		return new SparqlSelectQuery( expanded );
    }
}
