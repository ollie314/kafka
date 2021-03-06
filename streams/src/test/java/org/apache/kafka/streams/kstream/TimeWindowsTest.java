/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.kafka.streams.kstream;

import org.apache.kafka.streams.kstream.internals.TimeWindow;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TimeWindowsTest {

    private static final long ANY_SIZE = 123L;

    @Test
    public void shouldSetWindowSize() {
        assertEquals(ANY_SIZE, TimeWindows.of(ANY_SIZE).sizeMs);
    }

    @Test
    public void shouldSetWindowAdvance() {
        final long anyAdvance = 4;
        assertEquals(anyAdvance, TimeWindows.of(ANY_SIZE).advanceBy(anyAdvance).advanceMs);
    }

    @SuppressWarnings("deprecation") // specifically testing deprecated APIs
    @Test
    public void shouldSetWindowRetentionTime() {
        assertEquals(ANY_SIZE, TimeWindows.of(ANY_SIZE).until(ANY_SIZE).maintainMs());
    }

    @SuppressWarnings("deprecation") // specifically testing deprecated APIs
    @Test
    public void shouldUseWindowSizeAsRentitionTimeIfWindowSizeIsLargerThanDefaultRetentionTime() {
        final long windowSize = 2 * TimeWindows.of(1).maintainMs();
        assertEquals(windowSize, TimeWindows.of(windowSize).maintainMs());
    }

    @Test(expected = IllegalArgumentException.class)
    public void windowSizeMustNotBeZero() {
        TimeWindows.of(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void windowSizeMustNotBeNegative() {
        TimeWindows.of(-1);
    }

    @Test
    public void advanceIntervalMustNotBeZero() {
        final TimeWindows windowSpec = TimeWindows.of(ANY_SIZE);
        try {
            windowSpec.advanceBy(0);
            fail("should not accept zero advance parameter");
        } catch (final IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void advanceIntervalMustNotBeNegative() {
        final TimeWindows windowSpec = TimeWindows.of(ANY_SIZE);
        try {
            windowSpec.advanceBy(-1);
            fail("should not accept negative advance parameter");
        } catch (final IllegalArgumentException e) {
            // expected
        }
    }

    @Deprecated
    @Test
    public void advanceIntervalMustNotBeLargerThanWindowSize() {
        final TimeWindows windowSpec = TimeWindows.of(ANY_SIZE);
        try {
            windowSpec.advanceBy(ANY_SIZE + 1);
            fail("should not accept advance greater than window size");
        } catch (final IllegalArgumentException e) {
            // expected
        }
    }

    @Deprecated
    @Test
    public void retentionTimeMustNoBeSmallerThanWindowSize() {
        final TimeWindows windowSpec = TimeWindows.of(ANY_SIZE);
        try {
            windowSpec.until(ANY_SIZE - 1);
            fail("should not accept retention time smaller than window size");
        } catch (final IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void gracePeriodShouldEnforceBoundaries() {
        TimeWindows.of(3L).grace(0L);

        try {
            TimeWindows.of(3L).grace(-1L);
            fail("should not accept negatives");
        } catch (final IllegalArgumentException e) {
            //expected
        }
    }

    @Test
    public void shouldComputeWindowsForHoppingWindows() {
        final TimeWindows windows = TimeWindows.of(12L).advanceBy(5L);
        final Map<Long, TimeWindow> matched = windows.windowsFor(21L);
        assertEquals(12L / 5L + 1, matched.size());
        assertEquals(new TimeWindow(10L, 22L), matched.get(10L));
        assertEquals(new TimeWindow(15L, 27L), matched.get(15L));
        assertEquals(new TimeWindow(20L, 32L), matched.get(20L));
    }

    @Test
    public void shouldComputeWindowsForBarelyOverlappingHoppingWindows() {
        final TimeWindows windows = TimeWindows.of(6L).advanceBy(5L);
        final Map<Long, TimeWindow> matched = windows.windowsFor(7L);
        assertEquals(1, matched.size());
        assertEquals(new TimeWindow(5L, 11L), matched.get(5L));
    }

    @Test
    public void shouldComputeWindowsForTumblingWindows() {
        final TimeWindows windows = TimeWindows.of(12L);
        final Map<Long, TimeWindow> matched = windows.windowsFor(21L);
        assertEquals(1, matched.size());
        assertEquals(new TimeWindow(12L, 24L), matched.get(12L));
    }

}