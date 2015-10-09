/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.simpleAPI.query.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;

import com.epimorphics.simpleAPI.query.QueryBuilder;
import com.epimorphics.sparql.exprs.Call;
import com.epimorphics.sparql.exprs.Infix;
import com.epimorphics.sparql.exprs.Op;
import com.epimorphics.sparql.graphpatterns.Basic;
import com.epimorphics.sparql.graphpatterns.Bind;
import com.epimorphics.sparql.graphpatterns.GraphPattern;
import com.epimorphics.sparql.query.Order;
import com.epimorphics.sparql.templates.Settings;
import com.epimorphics.sparql.terms.Filter;
import com.epimorphics.sparql.terms.IsExpr;
import com.epimorphics.sparql.terms.Literal;
import com.epimorphics.sparql.terms.URI;
import com.epimorphics.sparql.terms.Var;
import com.epimorphics.simpleAPI.query.Query;

public class AlternativeSparqlQueryBuilder implements QueryBuilder {

	final com.epimorphics.sparql.query.Query q = new com.epimorphics.sparql.query.Query();
		
	public AlternativeSparqlQueryBuilder() {
	}
	
	@Override public QueryBuilder filter(String shortname, RDFNode value) {
		Var var = new Var(shortname);
		IsExpr val = nodeToTerm(value);
		Filter eq = new Filter(new Infix(var, Op.opEq, val));
		q.addPattern(new Basic(eq));		
		return this;
	}

	private IsExpr nodeToTerm(RDFNode value) {
		Node lit = value.asNode();
		if (value.isURIResource()) 
			return new URI(lit.getURI());
		if (value.isLiteral()) {
			return new Literal
				( lit.getLiteralLexicalForm()
				, asType(lit.getLiteralDatatypeURI())
				, lit.getLiteralLanguage()
				);
		}
		return null;
	}

	private URI asType(String uri) {
		if (uri == null) return null;
		return new URI(uri);
	}

	@Override public QueryBuilder filter(String shortname,	Collection<RDFNode> values) {
		if (values.size() == 1) {
			return filter(shortname, values.iterator().next());
		} else {
			Var var = new Var(shortname);
			List<IsExpr> operands = new ArrayList<IsExpr>();
			for (RDFNode value: values) operands.add(nodeToTerm(value));
			IsExpr oneOf = new Infix(var, Op.opIn, new Call(Op.Tuple, operands));
			q.addPattern(new Basic(new Filter(oneOf)));
			return this;
		}
	}

	@Override public QueryBuilder sort(String shortname, boolean down) {
		Order sc = (down ? Order.DESC : Order.ASC);
		q.addOrder(sc, new Var(shortname));
		return this;
	}

	@Override public QueryBuilder limit(long limit, long offset) {
		q.setLimit(limit);
		q.setOffset(offset);
		return this;
	}

	@Override public QueryBuilder bind(String varname, RDFNode value) {
		final Var var = new Var(varname);
		final IsExpr val = nodeToTerm(value);
		q.addPattern((GraphPattern) new Bind(val, var));
		return this;
	}
	
	protected static final String usualTemplate =
		"SELECT * WHERE {\n"
		+ "    #$INJECT$\n"
		+ "    $filters\n"
		+ "}\n"
		+ "#$SORT$\n"
		+ "#$MODIFIER$\n"
		;
	
    @Override public Query build() {	
		Settings s = new Settings();
		return new SparqlQuery(q.toSparql(s));
	}

}
