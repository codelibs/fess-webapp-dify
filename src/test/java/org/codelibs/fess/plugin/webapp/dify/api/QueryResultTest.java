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

import org.codelibs.fess.mylasta.direction.FessConfig;
import org.codelibs.fess.plugin.webapp.dify.api.QueryResult.DocumentResult;
import org.codelibs.fess.util.ComponentUtil;
import org.dbflute.utflute.lastaflute.LastaFluteTestCase;

public class QueryResultTest extends LastaFluteTestCase {

    @Override
    protected String prepareConfigFile() {
        return "test_app.xml";
    }

    @Override
    protected boolean isSuppressTestCaseTransaction() {
        return true;
    }

    @Override
    public void setUp() throws Exception {
        ComponentUtil.setFessConfig(new FessConfig.SimpleImpl() {
            private static final long serialVersionUID = 1L;

            @Override
            public String getAppValue() {
                return "";
            }

            @Override
            public String getPathEncoding() {
                return "UTF-8";
            }

            @Override
            public String[] getSupportedLanguagesAsArray() {
                return new String[] { "ja" };
            }

        });
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        ComponentUtil.setFessConfig(null);
        super.tearDown();
    }

    public void test_toJsonString() {
        QueryResult queryResult = new QueryResult();
        assertEquals("{\"data\":[]}", queryResult.toJsonString());
        queryResult.documents = new DocumentResult[] { new DocumentResult("doc1", "url1", "content1") };
        assertEquals("{\"data\":[{\"document_id\":\"doc1\",\"url\":\"url1\",\"content\":\"content1\"}]}", queryResult.toJsonString());
    }
}
