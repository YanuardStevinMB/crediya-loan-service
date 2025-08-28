package com.crediya.loan.usecase.generaterequest.shared;

import java.math.BigDecimal;

public final class Messages {

    public static final String REQ_BODY_REQUIRED   = "Los datos de la solicitud son obligatorios";
    public static final String DOC_REQUIRED        = "El documento de identidad es obligatorio";
    public static final String EMAIL_REQUIRED      = "El correo electrónico es obligatorio";
    public static final String AMOUNT_INVALID      = "El correo electrónico es obligatorio";

    public static final String EMAIL_INVALID       = "Correo electrónico inválido";
    public static final String AMOUNT_REQUIRED     = "El monto es obligatorio";
    public static final String AMOUNT_DECIMALS     = "El monto no debe tener más de 2 decimales";
    public static final String AMOUNT_RANGE        = "El monto debe estar en (0, 15000000]";
    public static final String TERM_REQUIRED       = "El plazo en meses es obligatorio";
    public static final String TERM_POSITIVE       = "El plazo en meses debe ser mayor a 0";
    public static final String LOAN_TYPE_REQUIRED  = "El tipo de préstamo es obligatorio";
    public static final String LOAN_TYPE_NO_EXIST  = "El tipo de préstamo es obligatorio";
    public static String amountNotAllowed(BigDecimal amountMin, BigDecimal amountMax) {
        return
        "El monto debe estar entre " + amountMin + " y " + amountMax +" ";
    }

    public static String stateNotFound(String code){ return "El estado inicial '" + code + "' no existe"; }

}
