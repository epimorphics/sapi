/******************************************************************
 * File:        DefaultDescribeEndpoint.java
 * Created by:  Dave Reynolds
 * Created on:  5 Jan 2015
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

import com.epimorphics.simpleAPI.attic.webapi.EndpointsBase;

@Path("id")
public class DefaultDescribeEndpoint extends EndpointsBase {
    
    @Path("/{id : .+}")
    @Produces({MediaType.APPLICATION_JSON})
    @GET
    public Response geDefault() {
        return startDescribe( ).respond();
    }
    
    @Path("/{id : .+}")
    @Produces({FULL_MEDIA_TYPE_TURTLE,MEDIA_TYPE_RDFXML})
    @GET
    public Response geDefaultRDF() {
        return startDescribe().asRDF().respond();
    }
    
    @Path("/{id : .+}")
    @Produces({MediaType.TEXT_HTML})
    @GET
    public Response geDefaultHtml() {
        return startDescribe().asHtml("testDescribe.vm", "arg", "test arg").respond();
    }
    
}
