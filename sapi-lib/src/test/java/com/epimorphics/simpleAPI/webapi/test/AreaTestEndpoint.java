/******************************************************************
 * File:        AreaTestEndpoint.java
 * Created by:  Dave Reynolds
 * Created on:  5 Jan 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.webapi.test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.epimorphics.simpleAPI.attic.webapi.EndpointsBase;

@Path("id/floodAreas")
public class AreaTestEndpoint extends EndpointsBase {

    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    @GET
    public Response getFloodArea(@PathParam("id") String id ) {
        return startDescribe("areaDescribe").respond();
    }
    
}
