/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.workbench.services.verifier.core.cache.inspectors;

import org.drools.workbench.services.verifier.api.client.configuration.AnalyzerConfiguration;
import org.drools.workbench.services.verifier.api.client.index.ObjectType;
import org.drools.workbench.services.verifier.api.client.index.Pattern;
import org.drools.workbench.services.verifier.core.checks.AnalyzerConfigurationMock;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PatternInspectorTest {

    private AnalyzerConfigurationMock configurationMock;

    private PatternInspector a;
    private PatternInspector b;

    @Before
    public void setUp() throws
                        Exception {
        configurationMock = new AnalyzerConfigurationMock();

        a = new PatternInspector( new Pattern( "a",
                                               new ObjectType( "org.Person",
                                                               configurationMock ),
                                               configurationMock ),
                                  mock( RuleInspectorUpdater.class ),
                                  mock( AnalyzerConfiguration.class ) );
        b = new PatternInspector( new Pattern( "b",
                                               new ObjectType( "org.Person",
                                                               configurationMock ),
                                               configurationMock ),
                                  mock( RuleInspectorUpdater.class ),
                                  mock( AnalyzerConfiguration.class ) );
    }

    @Test
    public void testRedundancy01() throws
                                   Exception {
        assertTrue( a.isRedundant( b ) );
        assertTrue( b.isRedundant( a ) );
    }

    @Test
    public void testRedundancy02() throws
                                   Exception {
        final PatternInspector x = new PatternInspector( new Pattern( "x",
                                                                      new ObjectType( "org.Address",
                                                                                      configurationMock ),
                                                                      configurationMock ),
                                                         mock( RuleInspectorUpdater.class ),
                                                         mock( AnalyzerConfiguration.class ) );

        assertFalse( x.isRedundant( b ) );
        assertFalse( b.isRedundant( x ) );
    }

    @Test
    public void testSubsumpt01() throws
                                 Exception {
        assertTrue( a.subsumes( b ) );
        assertTrue( b.subsumes( a ) );
    }

    @Test
    public void testSubsumpt02() throws
                                 Exception {
        final PatternInspector x = new PatternInspector( new Pattern( "x",
                                                                      new ObjectType( "org.Address",
                                                                                      configurationMock ),
                                                                      configurationMock ),
                                                         mock( RuleInspectorUpdater.class ),
                                                         mock( AnalyzerConfiguration.class ) );

        assertFalse( x.subsumes( b ) );
        assertFalse( b.subsumes( x ) );
    }

}