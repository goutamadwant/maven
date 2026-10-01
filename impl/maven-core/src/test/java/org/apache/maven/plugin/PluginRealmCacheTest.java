/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.repository.RemoteRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;

class PluginRealmCacheTest {
    @Test
    void defaultGetPreservesDependencyNode() throws Exception {
        PluginRealmCache cache = new SimpleCache();
        PluginRealmCache.Key key = new PluginRealmCache.Key() {};
        DependencyNode node = mock(DependencyNode.class);
        PluginRealmCache.CacheRecord record = new PluginRealmCache.CacheRecord(mock(ClassRealm.class), List.of(), node);

        assertSame(node, cache.get(key, () -> record).getDependencyNode());
        assertSame(
                node,
                cache.get(key, () -> {
                            fail("cached record should be reused");
                            return null;
                        })
                        .getDependencyNode());
    }

    private static class SimpleCache implements PluginRealmCache {
        private final Map<Key, CacheRecord> records = new HashMap<>();

        @Override
        public Key createKey(
                Plugin plugin,
                ClassLoader parentRealm,
                Map<String, ClassLoader> foreignImports,
                DependencyFilter dependencyFilter,
                List<RemoteRepository> repositories,
                RepositorySystemSession session) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CacheRecord get(Key key) {
            return records.get(key);
        }

        @Override
        public CacheRecord put(Key key, ClassRealm realm, List<Artifact> artifacts) {
            CacheRecord record = new CacheRecord(realm, artifacts);
            records.put(key, record);
            return record;
        }

        @Override
        public void flush() {
            records.clear();
        }

        @Override
        public void register(MavenProject project, Key key, CacheRecord record) {
            // no usage tracking
        }
    }
}
