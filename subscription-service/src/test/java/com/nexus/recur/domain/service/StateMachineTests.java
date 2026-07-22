package com.nexus.recur.domain.service;

import com.nexus.recur.application.common.BusinessException;
import com.nexus.recur.domain.model.SubscriptionStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StateMachineTests {
    private final StateMachine stateMachine = new StateMachine();

    @Test
    void allowsExpectedTransitionsAndSameState() {
        assertDoesNotThrow(() -> stateMachine.ensureAllowed(SubscriptionStatus.pending, SubscriptionStatus.active));
        assertDoesNotThrow(() -> stateMachine.ensureAllowed(SubscriptionStatus.active, SubscriptionStatus.paused));
        assertDoesNotThrow(() -> stateMachine.ensureAllowed(SubscriptionStatus.paused, SubscriptionStatus.active));
        assertDoesNotThrow(() -> stateMachine.ensureAllowed(SubscriptionStatus.active, SubscriptionStatus.active));
    }

    @Test
    void rejectsInvalidTransitions() {
        assertThrows(BusinessException.class, () -> stateMachine.ensureAllowed(SubscriptionStatus.canceled, SubscriptionStatus.active));
        assertThrows(BusinessException.class, () -> stateMachine.ensureAllowed(SubscriptionStatus.expired, SubscriptionStatus.paused));
    }
}
