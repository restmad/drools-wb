/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
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

package org.drools.workbench.screens.dsltext.backend.server.indexing;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.lucene.search.Query;
import org.drools.workbench.screens.dsltext.type.DSLResourceTypeDefinition;
import org.junit.Test;
import org.kie.workbench.common.services.refactoring.backend.server.BaseIndexingTest;
import org.kie.workbench.common.services.refactoring.backend.server.TestIndexer;
import org.kie.workbench.common.services.refactoring.backend.server.query.builder.SingleTermQueryBuilder;
import org.kie.workbench.common.services.refactoring.model.index.terms.valueterms.ValueReferenceIndexTerm;
import org.kie.workbench.common.services.refactoring.service.ResourceType;
import org.mockito.ArgumentMatcher;
import org.slf4j.LoggerFactory;
import org.uberfire.ext.metadata.engine.Index;
import org.uberfire.java.nio.file.Path;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;

public class IndexDslInvalidDrlTest extends BaseIndexingTest<DSLResourceTypeDefinition> {

    @Test
    public void testIndexDslInvalidDrl() throws IOException, InterruptedException {
        //Setup logging
        final Logger root = (Logger) LoggerFactory.getLogger( Logger.ROOT_LOGGER_NAME );
        final Appender<ILoggingEvent> mockAppender = mock( Appender.class );
        when( mockAppender.getName() ).thenReturn( "MOCK" );
        root.addAppender( mockAppender );

        //Add test files
        final Path path = basePath.resolve( "bz1269366.dsl" );
        final String dsl = loadText( "bz1269366.dsl" );
        ioService().write( path,
                           dsl );

        Thread.sleep( 5000 ); //wait for events to be consumed from jgit -> (notify changes -> watcher -> index) -> lucene index

        final Index index = getConfig().getIndexManager().get( org.uberfire.ext.metadata.io.KObjectUtil.toKCluster( basePath.getFileSystem() ) );

        {
            final Query query = new SingleTermQueryBuilder( new ValueReferenceIndexTerm( "org.drools.workbench.screens.dsltext.backend.server.indexing.classes.Applicant", ResourceType.JAVA ) )
                    .build();
            searchFor(index, query, 0);

            verify( mockAppender ).doAppend( argThat( new ArgumentMatcher<ILoggingEvent>() {

                @Override
                public boolean matches( final Object argument ) {
                    return ( (ILoggingEvent) argument ).getMessage().startsWith( "Unable to parse DRL" );
                }

            } ) );
        }

    }

    @Override
    protected TestIndexer getIndexer() {
        return new TestDslFileIndexer("org.drools.workbench.screens.dsltext.backend.server.indexing.classes");
    }

    @Override
    protected DSLResourceTypeDefinition getResourceTypeDefinition() {
        return new DSLResourceTypeDefinition();
    }

    @Override
    protected String getRepositoryName() {
        return this.getClass().getSimpleName();
    }

}
