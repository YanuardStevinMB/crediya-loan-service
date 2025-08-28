package com.crediya.loan.usecase.generaterequest;

import com.crediya.loan.model.application.Application;
import com.crediya.loan.usecase.generaterequest.shared.Messages;
import com.crediya.loan.usecase.generaterequest.shared.ValidationException;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;
import java.util.regex.Pattern;

final class ApplicationValidator {
    private ApplicationValidator() {}

    private static final BigDecimal MIN_AMOUNT = new BigDecimal("0");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("15000000");
    private static final Pattern EMAIL_RE = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    /** Valida y normaliza campos en memoria. Lanza ValidationException (runtime) si falla. */
    static void validateAndNormalize(Application a) {
        if (a == null) throw new ValidationException(null, Messages.REQ_BODY_REQUIRED);

        // Documento
        if (a.getIdentityDocument() == null || a.getIdentityDocument().isBlank())
            throw new ValidationException("identityDocument", Messages.DOC_REQUIRED);

        // Email
        if (a.getEmail() == null || a.getEmail().isBlank())
            throw new ValidationException("email", Messages.EMAIL_REQUIRED);

        String emailNorm = a.getEmail().trim().toLowerCase(Locale.ROOT);
        if (!EMAIL_RE.matcher(emailNorm).matches())
            throw new ValidationException("email", Messages.EMAIL_INVALID);
        a.setEmail(emailNorm);

        // Monto
        BigDecimal amount = a.getAmount();
        if (amount == null) throw new ValidationException("amount", Messages.AMOUNT_REQUIRED);
        if (amount.scale() > 2) throw new ValidationException("amount", Messages.AMOUNT_DECIMALS);
        if (amount.compareTo(MIN_AMOUNT) <= 0 || amount.compareTo(MAX_AMOUNT) > 0)
            throw new ValidationException("amount", Messages.AMOUNT_RANGE);

        // Plazo (como fecha futura)
        LocalDate term = a.getTerm();
        if (term == null) throw new ValidationException("term", Messages.TERM_REQUIRED);
        if (!term.isAfter(LocalDate.now())) throw new ValidationException("term", Messages.TERM_POSITIVE);

        // Tipo de préstamo
        if (a.getLoanTypeId() == null || a.getLoanTypeId() <= 0)
            throw new ValidationException("loanTypeId", Messages.LOAN_TYPE_REQUIRED);
    }
}
