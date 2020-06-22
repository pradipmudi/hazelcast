/*
 * Copyright (c) 2008-2020, Hazelcast, Inc. All Rights Reserved.
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
package com.hazelcast.internal.util.phonehome;

import com.hazelcast.collection.impl.list.ListService;
import com.hazelcast.collection.impl.queue.QueueService;
import com.hazelcast.collection.impl.set.SetService;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.instance.impl.Node;
import com.hazelcast.map.impl.MapService;
import com.hazelcast.multimap.impl.MultiMapService;
import com.hazelcast.ringbuffer.impl.RingbufferService;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;


class DistributedObjectCounterCollector implements MetricsCollector {


    @Override
    public Map<String, String> computeMetrics(Node hazelcastNode) {

        Collection<DistributedObject> distributedObjects = hazelcastNode.hazelcastInstance.getDistributedObjects();
        Map<String, Long> result = distributedObjects.stream().collect(groupingBy(DistributedObject::getServiceName, Collectors.counting()));
        Map<String, String> countInfo = new HashMap<>();

        countInfo.put("mpct", String.valueOf(result.getOrDefault(MapService.SERVICE_NAME, 0L)));
        countInfo.put("sect", String.valueOf(result.getOrDefault(SetService.SERVICE_NAME, 0L)));
        countInfo.put("quct", String.valueOf(result.getOrDefault(QueueService.SERVICE_NAME, 0L)));
        countInfo.put("mmct", String.valueOf(result.getOrDefault(MultiMapService.SERVICE_NAME, 0L)));
        countInfo.put("lict", String.valueOf(result.getOrDefault(ListService.SERVICE_NAME, 0L)));
        countInfo.put("rbct", String.valueOf(result.getOrDefault(RingbufferService.SERVICE_NAME, 0L)));

        return countInfo;
    }

}

