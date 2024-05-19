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

import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.codelibs.core.lang.StringUtil;
import org.codelibs.fess.plugin.webapp.dify.Constants;
import org.dbflute.utflute.lastaflute.LastaFluteTestCase;
import org.dbflute.utflute.mocklet.MockletHttpServletRequest;
import org.dbflute.utflute.mocklet.MockletHttpServletRequestImpl;
import org.dbflute.utflute.mocklet.MockletServletContextImpl;

public class QueryRequestTest extends LastaFluteTestCase {
    @Override
    protected String prepareConfigFile() {
        return "test_app.xml";
    }

    @Override
    protected boolean isSuppressTestCaseTransaction() {
        return true;
    }

    @Override
    public void tearDown() throws Exception {
        System.setProperty(Constants.FESS_DIFY_QUERY_NORMALIZATION, StringUtil.EMPTY);
        super.tearDown();
    }

    public void test_create() {
        MockletHttpServletRequest request = new MockletHttpServletRequestImpl(new MockletServletContextImpl("/dify"), "/query");

        request.setParameter("query", "aaa,bbb、ccc@あいう");
        request.setParameter("top_k", "10");
        QueryRequest queryRequest1 = QueryRequest.create(request);
        assertEquals("aaa,bbb、ccc@あいう", queryRequest1.getQuery());
        assertEquals(10, queryRequest1.getTopK());
        assertEquals(null, queryRequest1.getOperator());

        System.setProperty(Constants.FESS_DIFY_QUERY_NORMALIZATION, "true");
        request.setParameter("query", "aaa,bbb、ccc@あいう");
        request.setParameter("top_k", "5");
        request.setParameter("operator", "OR");
        QueryRequest queryRequest2 = QueryRequest.create(request);
        assertEquals("aaa bbb ccc あいう", queryRequest2.getQuery());
        assertEquals(5, queryRequest2.getTopK());
        assertEquals(Operator.OR, queryRequest2.getOperator());

        System.setProperty(Constants.FESS_DIFY_QUERY_NORMALIZATION, "false");
        request.setParameter("query", "aaa,bbb、ccc@あいう");
        request.setParameter("top_k", "2");
        request.setParameter("operator", "AND");
        QueryRequest queryRequest3 = QueryRequest.create(request);
        assertEquals("aaa,bbb、ccc@あいう", queryRequest3.getQuery());
        assertEquals(2, queryRequest3.getTopK());
        assertEquals(Operator.AND, queryRequest3.getOperator());
    }
}
