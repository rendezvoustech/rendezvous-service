package tech.rendezvous.rendezvousservice.api;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderRequestValidationTests {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenAllFieldsCorrectThenValidationSucceeds() {
        var orderRequest = new RendezvousRequest(1234567890L, "Name");
        Set<ConstraintViolation<RendezvousRequest>> violations = validator.validate(orderRequest);
        assertThat(violations).isEmpty();
    }

    @Test
    void whenOwnerNotDefinedThenValidationFails() {
        var orderRequest = new RendezvousRequest(null, "Name");
        Set<ConstraintViolation<RendezvousRequest>> violations = validator.validate(orderRequest);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("The owner Id must be defined.");
    }

    @Test
    void whenNameIsNotDefinedThenValidationFails() {
        var orderRequest = new RendezvousRequest(1234L, null);
        Set<ConstraintViolation<RendezvousRequest>> violations = validator.validate(orderRequest);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("The name of must be defined and not blank.");
    }

    @Test
    void whenNameIsBlankThenValidationFails() {
        var orderRequest = new RendezvousRequest(353553L, "");
        Set<ConstraintViolation<RendezvousRequest>> violations = validator.validate(orderRequest);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("The name of must be defined and not blank.");
    }
}
