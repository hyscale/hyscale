/**
 * Copyright 2019 Pramati Prism, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.hyscale.commons.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HyscaleStringUtilsTests {
    private String sampleString = "hyscaleUser";
    private String suffixString = "User";
    private String expectedString= "hyscale";

    @Test
    public void testRemoveSuffixStr() {
        String actualString = HyscaleStringUtil.removeSuffixStr(sampleString, suffixString);
        assertNotNull(actualString);
        assertEquals(expectedString, actualString);
    }

    @Test
    public void testRemoveSuffixStrBuilder() {
        StringBuilder str = new StringBuilder();
        str.append(sampleString);
        String actualString = HyscaleStringUtil.removeSuffixStr(str, suffixString);
        assertNotNull(actualString);
        assertEquals(expectedString, actualString);
    }

    @Test
    public void testRemoveSuffixChar() {
        StringBuilder str = new StringBuilder();
        str.append(sampleString);
        String actualString = HyscaleStringUtil.removeSuffixChar(str, 'r');
        assertNotNull(actualString);
        assertEquals("hyscaleUse", actualString);
    }

}
