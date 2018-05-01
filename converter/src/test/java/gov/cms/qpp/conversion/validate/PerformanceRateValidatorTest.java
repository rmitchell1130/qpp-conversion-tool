package gov.cms.qpp.conversion.validate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gov.cms.qpp.conversion.decode.PerformanceRateProportionMeasureDecoder;
import gov.cms.qpp.conversion.model.Node;
import gov.cms.qpp.conversion.model.TemplateId;
import gov.cms.qpp.conversion.model.error.ErrorCode;
import gov.cms.qpp.conversion.model.error.correspondence.DetailsErrorEquals;

import static com.google.common.truth.Truth.assertWithMessage;

class PerformanceRateValidatorTest {

	private PerformanceRateValidator performanceRateValidator;
	private Node node;

	@BeforeEach
	void setup() {
		performanceRateValidator = new PerformanceRateValidator();
		node = new Node(TemplateId.PERFORMANCE_RATE_PROPORTION_MEASURE);
	}

	@Test
	void testZeroValue() {
		node.putValue(PerformanceRateProportionMeasureDecoder.PERFORMANCE_RATE, "0");
		performanceRateValidator.internalValidateSingleNode(node);
		assertWithMessage("Must contain a proper value")
				.that(performanceRateValidator.getDetails()).isEmpty();
	}

	@Test
	void testOneValue() {
		node.putValue(PerformanceRateProportionMeasureDecoder.PERFORMANCE_RATE, "1");
		performanceRateValidator.internalValidateSingleNode(node);
		assertWithMessage("Must contain a proper value")
				.that(performanceRateValidator.getDetails()).isEmpty();
	}

	@Test
	void testNAValue() {
		node.putValue(PerformanceRateProportionMeasureDecoder.NULL_PERFORMANCE_RATE, "NA");
		performanceRateValidator.internalValidateSingleNode(node);
		assertWithMessage("Must contain a proper value")
				.that(performanceRateValidator.getDetails()).isEmpty();
	}

	@Test
	void testNegativeValue() {
		String invalidValue = "-1";
		node.putValue(PerformanceRateProportionMeasureDecoder.PERFORMANCE_RATE, invalidValue);
		performanceRateValidator.internalValidateSingleNode(node);
		assertWithMessage("Must contain a proper value")
				.that(performanceRateValidator.getDetails()).comparingElementsUsing(DetailsErrorEquals.INSTANCE)
				.containsExactly(ErrorCode.PERFORMANCE_RATE_INVALID_VALUE.format(invalidValue));
	}

	@Test
	void testInvalidValue() {
		String invalidValue = "2";
		node.putValue(PerformanceRateProportionMeasureDecoder.PERFORMANCE_RATE, invalidValue);
		performanceRateValidator.internalValidateSingleNode(node);
		assertWithMessage("Must contain a proper value")
				.that(performanceRateValidator.getDetails()).comparingElementsUsing(DetailsErrorEquals.INSTANCE)
				.containsExactly(ErrorCode.PERFORMANCE_RATE_INVALID_VALUE.format(invalidValue));
	}

	@Test
	void testInvalidStringValue() {
		String invalidValue = "Inval";
		node.putValue(PerformanceRateProportionMeasureDecoder.PERFORMANCE_RATE, invalidValue);
		performanceRateValidator.internalValidateSingleNode(node);
		assertWithMessage("Must contain a proper value")
				.that(performanceRateValidator.getDetails()).comparingElementsUsing(DetailsErrorEquals.INSTANCE)
				.containsExactly(ErrorCode.PERFORMANCE_RATE_INVALID_VALUE.format(invalidValue));
	}

	@Test
	void testEmptyValue() {
		node.putValue(PerformanceRateProportionMeasureDecoder.PERFORMANCE_RATE, "");
		performanceRateValidator.internalValidateSingleNode(node);
		assertWithMessage("The error code is incorrect")
			.that(performanceRateValidator.getDetails()).comparingElementsUsing(DetailsErrorEquals.INSTANCE)
			.containsExactly(ErrorCode.PERFORMANCE_RATE_MISSING);
	}

	@Test
	void testNonExistentValue() {
		performanceRateValidator.internalValidateSingleNode(node);
		assertWithMessage("The error code is incorrect")
			.that(performanceRateValidator.getDetails()).comparingElementsUsing(DetailsErrorEquals.INSTANCE)
			.containsExactly(ErrorCode.PERFORMANCE_RATE_MISSING);
	}
}
