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

package org.drools.workbench.screens.guided.dtable.client.widget.table.lockmanager;

import java.lang.reflect.Field;
import javax.enterprise.event.Event;

import com.google.gwtmockito.GwtMockito;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.drools.workbench.screens.guided.dtable.client.widget.table.GuidedDecisionTableModellerView;
import org.drools.workbench.screens.guided.dtable.client.widget.table.GuidedDecisionTableView;
import org.jboss.errai.security.shared.api.identity.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.uberfire.backend.vfs.ObservablePath;
import org.uberfire.backend.vfs.Path;
import org.uberfire.backend.vfs.impl.LockInfo;
import org.uberfire.backend.vfs.impl.ObservablePathImpl;
import org.uberfire.client.mvp.LockManagerImpl;
import org.uberfire.client.mvp.LockTarget;
import org.uberfire.client.resources.WorkbenchResources;
import org.uberfire.client.workbench.VFSLockServiceProxy;
import org.uberfire.client.workbench.events.ChangeTitleWidgetEvent;
import org.uberfire.mocks.EventSourceMock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(GwtMockitoTestRunner.class)
public class GuidedDecisionTableLockManagerImplTest {

    @Mock
    private LockInfo lockInfo;

    @Mock
    private VFSLockServiceProxy lockService;

    @Mock
    private User user;

    private Event<ChangeTitleWidgetEvent> changeTitleEvent = spy( new EventSourceMock<ChangeTitleWidgetEvent>() {
        @Override
        public void fire( final ChangeTitleWidgetEvent event ) {
            //Do nothing. Default implementation throws an exception.
        }
    } );

    @Mock
    private GuidedDecisionTableModellerView.Presenter modellerPresenter;

    @Mock
    private Path dtPath;

    private GuidedDecisionTableLockManagerImpl lockManager;

    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        GwtMockito.useProviderForType( WorkbenchResources.class,
                                       ( type ) -> null );

        lockManager = new GuidedDecisionTableLockManagerImpl();

        setLockManagerField( "lockService",
                             lockService );
        setLockManagerField( "lockInfo",
                             lockInfo );
        setLockManagerField( "changeTitleEvent",
                             changeTitleEvent );
        setLockManagerField( "user",
                             user );

        when( lockInfo.getFile() ).thenReturn( dtPath );
        when( user.getIdentifier() ).thenReturn( "user" );
    }

    private void setLockManagerField( final String fieldName,
                                      final Object value ) {
        try {
            final Field field = LockManagerImpl.class.getDeclaredField( fieldName );
            field.setAccessible( true );
            field.set( lockManager,
                       value );
        } catch ( NoSuchFieldException | IllegalAccessException e ) {
            fail( e.getMessage() );
        }
    }

    @Test
    public void testFireChangeTitleEvent_NoActiveDecisionTable() {
        lockManager.init( mock( LockTarget.class ),
                          modellerPresenter );

        when( modellerPresenter.getActiveDecisionTable() ).thenReturn( null );

        lockManager.fireChangeTitleEvent();

        verify( changeTitleEvent,
                never() ).fire( any( ChangeTitleWidgetEvent.class ) );
    }

    @Test
    public void testFireChangeTitleEvent_LockInfoUpdateForActiveDecisionTable() {
        lockManager.init( mock( LockTarget.class ),
                          modellerPresenter );

        final GuidedDecisionTableView.Presenter dtPresenter = mock( GuidedDecisionTableView.Presenter.class );
        when( dtPresenter.getCurrentPath() ).thenReturn( new ObservablePathImpl().wrap( dtPath ) );
        when( modellerPresenter.getActiveDecisionTable() ).thenReturn( dtPresenter );

        lockManager.fireChangeTitleEvent();

        verify( changeTitleEvent,
                times( 1 ) ).fire( any( ChangeTitleWidgetEvent.class ) );
    }

    @Test
    public void testFireChangeTitleEvent_LockInfoUpdateForNonActiveDecisionTable() {
        lockManager.init( mock( LockTarget.class ),
                          modellerPresenter );

        final GuidedDecisionTableView.Presenter dtPresenter = mock( GuidedDecisionTableView.Presenter.class );
        when( dtPresenter.getCurrentPath() ).thenReturn( mock( ObservablePath.class ) );
        when( modellerPresenter.getActiveDecisionTable() ).thenReturn( dtPresenter );

        lockManager.fireChangeTitleEvent();

        verify( changeTitleEvent,
                never() ).fire( any( ChangeTitleWidgetEvent.class ) );
    }

}
