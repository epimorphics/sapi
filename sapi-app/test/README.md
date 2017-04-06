Sample data to verify sapi-app is installed

Link or copy this directory to `/opt/sapi` and then start the sapi-app war.

Provides two list endpoints and default describe end point (relative to an assumed base of `http://environment.data.gov.uk/registry`

## Sample queries

List end points:

    curl -i http://localhost:8080/def/sampling-point-types
    curl -i http://localhost:8080/def/sampling-point-type-groups

Basic filter/sort:
    
    curl -i http://localhost:8080/def/sampling-point-types?notation=DB
    curl -i http://localhost:8080/def/sampling-point-types?_sort=notation
    curl -i http://localhost:8080/def/sampling-point-types?_sort=-notation
    curl -i http://localhost:8080/def/sampling-point-types?_sort=-notation&_limit=5

Item (describe) endpoint:
 
    curl -i http://localhost:8080/def/water-quality/sampling_point_types/AA
