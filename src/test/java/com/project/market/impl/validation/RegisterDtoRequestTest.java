package com.project.market.impl.validation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.market.constant.Constant;
import com.project.market.dto.req.register.RegisterDtoRequest;
import com.project.market.impl.exception.ResponseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RunWith(SpringRunner.class)
public class RegisterDtoRequestTest {
    private final static Logger logger = Logger.getLogger(RegisterDtoRequestTest.class);

    @Mock
    private Validator validator;

    @Before
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void validateInputRequest() throws Exception {
        File[] files = readTestCase();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setDateFormat(simpleDateFormat);

        try {
            for (File file : files){
                logger.debug("unit-test case => " + file.getName());
                RegisterDtoRequest registerDtoRequest = mapper.readValue(file, RegisterDtoRequest.class);
                List<String> actual = new ArrayList<>();

                Set<ConstraintViolation<RegisterDtoRequest>> errors = validator.validate(registerDtoRequest);
                for (ConstraintViolation<?> error : errors) {
                    actual.add(error.getPropertyPath().toString() + ": " + error.getMessage());
                }

                List<String> expected = mapper.readValue(FileUtils.readFileToString(new File(FilenameUtils.concat("./resources/register/case_validation_input/expected", file.getName())), StandardCharsets.UTF_8), new TypeReference<List<String>>() {});
                logger.debug("Actual error => " + actual);
                logger.debug("Expected error => " + expected);

                JSONCompareResult result = JSONCompare.compareJSON(mapper.writeValueAsString(expected),mapper.writeValueAsString(actual), JSONCompareMode.NON_EXTENSIBLE);
                Assert.assertTrue(result.passed());
                logger.debug("result => " + result.passed());

            }
        }catch (ResponseException e) {
            logger.error(String.format(Constant.THROW_EXCEPTION, e.getMessage()));
        }
    }

    private File[] readTestCase() throws Exception {
        File folder = new File("./resources/register/case_validation_input/request");
        return folder.listFiles();
    }
}
