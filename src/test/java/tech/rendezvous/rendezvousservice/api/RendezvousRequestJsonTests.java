package tech.rendezvous.rendezvousservice.api;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.validation.Validator;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class RendezvousRequestJsonTests {

    @Autowired
    private JacksonTester<RendezvousRequest> json;

    @Test
    void testDeserialize() throws Exception {
        var content = """
                {
                    "ownerId": 1234567890,
                    "name": "nz"
                }
                """;
        assertThat(this.json.parse(content))
                .usingRecursiveComparison().isEqualTo(new RendezvousRequest(1234567890L, "nz"));
    }

}

