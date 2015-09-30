/******************************************************************
 * File:        byURITest.java
 * Created by:  Dave Reynolds
 * Created on:  9 Feb 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.attic.webapi.test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.epimorphics.simpleAPI.attic.webapi.EndpointsBase;

@Path("byURITest")
public class ByURITest extends EndpointsBase {

    @Produces({MediaType.APPLICATION_JSON})
    @GET
    public Response get(@QueryParam("uri") String uri) {
        return startDescribeByURI(uri).respond();
    }
}
