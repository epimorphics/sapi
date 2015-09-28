/******************************************************************
 * File:        SparqlDataSource.java
 * Created by:  Dave Reynolds
 * Created on:  28 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core.impl;

import com.epimorphics.appbase.data.SparqlSource;
import com.epimorphics.simpleAPI.core.DataSource;
import com.epimorphics.simpleAPI.core.Query;
import com.epimorphics.simpleAPI.core.ResultStream;
import com.epimorphics.simpleAPI.core.ViewMap;

/**
 * Implementation of DataSource that is just a thin wrapper 
 * round a SPARQL source
 */
public class SparqlDataSource implements DataSource {
    protected SparqlSource source;
    
    public SparqlDataSource(){
    }
    
    public void setSparqlSource(SparqlSource source) {
        this.source = source;
    }

    @Override
    public ResultStream query(Query query, ViewMap view) {
        // TODO Auto-generated method stub
        return null;
    }

}
