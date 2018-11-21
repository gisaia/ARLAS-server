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


import eu.europa.ec.inspire.schemas.common._1.*;
import eu.europa.ec.inspire.schemas.inspire_dls._1.ExtendedCapabilitiesType;
import io.arlas.server.app.InspireConfiguration;
import io.arlas.server.app.OGCConfiguration;
import io.arlas.server.app.WFSConfiguration;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.OGC.OGCException;
import io.arlas.server.inspire.common.utils.InspireCheckParam;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.DublinCoreElementName;
import io.arlas.server.model.InspireConformity;
import io.arlas.server.model.Keyword;
import io.arlas.server.inspire.common.constants.InspireConstants;
import io.arlas.server.model.enumerations.InspireSupportedLanguages;
import io.arlas.server.model.enumerations.AccessConstraintEnum;
import io.arlas.server.ogc.common.model.Service;
import io.arlas.server.ogc.wfs.WFSHandler;
import io.arlas.server.ogc.wfs.utils.ExtendedWFSCapabilitiesType;
import io.arlas.server.ogc.wfs.utils.WFSConstant;
import io.arlas.server.ogc.wfs.utils.WFSRequestType;

import net.opengis.fes._2.*;
import net.opengis.ows._1.*;
import net.opengis.wfs._2.*;
import org.w3._1999.xlink.TypeType;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class GetCapabilitiesHandler {

    private static final String[] SECTION_NAMES = {"ServiceIdentification", "ServiceProvider", "FeatureTypeList", "Filter_Capabilities"};
    private static final String SECTION_DOMAIN_NAME = "Sections";
    private static final String ACCEPT_VERSIONS_DOMAIN_NAME = "AcceptVersions";
    private static final String RESOLVE_DOMAIN_NAME = "resolve";
    private static final String LOCAL_VALUE = "local";
    private static final String TRUE = "TRUE";
    private static final String FALSE = "FALSE";
    private static final String IMPLEMENTS_BASIC_WFS = "ImplementsBasicWFS";
    private static final String IMPLEMENTS_TRANSACTIONAL_WFS = "ImplementsTransactionalWFS";
    private static final String KVP_ENCODING = "KVPEncoding";
    private static final String RESOURCE_ID = "ResourceId";
    private static final String BOX = "BBOX";
    private static final String INTERSECTS = "Intersects";
    private static final String ENVELOPE = "Envelope";
    private static final String POLYGON = "Polygon";
    private static final String AFTER = "After";
    private static final String BEFORE = "Before";
    private static final String DURING = "During";
    private static final String TIMEINSTANT = "TimeInstant";
    private static final String TIMEPERIOD = "TimePeriod";
    private static final String METADATA_URL = "ogc/csw?service=CSW&request=GetRecordById&version=3.0.0&elementSetName=full&outputSchema=http://www.isotc211.org/2005/gmd&id=";
    private static final String METADATA_DEFAULT_SUPPORTED_LANGUAGE = InspireSupportedLanguages.eng.name();

    public WFSHandler wfsHandler;
    public ExtendedWFSCapabilitiesType getCapabilitiesType = new ExtendedWFSCapabilitiesType();
    public ExtendedCapabilitiesType inspireExtendedCapabilitiesType = new ExtendedCapabilitiesType();
    protected ValueType trueValueType = new ValueType();
    protected ValueType falseValueType = new ValueType();
    private WFSConfiguration wfsConfiguration;
    private OGCConfiguration ogcConfiguration;
    private InspireConfiguration inspireConfiguration;

    public GetCapabilitiesHandler(WFSHandler wfsHandler) {
        this.wfsHandler = wfsHandler;
        this.wfsConfiguration = wfsHandler.wfsConfiguration;
        this.ogcConfiguration = wfsHandler.ogcConfiguration;
        this.inspireConfiguration = wfsHandler.inspireConfiguration;

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
        Arrays.asList(SECTION_NAMES).forEach(sectionName -> addSection(sectionName, sections));
        //create resolve parameter for GetFeature and GetPropertyValue operation
        DomainType resolve = new DomainType();
        resolve.setName(RESOLVE_DOMAIN_NAME);
        resolve.setAllowedValues(new AllowedValues());
        ValueType local = new ValueType();
        local.setValue(LOCAL_VALUE);
        resolve.getAllowedValues().getValueOrRange().add(local);
        //add  operations
        DomainType[] getCapabilitiesParameters = {acceptVersions, sections};
        addOperation(WFSRequestType.GetCapabilities.name(), operationsMetadata, getCapabilitiesParameters);
        addOperation(WFSRequestType.DescribeFeatureType.name(), operationsMetadata, noParameters);
        addOperation(WFSRequestType.ListStoredQueries.name(), operationsMetadata, noParameters);
        addOperation(WFSRequestType.DescribeStoredQueries.name(), operationsMetadata, noParameters);
        addOperation(WFSRequestType.GetFeature.name(), operationsMetadata, resolve);
        addOperation(WFSRequestType.GetPropertyValue.name(), operationsMetadata, resolve);
        //add  conformance
        addConformanceType(operationsMetadata, IMPLEMENTS_BASIC_WFS, trueValueType);
        addConformanceType(operationsMetadata, IMPLEMENTS_TRANSACTIONAL_WFS, falseValueType);
        addConformanceType(operationsMetadata, KVP_ENCODING, trueValueType);
        addDefaultConformance(operationsMetadata);
        getCapabilitiesType.setOperationsMetadata(operationsMetadata);
    }

    public void setFeatureTypeListType(CollectionReference collectionReference, String uri) {
        FeatureTypeListType featureTypeListType = new FeatureTypeListType();
        FeatureTypeType featureTypeType = new FeatureTypeType();
        featureTypeType.setDefaultCRS(WFSConstant.SUPPORTED_CRS[0]);
        QName qname = new QName(uri, collectionReference.collectionName, wfsConfiguration.featureNamespace);
        featureTypeType.setName(qname);
        MetadataURLType metadataURLType = new MetadataURLType();
        String url = ogcConfiguration.serverUri + METADATA_URL + collectionReference.params.dublinCoreElementName.identifier;
        metadataURLType.setHref(url);
        metadataURLType.setType(TypeType.SIMPLE);
        featureTypeType.getMetadataURL().add(metadataURLType);
        OutputFormatListType outputFormatListType = new OutputFormatListType();
        Arrays.asList(WFSConstant.FEATURE_GML_FORMAT).forEach(format -> outputFormatListType.getFormat().add(format));
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
        QName envQname = new QName(WFSConstant.GML_NAMESPACE_URI, ENVELOPE, WFSConstant.GML_PREFIX);
        QName polyQname = new QName(WFSConstant.GML_NAMESPACE_URI, POLYGON, WFSConstant.GML_PREFIX);
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
        QName instantQname = new QName(WFSConstant.GML_NAMESPACE_URI, TIMEINSTANT, WFSConstant.GML_PREFIX);
        QName timeperiodQname = new QName(WFSConstant.GML_NAMESPACE_URI, TIMEPERIOD, WFSConstant.GML_PREFIX);

        QName duringQname = new QName(WFSConstant.FES_NAMESPACE_URI, DURING);
        TemporalCapabilitiesType temporalCapabilities = wfsHandler.fesFactory.createTemporalCapabilitiesType();
        TemporalOperandsType allTemporalOperandsType = new TemporalOperandsType();
        TemporalOperatorsType temporalOperatorsType = new TemporalOperatorsType();

        addTemporalOperator(temporalOperatorsType, afterQname, instantQname, timeperiodQname,allTemporalOperandsType);
        addTemporalOperator(temporalOperatorsType, beforeQname,instantQname, timeperiodQname,allTemporalOperandsType);
        addTemporalOperator(temporalOperatorsType, duringQname,instantQname, timeperiodQname,allTemporalOperandsType);

        temporalCapabilities.setTemporalOperands(allTemporalOperandsType);
        temporalCapabilities.setTemporalOperators(temporalOperatorsType);

        filterCapabilities.setTemporalCapabilities(temporalCapabilities);
        filterCapabilities.setSpatialCapabilities(spatialCapabilities);
        filterCapabilities.setIdCapabilities(idCapabilitiesType);
        filterCapabilities.setConformance(fesConformanceType);
        getCapabilitiesType.setFilterCapabilities(filterCapabilities);
    }

    private void addSection(String sectionName, DomainType sections) {
        ValueType serviceIdentification = new ValueType();
        serviceIdentification.setValue(sectionName);
        sections.getAllowedValues().getValueOrRange().add(serviceIdentification);
    }

    private void addOperation(String operationName, OperationsMetadata operationsMetadata, DomainType... parameters) {
        DCP dcp = new DCP();
        HTTP http = new HTTP();
        dcp.setHTTP(http);
        Operation operation = new Operation();
        operation.setName(operationName);
        operation.getDCP().add(dcp);
        Arrays.asList(parameters).forEach(parameter -> operation.getParameter().add(parameter));
        operationsMetadata.getOperation().add(operation);
    }

    private void addTemporalOperator(TemporalOperatorsType temporalOperatorsType, QName qNameOperator,QName qNameOperand1, QName qNameOperand2,TemporalOperandsType allTemporalOperandsType) {
        TemporalOperatorType temporalOperatorType = new TemporalOperatorType();
        TemporalOperandsType.TemporalOperand operand1 = new TemporalOperandsType.TemporalOperand();
        operand1.setName(qNameOperand1);
        TemporalOperandsType.TemporalOperand operand2 = new TemporalOperandsType.TemporalOperand();
        operand2.setName(qNameOperand2);
        temporalOperatorType.setName(qNameOperator.getLocalPart());
        TemporalOperandsType temporalOperandsType = new TemporalOperandsType();
        temporalOperandsType.getTemporalOperand().add(operand1);
        allTemporalOperandsType.getTemporalOperand().add(operand1);
        temporalOperandsType.getTemporalOperand().add(operand2);
        allTemporalOperandsType.getTemporalOperand().add(operand2);
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
        addConformanceType(conformanceType, "ImplementsTemporalFilter", trueValueType);
        addConformanceType(conformanceType, "ImplementsMinSpatialFilter", trueValueType);
        addConformanceType(conformanceType, "ImplementsSorting", trueValueType);
        addConformanceType(conformanceType, "ImplementsMinTemporalFilter", trueValueType);
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

    public void addINSPIRECompliantElements(CollectionReference collectionReference, String serviceUrl, String language) throws ArlasException {
        addExtendedCapabilities(collectionReference, serviceUrl);
        setInspireServiceIdentification(collectionReference, language);
        setInspireFeatureTypeBoundingBox(collectionReference);
    }

    private void addECResourceLocator(String serviceUrl) {
        ResourceLocatorType resourceLocatorType = new ResourceLocatorType();
        resourceLocatorType.setURL(serviceUrl + WFSConstant.WFS_GET_CAPABILITIES_PARAMETERS);
        inspireExtendedCapabilitiesType.getResourceLocator().clear();
        inspireExtendedCapabilitiesType.getResourceLocator().add(resourceLocatorType);
    }

    private void addECTemporalReference(String dateOfCreation) {
        TemporalReference temporalReference = new TemporalReference();
        temporalReference.setDateOfCreation(dateOfCreation);
        inspireExtendedCapabilitiesType.getTemporalReference().clear();
        inspireExtendedCapabilitiesType.getTemporalReference().add(temporalReference);
    }

    private void addECConformity() {
        Conformity metadataConformity = new Conformity();
        CitationConformity citationConformity = new CitationConformity();
        citationConformity.setTitle(InspireConformity.INSPIRE_METADATA_CONFORMITY_TITLE);
        citationConformity.setDateOfCreation(InspireConformity.INSPIRE_METADATA_CONFORMITY_DATE);
        metadataConformity.setSpecification(citationConformity);
        metadataConformity.setDegree(DegreeOfConformity.CONFORMANT);

        Conformity networkServicesConformity = new Conformity();
        citationConformity = new CitationConformity();
        citationConformity.setTitle(InspireConformity.INSPIRE_NETWORK_SERVICES_CONFORMITY_TITLE);
        citationConformity.setDateOfCreation(InspireConformity.INSPIRE_NETWORK_SERVICES_CONFORMITY_DATE);
        networkServicesConformity.setSpecification(citationConformity);
        networkServicesConformity.setDegree(DegreeOfConformity.NOT_EVALUATED);

        Conformity interoperabilityConformity = new Conformity();
        citationConformity = new CitationConformity();
        citationConformity.setTitle(InspireConformity.INSPIRE_INTEROPERABILITY_CONFORMITY_TITLE);
        citationConformity.setDateOfCreation(InspireConformity.INSPIRE_INTEROPERABILITY_CONFORMITY_DATE);
        interoperabilityConformity.setSpecification(citationConformity);
        interoperabilityConformity.setDegree(DegreeOfConformity.NOT_EVALUATED);

        inspireExtendedCapabilitiesType.getConformity().clear();
        inspireExtendedCapabilitiesType.getConformity().add(metadataConformity);
        inspireExtendedCapabilitiesType.getConformity().add(networkServicesConformity);
        inspireExtendedCapabilitiesType.getConformity().add(interoperabilityConformity);
    }

    private void addECMetadataPointOfContact() {
        MetadataPointOfContact metadataPointOfContact = new MetadataPointOfContact();
        String email = Optional.ofNullable(ogcConfiguration.serviceContactMail).orElse(InspireConstants.METADATA_POINT_OF_CONTACT_EMAIL);
        String name = Optional.ofNullable(ogcConfiguration.serviceProviderName).orElse(InspireConstants.METADATA_POINT_OF_CONTACT_NAME);
        metadataPointOfContact.setEmailAddress(email);
        metadataPointOfContact.setOrganisationName(name);
        inspireExtendedCapabilitiesType.getMetadataPointOfContact().clear();
        inspireExtendedCapabilitiesType.getMetadataPointOfContact().add(metadataPointOfContact);
    }

    private void addECMetadataLanguage(CollectionReference collectionReference) throws OGCException {
        SupportedLanguagesType supportedLanguagesType = new SupportedLanguagesType();
        String defaultLanguage = Optional.ofNullable(collectionReference.params.dublinCoreElementName).map(d -> d.language).filter(t -> !t.isEmpty()).map(String::toString).orElse(METADATA_DEFAULT_SUPPORTED_LANGUAGE);
        InspireCheckParam.checkLanguageInspireCompliance(defaultLanguage, Service.WFS);
        LanguageElementISO6392B defaultLanguageIso = new LanguageElementISO6392B();
        defaultLanguageIso.setLanguage(defaultLanguage);
        supportedLanguagesType.setDefaultLanguage(defaultLanguageIso);
        inspireExtendedCapabilitiesType.setSupportedLanguages(supportedLanguagesType);
        inspireExtendedCapabilitiesType.setResponseLanguage(defaultLanguageIso);
    }

    private void addECUniqueResourceIdentifier(CollectionReference collectionReference) {
        UniqueResourceIdentifier uniqueResourceIdentifier = new UniqueResourceIdentifier();
        String code = Optional.ofNullable(collectionReference.params.inspire).map(inspire -> inspire.inspireURI)
                .map(inspireURI -> inspireURI.code).map(c -> "WFS-" + c).orElse("WFS-" + collectionReference.params.dublinCoreElementName.identifier);
        String namespace = Optional.ofNullable(collectionReference.params.inspire).map(inspire -> inspire.inspireURI)
                .map(inspireURI -> inspireURI.namespace).map(String::toString).orElse("ARLAS." + collectionReference.collectionName.toUpperCase());
        uniqueResourceIdentifier.setCode(code);
        uniqueResourceIdentifier.setNamespace(namespace);
        inspireExtendedCapabilitiesType.getSpatialDataSetIdentifier().clear();
        inspireExtendedCapabilitiesType.getSpatialDataSetIdentifier().add(uniqueResourceIdentifier);
    }

    private void addECKeywords(CollectionReference collectionReference) {
        List<Keyword> keywords = Optional.ofNullable(collectionReference.params.inspire).map(inspire -> inspire.keywords).orElse(new ArrayList<>());
        inspireExtendedCapabilitiesType.getKeyword().clear();
        inspireExtendedCapabilitiesType.getMandatoryKeyword().clear();
        getCapabilitiesType.getServiceIdentification().getKeywords().clear();
        KeywordsType serviceIndentificationKeywords = new KeywordsType();

        // ADD Mandatory keyword
        ClassificationOfSpatialDataService classificationOfSpatialDataServiceMandatory = new ClassificationOfSpatialDataService();
        classificationOfSpatialDataServiceMandatory.setKeywordValue(InspireConstants.WFS_MANDATORY_KEYWORD);
        inspireExtendedCapabilitiesType.getMandatoryKeyword().add(classificationOfSpatialDataServiceMandatory);

        LanguageStringType mandatoryKeywordString = new LanguageStringType();
        mandatoryKeywordString.setValue(InspireConstants.WFS_MANDATORY_KEYWORD);
        serviceIndentificationKeywords.getKeyword().add(mandatoryKeywordString);

        keywords.forEach(keyword -> {
            /* Check if other keywords have a Controled vocabulary*/
            eu.europa.ec.inspire.schemas.common._1.Keyword inspireKeyword = new eu.europa.ec.inspire.schemas.common._1.Keyword();
            inspireKeyword.setKeywordValue(keyword.value);
            if (keyword.vocabulary != null && !keyword.vocabulary.equals("")) {
                OriginatingControlledVocabulary vocabulary = new OriginatingControlledVocabulary();
                vocabulary.setTitle(keyword.vocabulary);
                if (keyword.dateOfPublication != null && !keyword.dateOfPublication.equals("")) {
                    vocabulary.setDateOfCreation(keyword.dateOfPublication);
                }
                inspireKeyword.setOriginatingControlledVocabulary(vocabulary);
            }
            inspireExtendedCapabilitiesType.getKeyword().add(inspireKeyword);
            LanguageStringType languageStringType = new LanguageStringType();
            languageStringType.setValue(keyword.value);
            serviceIndentificationKeywords.getKeyword().add(languageStringType);
        });
        getCapabilitiesType.getServiceIdentification().getKeywords().add(serviceIndentificationKeywords);
    }

    private void addECMetadaURL(CollectionReference collectionReference) {
        ResourceLocatorType resourceLocatorType = new ResourceLocatorType();
        resourceLocatorType.setURL(ogcConfiguration.serverUri + METADATA_URL + collectionReference.params.dublinCoreElementName.identifier);
        inspireExtendedCapabilitiesType.setMetadataUrl(resourceLocatorType);
    }

    private void addExtendedCapabilities(CollectionReference collectionReference, String serviceUrl) throws OGCException {
        // Add INSPIRE MetadataURL
        addECMetadaURL(collectionReference);
        // Add INSPIRE Resource type
        inspireExtendedCapabilitiesType.setResourceType(ResourceType.SERVICE);
        // Add INSPIRE Spatial data service type
        inspireExtendedCapabilitiesType.setSpatialDataServiceType(SpatialDataServiceType.DOWNLOAD);
        // Add INSPIRE Metadata date
        String metadataDate = collectionReference.params.dublinCoreElementName.getDate();
        inspireExtendedCapabilitiesType.setMetadataDate(metadataDate);
        // Add INSPIRE Resource Locator
        addECResourceLocator(serviceUrl);
        // Add INSPIRE Temporal Reference
        addECTemporalReference(inspireConfiguration.servicesDateOfCreation);
        // Add INSPIRE Conformity
        addECConformity();
        // Add INSPIRE Metadata Point of Contact
        addECMetadataPointOfContact();
        // Add INSPIRE Metadata Language
        addECMetadataLanguage(collectionReference);
        // Add INSPIRE Unique Resource Identifier
        addECUniqueResourceIdentifier(collectionReference);
        // Add INSPIRE keywords
        addECKeywords(collectionReference);
        ExtendedCapabilities extendedCapabilities  = new ExtendedCapabilities();
        extendedCapabilities.setExtendedCapabilities(inspireExtendedCapabilitiesType);
        getCapabilitiesType.getOperationsMetadata().setExtendedCapabilities(extendedCapabilities);
    }

    private void setInspireServiceIdentification(CollectionReference collectionReference, String language) throws ArlasException {
        // Add INSPIRE 'Resource Title'
        LanguageStringType WFSTitle = new LanguageStringType();
        ServiceIdentification serviceIdentification = getCapabilitiesType.getServiceIdentification();

        // FOR NOW we just check if the language is correct but we always return the only language declared in the collection reference params
        if (language != null) {
            InspireCheckParam.checkLanguageInspireCompliance(language, Service.WFS);
        }

        WFSTitle.setValue(Optional.ofNullable(collectionReference.params.dublinCoreElementName).map(d -> d.title).filter(t -> !t.isEmpty()).map(t -> InspireConstants.INSPIRE_WFS_RESOURCE_TITLE + " - " + t).orElse(InspireConstants.INSPIRE_WFS_RESOURCE_TITLE));
        serviceIdentification.getTitle().clear();
        serviceIdentification.getTitle().add(WFSTitle);

        // Add INSPIRE 'Resource Abstract'
        LanguageStringType WFSAbstract = new LanguageStringType();
        WFSAbstract.setValue(Optional.ofNullable(collectionReference.params.dublinCoreElementName).map(d -> d.description).filter(t -> !t.isEmpty()).map(d -> InspireConstants.INSPIRE_WFS_RESOURCE_TITLE + " - " + d).orElse(InspireConstants.INSPIRE_WFS_RESOURCE_TITLE));
        serviceIdentification.getAbstract().clear();
        serviceIdentification.getAbstract().add(WFSAbstract);

        // Add INSPIRE 'Conditions for Access and Use'
        String conditionsForAccessAndUse = Optional.ofNullable(inspireConfiguration).map(c -> c.accessAndUseConditions).map(String::toString).orElse(InspireConstants.NO_CONDITIONS_FOR_ACCESS_AND_USE);
        serviceIdentification.setFees(conditionsForAccessAndUse);

        // Add INSPIRE 'Limitations on Public Access'
        serviceIdentification.getAccessConstraints().clear();
        String limitationsOnPublicAccess = Optional.ofNullable(collectionReference.params.inspire).map(inspire -> inspire.inspireLimitationAccess).map(inspireLimitationAccess -> inspireLimitationAccess.accessConstraints).orElse(AccessConstraintEnum.otherRestrictions.name());
        if (limitationsOnPublicAccess.equals(AccessConstraintEnum.otherRestrictions.name())) {
            limitationsOnPublicAccess = Optional.ofNullable(collectionReference.params.inspire).map(inspire -> inspire.inspireLimitationAccess).map(inspireLimitationAccess -> inspireLimitationAccess.otherConstraints).orElse(InspireConstants.LIMITATION_ON_PUBLIC_ACCESS);
        }
        serviceIdentification.getAccessConstraints().add(limitationsOnPublicAccess);

        getCapabilitiesType.setServiceIdentification(serviceIdentification);
    }

    private void setInspireFeatureTypeBoundingBox(CollectionReference collectionReference) {
        FeatureTypeListType featureTypeListType = getCapabilitiesType.getFeatureTypeList();
        DublinCoreElementName.Bbox bbox = collectionReference.params.dublinCoreElementName.bbox;
        featureTypeListType.getFeatureType().forEach( featureTypeType -> {
            featureTypeType.getWGS84BoundingBox().clear();
            WGS84BoundingBoxType wgs84BoundingBoxType = new WGS84BoundingBoxType();
            wgs84BoundingBoxType.getLowerCorner().add(bbox.west);
            wgs84BoundingBoxType.getLowerCorner().add(bbox.south);
            wgs84BoundingBoxType.getUpperCorner().add(bbox.east);
            wgs84BoundingBoxType.getUpperCorner().add(bbox.north);
            featureTypeType.getWGS84BoundingBox().add(wgs84BoundingBoxType);
        });
        getCapabilitiesType.setFeatureTypeList(featureTypeListType);
    }
}