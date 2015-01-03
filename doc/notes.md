# SAPI design notes

## Query construction cases

Item endpoint
Construct request API by removing context name and add base URI

   * describe request URI (default)

   * use custom describe from spec, substitute in the request URI

   * use custom construct from spec, substitute in the request URI

List endpoint
Extract parameter bindings (path params, query params).

   * Explicit SELECT query, just substitute

   * Implicit SELECT query from nesting specification

   * Elda style SELECT + describes?

   * Handle paging
   * Handle geo search

## Result rendering cases 

Item endpoint: Render using local names. Optional shortname mapping?

List endpoint:

   * If any optional/multi then coalesce.

   * Render SELECT variables flat

   * Render SELECT variables from nesting spec. Support multi->array.

### Request URI -> query

For item URI remove context name add base URI.
Expand to query (default describe, custom describe from config, construct)

For list URI extract parameters from request (Jersey, general template)
Expand query (explicit SPARQL with substitutions, SPARQL from mapping spec)

### Result -> JSON


## Questions and checks

   * Use Registry trie support or factor something out of Elda?

   * Test use of general router restlet

   * Remove context name when constructing URI 

## TODO

   * Clean up EndpointBase

   * Fall back - serve raw URIs

   * include @id in the metadata element

   * document
     (including reasons - control, flexible, simple)