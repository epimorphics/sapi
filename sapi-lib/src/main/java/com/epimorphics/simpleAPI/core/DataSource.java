/******************************************************************
 * File:        DataSource.java
 * Created by:  Dave Reynolds
 * Created on:  28 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core;

/**
 * An abstraction for a data source that can be queried.
 * All the interesting details are in the specification implementation.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public interface DataSource {

    public ResultStream query(Query query, ViewMap view);
    
}
