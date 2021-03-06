/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.drools.workbench.screens.guided.dtable.backend.server;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.drools.workbench.models.datamodel.imports.Import;
import org.drools.workbench.models.datamodel.imports.Imports;
import org.drools.workbench.models.datamodel.rule.BaseSingleFieldConstraint;
import org.drools.workbench.models.guided.dtable.shared.model.ConditionCol52;
import org.drools.workbench.models.guided.dtable.shared.model.DTCellValue52;
import org.drools.workbench.models.guided.dtable.shared.model.DescriptionCol52;
import org.drools.workbench.models.guided.dtable.shared.model.GuidedDecisionTable52;
import org.drools.workbench.models.guided.dtable.shared.model.Pattern52;
import org.drools.workbench.models.guided.dtable.shared.model.RowNumberCol52;
import org.guvnor.common.services.backend.file.FileDiscoveryService;
import org.guvnor.common.services.project.model.Package;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.java.nio.file.FileSystem;
import org.uberfire.java.nio.file.Path;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GuidedDecisionTableSourceServiceTest {

    @Mock
    Path path;

    @Mock
    Package packageMock;

    @Mock
    FileSystem fileSystem;

    @Mock
    KieProjectService projectService;

    @Mock
    FileDiscoveryService discoveryService;

    GuidedDecisionTable52 model;

    @InjectMocks
    GuidedDecisionTableSourceService service;

    Pattern52 pattern;

    ConditionCol52 condition;

    List<List<DTCellValue52>> data;

    @Before
    public void setUp() throws Exception {
        // Simulates that no DSL files are present
        when(projectService.resolvePackage(any())).thenReturn(packageMock);
        when(discoveryService.discoverFiles(any(),
                                            any())).thenReturn(new ArrayList<Path>());
        when(fileSystem.supportedFileAttributeViews()).thenReturn(new HashSet<String>());
        when(path.getFileSystem()).thenReturn(fileSystem);
        when(path.toString()).thenReturn("/");
        when(path.getFileName()).thenReturn(path);
        when(path.toUri()).thenReturn(new URI("/"));

        model = new GuidedDecisionTable52();

        model.setPackageName("som.sample");
        model.setImports(new Imports(Arrays.asList(new Import("com.sample.Person"))));
        model.setRowNumberCol(new RowNumberCol52());
        model.setDescriptionCol(new DescriptionCol52());

        pattern = new Pattern52();
        pattern.setBoundName("$p");
        pattern.setFactType("Person");

        condition = new ConditionCol52();
        condition.setConstraintValueType(BaseSingleFieldConstraint.TYPE_LITERAL);
        condition.setHeader("name equals to");
        condition.setFactField("name");
        condition.setOperator("==");

        pattern.setChildColumns(Arrays.asList(condition));

        model.setConditionPatterns(Arrays.asList(pattern));

        data = new ArrayList<>();
    }

    @Test
    public void testOtherwise() throws Exception {
        addRow(1,
               "John",
               false);
        addRow(2,
               "Peter",
               false);
        addRow(3,
               null,
               true);

        model.setData(data);

        String source = service.getSource(path,
                                          model);
        assertTrue("Expected: name == \"John\"",
                   source.contains("$p : Person( name == \"John\" )"));
        assertTrue("Expected: name == \"Peter\"",
                   source.contains("$p : Person( name == \"Peter\" )"));
        assertTrue("Expected: name not in ( \"John\", \"Peter\" )",
                   source.contains("$p : Person( name not in ( \"John\", \"Peter\" )"));
    }

    @Test
    public void testOtherwiseTwoSameValues() throws Exception {
        addRow(1,
               "John",
               false);
        addRow(2,
               "John",
               false);
        addRow(3,
               null,
               true);

        model.setData(data);

        String source = service.getSource(path,
                                          model);
        assertTrue("Expected: name not in ( \"John\" )",
                   source.contains("$p : Person( name not in ( \"John\" )"));
    }

    @Test
    public void testOtherwiseEmptyValue() throws Exception {
        addRow(1,
               "John",
               false);
        addRow(2,
               "",
               false);
        addRow(3,
               null,
               true);

        model.setData(data);

        String source = service.getSource(path,
                                          model);
        assertTrue("Expected: name not in ( \"John\", \"\" )",
                   source.contains("$p : Person( name not in ( \"John\", \"\" )"));
    }

    @Test
    public void testOtherwiseEmptyAndNullValue() throws Exception {
        addRow(1,
               "",
               false);
        addRow(2,
               null,
               false);
        addRow(3,
               null,
               true);

        model.setData(data);

        String source = service.getSource(path,
                                          model);
        assertTrue("Expected: name not in ( \"\" )",
                   source.contains("$p : Person( name not in ( \"\" )"));
    }

    @Test
    public void testOtherwiseTwoTimes() throws Exception {
        addRow(1,
               "John",
               false);
        addRow(2,
               null,
               true);
        addRow(3,
               "Peter",
               false);
        addRow(4,
               null,
               true);

        model.setData(data);

        String source = service.getSource(path,
                                          model);
        assertTrue("Expected: name not in ( \"John\", \"Peter\" )",
                   source.contains("$p : Person( name not in ( \"John\", \"Peter\" )"));
        source.replaceFirst("John",
                            "");
        assertTrue("Expected: name not in ( \"John\", \"Peter\" )",
                   source.contains("$p : Person( name not in ( \"John\", \"Peter\" )"));
    }

    private void addRow(int rowNumber,
                        String conditionValue,
                        boolean isOtherwise) {
        data.add(new ArrayList<DTCellValue52>() {{
            add(new DTCellValue52(rowNumber));
            add(new DTCellValue52("row " + rowNumber));
            if (!isOtherwise) {
                add(new DTCellValue52(conditionValue));
            } else {
                DTCellValue52 otherwise = new DTCellValue52();
                otherwise.setOtherwise(true);
                add(otherwise);
            }
        }});
    }
}
