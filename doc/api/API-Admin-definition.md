# ARLAS

The ARLAS Admin API let you change the collection references of the ARLAS catalog.

# URL Schema
The table below lists the URL endpoints.

| PATH Template               | Description                              |
| --------------------------- | ---------------------------------------- |
| /arlas/admin/               | List  the collections configured in ARLAS with the technical details |
| /arlas/admin/`{collection}` | Get, add or delete a collection reference in ARLAS |

# Managing collections

## /arlas/admin/`{collection}`

The following methods let you get, add and delete **collection references** from elasticsearch into the ARLAS catalog. 
A **collection reference** is the description of the elasticsearch index and the way ARLAS API will serve it.

| Method     | Input Data                    | Output Data                   | Description                              |
| ---------- | ----------------------------- | ----------------------------- | ---------------------------------------- |
| **GET**    | `None`                        | `CollectionReference` as JSON | Return the full description of the Collection Reference |
| **PUT**    | `CollectionReference` as JSON | `CollectionReference` as JSON | Add a new CollectionReference            |
| **DELETE** | `None`                        | `None`                        | Size of the intervals.                   |
