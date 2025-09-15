package com.crediya.loan.usecase.shared;

import java.math.BigDecimal;

public final class Messages {

    private Messages() {
        // Utility class: prevent instantiation
    }

    // =========================
    // Generales / Request
    // =========================
    public static final String REQ_BODY_REQUIRED = "Los datos de la solicitud son obligatorios";

    // =========================
    // Documento
    // =========================
    public static final String DOC_REQUIRED = "El documento de identidad es obligatorio";
    public static final String DOC_NUMERIC  = "El documento solo puede tener dígitos (0-9)";
    public static final String DOC_LENGTH   = "El documento debe tener entre 6 y 20 dígitos";

    // =========================
    // Correo electrónico
    // =========================
    public static final String EMAIL_REQUIRED = "El correo electrónico es obligatorio";
    public static final String EMAIL_INVALID  = "Correo electrónico inválido";

    // =========================
    // Monto
    // =========================
    public static final String AMOUNT_REQUIRED = "El monto es obligatorio";
    public static final String AMOUNT_INVALID  = "El monto es inválido";
    public static final String AMOUNT_DECIMALS = "El monto no debe tener más de 2 decimales";
    public static final String AMOUNT_RANGE    = "El monto debe estar en el rango (0, 15000000]";

    // =========================
    // Plazo
    // =========================
    public static final String TERM_REQUIRED = "El plazo en meses es obligatorio";
    public static final String TERM_POSITIVE = "El plazo en meses debe ser mayor a 0";

    // =========================
    // Tipo de préstamo
    // =========================
    public static final String LOAN_TYPE_REQUIRED = "El tipo de préstamo es obligatorio";
    public static final String LOAN_TYPE_NO_EXIST = "El tipo de préstamo no existe";

    // =========================
    // Estado / Solicitud
    // =========================
    public static final String ID_REQUIRED_APPLICATION = "Se debe ingresar el identificador de la solicitud que desea modificar";
    public static final String ID_REQUIRED_STATE = "Se debe ingresar el identificador del estado que desea asignar a la solicitud";
    public static final String INVALID_STATE = "El estado debe ser 'Aprobado' o 'Rechazado'";

    // REST consumer
    public static final String DATA_USER_NOT_EXIST = "Los datos ingresados del usuario no coinciden con los registrados en el sistema.";

    // Solicitud
    public static final String APPLICATION_CREATED = "Solicitud creada satisfactoriamente";
    public static final String APPLICATION_UPDATE_ERROR = "Error al actualizar la solicitud";
    public static final String ID_REQUIRED_APPLICATION_AND_ID_REQUIRED_STATE = "El identificador del estado y de la solicitud son obligatorios";
    public static final String APPLICATION_UPDATED = "Solicitud actualizada correctamente";
    public static final String INVALID_IDENTIFICADOR_APPLICATION = "El identificador de la solicitud no es válido";

    // =========================
    // Validación de usuario
    // =========================
    public static final String USER_INVALID =
            "Los datos ingresados no corresponden a un usuario registrado en el sistema. " +
                    "Por favor, intenta nuevamente o ponte en contacto con un administrador.";

    // =========================
    // Helpers con parámetros
    // =========================
    public static String amountNotAllowed(BigDecimal amountMin, BigDecimal amountMax) {
        return "El monto debe estar entre " + amountMin + " y " + amountMax;
    }

    public static String stateNotFound(String code) {
        return "El estado inicial '" + code + "' no existe";
    }

    // =========================
    // Evento / Reporting
    // =========================

    public static final String NOT_FOUND_ENVENT= "Error serializando CloudEvent";

}
