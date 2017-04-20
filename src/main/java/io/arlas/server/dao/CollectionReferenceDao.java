package io.arlas.server.dao;

import java.util.List;

import io.arlas.server.exceptions.InternalServerErrorException;
import io.arlas.server.exceptions.NotFoundException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.CollectionReferenceParameters;

/**
 * DAO interface for collection references
 * 
 * @author sfalquier
 *
 */
public interface CollectionReferenceDao {
    public CollectionReference getCollectionReference(String ref) throws NotFoundException;
    public List<CollectionReference> getAllCollectionReferences() throws NotFoundException;
    
    public CollectionReference putCollectionReference(String ref, CollectionReferenceParameters desc) throws InternalServerErrorException;
    public void deleteCollectionReference(String ref) throws NotFoundException, InternalServerErrorException;    
}
