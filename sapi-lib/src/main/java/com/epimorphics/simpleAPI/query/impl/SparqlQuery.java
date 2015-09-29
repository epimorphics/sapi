/******************************************************************
 * File:        SparqlQuery.java
 * Created by:  Dave Reynolds
 * Created on:  29 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.query.impl;

import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.query.Query;

public class SparqlQuery implements Query {
    protected String query;
    protected boolean select;
    
    public SparqlQuery(String query) {
        this.query = query;
    }
    
    public String getQuery() {
        return query;
    }
    
    public boolean isSelect() {
        return select;
    }
    
    public void setSelect(boolean select) {
        this.select = select;
    }
    
    @Override
    public Query finalize(EndpointSpec spec) {
        return this;
    }

}
