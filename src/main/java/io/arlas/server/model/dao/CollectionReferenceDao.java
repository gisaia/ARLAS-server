package io.arlas.server.model.dao;

import java.util.List;

import javax.ws.rs.NotFoundException;

import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.CollectionReferenceParameters;

/**
 * DAO interface for collection references
 * 
 * @author sfalquier
 *
 */
public interface CollectionReferenceDao {
    public CollectionReference getCollectionReference(String ref);
    public List<CollectionReference> getAllCollectionReferences();
    
    public void putCollectionReference(String ref, CollectionReferenceParameters desc);
    public void deleteCollectionReference(String ref) throws NotFoundException;    
}
