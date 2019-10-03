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

package io.arlas.server.ogc.csw.utils;

import io.arlas.server.core.app.InspireConfiguration;
import io.arlas.server.core.app.OGCConfiguration;
import io.arlas.server.core.model.*;
import io.arlas.server.ogc.common.exceptions.OGC.OGCException;
import io.arlas.server.ogc.common.inspire.common.constants.InspireConstants;
import io.arlas.server.ogc.common.utils.OGCConstant;
import net.opengis.gml._3.LengthType;
import org.isotc211._2005.gco.*;
import org.isotc211._2005.gmd.*;
import org.isotc211._2005.gmd.ObjectFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class MDMetadataBuilder {

    private static final String SCOPE_CODE_URL = "http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#MD_ScopeCode";
    private static final String DATE_TYPE_CODE_LIST = "http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#CI_DateTypeCode";
    private static final String ROLE_CODE_LIST = "http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#CI_RoleCode";
    private static final String RESTRICTION_CODE_LIST = "http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#MD_RestrictionCode";
    private static final String FUNCTION_CODE_LIST = "http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#CI_OnLineFunctionCode";
    private static final String SPATIAL_REPRESENTATION_CODE_LIST = "http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_SpatialRepresentationTypeCode";
    private static final String PUBLICATION_DATE_TYPE = "publication";
    private static final String CREATION_DATE_TYPE = "creation";

    private static final String RESOURCE_TYPE = "dataset";
    private static final String NAMESPACE = "/ogc/csw?service=CSW&request=GetRecordById&version=3.0.0&elementSetName=full&outputSchema=http://www.isotc211.org/2005/gmd&id=";

    public static final ObjectFactory gmdObjectFactory = new ObjectFactory();
    public static final org.isotc211._2005.gco.ObjectFactory gcoObjectFactory = new org.isotc211._2005.gco.ObjectFactory();


    public static MDMetadataType getBriefMDMetadata(CollectionReference collectionReference) {
        MDMetadataType mdMetadataType = new MDMetadataType();
        DublinCoreElementName dublinCoreElementName = collectionReference.params.dublinCoreElementName;
        addFileIdentifier(mdMetadataType, dublinCoreElementName.identifier);
        addHierarchyLevel(mdMetadataType, RESOURCE_TYPE);
        addBriefIdentificationInfo(mdMetadataType, collectionReference);
        return mdMetadataType;
    }

    public static MDMetadataType getSummaryMDMetadata(CollectionReference collectionReference, OGCConfiguration ogcConfiguration, InspireConfiguration inspireConfiguration, String baseUri) throws OGCException {
        MDMetadataType mdMetadataType = new MDMetadataType();
        DublinCoreElementName dublinCoreElementName = collectionReference.params.dublinCoreElementName;
        addFileIdentifier(mdMetadataType, dublinCoreElementName.identifier);
        addLanguage(mdMetadataType,dublinCoreElementName.language);
        addHierarchyLevel(mdMetadataType, RESOURCE_TYPE);
        addDateStamp(mdMetadataType, dublinCoreElementName.getDate());
        addMetadataStandardName(mdMetadataType);
        addSummaryIdentificationInfo(mdMetadataType, collectionReference, ogcConfiguration, inspireConfiguration);
        addDistibutionInfo(mdMetadataType, collectionReference, baseUri, ElementSetName.summary);
        return mdMetadataType;
    }

    public static MDMetadataType getFullMDMetadata(CollectionReference collectionReference, OGCConfiguration ogcConfiguration, InspireConfiguration inspireConfiguration, String baseUri) throws OGCException {
        MDMetadataType mdMetadataType = new MDMetadataType();
        DublinCoreElementName dublinCoreElementName = collectionReference.params.dublinCoreElementName;
        addFileIdentifier(mdMetadataType, dublinCoreElementName.identifier);
        addLanguage(mdMetadataType,dublinCoreElementName.language);

        // todo add charactersetcode
        addHierarchyLevel(mdMetadataType, RESOURCE_TYPE);
        addContact(mdMetadataType, ogcConfiguration);
        addDateStamp(mdMetadataType, dublinCoreElementName.getDate());
        addMetadataStandardName(mdMetadataType);
        addSummaryIdentificationInfo(mdMetadataType, collectionReference, ogcConfiguration, inspireConfiguration);
        addReferenceSystemInfo(mdMetadataType, "http://www.opengis.net/def/crs/EPSG/0/4258", "EPSG:4258", "UTC");
        addDistibutionInfo(mdMetadataType, collectionReference, baseUri, ElementSetName.full);
        addDataQuality(mdMetadataType, collectionReference, inspireConfiguration.enabled);
        return mdMetadataType;
    }

    public static void addReferenceSystemInfo(MDMetadataType mdMetadataType, String geoCodeSpace, String geoCode, String temporalReference) {
        MDReferenceSystemPropertyType geoReferenceSystemPropertyType = new MDReferenceSystemPropertyType();
        MDReferenceSystemType geoReferenceSystemType = new MDReferenceSystemType();
        RSIdentifierPropertyType rsIdentifierPropertyType = new RSIdentifierPropertyType();
        RSIdentifierType rsIdentifierType = new RSIdentifierType();
        rsIdentifierType.setCodeSpace(createCharacterStringPropertyType(geoCodeSpace));
        rsIdentifierType.setCode(createCharacterStringPropertyType(geoCode));
        rsIdentifierPropertyType.setRSIdentifier(rsIdentifierType);
        geoReferenceSystemType.setReferenceSystemIdentifier(rsIdentifierPropertyType);
        geoReferenceSystemPropertyType.setMDReferenceSystem(geoReferenceSystemType);

        MDReferenceSystemPropertyType temporalReferenceSystemPropertyType = new MDReferenceSystemPropertyType();
        MDReferenceSystemType temporalReferenceSystemType = new MDReferenceSystemType();
        RSIdentifierPropertyType temporalRsIdentifierPropertyType = new RSIdentifierPropertyType();
        RSIdentifierType temporalRsIdentifierType = new RSIdentifierType();
        temporalRsIdentifierType.setCode(createCharacterStringPropertyType(temporalReference));
        temporalRsIdentifierPropertyType.setRSIdentifier(temporalRsIdentifierType);
        temporalReferenceSystemType.setReferenceSystemIdentifier(temporalRsIdentifierPropertyType);
        temporalReferenceSystemPropertyType.setMDReferenceSystem(temporalReferenceSystemType);

        mdMetadataType.getReferenceSystemInfo().add(geoReferenceSystemPropertyType);
        mdMetadataType.getReferenceSystemInfo().add(temporalReferenceSystemPropertyType);
    }

    public static void addDistibutionInfo(MDMetadataType mdMetadataType, CollectionReference collectionReference, String baseUri, ElementSetName elementSetName) {
        MDDistributionPropertyType mdDistributionPropertyType = new MDDistributionPropertyType();
        MDDistributionType mdDistributionType = new MDDistributionType();
        String format = Optional.ofNullable(collectionReference.params.dublinCoreElementName.format).filter(f -> !f.equals("")).orElse("json");
        addDistributionFormat(mdDistributionType, format);
        addTransferOptions(mdDistributionType, collectionReference, baseUri, elementSetName);
        mdDistributionPropertyType.setMDDistribution(mdDistributionType);
        mdMetadataType.setDistributionInfo(mdDistributionPropertyType);
    }

    public static void addDataQuality(MDMetadataType mdMetadataType, CollectionReference collectionReference, boolean inspireEnabled) {
        DQDataQualityPropertyType dataQualityPropertyType = new DQDataQualityPropertyType();
        DQDataQualityType dataQualityType = new DQDataQualityType();
        DQScopePropertyType dqScopePropertyType = new DQScopePropertyType();
        DQScopeType dqScopeType = new DQScopeType();
        MDScopeCodePropertyType mdScopeCodePropertyType = createScope("dataset");
        dqScopeType.setLevel(mdScopeCodePropertyType);
        dqScopePropertyType.setDQScope(dqScopeType);
        dataQualityType.setScope(dqScopePropertyType);
        if (inspireEnabled) {
            DQElementPropertyType interoperabilityConformity = createDomainConsistency(InspireConformity.INSPIRE_INTEROPERABILITY_CONFORMITY_TITLE, InspireConformity.INSPIRE_INTEROPERABILITY_CONFORMITY_DATE, PUBLICATION_DATE_TYPE, false);
            DQElementPropertyType metadataConformity = createDomainConsistency(InspireConformity.INSPIRE_METADATA_CONFORMITY_TITLE, InspireConformity.INSPIRE_METADATA_CONFORMITY_DATE, PUBLICATION_DATE_TYPE, true);
            dataQualityType.getReport().add(interoperabilityConformity);
            dataQualityType.getReport().add(metadataConformity);
            if (collectionReference.params.inspire.lineage != null) {
                LILineagePropertyType liLineagePropertyType = new LILineagePropertyType();
                LILineageType liLineageType = new LILineageType();
                liLineageType.setStatement(createCharacterStringPropertyType(collectionReference.params.inspire.lineage));
                liLineagePropertyType.setLILineage(liLineageType);
                dataQualityType.setLineage(liLineagePropertyType);
            }
            dataQualityPropertyType.setDQDataQuality(dataQualityType);
        }
        mdMetadataType.getDataQualityInfo().add(dataQualityPropertyType);
    }

    public static void addContact(MDMetadataType mdMetadataType, OGCConfiguration ogcConfiguration) {
        mdMetadataType.getContact().add(createResponsableParty(ogcConfiguration));
    }

    public static void addDistributionFormat(MDDistributionType mdDistributionType, String format) {
        MDFormatPropertyType mdFormatPropertyType = new MDFormatPropertyType();
        MDFormatType mdFormatType = new MDFormatType();
        mdFormatType.setName(createCharacterStringPropertyType(format));
        mdFormatPropertyType.setMDFormat(mdFormatType);
        mdDistributionType.getDistributionFormat().add(mdFormatPropertyType);
    }
    public static void addTransferOptions(MDDistributionType mdDistributionType, CollectionReference collectionReference, String baseUri, ElementSetName elementSetName) {
        MDDigitalTransferOptionsPropertyType mdDigitalTransferOptionsPropertyType = new MDDigitalTransferOptionsPropertyType();
        MDDigitalTransferOptionsType mdDigitalTransferOptionsType = new MDDigitalTransferOptionsType();
        CIOnlineResourcePropertyType ciOnlineResourcePropertyType = new CIOnlineResourcePropertyType();
        CIOnlineResourceType ciOnlineResourceType = new CIOnlineResourceType();
        URLPropertyType urlPropertyType = new URLPropertyType();
        urlPropertyType.setURL(baseUri + "ogc/wfs/" + collectionReference.collectionName + "/?" + OGCConstant.WFS_GET_GETFEATURE_PARAMETERS);
        ciOnlineResourceType.setLinkage(urlPropertyType);
        ciOnlineResourceType.setProtocol(createCharacterStringPropertyType("OGC:WFS"));
        String description = "WFS GetFeature request to download the Dataset";
        ciOnlineResourceType.setName(createCharacterStringPropertyType("WFS Download service for " + collectionReference.collectionName));
        ciOnlineResourceType.setDescription(createCharacterStringPropertyType(description));
        CIOnLineFunctionCodePropertyType ciOnLineFunctionCodePropertyType = new CIOnLineFunctionCodePropertyType();
        CodeListValueType codeListValueType = getCodeListValueType(FUNCTION_CODE_LIST, "download");
        ciOnLineFunctionCodePropertyType.setCIOnLineFunctionCode(codeListValueType);
        ciOnlineResourceType.setFunction(ciOnLineFunctionCodePropertyType);
        ciOnlineResourcePropertyType.setCIOnlineResource(ciOnlineResourceType);
        mdDigitalTransferOptionsType.getOnLine().add(ciOnlineResourcePropertyType);
        mdDigitalTransferOptionsPropertyType.setMDDigitalTransferOptions(mdDigitalTransferOptionsType);
        mdDistributionType.getTransferOptions().add(mdDigitalTransferOptionsPropertyType);
    }


    public static void addAbstract(MDDataIdentificationType mdDataIdentificationType, String description) {
        mdDataIdentificationType.setAbstract(createCharacterStringPropertyType(description));
    }

    public static void addPointOfContact(MDDataIdentificationType dataIdentificationType, OGCConfiguration ogcConfiguration) {
        CIResponsiblePartyPropertyType ciResponsiblePartyPropertyType = createResponsableParty(ogcConfiguration);
        dataIdentificationType.getPointOfContact().add(ciResponsiblePartyPropertyType);
    }

    public static void addKeywords(MDDataIdentificationType mdDataIdentificationType, CollectionReference collectionReference) {
        List<Keyword> keywords = collectionReference.params.inspire.keywords;
        if (keywords != null) {
            for (Keyword keyword : keywords) {
                MDKeywordsPropertyType mdKeywordsPropertyType = new MDKeywordsPropertyType();
                MDKeywordsType mdKeywordsType = new MDKeywordsType();
                mdKeywordsType.getKeyword().add(createCharacterStringPropertyType(keyword.value));
                if (keyword.vocabulary != null && !keyword.vocabulary.equals("")) {
                    mdKeywordsType.setThesaurusName(createCICitation(keyword.vocabulary, keyword.dateOfPublication, PUBLICATION_DATE_TYPE));
                }
                mdKeywordsPropertyType.setMDKeywords(mdKeywordsType);
                mdDataIdentificationType.getDescriptiveKeywords().add(mdKeywordsPropertyType);
            }
        }
    }

    public static void addResourceConstraints(MDDataIdentificationType identificationType, CollectionReference collectionReference) {
        MDConstraintsPropertyType mdConstraintsPropertyType = new MDConstraintsPropertyType();
        MDConstraintsType mdConstraintsType = new MDConstraintsType();
        mdConstraintsType.getUseLimitation().add(createCharacterStringPropertyType(Optional.ofNullable(collectionReference.params.inspire).map(inspire -> inspire.inspireUseConditions)
                .orElse(InspireConstants.NO_CONDITIONS_FOR_ACCESS_AND_USE)));
        mdConstraintsPropertyType.setMDConstraints(gmdObjectFactory.createMDConstraints(mdConstraintsType));
        identificationType.getResourceConstraints().add(mdConstraintsPropertyType);

        MDConstraintsPropertyType mdLegalConstraintsPropertyType = new MDConstraintsPropertyType();
        MDLegalConstraintsType mdLegalConstraintsType = new MDLegalConstraintsType();
        MDRestrictionCodePropertyType mdRestrictionCodePropertyType = new MDRestrictionCodePropertyType();
        String legalConstraint = Optional.ofNullable(collectionReference.params.inspire).map(inspire -> inspire.inspireLimitationAccess)
                .map(inspireLimitationAccess -> inspireLimitationAccess.accessConstraints).get();

        CodeListValueType restrictionCodeListValueType = new CodeListValueType();
        restrictionCodeListValueType.setCodeList(RESTRICTION_CODE_LIST);
        restrictionCodeListValueType.setCodeListValue(legalConstraint);
        mdRestrictionCodePropertyType.setMDRestrictionCode(restrictionCodeListValueType);
        mdLegalConstraintsType.getAccessConstraints().add(mdRestrictionCodePropertyType);
        String otherConstraint = Optional.ofNullable(collectionReference.params.inspire).map(inspire -> inspire.inspireLimitationAccess)
                .map(inspireLimitationAccess -> inspireLimitationAccess.otherConstraints).orElse(InspireConstants.LIMITATION_ON_PUBLIC_ACCESS);
        mdLegalConstraintsType.getOtherConstraints().add(createCharacterStringPropertyType(otherConstraint));
        mdLegalConstraintsPropertyType.setMDConstraints(gmdObjectFactory.createMDConstraints(mdLegalConstraintsType));
        identificationType.getResourceConstraints().add(mdLegalConstraintsPropertyType);
    }


    public static void addDateStamp(MDMetadataType mdMetadataType, String date) {
        DatePropertyType datePropertyType = new DatePropertyType();
        datePropertyType.setDate(date);
        mdMetadataType.setDateStamp(datePropertyType);
    }
    public static void addFileIdentifier(MDMetadataType mdMetadataType, String identifier) {
        mdMetadataType.setFileIdentifier(createCharacterStringPropertyType(identifier));
    }

    public static void addLanguage(MDMetadataType mdMetadataType, String language) {
        mdMetadataType.setLanguage(createCharacterStringPropertyType(language));
    }



    public static void addMetadataStandardName(MDMetadataType mdMetadataType) {
        // todo standard name
        mdMetadataType.setMetadataStandardName(createCharacterStringPropertyType("ISO19115"));
    }


    public static void addHierarchyLevel(MDMetadataType mdMetadataType, String level) {
        MDScopeCodePropertyType mdScopeCodePropertyType = createScope(level);
        mdMetadataType.getHierarchyLevel().add(mdScopeCodePropertyType);
    }

    private static DQElementPropertyType createDomainConsistency(String specification, String date, String dateType, boolean isConformant) {
        DQDomainConsistencyType dqDomainConsistencyType = new DQDomainConsistencyType();
        DQConformanceResultType dqConformanceResultType = new DQConformanceResultType();
        CICitationPropertyType ciSpecification = createCICitation(specification, date, dateType);
        dqConformanceResultType.setSpecification(ciSpecification);
        String conformanceText = isConformant ? "This data set is conformant": "This data set is not conformant";
        dqConformanceResultType.setExplanation(createCharacterStringPropertyType(conformanceText + " with " + specification));
        BooleanPropertyType booleanPropertyType = new BooleanPropertyType();
        booleanPropertyType.setBoolean(isConformant);
        dqConformanceResultType.setPass(booleanPropertyType);
        DQResultPropertyType dqResultPropertyType = new DQResultPropertyType();
        dqResultPropertyType.setAbstractDQResult(gmdObjectFactory.createDQConformanceResult(dqConformanceResultType));
        dqDomainConsistencyType.getResult().add(dqResultPropertyType);
        DQElementPropertyType dqElementPropertyType = new DQElementPropertyType();
        dqElementPropertyType.setAbstractDQElement(gmdObjectFactory.createDQDomainConsistency(dqDomainConsistencyType));
        return dqElementPropertyType;
    }

    public static void addBriefIdentificationInfo(MDMetadataType mdMetadataType, CollectionReference collectionReference) {
        MDIdentificationPropertyType mdIdentificationPropertyType = new MDIdentificationPropertyType();
        MDDataIdentificationType mdDataIdentificationType = new MDDataIdentificationType();
        String uniqueResourceIdentifier = Optional.ofNullable(collectionReference.params.inspire)
                .map(inspire -> inspire.inspireURI)
                .map(inspireURI -> inspireURI.code)
                .orElse(collectionReference.params.dublinCoreElementName.identifier);
        addCICitation(mdDataIdentificationType, collectionReference.params.dublinCoreElementName.title, null, null, uniqueResourceIdentifier);
        addExtent(mdDataIdentificationType, collectionReference.params.dublinCoreElementName.bbox);
        mdIdentificationPropertyType.setAbstractMDIdentification(gmdObjectFactory.createMDDataIdentification(mdDataIdentificationType));
        mdMetadataType.getIdentificationInfo().add(mdIdentificationPropertyType);
    }

    public static void addSummaryIdentificationInfo(MDMetadataType mdMetadataType, CollectionReference collectionReference, OGCConfiguration ogcConfiguration, InspireConfiguration inspireConfiguration) throws OGCException {
        MDIdentificationPropertyType mdIdentificationPropertyType = new MDIdentificationPropertyType();
        MDDataIdentificationType mdDataIdentificationType = new MDDataIdentificationType();
        String formatedDate = formatDublinCoreDate(collectionReference.params.dublinCoreElementName.getDate());
        String uniqueResourceIdentifier = Optional.ofNullable(collectionReference.params.inspire)
                .map(inspire -> inspire.inspireURI)
                .map(inspireURI -> inspireURI.code)
                .orElse(collectionReference.params.dublinCoreElementName.identifier);
        addCICitation(mdDataIdentificationType, collectionReference.params.dublinCoreElementName.title, formatedDate, CREATION_DATE_TYPE, uniqueResourceIdentifier);
        addAbstract(mdDataIdentificationType, collectionReference.params.dublinCoreElementName.description);
        addPointOfContact(mdDataIdentificationType, ogcConfiguration);
        addKeywords(mdDataIdentificationType, collectionReference);
        if (inspireConfiguration.enabled) {
            if (collectionReference.params.inspire.spatialResolution != null) {
                addSpatialResolution(mdDataIdentificationType, collectionReference.params.inspire.spatialResolution);
            }
            if (collectionReference.params.inspire.topicCategories != null) {
                collectionReference.params.inspire.topicCategories.forEach(topicCategory -> {
                    addTopicCategory(mdDataIdentificationType,topicCategory);
                });
            }
            addResourceConstraints(mdDataIdentificationType, collectionReference);
        }
        addIdentificationLanguage(mdDataIdentificationType, collectionReference.params.dublinCoreElementName.language);
        addSpatialRepresentationType(mdDataIdentificationType);
        addExtent(mdDataIdentificationType, collectionReference.params.dublinCoreElementName.bbox);
        mdIdentificationPropertyType.setAbstractMDIdentification(gmdObjectFactory.createMDDataIdentification(mdDataIdentificationType));
        mdMetadataType.getIdentificationInfo().add(mdIdentificationPropertyType);
    }

    public static void addIdentificationLanguage(MDDataIdentificationType mdDataIdentificationType, String language) {
        mdDataIdentificationType.getLanguage().add(createCharacterStringPropertyType(language));
    }

    public static void addSpatialRepresentationType(MDDataIdentificationType mdDataIdentificationType) {
        MDSpatialRepresentationTypeCodePropertyType spatialRepresentationTypeCodePropertyType = new MDSpatialRepresentationTypeCodePropertyType();
        spatialRepresentationTypeCodePropertyType.setMDSpatialRepresentationTypeCode(getCodeListValueType(SPATIAL_REPRESENTATION_CODE_LIST, "vector"));
        mdDataIdentificationType.getSpatialRepresentationType().add(spatialRepresentationTypeCodePropertyType);
    }

    public static void addTopicCategory(MDDataIdentificationType mdDataIdentificationType, String topicCategory) {
        MDTopicCategoryCodePropertyType topicCategoryCodePropertyType = new MDTopicCategoryCodePropertyType();
        topicCategoryCodePropertyType.setMDTopicCategoryCode(MDTopicCategoryCodeType.fromValue(topicCategory));
        mdDataIdentificationType.getTopicCategory().add(topicCategoryCodePropertyType);
    }


    // TODO : spatial resolution Object
    public static void addSpatialResolution(MDDataIdentificationType mdDataIdentificationType, InspireSpatialResolution spatialResolution) {
        MDResolutionPropertyType mdResolutionPropertyType = new MDResolutionPropertyType();
        MDResolutionType mdResolutionType = new MDResolutionType();
        //Spatial resolution refers to the level of detail of the data set.
        // It shall be expressed as a set of zero to many resolution distances (typically for gridded data and imagery-derived products)
        // A resolution distance shall be expressed as a numerical value associated with a unit of length.
        //  ----  OR  ----
        // equivalent scales (typically for maps or map-derived product
        //An equivalent scale is generally expressed as an integer value expressing the scale denominator.
        if (spatialResolution.unitOfMeasure == null || spatialResolution.unitOfMeasure.equals("")) {
            // in this case, it's an equivalent scale
            MDRepresentativeFractionPropertyType representativeFractionPropertyType = new MDRepresentativeFractionPropertyType();
            MDRepresentativeFractionType representativeFractionType = new MDRepresentativeFractionType();
            IntegerPropertyType integerPropertyType = new IntegerPropertyType();
            integerPropertyType.setInteger(BigInteger.valueOf(spatialResolution.value.intValue()));
            representativeFractionType.setDenominator(integerPropertyType);
            representativeFractionPropertyType.setMDRepresentativeFraction(representativeFractionType);
            mdResolutionType.setEquivalentScale(representativeFractionPropertyType);
        } else {
            DistancePropertyType distancePropertyType = new DistancePropertyType();
            LengthType distance = new LengthType();
            distance.setValue(spatialResolution.value.doubleValue());
            distance.setUom(spatialResolution.unitOfMeasure);
            distancePropertyType.setDistance(distance);
            mdResolutionType.setDistance(distancePropertyType);
        }
        mdDataIdentificationType.getSpatialResolution().add(mdResolutionPropertyType);
    }


    public static void addCICitation(MDDataIdentificationType mdDataIdentificationType, String title, String date, String dateType, String uri) {
        CICitationPropertyType ciCitationPropertyType = createCICitation(title, date, dateType, uri);
        mdDataIdentificationType.setCitation(ciCitationPropertyType);
    }

    public static void addExtent(MDDataIdentificationType dataIdentificationType, DublinCoreElementName.Bbox bbox) {
        EXExtentPropertyType exExtentPropertyType = new EXExtentPropertyType();
        EXExtentType exExtentType = new EXExtentType();
        EXGeographicExtentPropertyType exGeographicExtentPropertyType = new EXGeographicExtentPropertyType();
        EXGeographicBoundingBoxType exGeographicBoundingBoxType = new EXGeographicBoundingBoxType();
        exGeographicBoundingBoxType.setEastBoundLongitude(createDecimalPropertyType(bbox.east));
        exGeographicBoundingBoxType.setWestBoundLongitude(createDecimalPropertyType(bbox.west));
        exGeographicBoundingBoxType.setNorthBoundLatitude(createDecimalPropertyType(bbox.north));
        exGeographicBoundingBoxType.setSouthBoundLatitude(createDecimalPropertyType(bbox.south));
        exGeographicExtentPropertyType.setAbstractEXGeographicExtent(gmdObjectFactory.createAbstractEXGeographicExtent(exGeographicBoundingBoxType));
        exExtentType.getGeographicElement().add(exGeographicExtentPropertyType);
        exExtentPropertyType.setEXExtent(exExtentType);
        dataIdentificationType.getExtent().add(exExtentPropertyType);
    }

    private static String formatDublinCoreDate(String date) throws OGCException {
        Date dublincoreDate = CSWParamsParser.getMetadataDate(date);
        DateFormat df = new SimpleDateFormat(InspireConstants.CSW_METADATA_DATE_FORMAT);
        return df.format(dublincoreDate);
    }

    private static CharacterStringPropertyType createCharacterStringPropertyType(String value) {
        CharacterStringPropertyType characterStringPropertyType = new CharacterStringPropertyType();
        characterStringPropertyType.setCharacterString(gcoObjectFactory.createCharacterString(value));
        return characterStringPropertyType;
    }

    private static CodeListValueType getCodeListValueType(String codeList, String codeListValue) {
        CodeListValueType codeListValueType = new CodeListValueType();
        codeListValueType.setCodeList(codeList);
        codeListValueType.setCodeListValue(codeListValue);
        return codeListValueType;
    }

    private static MDScopeCodePropertyType createScope(String level) {
        MDScopeCodePropertyType mdScopeCodePropertyType = new MDScopeCodePropertyType();
        CodeListValueType codeListValueType =  getCodeListValueType(SCOPE_CODE_URL, level);
        mdScopeCodePropertyType.setMDScopeCode(codeListValueType);
        return mdScopeCodePropertyType;
    }

    private static CIResponsiblePartyPropertyType createResponsableParty(OGCConfiguration ogcConfiguration) {
        CIResponsiblePartyPropertyType ciResponsiblePartyPropertyType = new CIResponsiblePartyPropertyType();
        CIResponsiblePartyType ciResponsiblePartyType = new CIResponsiblePartyType();
        ciResponsiblePartyType.setOrganisationName(createCharacterStringPropertyType(ogcConfiguration.serviceContactIndividualName));
        CIContactPropertyType ciContactPropertyType = new CIContactPropertyType();
        CIContactType ciContactType = new CIContactType();
        CIAddressPropertyType ciAddressPropertyType = new CIAddressPropertyType();
        CIAddressType ciAddressType = new CIAddressType();
        ciAddressType.setCity(createCharacterStringPropertyType(ogcConfiguration.serviceContactAdressCity));
        ciAddressType.setCountry(createCharacterStringPropertyType(ogcConfiguration.serviceContactAdressCountry));
        ciAddressType.setPostalCode(createCharacterStringPropertyType(ogcConfiguration.serviceContactAdressPostalCode));
        ciAddressPropertyType.setCIAddress(ciAddressType);
        ciContactType.setAddress(ciAddressPropertyType);
        ciContactPropertyType.setCIContact(ciContactType);
        ciResponsiblePartyType.setContactInfo(ciContactPropertyType);
        CIRoleCodePropertyType ciRoleCodePropertyType = new CIRoleCodePropertyType();
        CodeListValueType codeListValueType = getCodeListValueType(ROLE_CODE_LIST, ogcConfiguration.serviceProviderRole);
        ciRoleCodePropertyType.setCIRoleCode(codeListValueType);
        ciResponsiblePartyType.setRole(ciRoleCodePropertyType);
        ciResponsiblePartyPropertyType.setCIResponsibleParty(ciResponsiblePartyType);
        return ciResponsiblePartyPropertyType;
    }

    private static CICitationPropertyType createCICitation(String title, String date, String dateType) {
        CICitationPropertyType ciCitationPropertyType = new CICitationPropertyType();
        CICitationType ciCitationType = new CICitationType();
        CharacterStringPropertyType cs = createCharacterStringPropertyType(title);
        ciCitationType.setTitle(cs);
        if(date != null && !date.equals("")) {
            CIDatePropertyType ciDatePropertyType = new CIDatePropertyType();
            CIDateType ciDateType = new CIDateType();
            DatePropertyType datePropertyType = new DatePropertyType();
            datePropertyType.setDate(date);
            CIDateTypeCodePropertyType ciDateTypeCodePropertyType = new CIDateTypeCodePropertyType();
            CodeListValueType codeListValueType = getCodeListValueType(DATE_TYPE_CODE_LIST, dateType);
            ciDateTypeCodePropertyType.setCIDateTypeCode(codeListValueType);
            ciDateType.setDate(datePropertyType);
            ciDateType.setDateType(ciDateTypeCodePropertyType);
            ciDatePropertyType.setCIDate(ciDateType);
            ciCitationType.getDate().add(ciDatePropertyType);
        }
        ciCitationPropertyType.setCICitation(ciCitationType);
        return ciCitationPropertyType;
    }

    private static CICitationPropertyType createCICitation(String title, String date, String dateType, String uri) {
        CICitationPropertyType ciCitationPropertyType = createCICitation(title, date, dateType);
        MDIdentifierPropertyType mdIdentifierPropertyType = new MDIdentifierPropertyType();
        MDIdentifierType mdIdentifierType = new MDIdentifierType();
        mdIdentifierType.setCode(createCharacterStringPropertyType(uri));
        mdIdentifierPropertyType.setMDIdentifier(gmdObjectFactory.createMDIdentifier(mdIdentifierType));
        ciCitationPropertyType.getCICitation().getIdentifier().add(mdIdentifierPropertyType);
        return ciCitationPropertyType;
    }

    private static DecimalPropertyType createDecimalPropertyType(double value) {
        DecimalPropertyType decimalPropertyType = new DecimalPropertyType();
        decimalPropertyType.setDecimal(BigDecimal.valueOf(value));
        return decimalPropertyType;
    }

}
