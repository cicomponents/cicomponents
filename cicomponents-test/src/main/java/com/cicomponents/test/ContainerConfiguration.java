/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.cicomponents.test;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.MavenUrlReference;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.*;
import static org.ops4j.pax.exam.karaf.options.LogLevelOption.LogLevel.WARN;

public class ContainerConfiguration {

    @SuppressWarnings("unused")
    public static final MavenUrlReference karafStandardRepo =
            maven()
                    .groupId("org.apache.karaf.features")
                    .artifactId("standard")
                    .version("4.0.7")
                    .classifier("features")
                    .type("xml");


    @Configuration
    public static Option[] withDefaultConfig(final Option ...options) {
        Option[] defaultOptions = {
                keepCaches(),
                karafDistributionConfiguration()
                .frameworkUrl(maven().groupId("org.apache.karaf").artifactId("apache-karaf-minimal").type("zip")
                                     .version("4.0.7"))
                .karafVersion("4.0.7")
                .unpackDirectory(new File("target/exam")),
                keepRuntimeFolder(),
                logLevel(WARN),
                features(maven().groupId("org.cicomponents").artifactId("cicomponents").type("xml")
                       .classifier("features").version("0.2.0-SNAPSHOT"), "cicomponents"),
                junitBundles(),
                features(karafStandardRepo),
                bundle("wrap:mvn:com.google.guava/guava-testlib/19.0"),
                configureConsole().ignoreLocalConsole().ignoreRemoteShell()
        };
        return Stream.concat(Arrays.stream(defaultOptions), Arrays.stream(options)).collect(Collectors.toList())
                     .toArray(defaultOptions);
    }



}
