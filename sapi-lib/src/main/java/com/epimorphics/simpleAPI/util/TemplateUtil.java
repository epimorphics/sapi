/******************************************************************
 * File:        TemplateUtil.java
 * Created by:  Dave Reynolds
 * Created on:  5 May 2017
 * 
 * (c) Copyright 2017, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.epimorphics.simpleAPI.requests.Request;

/**
 * Utility for performing template substitutions of ${param} from request parameters.
 * Consumes the parameters.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class TemplateUtil {

    public static final Pattern VAR_PATTERN = Pattern.compile("\\$\\{[^}]*\\}");
    public static final String URI_VAR = "uri";
    
    public static String instatiateTemplate(String template, Request request) {
        Matcher m = VAR_PATTERN.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String found = m.group(0);
            String var = found.substring(2, found.length() - 1);
            if ( URI_VAR.equals(var) ) {
                m.appendReplacement(sb, request.getRequestedURI());
            } else {
                m.appendReplacement(sb, request.getFirst(var));
            }
            request.consume(var);
        }
        m.appendTail(sb);
        return sb.toString();
    }
    
    public static boolean isTemplate(String template) {
        return VAR_PATTERN.matcher(template).find();
    }
    
}
