SELECT
    description
FROM collection_reference
WHERE arlas_index = ?
ORDER BY collection_name ASC;
