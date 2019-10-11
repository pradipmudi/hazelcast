/*
 * Copyright (c) 2008-2019, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.sql.impl.calcite.logical.rule;

import com.hazelcast.sql.impl.calcite.RuleUtils;
import com.hazelcast.sql.impl.calcite.logical.rel.MapScanLogicalRel;
import org.apache.calcite.plan.RelOptRule;
import org.apache.calcite.plan.RelOptRuleCall;
import org.apache.calcite.rel.core.Project;
import org.apache.calcite.rel.core.RelFactories;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.util.mapping.Mapping;
import org.apache.calcite.util.mapping.Mappings;

import java.util.List;

public final class ProjectIntoScanLogicalRule extends RelOptRule {
    public static final ProjectIntoScanLogicalRule INSTANCE = new ProjectIntoScanLogicalRule();

    private ProjectIntoScanLogicalRule() {
        super(
            operand(Project.class,
                operandJ(TableScan.class, null, MapScanLogicalRel::isProjectableFilterable, none())),
            RelFactories.LOGICAL_BUILDER,
            ProjectIntoScanLogicalRule.class.getSimpleName()
        );
    }

    @Override
    public void onMatch(RelOptRuleCall call) {
        Project project = call.rel(0);
        TableScan scan = call.rel(1);

        Mappings.TargetMapping mapping = project.getMapping();

        if (mapping == null || Mappings.isIdentity(mapping)) {
            return;
        }

        List<Integer> oldProjects;
        RexNode filter;

        if (scan instanceof MapScanLogicalRel) {
            MapScanLogicalRel scan0 = (MapScanLogicalRel) scan;

            oldProjects = scan0.getProjects();
            filter = scan0.getFilter();

        } else {
            oldProjects = scan.identity();
            filter = null;
        }

        List<Integer> newProjects = Mappings.apply((Mapping) mapping, oldProjects);

        MapScanLogicalRel newScan = new MapScanLogicalRel(
            scan.getCluster(),
            RuleUtils.toLogicalConvention(scan.getTraitSet()),
            scan.getTable(),
            newProjects,
            filter
        );

        call.transformTo(newScan);
    }
}
