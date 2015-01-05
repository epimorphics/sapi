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

import com.epimorphics.json.JSONWritable;
import com.epimorphics.simpleAPI.webapi.EndpointsBase;

@Path("id/floodAreas")
public class AreaTestEndpoint extends EndpointsBase {

    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    @GET
    public JSONWritable getFloodArea(@PathParam("id") String id ) {
        String uri = getRequestedURI();
        return describeItemJson("DESCRIBE <" + uri + "> ?warning WHERE "
                 + "{OPTIONAL{<" + uri + "> <http://environment.data.gov.uk/flood-monitoring/def/core/currentWarning> ?warning}}", 
                 "item");
    }
    
}
