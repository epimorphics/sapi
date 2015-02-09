/******************************************************************
 * File:        AltertLimitTestEndpoint.java
 * Created by:  Dave Reynolds
 * Created on:  5 Feb 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.webapi.test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.epimorphics.simpleAPI.core.RequestParameters;
import com.epimorphics.simpleAPI.webapi.EndpointsBase;

@Path("alertTestLimit")
public class AltertLimitTestEndpoint extends EndpointsBase {

    @Produces({MediaType.APPLICATION_JSON})
    @GET
    public Response getAlertTest( ) {
        RequestParameters request = getRequestWithParms();
        return startList("alertTestImplicitQueryLimit", request).respond();
    }
}
