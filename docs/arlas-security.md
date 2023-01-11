# ARLAS security

This page describes how to configure ARLAS in order to control access to resources and data.

## Authentication
Authentication is the process of identifying a user and is a prerequisite to [Authorisation](#authorisation). 

Depending on the use case, some configuration must be set. Refer to [ARLAS-server authentication configuration](arlas-server-configuration.md)

The following use cases are illustrated with the ARLAS-server service but are valid for all other ARLAS back-ends such as [ARLAS-persistence](https://github.com/gisaia/ARLAS-persistence).

* **Use case 1**: public access, no endpoint is protected (e.g. dev, test deployment)
    - ARLAS-server:  set `arlas_auth_policy_class` to `io.arlas.filter.impl.NoPolicyEnforcer`.
    - ARLAS-wui: set [authentication.use_authent](http://docs.arlas.io/arlas-tech/current/arlas-wui-configuration/) to `false` in `settings.yaml`. 

* **Use case 2**: public access, some endpoints are protected (e.g. demo, freemium deployment)
    - ARLAS-server: set `arlas_auth_policy_class` to the policy enforcer class you want to use
    - ARLAS-wui: set [authentication.use_authent](http://docs.arlas.io/arlas-tech/current/arlas-wui-configuration/) to `false` or `true` in `settings.yaml`, it depends on whether ARLAS-wui must access protected end-points or not.

* **Use case 3**: protected access (e.g. customer deployment)
    - ARLAS-server: set `arlas_auth_policy_class` to the policy enforcer class you want to use
    - ARLAS-wui: set [authentication.use_authent](http://docs.arlas.io/arlas-tech/current/arlas-wui-configuration/) to `true` and [authentication.force_connect](http://docs.arlas.io/arlas-tech/current/arlas-wui-configuration/) to true in `settings.yaml`.

        !!! info "ARLAS-wui authentication"
            Get a complete functional [authentication configuration](http://docs.arlas.io/arlas-tech/current/arlas-wui-security) of ARLAS-wui

!!! note "Note"
    ARLAS-server 13.7.0+ is required in order to support wildcards in `arlas_auth.public_uris`  

When authentication is enabled, ARLAS-server expects to receive an HTTP header `Authorization: bearer <token>` from an identity provider.  
The token must be an RSA256 encrypted JWT token as specified by [RFC7519](https://tools.ietf.org/html/rfc7519).  
Example of decoded JWT token payload:
```json
{
  "http://arlas.io/permissions": [
    "rule:collections:GET:100",
    "rule:explore/_list:GET:200",
    "variable:organisation:acme",
    "header:Partition-Filter:${organisation}",
    "rule:explore/${organisation}/_search:GET:300"
  ],
  "http://arlas.io/roles": [
    "role:ArlasExplorer"
  ],
  "nickname": "john.smith",
  "name": "john.smith@acme.com",
  "picture": "https://...",
  "updated_at": "2019-09-03T09:27:47.265Z",
  "iss": "https://...",
  "sub": "...",
  "aud": "...",
  "iat": 1567502869,
  "exp": 1882862869
}
```

## Authorisation

It is assumed that the token of the user provides the following information:

- The principal of the user
- A set of permissions in a claim `http://arlas.io/permissions`
- A set of roles in a claim `http://arlas.io/roles`

Permissions can be composed of:

- **Variables** as key-value pairs, e.g. `variable:${key}:${value}`  
Variables are injected in rules and headers. 

*For instance, in the following permissions*

```asciidoc
"variable:organisation:acme",
"header:arlas-organization:${organisation}"
```

*will inject a header `arlas-organization:acme` in the request.*

- **A list of headers** to be injected to all the requests that require a restricted access 
(such as the partition-filter), e.g. `header:${header}:${value}`

!!! info "Tip"
    Defining the same header name multiple times will result in its values to be comma-concatenated and injected in a single header of that name.


- **A set of rules**, e.g. `rule:${resource}:${verbs}`, composed of:
    * ${resource} is the resource path pattern, relative to /arlas/ (regular expressions can be used)
    * ${verbs} is the comma separated list of allowed verbs (GET, POST...) for accessing the resources matching the resource path pattern

*For example, a user having the rules:*

```asciidoc
rule:/collection/.*:GET,
rule:/explore/.*/_search:GET
```

* will be able to explore the `collections` and to `search` in all of them, but won’t be able to add or delete collections (only `GET` verb is allowed for collections) and won’t be able to make aggregations (the resource `_aggregate` is not defined).*
 
## Protect data access

In order to specify a finer access control to the data, the headers `partition-filter` and `column-filter` 
can be set using the header mechanism described above.

#### Column-filter

The header `column-filter` allows you to pass a list of collections and fields of the data.
Only the collections and fields present in this list are visible in the response of a request.

This allows certain collections and fields to be restricted to certain users.

!!! info "column-filter syntax"
    See the `column-filter` syntax in [ARLAS Exploration API configuration section](http://docs.arlas.io/arlas-tech/current/arlas-api-exploration/#column-filtering).


#### Partition-filter

The header `partition-filter` allows you to pass an ARLAS filter to apply to the request.

This allows certain data to be restricted to certain users.

!!! info "partition-filter syntax"
    See the `partition-filter` syntax in [ARLAS Exploration API configuration section](http://docs.arlas.io/arlas-tech/current/arlas-api-exploration/#column-filtering).

#### Organisation filter
If a header `arlas-organization` is present (can be empty, or be a comma separated list of values), it will be used
to check if the collection's organisations (owner or shared with) match one of the provided values.
If no header `arlas-organization` then no check is done on the collection's organisations.