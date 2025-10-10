## Historical context

This sapi library has (mostly) been superseded by sapi-nt. 

There are two versions of it which remain in use in legacy services - sapi 2.x and sapi 3.x.

### sapi 2.x

This version is used in fsa-cat-api and standard-reports-manager. 

This is managed on the `main` branch.

### sapi 3.x

This version only used in one private legacy API.

This is managed here on branch `sapi3`.

Security fixes to `main` should be ported `sapi3` as well.

Branch `sapi3-orig` is a preserved version of the initial sapi3 development to support archeology.

# Simple API

Library and starter application for providing simple JSON APIs onto published Linked Data.

Endpoints can be manually created and customized in Java (e.g. using Jersey) or can be configured simple yaml files.

## Release

Update version numbers as appropriate then:

```
mvn clean deploy
```

## Changelog

`3.2.0` - merged sapi2 improvements to sapi3 branch

`2.3.9` - improve logging of error responses, bump appbase for improved RunShell logging
`2.3.8` - added query logging and MDC.transaction_id

`2.3.7` - improve logging (log query, MDC.returned_rows)

`2.3.6` - drop `request_id` logging unless incoming `x-request-id` header

`2.3.5` - updated tomcat to 9.0, MDC logging fields, metrics support
