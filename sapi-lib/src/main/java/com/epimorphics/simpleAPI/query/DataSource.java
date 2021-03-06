/******************************************************************
 * File:        DataSource.java
 * Created by:  Dave Reynolds
 * Created on:  28 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.query;

import com.epimorphics.simpleAPI.requests.Call;
import com.epimorphics.simpleAPI.results.Result;
import com.epimorphics.simpleAPI.results.ResultStream;

/**
 * An abstraction for a data source that can be queried.
 * All the interesting details are in the specification implementation.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public interface DataSource {

    public Result query(ItemQuery query, Call call);

    public ResultStream query(ListQuery query, Call call);
    
}
