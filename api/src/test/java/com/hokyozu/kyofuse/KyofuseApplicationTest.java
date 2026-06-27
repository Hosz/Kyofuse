package com.hokyozu.kyofuse;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

class KyofuseApplicationTest {

    @Test
    void mainStartsSpringApplication() {
        String[] args = {"--spring.main.web-application-type=none"};

        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            KyofuseApplication.main(args);

            springApplication.verify(() -> SpringApplication.run(KyofuseApplication.class, args));
        }
    }
}
