# Documentation needed

ModelSpec

Projections

Revised app.conf for how to bind RequestProcessors

Nested select is default

Aliases filter

Template handling

Per-endpoint processors

Per-endpoint bindings

excludeValue (URIs only)

clarified URI bindings in velocity context:

  $root    - server relative root for the application, based on configured base URI not request so works behind a proxy
  $uri     - the requested URI, based on configured base plus request path
  $url     - the requested URL (server relative URI) as a builder 
  $request - the full servlet request
