INSERT INTO collection_reference(
    arlas_index,
    collection_name,
    index_name,
    description
) VALUES(?,?,?,CAST(? as json));