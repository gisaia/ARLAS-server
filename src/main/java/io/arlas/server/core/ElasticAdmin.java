package io.arlas.server.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;

import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.CollectionReferenceParameters;
import io.arlas.server.model.response.CollectionReferenceDescription;
import io.arlas.server.model.response.CollectionReferenceDescriptionProperty;
import io.arlas.server.model.response.ElasticType;

public class ElasticAdmin {

    public TransportClient client;
    public ElasticAdmin(TransportClient client){
        this.client = client;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public CollectionReferenceDescription describeCollection (CollectionReference collectionReference) throws IOException {
        CollectionReferenceDescription collectionReferenceDescription = new CollectionReferenceDescription();
        collectionReferenceDescription.params = collectionReference.params;
        collectionReferenceDescription.collectionName = collectionReference.collectionName;
        GetMappingsResponse response;
        response = client.admin().indices()
                .prepareGetMappings(collectionReferenceDescription.params.indexName).setTypes(collectionReferenceDescription.params.typeName).get();
        LinkedHashMap fields = (LinkedHashMap)response.getMappings()
                .get(collectionReferenceDescription.params.indexName).get(collectionReferenceDescription.params.typeName).sourceAsMap().get("properties");
        List<CollectionReferenceDescriptionProperty> properties = new ArrayList<CollectionReferenceDescriptionProperty>();
        for(Object field : fields.keySet()) {
            ElasticType type = ElasticType.getType(((Map<String,String>)fields.get(field)).get("type"));
            String dateFormat;
            if (type.equals(ElasticType.DATE)){
                dateFormat = ((Map<String,String>)fields.get(field)).get("format");
                if (dateFormat == null) {
                    dateFormat = CollectionReference.DEFAULT_TIMESTAMP_FORMAT;
                }
            properties.add(new CollectionReferenceDescriptionProperty(field.toString(),type, dateFormat));
            }
            properties.add(new CollectionReferenceDescriptionProperty(field.toString(),type, null));
        }
        collectionReferenceDescription.properties = properties;
        return collectionReferenceDescription;
    }

    public List<CollectionReferenceDescription> describeAllCollections (List<CollectionReference> collectionReferenceList) throws IOException, ArlasException {
        List<CollectionReferenceDescription> collectionReferenceDescriptionList = new ArrayList<>();
        for (CollectionReference collectionReference : collectionReferenceList){
            collectionReferenceDescriptionList.add(describeCollection(collectionReference));
        }
        return collectionReferenceDescriptionList;
    }
    
    public List<CollectionReferenceDescription> getAllIndecesAsCollections() throws IOException {
        List<CollectionReferenceDescription> collections = new ArrayList<CollectionReferenceDescription>();
        ImmutableOpenMap<String, IndexMetaData> indices = client.admin().cluster()
                .prepareState().get().getState()
                .getMetaData().getIndices();
        for(Iterator<String> indexNames = indices.keysIt(); indexNames.hasNext();) {
            String indexName = indexNames.next();
            ImmutableOpenMap<String, MappingMetaData> mappings = indices.get(indexName).getMappings();
            for(Iterator<String> mappingNames = mappings.keysIt(); mappingNames.hasNext();) {
                String mappingName = mappingNames.next();
                CollectionReference collection = new CollectionReference();
                collection.collectionName = indexName+"-"+mappingName;
                collection.params = new CollectionReferenceParameters();
                collection.params.indexName = indexName;
                collection.params.typeName = mappingName;
                collections.add(describeCollection(collection));
            }
        }
        return collections;
    }
}
