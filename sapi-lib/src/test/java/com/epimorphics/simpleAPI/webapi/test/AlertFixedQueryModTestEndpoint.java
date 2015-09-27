/******************************************************************
 * File:        AlertFixedQueryTestEndpoint.java
 * Created by:  Dave Reynolds
 * Created on:  6 Jan 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.webapi.test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.epimorphics.simpleAPI.attic.core.RequestParameters;
import com.epimorphics.simpleAPI.attic.webapi.EndpointsBase;

@Path("fixedQueryModTest")
public class AlertFixedQueryModTestEndpoint extends EndpointsBase {

    @Produces({MediaType.APPLICATION_JSON})
    @GET
    public Response getAlertTest(@QueryParam("min-severity") String severity ) {
        RequestParameters request = getRequestWithParms();
        if (severity != null) {
            request.addFilter("FILTER(?severityLevel <= " + severity + ")");
        }
        return startList("alertTestExplicitQueryMod", request).respond();
    }
    
}
