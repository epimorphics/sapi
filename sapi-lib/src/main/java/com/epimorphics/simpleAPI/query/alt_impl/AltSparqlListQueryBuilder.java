/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.simpleAPI.query.alt_impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.jena.rdf.model.RDFNode;

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
import com.epimorphics.sparql.terms.Var;
import com.epimorphics.sparql.terms.TermUtils;
import com.epimorphics.simpleAPI.query.ListQuery;
import com.epimorphics.simpleAPI.query.ListQueryBuilder;
import com.epimorphics.simpleAPI.query.impl.SparqlSelectQuery;

public class AltSparqlListQueryBuilder implements ListQueryBuilder {

	final com.epimorphics.sparql.query.Query q = new com.epimorphics.sparql.query.Query();
		
	public AltSparqlListQueryBuilder() {
	}
	
	@Override public ListQueryBuilder filter(String shortname, RDFNode value) {
		Var var = new Var(shortname);
		IsExpr val = TermUtils.nodeToTerm(value);
		Filter eq = new Filter(new Infix(var, Op.opEq, val));
		q.addPattern(new Basic(eq));		
		return this;
	}

	@Override public ListQueryBuilder filter(String shortname,	Collection<RDFNode> values) {
		if (values.size() == 1) {
			return filter(shortname, values.iterator().next());
		} else {
			Var var = new Var(shortname);
			List<IsExpr> operands = new ArrayList<IsExpr>();
			for (RDFNode value: values) operands.add(TermUtils.nodeToTerm(value));
			IsExpr oneOf = new Infix(var, Op.opIn, new Call(Op.Tuple, operands));
			q.addPattern(new Basic(new Filter(oneOf)));
			return this;
		}
	}

	@Override public ListQueryBuilder sort(String shortname, boolean down) {
		Order sc = (down ? Order.DESC : Order.ASC);
		q.addOrder(sc, new Var(shortname));
		return this;
	}

	@Override public ListQueryBuilder limit(long limit, long offset) {
		q.setLimit(limit);
		q.setOffset(offset);
		return this;
	}

	@Override public ListQueryBuilder bind(String varname, RDFNode value) {
		final Var var = new Var(varname);
		final IsExpr val = TermUtils.nodeToTerm(value);
		q.addPattern((GraphPattern) new Bind(val, var));
		return this;
	}
	
    @Override public ListQuery build() {	
		Settings s = new Settings();
		return new SparqlSelectQuery(q.toSparqlSelect(s));
	}

}
