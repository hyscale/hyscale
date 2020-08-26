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
package io.hyscale.generator.services.processor;

import com.google.common.collect.Multimap;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.io.HyscaleFilesUtil;
import org.junit.Ignore;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@SpringBootTest
@Ignore
class CustomSnippetsProcessorTest {
    @Autowired
    CustomSnippetsProcessor customSnippetsProcessor;

    private static Stream<Arguments> input() throws HyscaleException{
        return Stream.of(Arguments.of(getList("Deployment"),
                getList("./src/test/resources/processor/k8sSnippets/init-containers.yaml")),
                Arguments.of(getList("Pod"),
                        getList("./src/test/resources/processor/k8sSnippets/pod-security-context.yaml")),
                Arguments.of(getList("Deployment","Pod"),
                        getList("./src/test/resources/processor/k8sSnippets/init-containers.yaml",
                                "./src/test/resources/processor/k8sSnippets/pod-security-context.yaml")));
    }

    @ParameterizedTest
    @MethodSource("input")
    void test(List<String> expectedKinds, List<String> k8sSnippetList) throws HyscaleException {
        Multimap<String,String> kindVsSnippets =  customSnippetsProcessor.processCustomSnippetFiles(k8sSnippetList);
        List<String> kinds = new ArrayList<String>();
        kinds.addAll(kindVsSnippets.keySet());
        assertTrue(kinds.stream().allMatch(each -> expectedKinds.contains(each)));
    }

    private static List<String> getList(String... items){
        List<String> list = new ArrayList<String>();
        for(String item : items){
            list.add(item);
        }
        return list;
    }
}
