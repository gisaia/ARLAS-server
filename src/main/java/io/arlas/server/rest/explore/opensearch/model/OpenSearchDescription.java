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

package io.arlas.server.rest.explore.opensearch.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "OpenSearchDescription", namespace ="http://a9.com/-/spec/opensearch/1.1/")
public class OpenSearchDescription {

    @XmlElement(name = "shortName")
    public String shortName = "";

    @XmlElement(name = "Description")
    public String description = "";

    @XmlElement(name = "Url")
    public List<Url> url = new ArrayList<>();

    @XmlElement(name = "Contact")
    public String contact = "";

    @XmlElement(name = "Tags")
    public String tags = "";

    @XmlElement(name = "LongName")
    public String longName = "";

    @XmlElement(name = "Image")
    public Image image;

    @XmlElement(name = "Developer")
    public String developer = "";

    @XmlElement(name = "Attribution")
    public String attribution = "";

    @XmlElement(name = "SyndicationRight")
    public String syndicationRight;

    @XmlElement(name = "AdultContent")
    public String adultContent = "";

    @XmlElement(name = "Language")
    public String language = "";

    @XmlElement(name = "InputEncoding")
    public String inputEncoding = "";

    @XmlElement(name = "OutputEncoding")
    public String outputEncoding = "";
}
