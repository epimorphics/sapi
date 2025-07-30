## Historical context

The sapi library has mostly been superseded by sapi-nt. However, it remains in use in cairnj (FSA Catalog API) and Sanoma as legacy.

It was used in legacy EA services which we no longer run and has likely been superseded there.

The master branch is for current 3.x releases.

Cairnj, and thus FSA catalog, depend on the 2.x family. Hot fixes and security fixes this family are maintained on the sapi2-fix branch.


# Simple API

Library and starter application for providing simple JSON APIs onto published Linked Data.

Endpoints can be manually created and customized in Java (e.g. using Jersey) or can be configured simple yaml files.

## Release

```
mvn clean deploy
```
