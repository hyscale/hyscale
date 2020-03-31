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
package io.hyscale.commons.handler;

import java.util.ArrayList;
import java.util.List;

import io.hyscale.commons.utils.TailHandler;

public class TailLogTestHandler implements TailHandler {

    private static final String EOF_MARKER = "EXIT";

    public List<String> getLines() {
        return lines;
    }

    private List<String> lines = new ArrayList<>();

    @Override
    public void handleLine(String line) {
            lines.add(line);
    }

        @Override
        public boolean handleEOF (String line){
            if (line == null) {
                return true;
            }
            return line.matches(EOF_MARKER);
        }
    }
