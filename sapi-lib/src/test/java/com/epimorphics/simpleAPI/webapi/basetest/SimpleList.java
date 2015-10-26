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

import com.epimorphics.simpleAPI.results.ResultOrStream;
import com.epimorphics.simpleAPI.webapi.EndpointsBase;

@Path("basetest")
public class SimpleList extends EndpointsBase {

    @GET
    @Path("list")
    @Produces({MediaType.APPLICATION_JSON, FULL_MEDIA_TYPE_TURTLE, FULL_MEDIA_TYPE_CSV})
    public ResultOrStream listNested() {
        return listResponse( getRequest(), "listTest2");
    }

    @POST
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ResultOrStream listNestedPost(String body) {
        return listResponse( getRequest(body), "listTest2");
    }
    
}
