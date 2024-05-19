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

import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.codelibs.core.lang.StringUtil;
import org.codelibs.fess.entity.SearchRenderData;
import org.codelibs.fess.mylasta.direction.FessConfig;
import org.codelibs.fess.plugin.webapp.dify.Constants;
import org.codelibs.fess.util.ComponentUtil;

public class QueryResult {

    DocumentResult[] documents;

    public String toJsonString() {
        final StringBuilder buf = new StringBuilder(100);
        buf.append("{\"data\":[");
        if (documents != null) {
            buf.append(Arrays.stream(documents).map(DocumentResult::toJsonString).collect(Collectors.joining(",")));
        }
        buf.append("]}");
        return buf.toString();
    }

    public static QueryResult create(final QueryRequest query, final SearchRenderData data) {
        final FessConfig fessConfig = ComponentUtil.getFessConfig();
        final int maxTextLength = Integer.getInteger(Constants.FESS_DIFY_DOC_MAX_LENGTH, 5000);

        final QueryResult queryResult = new QueryResult();

        final String docIdField = fessConfig.getIndexFieldDocId();
        final String urlField = fessConfig.getIndexFieldUrl();
        final String contentField = fessConfig.getIndexFieldContent();
        queryResult.documents = data.getDocumentItems().stream().map(doc -> {
            final DocumentResult docResult = new DocumentResult();
            docResult.documentId = (String) doc.get(docIdField);
            docResult.url = (String) doc.get(urlField);
            if (doc.get(contentField) instanceof final String content) {
                if (content.length() > maxTextLength) {
                    docResult.content = StringUtils.abbreviate(content, maxTextLength);
                } else {
                    docResult.content = content;
                }
            } else {
                docResult.content = StringUtil.EMPTY;
            }
            return docResult;
        }).toArray(n -> new DocumentResult[n]);

        return queryResult;
    }

    static class DocumentResult {
        protected String documentId;
        protected String url;
        protected String content;

        DocumentResult() {
            // nothing
        }

        DocumentResult(String documentId, String url, String content) {
            this.documentId = documentId;
            this.url = url;
            this.content = content;
        }

        public String toJsonString() {
            final StringBuilder buf = new StringBuilder(100);
            buf.append("{\"document_id\":\"").append(StringEscapeUtils.escapeJson(documentId)).append('"');
            buf.append(",\"url\":\"").append(StringEscapeUtils.escapeJson(url)).append('"');
            buf.append(",\"content\":\"").append(StringEscapeUtils.escapeJson(content)).append('"');
            buf.append('}');
            return buf.toString();
        }
    }

}
