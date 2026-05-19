## Historical context

This sapi library has (mostly) been superseded by sapi-nt.

There are two versions of it which remain in use in legacy services - sapi 2.x and sapi 3.x.

### sapi 2.x/4.x

This version is used in fsa-cat-api and standard-reports-manager.

This is managed on the `main` branch (this branch). The 2.x release is also maintained on the `maintenance/2.x` branch.

### sapi 3.x

This version only used in one private legacy API.

This is managed on branch `sapi3`.

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

Changes since 4.0.3 now recorded in [CHANGELOG.md](CHANGELOG.md).

`4.0.3`  - minor dependency updates

`4.0.2`  - minor dependency updates

`4.0.1`  - minor patches to dependencies to remove CVEs

`4.0.0`  - major new release of sapi from the 2.x line. Addresses multiple CVEs and moves to Java 21 and Jena 5

`2.3.15` - minor update of dependencies to address CVEs

`2.3.14` - allow use of CONSTRUCT in item queries

`2.3.13` - improve return status codes, only retry on 50X errors

`2.3.12` - find missing logging for query retry

`2.3.11` - add query retry (3s, not yet configurable)

`2.3.10` - fix missing transaction id in requests

`2.3.9` - improve logging of error responses, bump appbase for improved RunShell logging

`2.3.8` - added query logging and MDC.transaction_id

`2.3.7` - improve logging (log query, MDC.returned_rows)

`2.3.6` - drop `request_id` logging unless incoming `x-request-id` header

`2.3.5` - updated tomcat to 9.0, MDC logging fields, metrics support
