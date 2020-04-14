package io.hyscale.controller.hooks;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.controller.validator.impl.DockerDaemonValidator;

@SpringBootTest
public class DockerDaemonValidatorTest {

	@Autowired
	private DockerDaemonValidator dockerDaemonValidator;

	public static Stream<Arguments> input() {
		WorkflowContext context = new WorkflowContext();
		return Stream.of(Arguments.of(context));
	}

	@ParameterizedTest
	@MethodSource(value = "input")
	void testValidateForTrue(WorkflowContext context) {
		try {
			assertTrue(dockerDaemonValidator.validate(context));
		} catch (HyscaleException e) {
			fail();
		}
	}

	@ParameterizedTest
	@MethodSource(value = "input")
	void testValidateForFalse(WorkflowContext context) {
		try {
			//TODO here we have to pass wrong context
			assertFalse(dockerDaemonValidator.validate(context));
		} catch (HyscaleException e) {
			fail();
		}
	}

}
