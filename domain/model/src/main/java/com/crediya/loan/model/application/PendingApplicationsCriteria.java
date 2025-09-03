package com.crediya.loan.model.application;


public record PendingApplicationsCriteria(
        int page, int size,
        String filter,
        Long stateId,
        Long loanTypeId
) {}