/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents.test;

import com.google.common.collect.testing.MapInterfaceTest;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cicomponents.PersistentMapImplementation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.tinybundles.core.TinyBundles;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

import javax.inject.Inject;
import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.*;
import static org.ops4j.pax.exam.karaf.options.LogLevelOption.LogLevel.WARN;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class PersistentMapImplementationTest extends MapInterfaceTest<String, Object> {
    @Inject
    private BundleContext bundleContext;

    @Inject
    private PersistentMapImplementation implementation;

    final MavenUrlReference karafStandardRepo =
            maven()
                    .groupId("org.apache.karaf.features")
                    .artifactId("standard")
                    .version("4.0.5")
                    .classifier("features")
                    .type("xml");


    public PersistentMapImplementationTest() {
        super(false, true, true, true, true, false);
    }

    @Configuration
    public Option[] config() {
        return options(
                keepCaches(),
                karafDistributionConfiguration()
                .frameworkUrl(maven().groupId("org.apache.karaf").artifactId("apache-karaf").type("zip")
                                     .version("4.0.5"))
                .useDeployFolder(false)
                .karafVersion("4.0.5")
                .unpackDirectory(new File("target/exam")),
                keepRuntimeFolder(),
                logLevel(WARN),
                features(
                        maven().groupId("org.cicomponents").artifactId("cicomponents").type("xml")
                               .classifier("features").version("0.1.0-SNAPSHOT"), "cicomponents"),
                junitBundles(),
                bundle("wrap:mvn:com.google.guava/guava-testlib/19.0")
        );
    }

    @Test
    public void persistentMapAvailable() {
        assertNotNull(implementation);
    }

    @Test
    public void isolation() {
        Map<String, Object> map = implementation.getMapForBundle(bundleContext.getBundle());
        map.put("test", "value");
        assertTrue(map.containsKey("test"));
        Map<String, Object> anotherMap = implementation.getMapForBundle(bundleContext.getBundle(1));
        assertFalse(anotherMap.containsKey("test"));
    }


    @EqualsAndHashCode
    @ToString
    public static class SomeClass {}

    @Test
    public void bundleClass() {
        Map<String, Object> map = implementation.getMapForBundle(bundleContext.getBundle());
        map.put("test", new SomeClass());
        assertTrue(map.get("test") instanceof SomeClass);
    }

    @Override protected Map<String, Object> makeEmptyMap() throws UnsupportedOperationException {
        Map<String, Object> map = implementation.getMapForBundle(bundleContext.getBundle(), UUID.randomUUID().toString());
        return map;
    }

    @Override protected Map<String, Object> makePopulatedMap() throws UnsupportedOperationException {
        Map<String, Object> map = implementation.getMapForBundle(bundleContext.getBundle(), UUID.randomUUID().toString());
        map.put("test", new SomeClass());
        return map;
    }

    @Override protected String getKeyNotInPopulatedMap() throws UnsupportedOperationException {
        return "not";
    }

    @Override protected Object getValueNotInPopulatedMap() throws UnsupportedOperationException {
        return "hello";
    }

    @Test @Override public void testClear() {
        super.testClear();
    }

    @Test @Override public void testContainsKey() {
        super.testContainsKey();
    }

    @Test @Override public void testContainsValue() {
        super.testContainsValue();
    }

    @Test @Override public void testEntrySet() {
        super.testEntrySet();
    }

//    @Test @Override public void testEntrySetAddAndAddAll() {
//        super.testEntrySetAddAndAddAll();
//    }
//
//    @Test @Override public void testEntrySetClear() {
//        super.testEntrySetClear();
//    }
//
    @Test @Override public void testEntrySetContainsEntryIncompatibleKey() {
        super.testEntrySetContainsEntryIncompatibleKey();
    }

    @Test @Override public void testEntrySetContainsEntryNullKeyMissing() {
        super.testEntrySetContainsEntryNullKeyMissing();
    }

    @Test @Override public void testEntrySetContainsEntryNullKeyPresent() {
        super.testEntrySetContainsEntryNullKeyPresent();
    }

    @Test @Override public void testEntrySetForEmptyMap() {
        super.testEntrySetForEmptyMap();
    }

//    @Test @Override public void testEntrySetIteratorRemove() {
//        super.testEntrySetIteratorRemove();
//    }
//
//    @Test @Override public void testEntrySetRemove() {
//        super.testEntrySetRemove();
//    }
//
//    @Test @Override public void testEntrySetRemoveAll() {
//        super.testEntrySetRemoveAll();
//    }
//
//    @Test @Override public void testEntrySetRemoveAllNullFromEmpty() {
//        super.testEntrySetRemoveAllNullFromEmpty();
//    }
//
//    @Test @Override public void testEntrySetRemoveDifferentValue() {
//        super.testEntrySetRemoveDifferentValue();
//    }
//
//    @Test @Override public void testEntrySetRemoveMissingKey() {
//        super.testEntrySetRemoveMissingKey();
//    }
//
//    @Test @Override public void testEntrySetRemoveNullKeyMissing() {
//        super.testEntrySetRemoveNullKeyMissing();
//    }
//
//    @Test @Override public void testEntrySetRemoveNullKeyPresent() {
//        super.testEntrySetRemoveNullKeyPresent();
//    }
//
//    @Test @Override public void testEntrySetRetainAll() {
//        super.testEntrySetRetainAll();
//    }
//
//    @Test @Override public void testEntrySetRetainAllNullFromEmpty() {
//        super.testEntrySetRetainAllNullFromEmpty();
//    }
//
//    @Test @Override public void testEntrySetSetValue() {
//        super.testEntrySetSetValue();
//    }
//
//    @Test @Override public void testEntrySetSetValueSameValue() {
//        super.testEntrySetSetValueSameValue();
//    }

    @Test @Override public void testEqualsForEmptyMap() {
        super.testEqualsForEmptyMap();
    }

    @Test @Override public void testEqualsForEqualMap() {
        super.testEqualsForEqualMap();
    }

    @Test @Override public void testEqualsForLargerMap() {
        super.testEqualsForLargerMap();
    }

    @Test @Override public void testEqualsForSmallerMap() {
        super.testEqualsForSmallerMap();
    }

    @Test @Override public void testGet() {
        super.testGet();
    }

    @Test @Override public void testGetForEmptyMap() {
        super.testGetForEmptyMap();
    }

    @Test @Override public void testGetNull() {
        super.testGetNull();
    }

    @Test @Override public void testHashCode() {
        super.testHashCode();
    }

    @Test @Override public void testHashCodeForEmptyMap() {
        super.testHashCodeForEmptyMap();
    }

//    @Test @Override public void testKeySetClear() {
//        super.testKeySetClear();
//    }
//
//    @Test @Override public void testKeySetRemove() {
//        super.testKeySetRemove();
//    }
//
//    @Test @Override public void testKeySetRemoveAll() {
//        super.testKeySetRemoveAll();
//    }
//
//    @Test @Override public void testKeySetRemoveAllNullFromEmpty() {
//        super.testKeySetRemoveAllNullFromEmpty();
//    }
//
//    @Test @Override public void testKeySetRetainAll() {
//        super.testKeySetRetainAll();
//    }
//
//    @Test @Override public void testKeySetRetainAllNullFromEmpty() {
//        super.testKeySetRetainAllNullFromEmpty();
//    }

    @Test @Override public void testPutAllExistingKey() {
        super.testPutAllExistingKey();
    }

    @Test @Override public void testPutAllNewKey() {
        super.testPutAllNewKey();
    }

    @Test @Override public void testPutExistingKey() {
        super.testPutExistingKey();
    }

    @Test @Override public void testPutNewKey() {
        super.testPutNewKey();
    }

    @Test @Override public void testPutNullKey() {
        super.testPutNullKey();
    }

    @Test @Override public void testPutNullValue() {
        super.testPutNullValue();
    }

    @Test @Override public void testPutNullValueForExistingKey() {
        super.testPutNullValueForExistingKey();
    }

    @Test @Override public void testRemove() {
        super.testRemove();
    }

    @Test @Override public void testRemoveMissingKey() {
        super.testRemoveMissingKey();
    }

    @Test @Override public void testSize() {
        super.testSize();
    }

    @Test @Override public void testValues() {
        super.testValues();
    }

    @Test @Override public void testValuesClear() {
        super.testValuesClear();
    }

    @Test @Override public void testValuesIteratorRemove() {
        super.testValuesIteratorRemove();
    }

//    @Test @Override public void testValuesRemove() {
//        super.testValuesRemove();
//    }
//
//    @Test @Override public void testValuesRemoveAll() {
//        super.testValuesRemoveAll();
//    }
//
//    @Test @Override public void testValuesRemoveAllNullFromEmpty() {
//        super.testValuesRemoveAllNullFromEmpty();
//    }
//
//    @Test @Override public void testValuesRemoveMissing() {
//        super.testValuesRemoveMissing();
//    }
//
//    @Test @Override public void testValuesRetainAll() {
//        super.testValuesRetainAll();
//    }
//
//    @Test @Override public void testValuesRetainAllNullFromEmpty() {
//        super.testValuesRetainAllNullFromEmpty();
//    }

}
