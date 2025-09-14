package com.crediya.loan.usecase.shared;
import java.util.Locale;

public final class StatusChangeConstants {

    private StatusChangeConstants() {}

    // Formato de RequestId
    public static final String REQUEST_ID_PATTERN = "SOL-%d-%06d";

    // Estados normalizados
    public static final String STATUS_APROBADO   = "APROBADO";
    public static final String STATUS_RECHAZADO  = "RECHAZADO";

    // Mensajes custom por estado
    public static final String MSG_ON_APROBADO   = "Su desembolso estará disponible en las próximas 24 horas.";
    public static final String MSG_ON_RECHAZADO  = "Su solicitud fue rechazada. Puede volver a aplicar en 30 días.";
    public static final String MSG_ON_UPDATED    = "El estado de su solicitud ha sido actualizado.";

    // Errores y logs
    public static final String ERR_SERIALIZING_PAYLOAD = "Error serializando payload";

    // Atributos SQS
    public static final String ATTR_CORRELATION_ID     = "X-Correlation-Id";
    public static final String ATTR_DATA_TYPE_STRING   = "String";

    // Locale para normalización de estado
    public static final Locale NORMALIZE_LOCALE       = Locale.ROOT;
}