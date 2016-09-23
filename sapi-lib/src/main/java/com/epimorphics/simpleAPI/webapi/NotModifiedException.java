/******************************************************************
 * File:        NotModifiedException.java
 * Created by:  Dave Reynolds
 * Created on:  23 Sep 2016
 * 
 * (c) Copyright 2016, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.webapi;

import java.util.Date;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class NotModifiedException extends WebApplicationException {
    private static final long serialVersionUID = 6895043905741087825L;

    public NotModifiedException(Date lastModified) {
        super( Response.status(Status.NOT_MODIFIED).lastModified(lastModified).build() );
    }
    
}
