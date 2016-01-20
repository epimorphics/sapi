/******************************************************************
 * File:        SimpleList.java
 * Created by:  Dave Reynolds
 * Created on:  5 Oct 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.webapi.basetest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.epimorphics.simpleAPI.webapi.EndpointsBase;

@Path("basetest")
public class SimpleList extends EndpointsBase {

    @GET
    @Path("list")
    @Produces({MediaType.APPLICATION_JSON, TURTLE, CSV, MediaType.TEXT_HTML})
    public Response listTest2() {
        return listResponse( getRequest(), "listTest2");
    }

    @GET
    @Path("listSuppress")
    @Produces({MediaType.APPLICATION_JSON, TURTLE, CSV, MediaType.TEXT_HTML})
    public Response listTestSuppressed() {
        return listResponse( getRequest(), "listTestSuppressed");
    }

    @GET
    @Path("listNested")
    @Produces({MediaType.APPLICATION_JSON, TURTLE, CSV, MediaType.TEXT_HTML})
    public Response listNested() {
        return listResponse( getRequest(), "listTestNest");
    }

    @POST
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response listTest2Post(String body) {
        return listResponse( getRequest(body), "listTest2");
    }
    
}
