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
package io.hyscale.controller.commands.input;

import picocli.CommandLine;

public class ScaleArg {

    @CommandLine.Option(names = {"--up"}, required = false, description = "Number of replicas to scale up by")
    private Integer up;

    @CommandLine.Option(names = {"--down"}, required = false, description = "Number of replicas to scale down by")
    private Integer down;

    @CommandLine.Option(names = {"--to"}, required = false, description = "Number of replicas to scale to")
    private Integer to;


    public Integer getUp() {
        return up;
    }

    public Integer getDown() {
        return down;
    }

    public Integer getTo() {
        return to;
    }
}
