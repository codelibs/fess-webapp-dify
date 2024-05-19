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

import javax.servlet.http.HttpServletRequest;

import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.codelibs.core.lang.StringUtil;
import org.codelibs.fess.plugin.webapp.dify.Constants;
import org.codelibs.fess.plugin.webapp.dify.exception.InvalidRequestParameterException;

public class QueryRequest {

    private static final String PUNCT = "[\\p{P}\\p{IsPunctuation}]";

    public static final String QUERY = "fess.dify.Query";

    private String query;

    private int topK = 3;

    private Operator operator = null;

    public static QueryRequest create(final HttpServletRequest request) {
        final QueryRequest queryRequest = new QueryRequest();
        queryRequest.query = request.getParameter("query");
        if (Constants.TRUE.equalsIgnoreCase(System.getProperty(Constants.FESS_DIFY_QUERY_NORMALIZATION))) {
            queryRequest.query = queryRequest.query.replaceAll(PUNCT, " ");
        }
        final String topKValue = request.getParameter("top_k");
        if (StringUtil.isNotBlank(topKValue)) {
            try {
                queryRequest.topK = Integer.parseInt(topKValue);
            } catch (final NumberFormatException e) {
                throw new InvalidRequestParameterException("top_k must be an integer. Your input is " + topKValue + ".", e);
            }
        }

        final String operatorValue = request.getParameter("operator");
        if (Operator.AND.name().equalsIgnoreCase(operatorValue)) {
            queryRequest.operator = Operator.AND;
        } else if (Operator.OR.name().equalsIgnoreCase(operatorValue)) {
            queryRequest.operator = Operator.OR;
        } else if (StringUtil.isNotEmpty(operatorValue)) {
            throw new InvalidRequestParameterException("operator must be AND or OR. Your input is " + operatorValue + ".");
        }
        return queryRequest;
    }

    public String getQuery() {
        return query;
    }

    public int getTopK() {
        return topK;
    }

    public Operator getOperator() {
        return operator;
    }

}
