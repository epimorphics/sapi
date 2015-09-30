/******************************************************************
 * File:        AlertFixedQueryTestEndpoint.java
 * Created by:  Dave Reynolds
 * Created on:  6 Jan 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.attic.webapi.test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.epimorphics.simpleAPI.attic.webapi.EndpointsBase;

@Path("paramFixedQueryTest")
public class AlertParamFixedQueryTestEndpoint extends EndpointsBase {

    @Produces({MediaType.APPLICATION_JSON})
    @GET
    public Response getAlertTest( ) {
        return startList("alertTestParamExplicitQuery", getRequestWithParms()).respond();
    }
    
}
