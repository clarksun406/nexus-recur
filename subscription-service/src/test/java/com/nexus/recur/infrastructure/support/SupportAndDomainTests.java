package com.nexus.recur.infrastructure.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.recur.application.common.BusinessException;
import com.nexus.recur.domain.model.BillingCycle;
import com.nexus.recur.interfaces.rest.common.ApiResponse;
import java.time.OffsetDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SupportAndDomainTests {
    @Test
    void billingCycleParsesAliasesAndAddsPeriods() {
        OffsetDateTime start = OffsetDateTime.parse("2026-07-13T00:00:00Z");

        assertThat(BillingCycle.from("monthly").addTo(start)).isEqualTo(start.plusMonths(1));
        assertThat(BillingCycle.from("3month").addTo(start)).isEqualTo(start.plusMonths(3));
        assertThat(BillingCycle.from("six_month").addTo(start)).isEqualTo(start.plusMonths(6));
        assertThat(BillingCycle.from("yearly").addTo(start)).isEqualTo(start.plusYears(1));
        assertThatThrownBy(() -> BillingCycle.from("weekly")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void jsonServiceReadsWritesAndHandlesInvalidJson() {
        JsonService jsonService = new JsonService(new ObjectMapper());

        assertThat(jsonService.write(null)).isNull();
        assertThat(jsonService.read(null)).isEmpty();
        assertThat(jsonService.read("")).isEmpty();
        assertThat(jsonService.read("not-json")).isEmpty();

        String json = jsonService.write(Map.of("max_api_calls", 10000));
        assertThat(jsonService.read(json)).containsEntry("max_api_calls", 10000);
    }

    @Test
    void apiResponseAndBusinessExceptionExposeExpectedFields() {
        ApiResponse<String> ok = ApiResponse.ok("data");
        assertThat(ok.success()).isTrue();
        assertThat(ok.code()).isEqualTo("OK");
        assertThat(ok.data()).isEqualTo("data");

        ApiResponse<Void> error = ApiResponse.error("BAD", "bad request");
        assertThat(error.success()).isFalse();
        assertThat(error.message()).isEqualTo("bad request");

        BusinessException exception = new BusinessException("NOPE", "nope", HttpStatus.CONFLICT);
        assertThat(exception.getCode()).isEqualTo("NOPE");
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
    }
}
