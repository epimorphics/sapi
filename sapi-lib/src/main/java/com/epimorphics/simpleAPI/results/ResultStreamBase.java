/******************************************************************
 * File:        ResultStreamBase.java
 * Created by:  Dave Reynolds
 * Created on:  9 Oct 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.results;

import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.requests.Call;
import com.epimorphics.simpleAPI.requests.Request;

public abstract class ResultStreamBase implements ResultStream {
    protected Call call;
    
    public ResultStreamBase(Call call) {
        this.call = call;
    }
    

    @Override
    public EndpointSpec getSpec() {
        return call.getEndpoint();
    }

    @Override
    public Request getRequest() {
        return call.getRequest();
    }

    @Override
    public Call getCall() {
        return call;
    }

    @Override
    public void close() {
        // Defalt is to do nothing
    }

}
