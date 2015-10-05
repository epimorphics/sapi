/******************************************************************
 * File:        SimpleList.java
 * Created by:  Dave Reynolds
 * Created on:  5 Oct 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.webapi.basetest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.epimorphics.simpleAPI.webapi.EndpointsBase;
import com.epimorphics.simpleAPI.results.ResultStream;

@Path("basetest")
public class SimpleList extends EndpointsBase {

    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public ResultStream listNested() {
        return listResponse( getRequest(), "listTest2");
    }
    
}
