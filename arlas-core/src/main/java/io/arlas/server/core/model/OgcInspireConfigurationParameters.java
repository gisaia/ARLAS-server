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

package io.arlas.server.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class OgcInspireConfigurationParameters implements Serializable {
    private static final long serialVersionUID = 6082813889579249173L;
    @JsonProperty(value = "responsible_party", required = false)
    public String reponsibleParty;
    @JsonProperty(value = "responsible_party_role", required = false)
    public String reponsiblePartyRole;
    @JsonProperty(value = "resource_type", required = false)
    public String resourceType = "dataset";
    @JsonProperty(value = "inspire_conformity_list", required = false)
    public List<InspireConformity> inspireConformityList;
    @JsonProperty(value = "access_and_use_conditions", required = false)
    public String accessAndUseConditions;
    @JsonProperty(value = "public_access_limitations", required = false)
    public String publicAccessLimitations;


    public void setConformityParameter() {
        inspireConformityList = new ArrayList<>();

        InspireConformity metadataConformity = new InspireConformity();
        metadataConformity.specificationTitle = InspireConformity.INSPIRE_METADATA_CONFORMITY_TITLE;
        metadataConformity.specificationDate = InspireConformity.INSPIRE_METADATA_CONFORMITY_DATE;
        metadataConformity.specificationDateType = "publication";
        metadataConformity.degree = "conformant";
        inspireConformityList.add(metadataConformity);

        InspireConformity interOperabilityConformity = new InspireConformity();
        interOperabilityConformity.specificationTitle = InspireConformity.INSPIRE_INTEROPERABILITY_CONFORMITY_TITLE;
        interOperabilityConformity.specificationDate = InspireConformity.INSPIRE_INTEROPERABILITY_CONFORMITY_DATE;
        interOperabilityConformity.specificationDateType = "publication";
        interOperabilityConformity.degree = "notEvaluated";
        inspireConformityList.add(interOperabilityConformity);


    }

}
