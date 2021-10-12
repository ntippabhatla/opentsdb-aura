/*
 * This file is part of OpenTSDB.
 * Copyright (C) 2021  Yahoo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.opentsdb.aura.metrics.meta.endpoints.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import net.opentsdb.aura.metrics.meta.endpoints.ShardEndPoint;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MystStatefulSetRegistryTest {

    private static final String pod_name_pattern = "%s-myst-%s-%s.%s.svc.%s";
    private static final String[] replicas = {"a", "b", "c", "d", "e"};
    private static final String cluster_domain = "cluster.local";

    @Test
    public void testListOfEndpoints1() throws IOException {

        ObjectMapper mapper1 = new ObjectMapper(new YAMLFactory());

        try {
            DeploymentConfig cluster_config1 = mapper1.readValue(new File("src/test/resources/ConfigTest1.yaml"), DeploymentConfig.class);
            Map<String, Namespace> namespaceMap = cluster_config1.toMap();
            MystStatefulSetRegistry myststs1 = new MystStatefulSetRegistry(cluster_config1);
            HashMap<String, List<ShardEndPoint>> expectedEndpointsMap = new HashMap<>();
            List<Component> componentList1 = new ArrayList<>();
            List<Component> componentList2 = new ArrayList<>();
            Component a = new Component();
            a.setNumReplicas(1);
            a.setNumShards(1);
            a.setName("myst");
            a.setPort(9999);
            Component b = new Component();
            b.setNumReplicas(1);
            b.setNumShards(1);
            b.setName("aura-metrics");
            b.setPort(4080);
            componentList1.add(b);
            componentList1.add(a);
            Component m = new Component();
            m.setNumReplicas(1);
            m.setNumShards(1);
            m.setName("myst");
            m.setPort(9999);
            componentList2.add(m);
            Namespace n1 = new Namespace();
            n1.setName("default");
            n1.setk8sNamespace("default");
            n1.setComponents(componentList1);
            Namespace n2 = new Namespace();
            n2.setName("ssp");
            n2.setk8sNamespace("ssp");
            n2.setComponents(componentList2);
            List<Namespace> namespaceList1 = new ArrayList<>();
            namespaceList1.add(n1);
            namespaceList1.add(n2);
            DeploymentConfig expectedConfig = new DeploymentConfig();
            expectedConfig.setClusterName("kubernetes.default.GQ");
            expectedConfig.setClusterDomain("cluster.local");
            expectedConfig.setListOfNamespaces(namespaceList1);


            System.out.println("Test Case 1: ");
            for (String key : namespaceMap.keySet()) {
                Map<String, List<ShardEndPoint>> result = myststs1.getEndpoints(key);
                final int numShards = expectedConfig.toMap().get(key).toMap().get("myst").getNumShards();;
                final int numReplicas = expectedConfig.toMap().get(key).toMap().get("myst").getNumReplicas();
                String namespace = key;
                for (int i = 0; i < numReplicas; i++) {
                    String replica = replicas[i];
                    List<ShardEndPoint> expectedShardEndPoints = new ArrayList<>();
                    expectedEndpointsMap.put(replica, expectedShardEndPoints);
                    for (int j = 0; j < numShards; j++) {
                         MystEndpoint endpoint = MystEndpoint.Builder.newBuilder()
                                .withHost(
                                        String.format(
                                                pod_name_pattern,
                                                namespace,
                                                replica,
                                                j,
                                                namespace,
                                                cluster_domain))
                                .withPort(9999)
                                .build();
                        expectedShardEndPoints.add(endpoint);

                    }
                }

                System.out.println("Result"+myststs1.getEndpoints(key));
                System.out.println("Expected"+expectedEndpointsMap);
                assertEquals(expectedEndpointsMap,result);
            }


        } catch (Exception e) {
            System.out.println(e);
        }


    }


    @Test
    public void testListOfEndpoints2() throws IOException {

        ObjectMapper mapper1 = new ObjectMapper(new YAMLFactory());
        try {
            DeploymentConfig cluster_config1 = mapper1.readValue(new File("src/test/resources/ConfigTest2.yaml"), DeploymentConfig.class);

            Map<String, Namespace> namespaceMap = cluster_config1.toMap();
            MystStatefulSetRegistry myststs1 = new MystStatefulSetRegistry(cluster_config1);
            Map<String, List<ShardEndPoint>> expectedEndpointsMap = new HashMap<>();
            List<Component> componentList1 = new ArrayList<>();
            List<Component> componentList2 = new ArrayList<>();
            Component a = new Component();
            a.setNumReplicas(1);
            a.setNumShards(1);
            a.setName("myst");
            a.setPort(9999);
            Component b = new Component();
            b.setNumReplicas(1);
            b.setNumShards(1);
            b.setName("aura-metrics");
            b.setPort(4080);
            componentList1.add(b);
            componentList1.add(a);
            Component m = new Component();
            m.setNumReplicas(2);
            m.setNumShards(9);
            m.setName("myst");
            m.setPort(9999);
            componentList2.add(m);
            Namespace n1 = new Namespace();
            n1.setName("default");
            n1.setk8sNamespace("default");
            n1.setComponents(componentList1);
            Namespace n2 = new Namespace();
            n2.setName("Onevideo");
            n2.setk8sNamespace("Onevideo");
            n2.setComponents(componentList2);
            List<Namespace> namespaceList1 = new ArrayList<>();
            namespaceList1.add(n1);
            namespaceList1.add(n2);
            DeploymentConfig expectedConfig = new DeploymentConfig();
            expectedConfig.setClusterName("kubernetes.default.GQ");
            expectedConfig.setClusterDomain("cluster.local");
            expectedConfig.setListOfNamespaces(namespaceList1);

            System.out.println("Test Case 2: ");
            for (String key : namespaceMap.keySet()) {
                Map<String, List<ShardEndPoint>> result = myststs1.getEndpoints(key);


                final int numShards = expectedConfig.toMap().get(key).toMap().get("myst").getNumShards();;
                final int numReplicas = expectedConfig.toMap().get(key).toMap().get("myst").getNumReplicas();
                String namespace = key;

                for (int i = 0; i < numReplicas; i++) {
                    String replica = replicas[i];
                    List<ShardEndPoint> expectedShardEndPoints = new ArrayList<>();
                    expectedEndpointsMap.put(replica, expectedShardEndPoints);
                    for (int j = 0; j < numShards; j++) {
                         MystEndpoint endpoint = MystEndpoint.Builder.newBuilder()
                                .withHost(
                                        String.format(
                                                pod_name_pattern,
                                                namespace,
                                                replica,
                                                j,
                                                namespace,
                                                cluster_domain))
                                .withPort(9999)
                                .build();
                        expectedShardEndPoints.add(endpoint);

                    }
                }

                System.out.println("Result"+myststs1.getEndpoints(key));
                System.out.println("Expected"+expectedEndpointsMap);
                assertEquals(expectedEndpointsMap, result);
            }


        } catch (Exception e) {
            System.out.println(e);
        }


    }

    @Test
    public void testListOfEndpoints3() throws IOException {

        ObjectMapper mapper1 = new ObjectMapper(new YAMLFactory());
        try {
            DeploymentConfig cluster_config1 = mapper1.readValue(new File("src/test/resources/ConfigTest3.yaml"), DeploymentConfig.class);

            Map<String, Namespace> namespaceMap = cluster_config1.toMap();
            MystStatefulSetRegistry myststs1 = new MystStatefulSetRegistry(cluster_config1);
            Map<String, List<ShardEndPoint>> expectedEndpointsMap = new HashMap<>();
            List<Component> componentList1 = new ArrayList<>();
            List<Component> componentList2 = new ArrayList<>();
            Component a = new Component();
            a.setNumReplicas(1);
            a.setNumShards(1);
            a.setName("myst");
            a.setPort(9999);
            Component b = new Component();
            b.setNumReplicas(1);
            b.setNumShards(1);
            b.setName("aura-metrics");
            b.setPort(4080);
            componentList1.add(b);
            componentList1.add(a);
            Component m = new Component();
            m.setNumShards(0);
            m.setName("myst");
            componentList2.add(m);
            Namespace n1 = new Namespace();
            n1.setName("default");
            n1.setk8sNamespace("default");
            n1.setComponents(componentList1);
            Namespace n2 = new Namespace();
            n2.setName("Onevideo");
            n2.setk8sNamespace("Onevideo");
            n2.setComponents(componentList2);
            List<Namespace> namespaceList1 = new ArrayList<>();
            namespaceList1.add(n1);
            namespaceList1.add(n2);
            DeploymentConfig expectedConfig = new DeploymentConfig();
            expectedConfig.setClusterName("kubernetes.default.GQ");
            expectedConfig.setClusterDomain("cluster.local");
            expectedConfig.setListOfNamespaces(namespaceList1);

            System.out.println("Test Case 3: ");
            for (String key : namespaceMap.keySet()) {
                Map<String, List<ShardEndPoint>> result = myststs1.getEndpoints(key);
                final int numShards = expectedConfig.toMap().get(key).toMap().get("myst").getNumShards();;
                final int numReplicas = expectedConfig.toMap().get(key).toMap().get("myst").getNumReplicas();
                String namespace = key;
                for (int i = 0; i < numReplicas; i++) {
                    String replica = replicas[i];
                    List<ShardEndPoint> expectedShardEndPoints = new ArrayList<>();
                    expectedEndpointsMap.put(replica, expectedShardEndPoints);
                    for (int j = 0; j < numShards; j++) {
                        final MystEndpoint endpoint = MystEndpoint.Builder.newBuilder()
                                .withHost(
                                        String.format(
                                                pod_name_pattern,
                                                namespace,
                                                replica,
                                                j,
                                                namespace,
                                                cluster_domain))
                                .withPort(9999)
                                .build();
                        expectedShardEndPoints.add(endpoint);

                    }
                }

                System.out.println("Result"+myststs1.getEndpoints(key));
                System.out.println("Expected"+expectedEndpointsMap);
                assertEquals(expectedEndpointsMap.toString(), result.toString());
            }


        } catch (Exception e) {
            System.out.println(e);
        }

    }

    @Test
    public void testListOfEndpoints4() throws IOException {

        ObjectMapper mapper1 = new ObjectMapper(new YAMLFactory());
        try {
            DeploymentConfig cluster_config1 = mapper1.readValue(new File("src/test/resources/ConfigTest4.yaml"), DeploymentConfig.class);

            Map<String, Namespace> namespaceMap = cluster_config1.toMap();
            MystStatefulSetRegistry myststs1 = new MystStatefulSetRegistry(cluster_config1);
            Map<String, List<ShardEndPoint>> expectedEndpointsMap = new HashMap<>();
            List<Component> componentList1 = new ArrayList<>();
            List<Component> componentList2 = new ArrayList<>();
            Component a = new Component();
            a.setNumReplicas(1);
            a.setNumShards(1);
            a.setName("myst");
            a.setPort(9999);
            Component b = new Component();
            b.setNumReplicas(1);
            b.setNumShards(1);
            b.setName("aura-metrics");
            b.setPort(4080);
            componentList1.add(b);
            componentList1.add(a);
            Component m = new Component();
            m.setNumReplicas(6);
            m.setNumShards(10);
            m.setName("myst");
            componentList2.add(m);
            Namespace n1 = new Namespace();
            n1.setName("default");
            n1.setk8sNamespace("default");
            n1.setComponents(componentList1);
            Namespace n2 = new Namespace();
            n2.setName("Onevideo");
            n2.setk8sNamespace("Onevideo");
            n2.setComponents(componentList2);
            List<Namespace> namespaceList1 = new ArrayList<>();
            namespaceList1.add(n1);
            namespaceList1.add(n2);
            DeploymentConfig expectedConfig = new DeploymentConfig();
            expectedConfig.setClusterName("kubernetes.default.GQ");
            expectedConfig.setClusterDomain("cluster.local");
            expectedConfig.setListOfNamespaces(namespaceList1);

            System.out.println("Test Case 4: ");
            for (String key : namespaceMap.keySet()) {
                Map<String, List<ShardEndPoint>> result = myststs1.getEndpoints(key);
                final int numShards = expectedConfig.toMap().get(key).toMap().get("myst").getNumShards();
                final int numReplicas = expectedConfig.toMap().get(key).toMap().get("myst").getNumReplicas();
                String namespace = key;
                for (int i = 0; i < numReplicas; i++) {
                    String replica = replicas[i];
                    List<ShardEndPoint> expectedShardEndPoints = new ArrayList<>();
                    expectedEndpointsMap.put(replica, expectedShardEndPoints);
                    for (int j = 0; j < numShards; j++) {
                        final MystEndpoint endpoint = MystEndpoint.Builder.newBuilder()
                                .withHost(
                                        String.format(
                                                pod_name_pattern,
                                                namespace,
                                                replica,
                                                j,
                                                namespace,
                                                cluster_domain))
                                .withPort(9999)
                                .build();
                        expectedShardEndPoints.add(endpoint);

                    }
                }


                System.out.println("Result"+myststs1.getEndpoints(key));
                System.out.println("Expected"+expectedEndpointsMap);
                assertEquals(expectedEndpointsMap, result);
            }


        } catch (Exception e) {
            System.out.println(e);
        }

    }
}