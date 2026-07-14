package com.hokyozu.kyofuse.teams.config;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.assertj.core.api.Assertions.assertThat;

class SchedulerConfigTest {

    @Test
    void schedulerConfigCanBeInstantiatedAndEnablesScheduling() {
        assertThat(new SchedulerConfig()).isNotNull();
        assertThat(SchedulerConfig.class).hasAnnotation(EnableScheduling.class);
    }
}
