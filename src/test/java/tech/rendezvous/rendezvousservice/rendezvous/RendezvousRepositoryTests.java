package tech.rendezvous.rendezvousservice.rendezvous;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.test.StepVerifier;
import tech.rendezvous.rendezvousservice.config.DataConfig;

@DataR2dbcTest
@Import(DataConfig.class)
@Testcontainers
public class RendezvousRepositoryTests {

    @Container
    static PostgreSQLContainer<?> postgresql =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:14.3"));

    @Autowired
    private RendezvousRepository rendezvousRepository;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", RendezvousRepositoryTests::r2dbcUrl);
        registry.add("spring.r2dbc.username", postgresql::getUsername);
        registry.add("spring.r2dbc.password", postgresql::getPassword);
        registry.add("spring.flyway.url", postgresql::getJdbcUrl);
    }

    private static String r2dbcUrl() {
        return String.format("r2dbc:postgresql://%s:%s/%s",
                postgresql.getHost(),
                postgresql.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT),
                postgresql.getDatabaseName());
    }

    @Test
    void findOrderByIdWhenNotExisting() {
        StepVerifier.create(rendezvousRepository.findById(394L))
                .expectNextCount(0)
                .verifyComplete();
    }
    @Test
    void createRejectedOrder() {
        var rejected = RendezvousService.buildRejectedRendezvous(12345456L, "R");
        StepVerifier
                .create(rendezvousRepository.save(rejected))
                .expectNextMatches(
                        rendezvous -> rendezvous.status().equals(RendezvousStatus.REJECTED)
                )
                .verifyComplete();
    }
}
