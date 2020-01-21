/**
 * Copyright 2019 Pramati Prism, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.hyscale.troubleshooting.framework.nodes;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.troubleshooting.framework.models.NodeContext;

/**
 * Interface for any component in the flowchart.
 * A component can be a terminator, decision maker, process, data,
 * connector in a flowchart.
 *
 * <p>ImplementationNotes</p>
 * The implementor should tell what type of component they are
 * in the flowchart
 *
 * @param <C> defines the data that has to flow across the flowchart
 *            workflow.
 */

public interface Node<R, C extends NodeContext> {

    public NodeType getType();

    public R process(C context) throws HyscaleException;
}
