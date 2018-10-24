package io.arlas.server.inspire.common.utils;

import io.arlas.server.exceptions.INSPIRE.INSPIREException;
import io.arlas.server.exceptions.INSPIRE.INSPIREExceptionCode;
import io.arlas.server.exceptions.OGC.OGCException;
import io.arlas.server.inspire.common.enums.InspireSupportedLanguages;
import io.arlas.server.ogc.common.model.Service;

public class INSPIRECheckParam {
    public static void checkLanguageInspireCompliance(String language, Service service) throws INSPIREException {
        try {
            InspireSupportedLanguages.valueOf(language);
        } catch (IllegalArgumentException e) {
            throw new INSPIREException(INSPIREExceptionCode.INVALID_INSPIRE_PARAMETER_VALUE, language + " is not a valid language. Languages must be one of the 24 Official languages of the EU in ISO 639-2 (B)", service);
        }
    }
}
