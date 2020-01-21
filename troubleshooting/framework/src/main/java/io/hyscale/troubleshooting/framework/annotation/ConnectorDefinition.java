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
package io.hyscale.troubleshooting.framework.annotation;

import io.hyscale.troubleshooting.framework.nodes.Node;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *  {@link ConnectorDefinition} annotation defines the connection between two
 *  {@link Node} on 'yes' and 'no' conditions. Both yes() & no () define the
 *  outbound edges of the node. In case of yes , the flow
 *  continues with the node connected with 'yes' . In case of 'no' the flow continues
 *  with node connected with 'no'.
 *
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConnectorDefinition {

    public Class<? extends Node> yes();

    public Class<? extends Node> no();

}



