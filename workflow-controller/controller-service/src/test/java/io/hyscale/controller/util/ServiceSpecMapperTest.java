package io.hyscale.controller.util;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

@SpringBootTest
public class ServiceSpecMapperTest {
    
    @Autowired
    private ServiceSpecMapper serviceSpecMapper;

	private static final String FILEPATH = "/servicespecs/myservice.hspec";

	private static String ABS_FILE_PATH;

	@BeforeAll
	public static void beforeTest() throws IOException {
		ABS_FILE_PATH = ServiceSpecMapperTest.class.getResource(FILEPATH).getFile();
	}

	@Test
	public void nullFilePath() {
		String filePath = null;
		assertThrows(HyscaleException.class, () -> serviceSpecMapper.from(filePath));
	}

	@Test
	public void nullFile() {
		File file = null;
		assertThrows(HyscaleException.class, () -> serviceSpecMapper.from(file));
	}

	@Test
	public void readServiceSpecFromFilePath() {
		ServiceSpec serviceSpec = null;
		try {
			serviceSpec = serviceSpecMapper.from(ABS_FILE_PATH);
		} catch (HyscaleException e) {
		    fail();
		}
		assertNotNull(serviceSpec);
	}

	@Test
	public void readServiceSpecFromFile() {
		File file = new File(ABS_FILE_PATH);
		ServiceSpec serviceSpec = null;
		try {
			serviceSpec = serviceSpecMapper.from(file);
		} catch (HyscaleException e) {
		    fail();
		}
		assertNotNull(serviceSpec);

	}
}
