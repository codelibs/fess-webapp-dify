/*
 * Copyright 2012-2024 CodeLibs Project and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.codelibs.fess.plugin.webapp.dify.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codelibs.core.io.ResourceUtil;
import org.codelibs.core.lang.StringUtil;
import org.codelibs.fess.Constants;
import org.codelibs.fess.api.BaseApiManager;
import org.codelibs.fess.entity.FacetInfo;
import org.codelibs.fess.entity.GeoInfo;
import org.codelibs.fess.entity.HighlightInfo;
import org.codelibs.fess.entity.SearchRenderData;
import org.codelibs.fess.entity.SearchRequestParams;
import org.codelibs.fess.exception.InvalidAccessTokenException;
import org.codelibs.fess.helper.SearchHelper;
import org.codelibs.fess.util.ComponentUtil;
import org.dbflute.optional.OptionalThing;
import org.lastaflute.web.util.LaResponseUtil;

public class DifyApiManager extends BaseApiManager {

    private static final Logger logger = LogManager.getLogger(DifyApiManager.class);

    protected static final String MIMETYPE_JSON = "application/json";

    protected static final String FESS_DIFY_OPENAPI_URL = "fess.dify.openapi.url";

    protected static final String FESS_DIFY_RESPONSE_FIELDS = "fess.dify.response_fields";

    protected static final String LOCALHOST_URL = "http://localhost:8080";

    public DifyApiManager() {
        setPathPrefix("/dify");
    }

    @PostConstruct
    public void register() {
        if (logger.isInfoEnabled()) {
            logger.info("Load {}", this.getClass().getSimpleName());
        }

        ComponentUtil.getWebApiManagerFactory().add(this);
    }

    @Override
    public boolean matches(final HttpServletRequest request) {
        final String servletPath = request.getServletPath();
        return servletPath.startsWith(pathPrefix);
    }

    @Override
    public void process(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        final String servletPath = request.getServletPath();
        final String[] values = servletPath.replaceAll("/+", "/").split("/");
        try {
            if (values.length > 2) {
                switch (values[2]) {
                case "openapi.yaml": {
                    processOpenApiYaml(request, response);
                    return;
                }
                case "query": {
                    if ("get".equalsIgnoreCase(request.getMethod())) { // TODO POST
                        processQuery(request, response);
                        return;
                    }
                    break;
                }
                default:
                    break;
                }
            }
        } catch (final Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to process {}", servletPath, e);
            }
            writeErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e);
            return;
        }
        writeErrorResponse(HttpServletResponse.SC_NOT_FOUND, "Cannot understand your request.", StringUtil.EMPTY_STRINGS);
    }

    @Override
    protected void writeHeaders(final HttpServletResponse response) {
        ComponentUtil.getFessConfig().getApiJsonResponseHeaderList().forEach(e -> response.setHeader(e.getFirst(), e.getSecond()));
    }

    protected String[] getResponseFields() {
        return System.getProperty(FESS_DIFY_RESPONSE_FIELDS, "url,timestamp,doc_id,content").split(",");
    }

    protected void processOpenApiYaml(final HttpServletRequest request, final HttpServletResponse response) {
        final StringBuilder buf = new StringBuilder(8000);
        try (final BufferedReader br = new BufferedReader(
                new InputStreamReader(ResourceUtil.getResourceAsStream("/dify/openapi.yaml"), Constants.CHARSET_UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                buf.append(line).append('\n');
            }

            final String url = System.getProperty(FESS_DIFY_OPENAPI_URL, LOCALHOST_URL + "/dify");
            response.setStatus(HttpServletResponse.SC_OK);
            write(buf.toString().replace(LOCALHOST_URL + "/dify", url), "application/x-yaml", Constants.UTF_8);
        } catch (final Exception e) {
            writeErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot process your request.", e);
        }
    }

    protected void processQuery(final HttpServletRequest request, final HttpServletResponse response) {
        final QueryRequest query = QueryRequest.create(request);
        final SearchHelper searchHelper = ComponentUtil.getSearchHelper();
        final SearchRenderData data = new SearchRenderData();
        final QueryRequestParams params = new QueryRequestParams(request, query, getResponseFields());
        request.setAttribute(QueryRequest.QUERY, query);
        searchHelper.search(params, data, OptionalThing.empty());
        final QueryResult queryResult = QueryResult.create(query, data);
        response.setStatus(HttpServletResponse.SC_OK);
        write(queryResult.toJsonString(), MIMETYPE_JSON, Constants.UTF_8);
    }

    protected void writeErrorResponse(final int status, final String message, final Throwable t) {
        final Supplier<String> stacktraceString = () -> {
            final StringBuilder buf = new StringBuilder(100);
            if (StringUtil.isBlank(t.getMessage())) {
                buf.append(t.getClass().getName());
            } else {
                buf.append(t.getMessage());
            }
            try (final StringWriter sw = new StringWriter(); final PrintWriter pw = new PrintWriter(sw)) {
                t.printStackTrace(pw);
                pw.flush();
                buf.append(" [ ").append(sw.toString()).append(" ]");
            } catch (final IOException ignore) {}
            return buf.toString();
        };
        final String[] locations;
        if (Constants.TRUE.equalsIgnoreCase(ComponentUtil.getFessConfig().getApiJsonResponseExceptionIncluded())) {
            locations = stacktraceString.get().split("\n");
        } else {
            final String errorCode = UUID.randomUUID().toString();
            locations = new String[] { "error_code:" + errorCode };
            if (logger.isDebugEnabled()) {
                logger.debug("[{}] {}", errorCode, stacktraceString.get().replace("\n", "\\n"));
            } else {
                logger.warn("[{}] {}", errorCode, t.getMessage());
            }
        }
        final HttpServletResponse response = LaResponseUtil.getResponse();
        if (t instanceof final InvalidAccessTokenException e) {
            response.setHeader("WWW-Authenticate", "Bearer error=\"" + e.getType() + "\"");
            writeErrorResponse(HttpServletResponse.SC_UNAUTHORIZED, message, locations);
        } else {
            writeErrorResponse(status, message, locations);
        }
    }

    protected void writeErrorResponse(final int status, final String message, final String[] locations) {
        final HttpServletResponse response = LaResponseUtil.getResponse();
        response.setStatus(status);
        final StringBuilder buf = new StringBuilder();
        buf.append("{\"detail\":[");
        buf.append("{\"msg\":\"").append(StringEscapeUtils.escapeJson(message)).append('"');
        if (locations.length > 0) {
            buf.append(",\"loc\":[");
            buf.append(Arrays.stream(locations).map(s -> "\"" + StringEscapeUtils.escapeJson(s) + "\"").collect(Collectors.joining(",")));
            buf.append(']');
        }
        buf.append('}');
        buf.append("]}");
        write(buf.toString(), MIMETYPE_JSON, Constants.UTF_8);
    }

    protected static class QueryRequestParams extends SearchRequestParams {

        private final HttpServletRequest request;
        private final QueryRequest query;
        private final String[] responseFields;

        protected QueryRequestParams(final HttpServletRequest request, final QueryRequest query, final String[] responseFields) {
            this.request = request;
            this.query = query;
            this.responseFields = responseFields;
        }

        @Override
        public String getQuery() {
            return query.getQuery();
        }

        @Override
        public Map<String, String[]> getFields() {
            return Collections.emptyMap();
        }

        @Override
        public Map<String, String[]> getConditions() {
            return Collections.emptyMap();
        }

        @Override
        public String[] getLanguages() {
            return StringUtil.EMPTY_STRINGS;
        }

        @Override
        public GeoInfo getGeoInfo() {
            return null;
        }

        @Override
        public FacetInfo getFacetInfo() {
            return null;
        }

        @Override
        public HighlightInfo getHighlightInfo() {
            return null;
        }

        @Override
        public String getSort() {
            return null;
        }

        @Override
        public int getStartPosition() {
            return 0;
        }

        @Override
        public int getOffset() {
            return 0;
        }

        @Override
        public int getPageSize() {
            return query.getTopK();
        }

        @Override
        public String[] getExtraQueries() {
            return StringUtil.EMPTY_STRINGS;
        }

        @Override
        public Object getAttribute(final String name) {
            return request.getAttribute(name);
        }

        @Override
        public Locale getLocale() {
            return request.getLocale();
        }

        @Override
        public SearchRequestType getType() {
            return SearchRequestType.JSON;
        }

        @Override
        public String getSimilarDocHash() {
            return null;
        }

        @Override
        public String[] getResponseFields() {
            return responseFields;
        }
    }
}
