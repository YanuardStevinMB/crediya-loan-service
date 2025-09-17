package com.crediya.loan.usecase.generaterequest.generaterequest;

import com.crediya.loan.model.application.Application;
import com.crediya.loan.usecase.shared.Messages;
import com.crediya.loan.usecase.shared.exception.BusinessValidationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * EJEMPLO MEJORADO de cómo usar las nuevas excepciones personalizadas.
 * Esta clase muestra cómo migrar del ValidationException legacy al nuevo BusinessValidationException.
 * 
 * USO: Reemplazar paulatinamente el ApplicationValidator existente por esta implementación.
 * IMPORTANTE: Mantener ambas versiones hasta completar la migración.
 */
public final class EnhancedApplicationValidator {
    private EnhancedApplicationValidator() {}

    private static final BigDecimal MIN_AMOUNT = new BigDecimal("0");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("15000000");
    private static final Pattern EMAIL_RE = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    /** Valida y normaliza campos usando las nuevas excepciones personalizadas */
    public static void validateAndNormalize(Application a) {
        if (a == null) {
            throw new BusinessValidationException(
                "application",
                "Application object is null", 
                "Los datos de la solicitud son requeridos"
            );
        }

        validateIdentityDocument(a);
        validateAndNormalizeEmail(a);
        validateAmount(a);
        validateTerm(a);
        validateLoanType(a);
    }

    private static void validateIdentityDocument(Application a) {
        String doc = a.getIdentityDocument();
        
        if (doc == null || doc.isBlank()) {
            throw new BusinessValidationException(
                "identityDocument",
                "Identity document is null or blank",
                "El documento de identidad es requerido"
            );
        }
        
        if (!doc.chars().allMatch(Character::isDigit)) {
            throw new BusinessValidationException(
                "identityDocument", 
                doc,
                "Identity document contains non-numeric characters: " + doc,
                "El documento de identidad debe contener solo números"
            );
        }
        
        if (doc.length() < 6 || doc.length() > 20) {
            throw new BusinessValidationException(
                "identityDocument",
                doc,
                String.format("Identity document length %d is outside valid range [6, 20]", doc.length()),
                "El documento de identidad debe tener entre 6 y 20 dígitos"
            );
        }
    }

    private static void validateAndNormalizeEmail(Application a) {
        if (a.getEmail() == null || a.getEmail().isBlank()) {
            throw new BusinessValidationException(
                "email",
                "Email is null or blank",
                "El correo electrónico es requerido"
            );
        }

        String emailNorm = a.getEmail().trim().toLowerCase(Locale.ROOT);
        if (!EMAIL_RE.matcher(emailNorm).matches()) {
            throw new BusinessValidationException(
                "email",
                a.getEmail(),
                "Email format is invalid: " + a.getEmail(),
                "El formato del correo electrónico no es válido"
            );
        }
        a.setEmail(emailNorm);
    }

    private static void validateAmount(Application a) {
        BigDecimal amount = a.getAmount();
        
        if (amount == null) {
            throw new BusinessValidationException(
                "amount",
                "Amount is null",
                "El monto del préstamo es requerido"
            );
        }
        
        if (amount.scale() > 2) {
            throw new BusinessValidationException(
                "amount",
                amount,
                String.format("Amount has %d decimal places, maximum allowed is 2", amount.scale()),
                "El monto no puede tener más de 2 decimales"
            );
        }
        
        if (amount.compareTo(MIN_AMOUNT) <= 0 || amount.compareTo(MAX_AMOUNT) > 0) {
            throw BusinessValidationException.loanAmountExceedsLimit(
                amount.doubleValue(), 
                MAX_AMOUNT.doubleValue()
            );
        }
    }

    private static void validateTerm(Application a) {
        LocalDate term = a.getTerm();
        
        if (term == null) {
            throw new BusinessValidationException(
                "term",
                "Term date is null",
                "La fecha de vencimiento es requerida"
            );
        }
        
        if (!term.isAfter(LocalDate.now())) {
            throw new BusinessValidationException(
                "term",
                term,
                String.format("Term date %s is not after current date %s", term, LocalDate.now()),
                "La fecha de vencimiento debe ser posterior a la fecha actual"
            );
        }
    }

    private static void validateLoanType(Application a) {
        if (a.getLoanTypeId() == null || a.getLoanTypeId() <= 0) {
            throw new BusinessValidationException(
                "loanTypeId",
                a.getLoanTypeId(),
                "Loan type ID is null or invalid: " + a.getLoanTypeId(),
                "Debe seleccionar un tipo de préstamo válido"
            );
        }
    }
}