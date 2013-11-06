/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sirona.cube;

import org.apache.sirona.Role;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.gauges.GaugeDataStoreAdapter;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CubeGaugeDataStore extends GaugeDataStoreAdapter {
    private static final Logger LOGGER = Logger.getLogger(CubeGaugeDataStore.class.getName());

    private final Cube cube = Configuration.findOrCreateInstance(CubeBuilder.class).build();

    @Override
    public void addToGauge(final Role role, final long time, final double value) {
        try {
            cube.post(cube.gaugeSnapshot(time, role, value));
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
