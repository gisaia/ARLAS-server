# ARLAS

The ARLAS Admin API let you change the collection references of the ARLAS catalog.

# URL Schema
The table below lists the URL endpoints.

| PATH Template                            | Description                              |
| ---------------------------------------- | ---------------------------------------- |
| /arlas-admin/                 | List  the collections configured in ARLAS with the technical details |
| /arlas-admin/`{collection}`   | Get, add or delete a collection reference in ARLAS |

# Managing collections

## /arlas-admin/`{collection}`

The following methods let you get, add and delete collection references from elasticsearch into the ARLAS catalog:

| Method     | Input Data | Output Data | Description                  | Multiple |
| -----------| ------------- | ------------- | ---------------------------------------- | ---------------------------- | -------- |
| **GET**    | `None` | Collection Reference as JSON |  Type of aggregation          | false    |
| **PUT**    | Collection Reference Profile as JSON | Collection Reference as JSON |  Aggregates on the `{field}`. | true     |
| **DELETE** | `None` | `None` | Size of the intervals.       | true     |
