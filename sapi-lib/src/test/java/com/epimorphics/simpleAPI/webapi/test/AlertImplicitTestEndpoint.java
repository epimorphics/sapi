/******************************************************************
 * File:        AlertImplicitTestEndpoint.java
 * Created by:  Dave Reynolds
 * Created on:  10 Jan 2015
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

import com.epimorphics.simpleAPI.attic.core.RequestParameters;
import com.epimorphics.simpleAPI.attic.webapi.EndpointsBase;

@Path("implicitQueryTest")
public class AlertImplicitTestEndpoint extends EndpointsBase {

    @Produces({MediaType.APPLICATION_JSON})
    @GET
    public Response getAlertTest( ) {
        return startList("alertTestImplicitQuery", getRequestWithParms()).respond();
    }
    

    @Produces({"text/csv"})
    @GET
    public Response getAlertTestCSV( ) {
        RequestParameters request = getRequestWithParms();
        request.addModifier("ORDER BY ?id");
        return startList("alertTestImplicitQuery", request).asCSV().respond();
    }
}
