# ARLAS Security general concepts

## What is it?
This module aims at adding an Identity and Access Management (IAM) to the ARLAS stack.  
IAM provides authentication (user login) and authorisation (permissions to access data and APIs) services to ARLAS
components: server, WUI, hub, builder...

The stack can be started with or without ARLAS security management.

ARLAS security management is composed of 2 main components:

1. an implementation of the ARLAS PolicyEnforcer (interface available in the ARLAS-server/arlas-commons module: `io.arlas.filter.core.PolicyEnforcer`)
2. an authentication and authorisation server : [Keycloak](arlas-keycloak.md) or [ARLAS-IAM](arlas-iam.md)

## Policy Enforcers configuration
The policy enforcers are in the `arlas-commons` module.  
The implementation to activate must be defined in the backend component configuration:


| Environment variable    | configuration variable  | Default                               | Possible values                                                                                                                      |
|-------------------------|-------------------------|---------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------|
| ARLAS_AUTH_POLICY_CLASS | arlas_auth_policy_class | io.arlas.filter.impl.NoPolicyEnforcer | io.arlas.filter.impl.Auth0PolicyEnforcer<br/>io.arlas.filter.impl.KeycloakPolicyEnforcer<br/>io.arlas.filter.impl.HTTPPolicyEnforcer |

Further global configuration is required:

| Environment variable         | configuration variable                 | Default                                                   | Policy enforcer |
|------------------------------|----------------------------------------|-----------------------------------------------------------|-----------------|
| ARLAS_AUTH_PUBLIC_URIS       | arlas_auth.public_uris                 | swagger.\*:\*                                             | All             |
| ARLAS_HEADER_USER            | arlas_auth.header_user                 | arlas-user                                                | All             |
| ARLAS_HEADER_GROUP           | arlas_auth.header_group                | arlas-groups                                              | All             |
| ARLAS_ANONYMOUS_VALUE        | arlas_auth.anonymous_value             | anonymous                                                 | All             |

## Protection rules

ARLAS security management allows the enforcement of two kinds of protection mechanisms. They are achieved by the definition of rules and
(HTTP) headers that are collected by the *Policy Enforcer* and transferred to the ARLAS backend component it protects.  
They are strings of characters with a specific formatting that are expected to be found in the access token and/or permission token
(RPT: requesting party token) in specific claims.

### Protection of ARLAS WUI dashboards: groups

By default dashboards are only viewable and editable by their creator (owner).  
One can share (view and/or edit rights) a dashboard with any group of users they already belong to,
e.g. if `userA` belongs to groups `grp1` and `grp2`, they can share their dashboards with part or all of these groups,
and these groups only.

It implies that prior to sharing a dashboard, groups must be created and assigned to users.  
This is done by assigning specific roles whose names are formatted as `group/config.json/GRPNAME`,
e.g. `group/config.json/spot6`(in this example, the group name is `spot6` and will be displayed as such in ARLAS hub).

A good practice is to assign data protection headers to these roles in order to enforce an even better protection level, e.g.:

- `group/config.json/spot6`
- `h:column-filter:spot6_*:*`

### Protection of ARLAS APIs: rules
The actions a user can do, i.e. API endpoints and HTTP verbs, can be limited to a configurable list of URIs.

The expected format is `rule:resource:verbs` or `r:resource:verbs`.

The `resource` part of the rule is used as a regex to match a requested URI.

!!! example
    - `r:explore/.*:GET,POST`
    - `r:collections/.*:GET`

These rules do not need to be configured as they are already associated to default roles defined in the arlas-commons module.  

These roles are:

- `role/arlas/user` (rules to view data)
- `role/arlas/tagger` (rules to use the Tagger backend)
- `role/arlas/builder` (rules to create/edit/delete ARLAS WUI dashboards)
- `role/arlas/owner` (rules to manage collections in ARLAS server and organisations/users in [ARLAS-IAM](arlas-iam.md) server)
- `role/m2m/importer` (rules to import collections via the dedicated ARLAS server endpoint, mainly used by M2M processes)
- `role/iam/admin` (rules to manage organisations and users in [ARLAS-IAM](arlas-iam.md) server)

The associated rules configured for these roles can be found in the file `arlas-commons/src/main/resources/roles.yaml`.

## Protection flow

ARLAS backend components follow several steps in order to enforce security, once a user is logged in and has acquired ID and access tokens.

1. The request to a given ARLAS URI is intercepted by the configured *Policy Enforcer*:
    - if no HTTP header `Authorization: bearer <access token>` is provided
        - if the URI is configured to be public (via `ARLAS_AUTH_PUBLIC_URIS`) then access is granted
        - else access is rejected with code `401 Unauthorized`
    - else continue to next step
2. Get permission token (RPT) from auth server (optionally filtered with an organisation name via a `arlas-org-filter` header, only for [ARLAS-IAM](arlas-iam.md))
3. Get `subject` (user ID) from RPT and inject it in a configurable HTTP header (via `ARLAS_HEADER_USER`)
4. Get roles claim from RPT (via `ARLAS_CLAIM_ROLES`) and inject groups in a configurable HTTP header (via `ARLAS_HEADER_GROUP`)
5. Get permissions from RPT (via `ARLAS_CLAIM_PERMISSIONS`) and compare requested URI with allowed and public URIs (including HTTP verb, i.e. GET, POST, PUT, DELETE...)
    - if not allowed then access is rejected with code `403 Forbidden`
    - else add headers that are defined in permissions to the request
6. Forward resulting request to the ARLAS backend component

## Authentication

Authentication is the process of identifying a user and is a prerequisite to [Authorisation](#authorisation).

Depending on the use case, some configuration must be set. Refer to [ARLAS-server authentication configuration](arlas-server-configuration.md)

The following use cases are illustrated with the ARLAS-server service but are valid for all other ARLAS back-ends such as [ARLAS-persistence](../ARLAS-persistence/arlas-persistence-overview.md).

* **Use case 1**: public access, no endpoint is protected (e.g. dev, test deployment)
    - ARLAS-server:  set `arlas_auth_policy_class` to `io.arlas.filter.impl.NoPolicyEnforcer`.
    - ARLAS-wui: set [authentication.use_authent](../ARLAS-wui/arlas-wui-security.md#authentication) to `false` in `settings.yaml`.

* **Use case 2**: public access, some endpoints are protected (e.g. demo, freemium deployment)
    - ARLAS-server: set `arlas_auth_policy_class` to the policy enforcer class you want to use
    - ARLAS-wui: set [authentication.use_authent](../ARLAS-wui/arlas-wui-security.md#authentication) to `false` or `true` in `settings.yaml`, it depends on whether ARLAS-wui must access protected end-points or not.

* **Use case 3**: protected access (e.g. customer deployment)
    - ARLAS-server: set `arlas_auth_policy_class` to the policy enforcer class you want to use
    - ARLAS-wui: set [authentication.use_authent](../ARLAS-wui/arlas-wui-security.md#authentication) to `true` and [authentication.force_connect](../ARLAS-wui/arlas-wui-security.md#authentication) to true in `settings.yaml`.


!!! info "ARLAS-wui authentication"
    Get a complete functional [authentication configuration](../ARLAS-wui/arlas-wui-security.md) of ARLAS-wui


When authentication is enabled, ARLAS-server expects to receive an HTTP header `Authorization: bearer <token>` from an identity provider.  

The token must be an RSA256 encrypted JWT token as specified by [RFC7519](https://tools.ietf.org/html/rfc7519){:target="_blank"}.  

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

    !!! example
        The following permissions
        ```asciidoc
        "variable:organisation:acme",
        "header:arlas-organization:${organisation}"
        ```
        will inject a header `arlas-organization:acme` in the request.

- **A list of headers** to be injected to all the requests that require a restricted access (such as the partition-filter), e.g. `header:${header}:${value}`

    !!! info "Tip"
        Defining the same header name multiple times will result in its values to be comma-concatenated and injected in a single header of that name.


- **A set of rules**, e.g. `rule:${resource}:${verbs}`, composed of:
    * ${resource} is the resource path pattern, relative to /arlas/ (regular expressions can be used)
    * ${verbs} is the comma separated list of allowed verbs (GET, POST...) for accessing the resources matching the resource path pattern

    !!! example
        A user having the rules:
        ```asciidoc
        rule:/collection/.*:GET,
        rule:/explore/.*/_search:GET
        ```
        will be able to explore the `collections` and to `search` in all of them, but won’t be able to add or delete 
        collections (only `GET` verb is allowed for collections) and won’t be able to make aggregations (the resource `_aggregate` is not defined).

## Protect data access

In order to specify a finer access control to the data, the headers `partition-filter` and `column-filter`
can be set using the header mechanism described above.

#### Column-filter

The header `column-filter` allows you to pass a list of collections and fields of the data.
Only the collections and fields present in this list are visible in the response of a request.

This allows certain collections and fields to be restricted to certain users.

!!! info "column-filter syntax"
    See the `column-filter` syntax in [ARLAS Exploration API configuration section](arlas-api-exploration.md#column-filtering).

These must be defined and associated to roles (preferably 'group' roles) in order to be available in the permission token.  
If multiple instances of the same header name are found in the resulting list of permissions, they are merged into a
single multi-value header (values separated by commas).

#### Partition-filter

The header `partition-filter` allows you to pass an ARLAS filter to apply to the request.

This allows certain data to be restricted to certain users.

!!! info "partition-filter syntax"
    See the `partition-filter` syntax in [ARLAS Exploration API configuration section](arlas-api-exploration.md#partition-filtering).

These must be defined and associated to roles (preferably 'group' roles) in order to be available in the permission token.  
If multiple instances of the same header name are found in the resulting list of permissions, they are merged into a
single multi-value header (values separated by commas).

#### Organisation filter

If a header `arlas-organization` is present (can be empty, or be a comma separated list of values), it will be used
to check if the collection's organisations (owner or shared with) match one of the provided values.  

If the collection is defined as public in its organisation parameters, it will be visible no matter what.

If the collection has been defined without organisation parameters, it will not be visible.

If no header `arlas-organization` is present then no check is done on the collection's organisations.