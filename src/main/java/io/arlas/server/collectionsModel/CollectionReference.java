package io.arlas.server.collectionsModel;


public class CollectionReference {

    private String indexName;
    private String idPath;
    private String geomPath;
    private String centroidPath;

    public CollectionReference() {
    }

    public CollectionReference(String indexName, String idPath, String geomPath, String centroidPath) {
        this.indexName = indexName;
        this.idPath = idPath;
        this.geomPath = geomPath;
        this.centroidPath = centroidPath;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getIdPath() {
        return idPath;
    }

    public void setIdPath(String idPath) {
        this.idPath = idPath;
    }

    public String getGeomPath() {
        return geomPath;
    }

    public void setGeomPath(String geomPath) {
        this.geomPath = geomPath;
    }

    public String getCentroidPath() {
        return centroidPath;
    }

    public void setCentroidPath(String centroidPath) {
        this.centroidPath = centroidPath;
    }
}
