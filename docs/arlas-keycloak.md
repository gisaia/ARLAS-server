# ARLAS with Keycloak

## What is it?
Keycloak aims at adding an Identity and Access Management (IAM) to the ARLAS stack.  
Keycloak provides authentication (user login) and authorisation (permissions to access data and APIs) services to ARLAS
components: server, WUI, hub, builder...

The stack can be started with or without Keycloak.

![Keycloak diagram](keycloak.png)

ARLAS with Keycloak is composed of 2 main components:

1. an implementation of the ARLAS PolicyEnforcer (interface available in the ARLAS-server/arlas-commons module: `io.arlas.filter.core.PolicyEnforcer`)
    - Keycloak implementation (`io.arlas.filter.impl.KeycloakPolicyEnforcer`)
2. a Keycloak instance.

## Policy Enforcers configuration
The policy enforcers are in the `arlas-commons` module.  
The implementation to activate must be defined in the backend component configuration:


| Environment variable    | configuration variable  | Default                               | Value for Keycloak                          |
|-------------------------|-------------------------|---------------------------------------|---------------------------------------------|
| ARLAS_AUTH_POLICY_CLASS | arlas_auth_policy_class | io.arlas.filter.impl.NoPolicyEnforcer | io.arlas.filter.impl.KeycloakPolicyEnforcer |

Further configuration is required:

| Environment variable         | configuration variable                 | Default                                                   | Policy enforcer |
|------------------------------|----------------------------------------|-----------------------------------------------------------|-----------------|
| ARLAS_AUTH_KEYCLOAK_REALM    | arlas_auth.keycloak.realm              | arlas                                                     | Keycloak        |
| ARLAS_AUTH_KEYCLOAK_URL      | arlas_auth.keycloak.auth-server-url    | http://keycloak:8080/auth                                 | Keycloak        |
| ARLAS_AUTH_KEYCLOAK_RESOURCE | arlas_auth.keycloak.resource           | arlas                                                     | Keycloak        |
| ARLAS_AUTH_KEYCLOAK_SECRET   | arlas_auth.keycloak.credentials.secret | none                                                      | Keycloak        |

## Keycloak v23.0 Configuration

### Concepts
Keycloak is configured through multiple items. ARLAS uses only a subset of them, which are described here.

- **Realm**: a realm is the container of the configuration used by an ARLAS instance (clients, users, groups...)
- **Client**: a client contains the configuration that controls the ARLAS components access.
    - **Client roles**: roles can be given to users in order to control their rights. Some role names must follow some specific naming rules (such as ARLAS groups).
    - **Authorization/Resources**: resources are a list of ARLAS rules and headers (see before). Their **names** contain the rule/header to be sent to ARLAS.
      Resources with the same **type** value can be referenced together through that value in *permissions*.
    - **Authorization/Policies**: policies are pieces of logic that are used to check a condition.
      For now only "role policies" are used: it checks if a *user* has a given *client role*.
    - **Authorization/Permissions**: only "resource-based" permissions are used. They associate *resources* or *resource types* with *policies*.
      Whenever a *permission*'s *policy* matches, the associated *resources* are sent to ARLAS in the authentication process of a *user* (in the permission claim of the RPT : requesting party token)
- **Groups**: are used to group together a list of *client roles* in order to facilitate their allocation to *users*.
  Modifications to a group (adding/removing roles) will be spread to all users belonging to the group. This is not the same concept as an ARLAS group.
- **Users**: are accounts allowed to connect to ARLAS. They can (and should) belong to keycloak *groups* and can also be mapped to individual *client roles*.

!!! warning
    The list of *client roles* associated to a user must result in at least one *resource* once the *permissions* are
    evaluated, as "no permissions" equals to 403 response (and not an empty permission list) when requesting the RPT from Keycloak

### Manual configuration
In order to configure Keycloak from scratch, follow this tutorial, as a minimum set of settings to make it work with ARLAS.  
Another way is to import the default configuration file given with this module (see next section).

0. In order to remove limitations in permissions size (default is 256 characters), one must alter the database:  
   `ALTER TABLE public.resource_server_resource ALTER COLUMN name TYPE TEXT`
1. Create a realm. Its name must be configured in `ARLAS_AUTH_KEYCLOAK_REALM`. Switch to the realm administration console.
   The following lines refer to the appropriate menu items from the console with a prefix text in parentheses (left menu/right tabs/...), e.g. (Clients/Lookup)
2. *(Clients/Lookup/Create)* Create a new client with:
    - Client ID=`arlas-backend` (must be configured in `ARLAS_AUTH_KEYCLOAK_RESOURCE`)
    - Client Protocol=`openid-connect`
    - Next
3. *(Clients/Arlas-backend/Capability Config)* Change the following configuration items:
   - Client authentication=`ON`
   - Authorization=`ON`
   - Standard Flow=`ON`
   - Next
   - Direct access grants=`ON`
   - Valid Redirect URIs=
   - `*`
   - Valid post logout redirect URIs=
   - `*`
   - Web Origins=`+`
   - Save
4. *(Clients/Arlas-backend/Login settings)* Select Client Authenticator "Client Id and Secret" and copy the Secret value to `ARLAS_AUTH_KEYCLOAK_SECRET`
5. *(Clients/Arlas-backend/Roles)* Add the following roles:
   - `group/public` (required to allow dashboards to be shared to anonymous users)
   - `role/arlas/builder` (rules to create/edit/delete ARLAS WUI dashboards)
   - `role/arlas/importer` (rule to import collections via the dedicated ARLAS server endpoint, mainly used by M2M processes)
   - `role/arlas/tagger` (rules to use the [Tagger]() backend)
   - `role/arlas/user`  (rules to view data)
   - `role/arlas/downloader` (rules to download data with [AIAS])
   - `role/arlas/datasets` (rules to manage data (ingest, update and enrich) in ARLAS)
   - `group/config.json/XXXXX`: add as many groups as needed where `XXXXX` will be the name of groups available to share
     dashboards in ARLAS hub and that can be associated to data filters.
6. *(Clients/Arlas-backend/Authorization/Policies)* Add role policies for each new group you have added.
   Don't select a Realm Role but choose the `arlas` Client and choose a Client Role. Keep the logic to Positive.
7. *(Clients/Arlas-backend/Authorization/Resources)* Add any resource `header:name:value` (as name) you need (optionally setting
   a `type` if you need to map more than one in a `group/config.json/...` *role*) and create permission (select relevant role policy).
   A basic example: you might need to authorize a user to see all the fields of all the collections. This is a resource   named `h:column-filter:*:*`. Create a permission for this resource and apply `role/arlas/user` to it.
8. *(Groups)* Add groups with some `arlas-backend` *client roles* according to the way you want to assign permissions to users.
9. *(Users)* Add users:
    - Username= choose name
    - Groups= choose groups
    - Role mappings: assign relevant individual roles from `arlas-backend` *client roles* if not assigned through groups
    - Save
10. *(Users/\<user\>/Credentials)* Set password
11. *(Clients/Lookup/Create)* Create a new client with:
    - Client ID=arlasm2m
    - Client Protocol=openid-connect
    - Next
12. *(Clients/Arlasm2m/Capability config)* Change the following configuration items:
    - Access Type=confidential
    - Switch off Standard Flow Enabled
    - Switch on Service Accounts Enabled
    - Save
13. *(Clients/Arlasm2m/Service Account Roles)* Select `arlas-backend` in the Client Roles drop down list and add selected roles:
    - role/arlas/user
    - role/arlas/importer
14. *(Clients/Lookup/Create)* Create a new client with:
     - Client ID=`arlas-front` (must be configured in arlas frontend app configurations)
     - Client Protocol=`openid-connect`
     - Save
15. *(Clients/Arlas-backend/Capability config)* Change the following configuration items:
     - Client authentication=`OFF`
     - Authorization=`OFF`
     - Standard Flow=`ON`
     - Direct access grants=`OFF`
     - Implicit Flow=`OFF`
     - Valid Redirect URIs=
         - `*`
     - Valid post logout redirect URIs=
         - `+`
     - Web Origins=`*`
     - Save
    
### Import configuration
    !!! warning
    To import configuration remove all the JS type resource (Default resource).
0. In order to remove limitations in permissions size (default is 256 characters), one must alter the database:  
   `ALTER TABLE public.resource_server_resource ALTER COLUMN name TYPE TEXT`
1. In the realm selection drop down list, select "Create realm".
2. Select the file to import and click `Create` (template is in `arlas-commons/src/main/resources/realm-export.json`)
3. *(Clients/Arlas-backend/Credentials)* Select Client Authenticator "Client Id and Secret", regenerate the secret,
   and copy the Secret value to `ARLAS_AUTH_KEYCLOAK_SECRET`
4. *(Clients/Arlasm2m/Credentials)* Select Client Authenticator "Client Id and Secret", regenerate the secret (and copy if needed)
5. *(Clients/Arlas/Roles)* Add the following roles:
    - `group/config.json/XXXXX`: add as many groups as needed where `XXXXX` will be the name of groups available to share
      dashboards in ARLAS hub and that can be associated to data filters.
6. *(Clients/Arlas/Authorization/Policies)* Add role policies for each new group you have added
7. *(Clients/Arlas/Authorization/Resources)* Add any `header:name:value` you need (optionally setting a `type` if you need
   to map more than one in a `group/config.json/...` *role*) and create permission (select relevant role policy)
8. *(Groups)* Add groups with some `arlas-backend` *client roles* according to the way you want to assign permissions to users.
9. *(Users)* Add users:
   - Username= choose name
   - Groups= choose groups
   - Role mappings: assign relevant individual roles from `arlas-backend` *client roles* if not assigned through groups
   - Save
10. *(Users/\<user\>/Credentials)* Set password