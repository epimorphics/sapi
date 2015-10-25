/******************************************************************
 * File:        ResultOrStream.java
 * Created by:  Dave Reynolds
 * Created on:  25 Oct 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.results;

import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.requests.Call;
import com.epimorphics.simpleAPI.requests.Request;

/**
 * Marker interface which represents the result of a call. 
 * This can be a single Result (as in the case of an Item query)
 * or a stream of Results (as in the case of a List query)
 */
public interface ResultOrStream {

    public EndpointSpec getSpec();
    
    public Request getRequest();
    
    /**
     * The call that generated this result
     */
    public Call getCall();

}
