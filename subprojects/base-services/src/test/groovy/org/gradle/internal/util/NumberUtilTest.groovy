/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.internal.util

import spock.lang.Specification

import static org.gradle.internal.util.NumberUtil.formatBytes
import static org.gradle.internal.util.NumberUtil.percentOf

class NumberUtilTest extends Specification {

    def "knows percentage"() {
        expect:
        percentOf(100, 0) == 0
        percentOf(100, 1) == 1
        percentOf(100, 99) == 99
        percentOf(100, 100) == 100
        percentOf(100, 101) == 101
        percentOf(200, 50) == 25
        percentOf(200, 50) == 25
        percentOf(301, 17) == 5
        percentOf(0, 50) == 0
    }

    def "percentage does not allow negative values"() {
        when: percentOf(-10, 50) == 0
        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == "Unable to calculate percentage: 50 of -10. All inputs must be >= 0"

        when: percentOf(100, -1) == 0
        then:
        ex = thrown(IllegalArgumentException)
        ex.message == "Unable to calculate percentage: -1 of 100. All inputs must be >= 0"
    }

    def "formats bytes"() {
        expect:
        formatBytes(-1) == "-1 B"
        formatBytes(0) == "0 B"
        formatBytes(1) == "1 B"
        formatBytes(999) == "999 B"
        formatBytes(1000) == "1.0 kB"
        formatBytes(1001) == "1.0 kB"
        formatBytes(1501) == "1.5 kB"
        formatBytes(1999) == "2.0 kB"
        formatBytes(-1999) == "-2.0 kB"
        formatBytes(1000000) == "1.0 MB"
        formatBytes(1000000000) == "1.0 GB"
        formatBytes(1000000000000) == "1.0 TB"
        formatBytes(1000000000000000) == "1.0 PB"
        formatBytes(1000000000000000000) == "1.0 EB"
    }

    def "knows ordinal"() {
        expect:
        ordinal == NumberUtil.ordinal(input)

        where:
        input | ordinal
        0     | "0th"
        1     | "1st"
        2     | "2nd"
        3     | "3rd"
        4     | "4th"
        10    | "10th"
        11    | "11th"
        12    | "12th"
        13    | "13th"
        14    | "14th"
        20    | "20th"
        21    | "21st"
        22    | "22nd"
        23    | "23rd"
        24    | "24th"
        100   | "100th"
        1001  | "1001st"
        10012 | "10012th"
        10013 | "10013th"
        10014 | "10014th"
    }
}

