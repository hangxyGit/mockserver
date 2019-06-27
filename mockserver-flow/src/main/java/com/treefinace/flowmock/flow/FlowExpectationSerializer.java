package com.treefinace.flowmock.flow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.treefinace.flowmock.flow.model.FlowExpectation;
import com.treefinace.flowmock.flow.model.FlowExpectationDTO;
import org.mockserver.logging.MockServerLogger;
import org.mockserver.mock.Expectation;
import org.mockserver.model.HttpRequest;
import org.mockserver.serialization.ExpectationSerializer;
import org.mockserver.serialization.ObjectMapperFactory;
import org.mockserver.serialization.model.ExpectationDTO;
import org.mockserver.validator.jsonschema.JsonSchemaExpectationValidator;

import java.util.Arrays;

import static org.mockserver.character.Character.NEW_LINE;

public class FlowExpectationSerializer extends ExpectationSerializer {
    ObjectMapper objectMapper = ObjectMapperFactory.createObjectMapper();
    MockServerLogger mockServerLogger;
    private JsonSchemaExpectationValidator expectationValidator = new JsonSchemaExpectationValidator(mockServerLogger);

    public FlowExpectationSerializer(MockServerLogger mockServerLogger) {
        super(mockServerLogger);
        this.mockServerLogger = mockServerLogger;
    }

    @Override
    public String serialize(Expectation expectation) {
        if (expectation instanceof FlowExpectation) {
            try {
                return objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(new ExpectationDTO(expectation));
            } catch (Exception e) {
                mockServerLogger.error(String.format("wException while serializing expectation to JSON with value %s", expectation), e);
                throw new RuntimeException(String.format("Exception while serializing expectation to JSON with value %s", expectation), e);
            }
        }
        return super.serialize(expectation);
    }

    @Override
    public String serialize(Expectation... expectations) {
        try {
            if (expectations != null && expectations.length > 0) {
                ExpectationDTO[] expectationDTOs = new ExpectationDTO[expectations.length];
                for (int i = 0; i < expectations.length; i++) {
                    expectationDTOs[i] = new FlowExpectationDTO((expectations[i]));
                }
                return objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(expectationDTOs);
            } else {
                return "[]";
            }
        } catch (Exception e) {
            mockServerLogger.error("Exception while serializing expectation to JSON with value " + Arrays.asList(expectations), e);
            throw new RuntimeException("Exception while serializing expectation to JSON with value " + Arrays.asList(expectations), e);
        }
    }

    @Override
    public Expectation deserialize(String jsonExpectation) {
        if (Strings.isNullOrEmpty(jsonExpectation)) {
            throw new IllegalArgumentException("1 error:" + NEW_LINE + " - an expectation is required but value was \"" + String.valueOf(jsonExpectation) + "\"");
        } else {
            String validationErrors = expectationValidator.isValid(jsonExpectation);
            if (validationErrors.isEmpty()) {
                Expectation expectation = null;
                try {
                    FlowExpectationDTO expectationDTO = objectMapper.readValue(jsonExpectation, FlowExpectationDTO.class);
                    if (expectationDTO != null) {
                        expectation = expectationDTO.buildObject();
                    }
                } catch (Exception e) {
                    mockServerLogger.error((HttpRequest) null, e, "exception while parsing {} for Expectation", jsonExpectation);
                    throw new RuntimeException("Exception while parsing [" + jsonExpectation + "] for Expectation", e);
                }
                return expectation;
            } else {
                mockServerLogger.error("validation failed:{}expectation:{}", validationErrors, jsonExpectation);
                throw new IllegalArgumentException(validationErrors);
            }
        }
    }
}
