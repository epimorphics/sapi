/******************************************************************
 * File:        SimpleList.java
 * Created by:  Dave Reynolds
 * Created on:  5 Oct 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.webapi.basetest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.epimorphics.simpleAPI.query.QueryBuilder;
import com.epimorphics.simpleAPI.query.impl.SparqlQueryBuilder;
import com.epimorphics.simpleAPI.requests.Call;
import com.epimorphics.simpleAPI.requests.Request;
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

    public static final String FILTER_PARAM = "filter";
    @GET
    @Path("listNestedSelect")
    @Produces({MediaType.APPLICATION_JSON, TURTLE, CSV, MediaType.TEXT_HTML})
    public Response listNestedSelect() {
        Request request = getRequest();
        Call call = new Call(getAPI(), "listNestedSelect", request);
        if ( request.hasAvailableParameter(FILTER_PARAM) ) {
            String filter = " { ?id egn:group ?group . FILTER(?group = '%s')} ".replace("%s", request.getFirst(FILTER_PARAM));
            request.consume(FILTER_PARAM);
            call.updateQueryBuilder( (QueryBuilder qb) -> ((SparqlQueryBuilder)qb).filter(filter) );
        }
        return respondWith( call.getResults() );
    }

    @GET
    @Path("listUS")
    @Produces({MediaType.APPLICATION_JSON, TURTLE, CSV, MediaType.TEXT_HTML})
    public Response listTestUS() {
        return listResponse( getRequest(), "listTestUS");
    }

    @POST
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response listTest2Post(String body) {
        return listResponse( getRequest(body), "listTest2");
    }
    
}
