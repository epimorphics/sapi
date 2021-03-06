/******************************************************************
 * File:        ViewPath.java
 * Created by:  Dave Reynolds
 * Created on:  4 Oct 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.views;

import java.util.LinkedList;
import java.util.List;
import static com.epimorphics.simpleAPI.core.ConfigConstants.ROOT_VAR;
import static java.util.stream.Collectors.*;

/**
 * Represents a path within a ViewTree, comprising a sequence of shortnames. 
 * The empty path and refers to the URI of the root resource.
 */
public class ViewPath {
    protected LinkedList<String> path;
    
    public ViewPath() {
        path = new LinkedList<>();
    }
    
    public ViewPath(String...elts) {
        path = new LinkedList<>();
        for (String elt : elts) {
            path.add(elt);
        }
    }
    
    public ViewPath(LinkedList<String> path) {
        this.path = path;
    }
    
    /**
     * Return the first element in the path or none if the path is empty
     */
    public String first() {
        return path.getFirst();
    }
    
    /**
     * Return the last element in the path
     */
    public String last() {
        return path.getLast();
    }
    
    /**
     * Return true if the path is empty
     */
    public boolean isEmpty() {
        return path.isEmpty();
    }
    
    /**
     * Return true of the path is a step long
     */
    public boolean isSingleton() {
        return path.size() == 1;
    }

    /**
     * Remove the first element in the path (returns that first element)
     */
    public String removeFirst() {
        return path.removeFirst();
    }
    
    /**
     * Return the remainder of the path after removing the first element
     */
    public ViewPath rest() {
        LinkedList<String> newpath = new LinkedList<>(path);
        newpath.removeFirst();
        return new ViewPath(newpath);
    }
    
    /**
     * Return the path as a list of element names
     */
    public List<String> asList() {
        return path;
    }
    
    /**
     * Add an element to the end of this path (side effecting)
     */
    public ViewPath add(String elt) {
        path.add(elt);
        return this;
    }
    
    /**
     * Return a new path with the given element added at the end
     */
    public ViewPath withAdd(String elt) {
        LinkedList<String> newpath = new LinkedList<>(path);
        newpath.add(elt);
        return new ViewPath(newpath);
    }
    
    /**
     * Return the path in dotted notation
     */
    public String asDotted() {
        return path.stream().collect(joining("."));
    }
    
    /**
     * Return the path as a variable name in a query using "_" to join path elements and "__" to escape "_" in segment names
     */
    public String asVariableName() {
        if (isEmpty()) {
            return ROOT_VAR;
        }
        return path.stream().map(s -> s.replace("_", "__")).collect(joining("_"));
    }
    
    /**
     * Construct a path from a variable name using the same conventions as asVariableName
     */
    public static ViewPath fromVariableName(String varname) {
        String path = varname.replace("__", "|").replace("_", ".").replace("|", "_");
        return fromDotted(path);
    }
    
    /**
     * Construct a path from a dotted-notation string
     */
    public static ViewPath fromDotted(String path) {
        if (path.isEmpty()) {
            return new ViewPath();
        } else {
            return new ViewPath( path.split("\\.") );
        }
    }
    
    @Override
    public String toString() {
        return asDotted();
    }
}
