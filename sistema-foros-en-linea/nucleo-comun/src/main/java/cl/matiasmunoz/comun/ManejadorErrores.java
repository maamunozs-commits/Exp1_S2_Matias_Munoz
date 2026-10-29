package cl.matiasmunoz.comun;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class ManejadorErrores {
    public record RespuestaError(
            Instant fecha, int estado, String mensaje, String ruta, List<String> detalles) {}

    @ExceptionHandler(ExcepcionNegocio.class)
    public ResponseEntity<RespuestaError> manejarNegocio(
            ExcepcionNegocio error, HttpServletRequest solicitud) {
        return responder(error.obtenerEstado().value(), error.getMessage(), solicitud, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespuestaError> manejarValidacion(
            MethodArgumentNotValidException error, HttpServletRequest solicitud) {
        var detalles =
                error.getBindingResult().getFieldErrors().stream()
                        .map(campo -> campo.getField() + ": " + campo.getDefaultMessage())
                        .toList();
        return responder(400, "La solicitud contiene datos inválidos", solicitud, detalles);
    }

    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class,
        ConstraintViolationException.class
    })
    public ResponseEntity<RespuestaError> manejarFormato(
            Exception error, HttpServletRequest solicitud) {
        return responder(400, "El formato de la solicitud es inválido", solicitud, List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<RespuestaError> manejarIntegridad(
            DataIntegrityViolationException error, HttpServletRequest solicitud) {
        return responder(
                409,
                "La operación entra en conflicto con los registros existentes",
                solicitud,
                List.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<RespuestaError> manejarPermiso(
            AccessDeniedException error, HttpServletRequest solicitud) {
        return responder(
                403, "No dispone de permisos para realizar esta operación", solicitud, List.of());
    }

    private ResponseEntity<RespuestaError> responder(
            int estado, String mensaje, HttpServletRequest solicitud, List<String> detalles) {
        return ResponseEntity.status(estado)
                .body(
                        new RespuestaError(
                                Instant.now(),
                                estado,
                                mensaje,
                                solicitud.getRequestURI(),
                                detalles));
    }
}
