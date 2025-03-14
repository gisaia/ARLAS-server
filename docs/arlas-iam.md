# ARLAS-IAM

## What is it?
This module aims at adding an Identity and Access Management (IAM) to the ARLAS stack.  
IAM provides authentication (user login) and authorisation (permissions to access data and APIs) services to ARLAS
components: server, WUI, hub, builder...

The stack can be started with or without ARLAS IAM.

ARLAS IAM is open source as all the ARLAS stack.

![IAM diagram](iam.png)

IAM is composed of 2 main components:

1. an implementation of the ARLAS PolicyEnforcer (interface available in the ARLAS-server/arlas-commons module: `io.arlas.filter.core.PolicyEnforcer`)
    - ARLAS IAM implementation (`io.arlas.filter.impl.HTTPPolicyEnforcer`)
2. an IAM server

## Policy Enforcers configuration
The policy enforcers are in the `arlas-commons` module.  
The implementation to activate must be defined in the backend component configuration:


| Environment variable    | configuration variable  | Default                               | Value for ARLAS-IAM                     |
|-------------------------|-------------------------|---------------------------------------|-----------------------------------------|
| ARLAS_AUTH_POLICY_CLASS | arlas_auth_policy_class | io.arlas.filter.impl.NoPolicyEnforcer | io.arlas.filter.impl.HTTPPolicyEnforcer |

Further configuration is required:

| Environment variable         | configuration variable                 | Default                                                   | Policy enforcer |
|------------------------------|----------------------------------------|-----------------------------------------------------------|-----------------|
| ARLAS_CLAIM_ROLES            | arlas_auth.claim_roles                 | http://arlas.io/roles                                     | HTTP            |
| ARLAS_CLAIM_PERMISSIONS      | arlas_auth.claim_permissions           | http://arlas.io/permissions                               | HTTP            |
| ARLAS_AUTH_PERMISSION_URL    | arlas_auth.permission_url              | http://arlas-iam-server/arlas_iam_server/auth/permissions | HTTP            |