package com.epimorphics.simpleAPI.app.webapi;
/******************************************************************
 * File:        DefaultHanlder.java
 * Created by:  Dave Reynolds
 * Created on:  14 Jan 2016
 * 
 * (c) Copyright 2016, Epimorphics Limited
 *
 *****************************************************************/


import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.epimorphics.simpleAPI.webapi.EndpointsBase;

@Path("/{__path:.*}")
public class DefaultHandler extends EndpointsBase {

    @GET
    public Response handleDefaultJson() {
        return defaultResponse();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response handleDefault(String body) {
        return defaultResponse(body);
    }
}