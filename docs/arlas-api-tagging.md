# ARLAS Tagging API

This API is available for tagging `hits` in ARLAS `collections`. The ARLAS Tagger service is running separately from the ARLAS Server.

By tagging, we mean here adding a value to a field that is of type `Array`.

The field to tag must be declared in the [ARLAS collection](arlas-collection-model#model).

There are two ways to select `hits` to be tagged:
- if no `propagation` parameter is specified, the value is added to the field of all the hits of a collection matching 
the `Search` part of the TagRequest. If no `Search` is provided, then the whole collection is tagged.
- if a `propagation` parameter is specified, then a first selection is made from the `Search` part in order to list all 
the unique `propagation.field` values. All hits of the collection matching each unique `propagation.field` value 
(optionally limited by the `propagation.filter` parameter) are then tagged with the tag field. 

The collection name, the path to the field carrying the tag and the tag values must be provided in order to tag a collection. 
If needed a `Search` can be provided to specify the hits to be tagged:

```shell
curl -X POST  \
    --header 'Accept: application/json;charset=utf-8' \
    -d '{ "search": {}, "tag": { "path": "plant.color","value": "pink"}, "propagation": { "field": "id", "filter": {} }, "label": "pinktag" }' \
    'http://<arlas-tagger-host>:<arlas-tagger-port>/arlas/write/geodata/_tag?pretty=false'
```


In order to remove a tag, meaning a value from the field, the same tag request is sent, but on the `untag` endpoint:
```shell
curl -X POST  \
    --header 'Accept: application/json;charset=utf-8' \
    -d '{ "search": {}, "tag": { "path": "plant.color","value": "pink"}}' \
    'http://<arlas-tagger-host>:<arlas-tagger-port>/arlas/write/geodata/_untag?pretty=false'
```


To remove all the tags for a given field, simply omit the value of the tag:
```shell
curl -X POST  \
    --header 'Accept: application/json;charset=utf-8' \
    -d '{ "search": {}, "tag": { "path": "plant.color"}}' \
    'http://<arlas-tagger-host>:<arlas-tagger-port>/arlas/write/geodata/_untag?pretty=false'
```


Tagging is done asynchronously, through Kafka logs. The response from the first (un)tag request gives an id 
that can be used to request the status of the tagging process itself. 
```shell
curl -X GET  \
    --header 'Accept: application/json;charset=utf-8' \
    -d '{ "search": {}, "tag": { "path": "plant.color"}}' \
    'http://<arlas-tagger-host>:<arlas-tagger-port>/arlas/status/geodata/_tagging?id=...'
```


!!! warning
    Only taggable fields can be tagged. In order to be taggable, a field must have its path provided in the `CollectionReference`, more specifically in `params.taggable_fields`.
