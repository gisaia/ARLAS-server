# ARLAS

The ARLAS Collection API let you change the collection references of the ARLAS catalog.

# URL Schema
The table below lists the URL endpoints.

| PATH Template                     | Description                                                          |
| --------------------------------- | -------------------------------------------------------------------- |
| /arlas/collections/               | List  the collections configured in ARLAS with the technical details |
| /arlas/collections/`{collection}` | Get, add or delete a collection reference in ARLAS                   |
| /arlas/collections/_export        | Export all collections as a JSON file                                |
| /arlas/collections/_import        | Import collections from a JSON file posted as a multipart parameter  |

# Managing collections

## /arlas/collections/

| Method     | Input Data                    | Output Data                            | Description                                                                        |
| ---------- | ----------------------------- | ---------------------------------------| ---------------------------------------------------------------------------------- |
| **GET**    | `None`                        | Array of `CollectionReference` as JSON | Return the full description of all the Collection Reference stored in ARLAS-server |


## /arlas/collections/`{collection}`

The following methods let you get, add and delete **collection references** from elasticsearch into the ARLAS catalog. 
A **collection reference** is the description of the elasticsearch index and the way ARLAS API will serve it.

| Method     | Input Data                    | Output Data                   | Description                                             |
| ---------- | ----------------------------- | ----------------------------- | ------------------------------------------------------- |
| **GET**    | `None`                        | `CollectionReference` as JSON | Return the full description of the Collection Reference |
| **PUT**    | `CollectionReference` as JSON | `CollectionReference` as JSON | Add or update a CollectionReference                     |
| **DELETE** | `None`                        | `None`                        | Delete a CollectionReference                            |

## /arlas/collections/_export

| Method     | Input Data                    | Output Data                                 | Description                                                                        |
| ---------- | ----------------------------- | ------------------------------------------- | ---------------------------------------------------------------------------------- |
| **GET**    | `None`                        | JSON array of `CollectionReference` as file | Return the full description of all the Collection Reference stored in ARLAS-server |

## /arlas/collections/_import

| Method     | Input Data                                                              | Output Data                            | Description                                                                                                                               |
| ---------- | ----------------------------------------------------------------------- | -------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------- |
| **POST**   | JSON array of `CollectionReference` as file posted in a multipart param | Array of `CollectionReference` as JSON | Return the full description of all the Collection Reference saved in ARLAS from this request (already stored collections will be updated) |