/******************************************************************
 * File:        DateFormatUtil.java
 * Created by:  Dave Reynolds
 * Created on:  31 Jul 2017
 * 
 * (c) Copyright 2017, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.util;

import java.util.regex.Pattern;

import javax.ws.rs.core.Response.Status;

import com.epimorphics.appbase.webapi.WebApiException;
import com.epimorphics.simpleAPI.requests.Request;

/**
 * Utility to support hanlding of dates in API requests.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class DateFormatUtil {
    
    protected static final String DATE_BLOCK = "[0-9]{4}-[01][0-9]-[0-3][0-9]";
    protected static final String TIME_BLOCK = "[0-6][0-9]:[0-6][0-9]:[0-6][0-9](\\.[0-9]+)?";
    protected static final String TZONE_BLOCK = "([+-][0-6][0-9]:[0-6][0-9])|Z";
    protected static final String GYM_BLOCK = "[0-9]{4}-[01][0-9]";
    protected static final Pattern DATETIME_PATTERN = Pattern.compile( String.format("-?%sT%s(%s)?", DATE_BLOCK, TIME_BLOCK, TZONE_BLOCK) );
    protected static final Pattern DATE_PATTERN = Pattern.compile( String.format("-?%s(%s)?", DATE_BLOCK, TZONE_BLOCK) );
    protected static final Pattern TIME_PATTERN = Pattern.compile( String.format("%s(%s)?", TIME_BLOCK, TZONE_BLOCK) );
    protected static final Pattern GYEARMONTH_PATTERN = Pattern.compile( String.format("%s(%s)?", GYM_BLOCK, TZONE_BLOCK) );
    protected static final Pattern ANYDATE_PATTERN = Pattern.compile( String.format("-?(%sT%s|%s|%s|%s)(%s)?", DATE_BLOCK, TIME_BLOCK, DATE_BLOCK, TIME_BLOCK, GYM_BLOCK, TZONE_BLOCK) );

    public static String checkIsDate(Request request, String parameter) {
        String date = request.getFirst(parameter);
        if ( ! DATE_PATTERN.matcher(date).matches() ) {
            throw new WebApiException(Status.BAD_REQUEST, String.format("Bad format for %s, should be a legal ISO date but was %s", parameter, date));
        }
        return date;
    }
    
    public static String checkIsDateTime(Request request, String parameter) {
        String date = request.getFirst(parameter);
        if ( ! DATETIME_PATTERN.matcher(date).matches() ) {
            if ( ! DATE_PATTERN.matcher(date).matches() ) {
                throw new WebApiException(Status.BAD_REQUEST, String.format("Bad format for %s, should be a legal ISO date or datetime but was %s", parameter, date));
            } else {
                return date + "T00:00:00Z";
            }
        }
        return date;
    }

}
