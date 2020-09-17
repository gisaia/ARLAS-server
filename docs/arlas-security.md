# ARLAS security

This page describes how to configure ARLAS in order to control access to resources and data.

## Authentication
Authentication is the process of identifying a user and is a prerequisite to [Authorisation](#authorisation).  
Depending on the use case, some configuration must be set. Refer to [ARLAS server authentication configuration](arlas-server-configuration.md)
The following use cases are illustrated with the ARLAS-Server service but are valid for all other ARLAS backe-ends such ARLAS-persistence.

* Use case 1: public access, no endpoint is protected (e.g. dev, test deployment)
  - ARLAS server:  set `arlas_auth.enabled` to `false`
  - ARLAS wui: set [authentication.use_authent](http://docs.arlas.io/arlas-tech/current/arlas-wui-configuration/) to `false` in `settings.yaml`. 

* Use case 2: public access, some endpoints are protected (e.g. demo, freemium deployment)
  - ARLAS server:
       * set `arlas_auth.enabled` to `true`
       * set `arlas_auth.certificate_url` to the url of the .pem  certificat of the public key link to your identity provider.
       * set `arlas_auth.public_uris` to the needed value, 
  e.g. `swagger.*:*,explore/.*:*` will only allow public access to URIs `/swagger.*` and 
  `/explore/.*`
  - ARLAS wui: set [authentication.use_authent](http://docs.arlas.io/arlas-tech/current/arlas-wui-configuration/) to `false` or `true` in `settings.yaml`, it depends on whether ARLAS wui must access protected end-points or not.

* Use case 3: protected access (e.g. customer deployment)
  - ARLAS server:  
       * set `arlas_auth.enabled` to `true`
       * set `arlas_auth.certificate_url` to the url of the .pem  certificat of the public key link to your identity provider.
       * set `arlas_auth.public_uris` to the needed value, 
  e.g. `swagger.*:*` will only allow public access to URIs `/swagger.*`
  - ARLAS wui: set [authentication.use_authent](http://docs.arlas.io/arlas-tech/current/arlas-wui-configuration/) to `true` and [authentication.force_connect](http://docs.arlas.io/arlas-tech/current/arlas-wui-configuration/) to true in `settings.yaml`. Get more details about [the other security properties to set in ARLAS wui](http://docs.arlas.io/arlas-tech/current/arlas-wui-security)

NB1: ARLAS server 13.7.0+ is required in order to support wildcards in `arlas_auth.public_uris`  

When authentication is enabled, ARLAS server expects to receive an HTTP header `Authorization: bearer <token>` from an identity provider.  
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
- Variables as key-value pairs, e.g. `variable:${key}:${value}`  
Variables are injected in rules and headers. For instance:
```asciidoc
    "variable:organisation:acme",
    "header:arlas-organisation:${organisation}",
```
will inject a header `arlas-organisation:acme` in the request.
- A list of headers to be injected to all the requests that require a restricted access 
(such as the partition-filter), e.g. `header:${header}:${value}`
- A set of rules, e.g. `rule:${resource}:${verbs}:${priority}`, composed of:
  * ${resource} is the resource path pattern, relative to /arlas/ (regular expressions can be used)
  * ${verbs} is the comma separated list of allowed verbs (GET, POST...) for accessing the resources matching the resource path pattern
  * ${priority} is the rule’s priority. 1 is the lowest priority.

Example:  
For example a user having the rules:
```
rule:/collection/.*:GET:1
rule:/explore/.*/_search:GET:1
```
Will be able to explore the collections and to search in all of them but won’t be able to add or delete collections and won’t be able to make aggregations.
 
## Protect data access

In order to specify a finer access control to the data, the headers `partition-filter` and `column-filter` 
can be set using the header mechanism described above.

#### Column-filter

The header `column-filter` allows you to pass a list of fields of the data.
Only the fields present in this list are visible in the response during a request.
This allows certain fields to be restricted to certain users.
See relevant sections in [ARLAS Exploration API configuration](arlas-api-exploration.md/#column-filtering).


#### Partition-filter

The header `partition-filter` allows you to pass an ARLAS filter to apply on the request.
This allows certain data to be restricted to certain users.
See relevant sections in [ARLAS Exploration API configuration](arlas-api-exploration.md/#partition-filtering).


