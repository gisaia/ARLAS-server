module io.arlas.server.core {
    requires javax.ws.rs.api;
    requires slf4j.api;
    requires elasticsearch;

    exports io.arlas.server.core.app;
    exports io.arlas.server.core.core;
    exports io.arlas.server.core.exceptions;
    exports io.arlas.server.core.dao;
    exports io.arlas.server.core.utils;
    exports io.arlas.server.core.model;
    exports io.arlas.server.core.model.response;
}