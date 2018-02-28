/*
 * Licensed to Gisaïa under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with
 * this work for additional information regarding copyright
 * ownership. Gisaïa licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.arlas.server.wfs.operation.getcapabilities;

import io.arlas.server.app.ArlasServerConfiguration;
import io.arlas.server.exceptions.ArlasConfigurationException;
import io.arlas.server.wfs.WFSHandler;
import net.opengis.fes._2.*;
import net.opengis.ows._1.*;
import net.opengis.wfs._2.FeatureTypeListType;
import net.opengis.wfs._2.FeatureTypeType;
import net.opengis.wfs._2.OutputFormatListType;
import net.opengis.wfs._2.WFSCapabilitiesType;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

public class GetCapabilitiesHandler {

    public ArlasServerConfiguration arlasServerConfiguration;
    public WFSHandler wfsHandler;
    protected WFSCapabilitiesType getCapabilitiesType = new WFSCapabilitiesType();
    protected ValueType trueValueType = new ValueType();
    protected ValueType falseValueType = new ValueType();

    public GetCapabilitiesHandler(ArlasServerConfiguration arlasServerConfiguration, WFSHandler wfsHandler)
            throws ArlasConfigurationException {
        this.wfsHandler = wfsHandler;
        this.arlasServerConfiguration = arlasServerConfiguration;
        this.arlasServerConfiguration.wfsConfiguration.getSupportedVersion().forEach(v ->getCapabilitiesType.setVersion(v));
        trueValueType.setValue("TRUE");
        falseValueType.setValue("FALSE");
        this.setServiceProvider();
        this.setServiceIdentification();
        this.setOperations();
        this.setFilterCapabilities();
    }

    public JAXBElement<WFSCapabilitiesType> getWFSCapabilitiesResponse() {
        return wfsHandler.wfsFactory.createWFSCapabilities(getCapabilitiesType);
    }

    private void setServiceProvider() {
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setProviderName("ARLAS");
        OnlineResourceType onlineResourceType = new OnlineResourceType();
        onlineResourceType.setHref("www.gisaia.com");
        onlineResourceType.setRole("Main Developer");
        serviceProvider.setProviderSite(onlineResourceType);
        ResponsiblePartySubsetType responsiblePartySubsetType = new ResponsiblePartySubsetType();
        responsiblePartySubsetType.setIndividualName("Matthieu Barbet");
        ContactType contactType = new ContactType();
        AddressType addressType = new AddressType();
        addressType.setCity("Blagnac");
        addressType.setCountry("France");
        addressType.setPostalCode("31700");
        contactType.setAddress(addressType);
        responsiblePartySubsetType.setContactInfo(contactType);
        serviceProvider.setServiceContact(responsiblePartySubsetType);
        getCapabilitiesType.setServiceProvider(serviceProvider);
    }

    private void setServiceIdentification() {
        ServiceIdentification serviceIdentification = new ServiceIdentification();
        CodeType codeType = new CodeType();
        codeType.setValue("WFS");
        serviceIdentification.setServiceType(codeType);
        serviceIdentification.getServiceTypeVersion().add("2.0.0");
        getCapabilitiesType.setServiceIdentification(serviceIdentification);

    }

    public void setOperationsUrl(String url) {
        getCapabilitiesType.getOperationsMetadata().getOperation().forEach(op -> {
            HTTP http = new HTTP();
            RequestMethodType requestMethodType = new RequestMethodType();
            requestMethodType.setHref(url);
            JAXBElement<RequestMethodType> get = wfsHandler.owsFactory.createHTTPGet(requestMethodType);
            http.getGetOrPost().add(get);
            op.getDCP().get(0).setHTTP(http);
        });

    }

    private void setOperations() {
        OperationsMetadata operationsMetadata = new OperationsMetadata();
        DCP dcp = new DCP();
        HTTP http = new HTTP();
        dcp.setHTTP(http);

        Operation getCapabilities = new Operation();
        getCapabilities.setName("GetCapabilities");

        DomainType acceptVersions = new DomainType();
        acceptVersions.setName("AcceptVersions");
        acceptVersions.setAllowedValues(new AllowedValues());
        ValueType version = new ValueType();
        version.setValue("2.0.0");
        acceptVersions.getAllowedValues().getValueOrRange().add(version);
        DomainType sections = new DomainType();
        sections.setName("Sections");
        sections.setAllowedValues(new AllowedValues());

        ValueType serviceIdentification = new ValueType();
        serviceIdentification.setValue("ServiceIdentification");
        sections.getAllowedValues().getValueOrRange().add(serviceIdentification);
        ValueType serviceProvider = new ValueType();
        serviceProvider.setValue("ServiceProvider");
        sections.getAllowedValues().getValueOrRange().add(serviceProvider);

        ValueType featureTypeList = new ValueType();
        featureTypeList.setValue("FeatureTypeList");
        sections.getAllowedValues().getValueOrRange().add(featureTypeList);

        ValueType filterCapabilities = new ValueType();
        filterCapabilities.setValue("Filter_Capabilities");
        sections.getAllowedValues().getValueOrRange().add(filterCapabilities);

        getCapabilities.getParameter().add(acceptVersions);
        getCapabilities.getParameter().add(sections);
        getCapabilities.getDCP().add(dcp);

        Operation describeFeatureType = new Operation();
        describeFeatureType.setName("DescribeFeatureType");
        describeFeatureType.getDCP().add(dcp);

        Operation listStoredQueries = new Operation();
        listStoredQueries.setName("ListStoredQueries");
        listStoredQueries.getDCP().add(dcp);

        Operation getPropertyValue = new Operation();
        getPropertyValue.setName("GetPropertyValue");
        getPropertyValue.getDCP().add(dcp);

        Operation describedStoredQueries = new Operation();
        describedStoredQueries.setName("DescribeStoredQueries");
        describedStoredQueries.getDCP().add(dcp);

        Operation getFeature = new Operation();
        getFeature.setName("GetFeature");
        getFeature.getDCP().add(dcp);
        DomainType resolve = new DomainType();
        resolve.setName("resolve");
        resolve.setAllowedValues(new AllowedValues());
        ValueType local = new ValueType();
        local.setValue("local");
        resolve.getAllowedValues().getValueOrRange().add(local);

        getFeature.getParameter().add(resolve);
        getPropertyValue.getParameter().add(resolve);
        operationsMetadata.getOperation().add(getCapabilities);
        operationsMetadata.getOperation().add(describeFeatureType);
        operationsMetadata.getOperation().add(listStoredQueries);
        operationsMetadata.getOperation().add(describedStoredQueries);
        operationsMetadata.getOperation().add(getFeature);
        operationsMetadata.getOperation().add(getPropertyValue);

        addConformanceType(operationsMetadata, "ImplementsBasicWFS", falseValueType);
        addConformanceType(operationsMetadata, "ImplementsTransactionalWFS", falseValueType);
        addConformanceType(operationsMetadata, "KVPEncoding", trueValueType);
        addDefaultConformance(operationsMetadata);
        getCapabilitiesType.setOperationsMetadata(operationsMetadata);

    }

    public void setFeatureTypeListType(String name,String uri) {
        FeatureTypeListType featureTypeListType = new FeatureTypeListType();
        FeatureTypeType featureTypeType = new FeatureTypeType();
        featureTypeType.setDefaultCRS("urn:ogc:def:crs:EPSG::4326");
        QName qname = new QName(uri, name,"arlas");
        featureTypeType.setName(qname);
        OutputFormatListType outputFormatListType = new OutputFormatListType();
        outputFormatListType.getFormat().add("application/gml+xml; version=3.2");
        outputFormatListType.getFormat().add("text/xml; subtype=gml/3.2");
        featureTypeType.setOutputFormats(outputFormatListType);
        featureTypeListType.getFeatureType().add(featureTypeType);
        getCapabilitiesType.setFeatureTypeList(featureTypeListType);

    }


    public void setFilterCapabilities() {
        FilterCapabilities filterCapabilities = wfsHandler.fesFactory.createFilterCapabilities();
        ConformanceType fesConformanceType = new ConformanceType();
        addDefaultConformance(fesConformanceType);
        IdCapabilitiesType idCapabilitiesType = wfsHandler.fesFactory.createIdCapabilitiesType();
        ResourceIdentifierType resourceIdentifierType = new ResourceIdentifierType();
        resourceIdentifierType.setName(QName.valueOf("ResourceId"));
        idCapabilitiesType.getResourceIdentifier().add(resourceIdentifierType);
        SpatialCapabilitiesType spatialCapabilities = wfsHandler.fesFactory.createSpatialCapabilitiesType();
        SpatialOperatorsType spatialOperatorsType = new SpatialOperatorsType();
        GeometryOperandsType geometryOperandsType = new GeometryOperandsType();
        SpatialOperatorType spatialOperatorType = new SpatialOperatorType();
        GeometryOperandsType.GeometryOperand geometryOperand = new GeometryOperandsType.GeometryOperand();
        geometryOperand.setName(QName.valueOf("BBOX"));
        geometryOperandsType.getGeometryOperand().add(geometryOperand);
        spatialOperatorType.setGeometryOperands(geometryOperandsType);
        spatialCapabilities.setGeometryOperands(geometryOperandsType);
        spatialOperatorType.setName("BBOX");
        spatialOperatorsType.getSpatialOperator().add(spatialOperatorType);
        spatialCapabilities.setSpatialOperators(spatialOperatorsType);
        filterCapabilities.setSpatialCapabilities(spatialCapabilities);
        filterCapabilities.setIdCapabilities(idCapabilitiesType);
        filterCapabilities.setConformance(fesConformanceType);
        getCapabilitiesType.setFilterCapabilities(filterCapabilities);


    }

    private void addConformanceType(ConformanceType conformanceType, String name, ValueType value) {
        DomainType domainType = new DomainType();
        domainType.setName(name);
        domainType.setDefaultValue(value);
        NoValues noValues = new NoValues();
        domainType.setNoValues(noValues);
        conformanceType.getConstraint().add(domainType);
    }

    private void addConformanceType(OperationsMetadata operationsMetadata, String name, ValueType value) {
        DomainType domainType = new DomainType();
        domainType.setName(name);
        domainType.setDefaultValue(value);
        NoValues noValues = new NoValues();
        domainType.setNoValues(noValues);
        operationsMetadata.getConstraint().add(domainType);
    }

    private void addDefaultConformance(OperationsMetadata operationsMetadata) {
        addConformanceType(operationsMetadata, "ImplementsQuery", trueValueType);
        addConformanceType(operationsMetadata, "ImplementsLockingWFS", falseValueType);
        addConformanceType(operationsMetadata, "ImplementsInheritance", falseValueType);
        addConformanceType(operationsMetadata, "ImplementsRemoteResolve", falseValueType);
        addConformanceType(operationsMetadata, "ImplementsResultPaging", falseValueType);
        addConformanceType(operationsMetadata, "ImplementsStandardJoins", falseValueType);
        addConformanceType(operationsMetadata, "ImplementsSpatialJoins", falseValueType);
        addConformanceType(operationsMetadata, "ImplementsTemporalJoins", falseValueType);
        addConformanceType(operationsMetadata, "ImplementsFeatureVersioning", falseValueType);
        addConformanceType(operationsMetadata, "ManageStoredQueries", falseValueType);
    }

    private void addDefaultConformance(ConformanceType conformanceType) {
        addConformanceType(conformanceType, "ImplementsQuery", trueValueType);
        addConformanceType(conformanceType, "ImplementsAdHocQuery", trueValueType);
        addConformanceType(conformanceType, "ImplementsResourceId", trueValueType);
        addConformanceType(conformanceType, "ImplementsMinStandardFilter", trueValueType);
        addConformanceType(conformanceType, "ImplementsStandardFilter", trueValueType);
        addConformanceType(conformanceType, "ImplementsMinSpatialFilter", trueValueType);
        addConformanceType(conformanceType, "ImplementsSorting", trueValueType);
        addConformanceType(conformanceType, "ImplementsMinimumXPath", trueValueType);
        addConformanceType(conformanceType, "ImplementsLockingWFS", falseValueType);
        addConformanceType(conformanceType, "ImplementsInheritance", falseValueType);
        addConformanceType(conformanceType, "ImplementsRemoteResolve", falseValueType);
        addConformanceType(conformanceType, "ImplementsResultPaging", falseValueType);
        addConformanceType(conformanceType, "ImplementsStandardJoins", falseValueType);
        addConformanceType(conformanceType, "ImplementsSpatialJoins", falseValueType);
        addConformanceType(conformanceType, "ImplementsTemporalJoins", falseValueType);
        addConformanceType(conformanceType, "ImplementsFeatureVersioning", falseValueType);
        addConformanceType(conformanceType, "ManageStoredQueries", falseValueType);
    }
}