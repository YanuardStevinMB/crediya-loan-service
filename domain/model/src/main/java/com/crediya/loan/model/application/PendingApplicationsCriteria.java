package com.crediya.loan.model.application;


public record PendingApplicationsCriteria(
        String state,
        String document,
        String email,
        int page,
        int size
) {}