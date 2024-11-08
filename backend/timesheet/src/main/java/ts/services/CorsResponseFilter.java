/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ts.services;

/*
 * #%L
 * jaxrs-cors
 * %%
 * Copyright (C) 2014 Adam Bien
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
import java.io.IOException;
import java.util.List;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

/**
 * Filtro per gestire le richieste CORS.
 */
@Provider
public class CorsResponseFilter implements ContainerResponseFilter {

    public static final String ALLOWED_METHODS = "GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD";
    public static final int MAX_AGE = 42 * 60 * 60; // 42 ore
    public static final String DEFAULT_ALLOWED_HEADERS = "Origin, Accept, Content-Type, Authorization, X-Requested-With";
    public static final String DEFAULT_EXPOSED_HEADERS = "Location, Info";

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        final MultivaluedMap<String, Object> headers = responseContext.getHeaders();
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Headers", getRequestedAllowedHeaders(requestContext));
        headers.add("Access-Control-Expose-Headers", getRequestedExposedHeaders(requestContext));
        headers.add("Access-Control-Allow-Credentials", "true");
        headers.add("Access-Control-Allow-Methods", ALLOWED_METHODS);
        headers.add("Access-Control-Max-Age", MAX_AGE);
        headers.add("X-Responded-By", "cors-response-filter");
    }

    /**
     * Ottiene gli header consentiti per la richiesta.
     *
     * @param requestContext contesto della richiesta
     * @return una stringa con gli header consentiti
     */
    private String getRequestedAllowedHeaders(ContainerRequestContext requestContext) {
        List<String> headers = requestContext.getHeaders().get("Access-Control-Request-Headers");
        return createHeaderList(headers, DEFAULT_ALLOWED_HEADERS);
    }

    /**
     * Ottiene gli header esposti nella risposta.
     *
     * @param requestContext contesto della richiesta
     * @return una stringa con gli header esposti
     */
    private String getRequestedExposedHeaders(ContainerRequestContext requestContext) {
        List<String> headers = requestContext.getHeaders().get("Access-Control-Expose-Headers");
        return createHeaderList(headers, DEFAULT_EXPOSED_HEADERS);
    }

    /**
     * Crea una lista di header sotto forma di stringa.
     *
     * @param headers lista di header
     * @param defaultHeaders header di default da includere
     * @return stringa con gli header concatenati
     */
    private String createHeaderList(List<String> headers, String defaultHeaders) {
        if (headers == null || headers.isEmpty()) {
            return defaultHeaders;
        }
        StringBuilder retVal = new StringBuilder();
        for (int i = 0; i < headers.size(); i++) {
            retVal.append(headers.get(i));
            retVal.append(',');
        }
        retVal.append(defaultHeaders);
        return retVal.toString();
    }
}