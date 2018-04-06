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

package io.arlas.server.ogc.wfs.operation.getcapabilities;

import io.arlas.server.app.OGCConfiguration;
import io.arlas.server.app.WFSConfiguration;
import io.arlas.server.ogc.wfs.WFSHandler;
import io.arlas.server.ogc.wfs.utils.WFSConstant;
import io.arlas.server.ogc.wfs.utils.WFSRequestType;
import net.opengis.fes._2.*;
import net.opengis.ows._1.*;
import net.opengis.wfs._2.FeatureTypeListType;
import net.opengis.wfs._2.FeatureTypeType;
import net.opengis.wfs._2.OutputFormatListType;
import net.opengis.wfs._2.WFSCapabilitiesType;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.util.Arrays;

public class GetCapabilitiesHandler {

    private static final String[] SECTION_NAMES = {"ServiceIdentification","ServiceProvider","FeatureTypeList","Filter_Capabilities"};
    private static final String SECTION_DOMAIN_NAME = "Sections";
    private static final String ACCEPT_VERSIONS_DOMAIN_NAME ="AcceptVersions";
    private static final String RESOLVE_DOMAIN_NAME ="resolve";
    private static final String LOCAL_VALUE ="local";
    private static final String TRUE ="TRUE";
    private static final String FALSE ="FALSE";
    private static final String IMPLEMENTS_BASIC_WFS ="ImplementsBasicWFS";
    private static final String IMPLEMENTS_TRANSACTIONAL_WFS ="ImplementsTransactionalWFS";
    private static final String KVP_ENCODING ="KVPEncoding";
    private static final String RESOURCE_ID ="ResourceId";
    private static final String BOX ="BBOX";
    private static final String INTERSECTS ="Intersects";
    private static final String ENVELOPE ="Envelope";
    private static final String POLYGON ="Polygon";
    private static final String AFTER ="After";
    private static final String BEFORE ="Before";
    private static final String DURING ="During";

    public WFSHandler wfsHandler;
    public WFSCapabilitiesType getCapabilitiesType = new WFSCapabilitiesType();
    protected ValueType trueValueType = new ValueType();
    protected ValueType falseValueType = new ValueType();
    private WFSConfiguration wfsConfiguration;
    private OGCConfiguration ogcConfiguration;

    public GetCapabilitiesHandler(WFSConfiguration wfsConfiguration, OGCConfiguration ogcConfiguration, WFSHandler wfsHandler) {
        this.wfsHandler = wfsHandler;
        this.wfsConfiguration = wfsConfiguration;
        this.ogcConfiguration = ogcConfiguration;

        getCapabilitiesType.setVersion(WFSConstant.SUPPORTED_WFS_VERSION);
        trueValueType.setValue(TRUE);
        falseValueType.setValue(FALSE);
        setServiceProvider();
        setServiceIdentification();
        setOperations();
        setFilterCapabilities();
    }

    public JAXBElement<WFSCapabilitiesType> getWFSCapabilitiesResponse() {
        return wfsHandler.wfsFactory.createWFSCapabilities(getCapabilitiesType);
    }

    private void setServiceProvider() {
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setProviderName(ogcConfiguration.serviceProviderName);
        OnlineResourceType onlineResourceType = new OnlineResourceType();
        onlineResourceType.setHref(ogcConfiguration.serviceProviderSite);
        onlineResourceType.setRole(ogcConfiguration.serviceProviderRole);
        serviceProvider.setProviderSite(onlineResourceType);
        ResponsiblePartySubsetType responsiblePartySubsetType = new ResponsiblePartySubsetType();
        responsiblePartySubsetType.setIndividualName(ogcConfiguration.serviceContactIndividualName);
        ContactType contactType = new ContactType();
        AddressType addressType = new AddressType();
        addressType.setCity(ogcConfiguration.serviceContactAdressCity);
        addressType.setCountry(ogcConfiguration.serviceContactAdressCountry);
        addressType.setPostalCode(ogcConfiguration.serviceContactAdressPostalCode);
        contactType.setAddress(addressType);
        responsiblePartySubsetType.setContactInfo(contactType);
        serviceProvider.setServiceContact(responsiblePartySubsetType);
        getCapabilitiesType.setServiceProvider(serviceProvider);
    }

    private void setServiceIdentification() {
        ServiceIdentification serviceIdentification = new ServiceIdentification();
        CodeType codeType = new CodeType();
        codeType.setValue(WFSConstant.WFS);
        serviceIdentification.setServiceType(codeType);
        serviceIdentification.getServiceTypeVersion().add(WFSConstant.SUPPORTED_WFS_VERSION);
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
        DomainType[] noParameters = {};
        //create AcceptVersions parameter for GetCapabilities operation
        DomainType acceptVersions = new DomainType();
        acceptVersions.setName(ACCEPT_VERSIONS_DOMAIN_NAME);
        acceptVersions.setAllowedValues(new AllowedValues());
        ValueType version = new ValueType();
        version.setValue(WFSConstant.SUPPORTED_WFS_VERSION);
        acceptVersions.getAllowedValues().getValueOrRange().add(version);
        //create sections parameter for GetCapabilities operation
        DomainType sections = new DomainType();
        sections.setName(SECTION_DOMAIN_NAME);
        sections.setAllowedValues(new AllowedValues());
        //add sections
        Arrays.asList(SECTION_NAMES).forEach(sectionName->addSection(sectionName,sections));
        //create resolve parameter for GetFeature and GetPropertyValue operation
        DomainType resolve = new DomainType();
        resolve.setName(RESOLVE_DOMAIN_NAME);
        resolve.setAllowedValues(new AllowedValues());
        ValueType local = new ValueType();
        local.setValue(LOCAL_VALUE);
        resolve.getAllowedValues().getValueOrRange().add(local);
        //add  operations
        DomainType[] getCapabilitiesParameters = {acceptVersions,sections};
        addOperation(WFSRequestType.GetCapabilities.name(),operationsMetadata,getCapabilitiesParameters);
        addOperation(WFSRequestType.DescribeFeatureType.name(),operationsMetadata,noParameters);
        addOperation(WFSRequestType.ListStoredQueries.name(),operationsMetadata,noParameters);
        addOperation(WFSRequestType.DescribeStoredQueries.name(),operationsMetadata,noParameters);
        addOperation(WFSRequestType.GetFeature.name(),operationsMetadata,resolve);
        addOperation(WFSRequestType.GetPropertyValue.name(),operationsMetadata,resolve);
        //add  conformance
        addConformanceType(operationsMetadata, IMPLEMENTS_BASIC_WFS, trueValueType);
        addConformanceType(operationsMetadata, IMPLEMENTS_TRANSACTIONAL_WFS, falseValueType);
        addConformanceType(operationsMetadata, KVP_ENCODING, trueValueType);
        addDefaultConformance(operationsMetadata);
        getCapabilitiesType.setOperationsMetadata(operationsMetadata);
    }

    public void setFeatureTypeListType(String name,String uri) {
        FeatureTypeListType featureTypeListType = new FeatureTypeListType();
        FeatureTypeType featureTypeType = new FeatureTypeType();
        featureTypeType.setDefaultCRS(WFSConstant.SUPPORTED_CRS[0]);
        QName qname = new QName(uri, name,wfsConfiguration.featureNamespace);
        featureTypeType.setName(qname);
        OutputFormatListType outputFormatListType = new OutputFormatListType();
        Arrays.asList(WFSConstant.FEATURE_GML_FORMAT).forEach(format->outputFormatListType.getFormat().add(format));
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
        QName _ResourceId_QNAME = new QName(WFSConstant.FES_NAMESPACE_URI, RESOURCE_ID);
        resourceIdentifierType.setName(_ResourceId_QNAME);
        idCapabilitiesType.getResourceIdentifier().add(resourceIdentifierType);

        SpatialCapabilitiesType spatialCapabilities = wfsHandler.fesFactory.createSpatialCapabilitiesType();
        SpatialOperatorsType spatialOperatorsType = new SpatialOperatorsType();
        GeometryOperandsType geometryOperandsType = new GeometryOperandsType();
        SpatialOperatorType spatialOperatorBBOXType = new SpatialOperatorType();
        SpatialOperatorType spatialOperatorIntersectsType = new SpatialOperatorType();
        GeometryOperandsType.GeometryOperand envGeometryOperand = new GeometryOperandsType.GeometryOperand();
        GeometryOperandsType.GeometryOperand polyGeometryOperand = new GeometryOperandsType.GeometryOperand();
        QName envQname = new QName(WFSConstant.GML_NAMESPACE_URI, ENVELOPE,WFSConstant.GML_PREFIX);
        QName polyQname = new QName(WFSConstant.GML_NAMESPACE_URI, POLYGON,WFSConstant.GML_PREFIX);
        envGeometryOperand.setName(envQname);
        polyGeometryOperand.setName(polyQname);
        geometryOperandsType.getGeometryOperand().add(envGeometryOperand);
        geometryOperandsType.getGeometryOperand().add(polyGeometryOperand);
        spatialCapabilities.setGeometryOperands(geometryOperandsType);
        spatialOperatorBBOXType.setGeometryOperands(geometryOperandsType);
        spatialOperatorIntersectsType.setGeometryOperands(geometryOperandsType);
        spatialOperatorBBOXType.setName(BOX);
        spatialOperatorIntersectsType.setName(INTERSECTS);
        spatialOperatorsType.getSpatialOperator().add(spatialOperatorBBOXType);
        spatialOperatorsType.getSpatialOperator().add(spatialOperatorIntersectsType);
        spatialCapabilities.setSpatialOperators(spatialOperatorsType);

        QName afterQname = new QName(WFSConstant.FES_NAMESPACE_URI, AFTER);
        QName beforeQname = new QName(WFSConstant.FES_NAMESPACE_URI, BEFORE);
        QName duringQname = new QName(WFSConstant.FES_NAMESPACE_URI, DURING);
        TemporalCapabilitiesType temporalCapabilities = wfsHandler.fesFactory.createTemporalCapabilitiesType();
        TemporalOperandsType allTemporalOperandsType = new TemporalOperandsType();
        TemporalOperatorsType temporalOperatorsType = new TemporalOperatorsType();

        addTemporalOperator(temporalOperatorsType,afterQname,allTemporalOperandsType);
        addTemporalOperator(temporalOperatorsType,beforeQname,allTemporalOperandsType);
        addTemporalOperator(temporalOperatorsType,duringQname,allTemporalOperandsType);

        temporalCapabilities.setTemporalOperands(allTemporalOperandsType);
        temporalCapabilities.setTemporalOperators(temporalOperatorsType);

        filterCapabilities.setTemporalCapabilities(temporalCapabilities);
        filterCapabilities.setSpatialCapabilities(spatialCapabilities);
        filterCapabilities.setIdCapabilities(idCapabilitiesType);
        filterCapabilities.setConformance(fesConformanceType);
        getCapabilitiesType.setFilterCapabilities(filterCapabilities);


    }

    private void addSection(String sectionName,DomainType sections){
        ValueType serviceIdentification = new ValueType();
        serviceIdentification.setValue(sectionName);
        sections.getAllowedValues().getValueOrRange().add(serviceIdentification);
    }

    private void addOperation(String operationName,OperationsMetadata operationsMetadata,DomainType... parameters){
        DCP dcp = new DCP();
        HTTP http = new HTTP();
        dcp.setHTTP(http);
        Operation operation = new Operation();
        operation.setName(operationName);
        operation.getDCP().add(dcp);
        Arrays.asList(parameters).forEach(parameter->operation.getParameter().add(parameter));
        operationsMetadata.getOperation().add(operation);
    }

    private void addTemporalOperator(TemporalOperatorsType temporalOperatorsType, QName qNameOperand,TemporalOperandsType allTemporalOperandsType){
        TemporalOperatorType temporalOperatorType = new TemporalOperatorType();
        TemporalOperandsType.TemporalOperand operand = new TemporalOperandsType.TemporalOperand();
        operand.setName(qNameOperand);
        temporalOperatorType.setName(qNameOperand.getLocalPart());
        TemporalOperandsType temporalOperandsType = new TemporalOperandsType();
        temporalOperandsType.getTemporalOperand().add(operand);
        allTemporalOperandsType.getTemporalOperand().add(operand);
        temporalOperatorType.setTemporalOperands(temporalOperandsType);
        temporalOperatorsType.getTemporalOperator().add(temporalOperatorType);
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
        addConformanceType(conformanceType, "ImplementsSpatialFilter", trueValueType);
        addConformanceType(conformanceType, "ImplementsTemporalFilter", falseValueType);
        addConformanceType(conformanceType, "ImplementsMinSpatialFilter", trueValueType);
        addConformanceType(conformanceType, "ImplementsSorting", trueValueType);
        addConformanceType(conformanceType, "ImplementsMinTemporalFilter", falseValueType);
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