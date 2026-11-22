package co.edu.uniquindio.proyecto.rest;

import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.excepciones.NoEncontradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * Convierte las excepciones de los controladores REST en respuestas JSON {@code { "mensaje": "..." }}
 * con el código HTTP adecuado.
 * <p>
 * No se limita a un paquete: los errores que ocurren antes de elegir el controlador (por ejemplo un
 * método HTTP no permitido, 405) solo llegan a un advice global. No interfiere con la aplicación JSF,
 * porque las páginas las atiende el {@code FacesServlet} y no el {@code DispatcherServlet} de Spring MVC,
 * donde solo están la API REST y la redirección de {@code /}.
 */
@RestControllerAdvice
public class ManejadorErroresRest {

    private static final Logger LOG = LoggerFactory.getLogger(ManejadorErroresRest.class);

    private static ResponseEntity<Mensaje> responder(HttpStatus estado, String texto) {
        return ResponseEntity.status(estado).body(new Mensaje(texto));
    }

    /** El recurso pedido no existe: 404 con el mensaje de la excepción. */
    @ExceptionHandler(NoEncontradoException.class)
    public ResponseEntity<Mensaje> noEncontrado(NoEncontradoException e) {
        return responder(HttpStatus.NOT_FOUND, e.getMessage());
    }

    /** Regla de negocio incumplida o dato inválido: 400 con el mensaje de la excepción. */
    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<Mensaje> negocio(NegocioException e) {
        return responder(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    /** Falla la validación del cuerpo ({@code @Valid}): 400 con los errores de campo unidos por "; ". */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Mensaje> cuerpoInvalido(MethodArgumentNotValidException e) {
        String texto = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        if (texto.isEmpty()) {
            texto = "Los datos enviados no son válidos";
        }
        return responder(HttpStatus.BAD_REQUEST, texto);
    }

    /** Falla la validación de parámetros o variables de ruta: 400 con las violaciones unidas por "; ". */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Mensaje> violaciones(ConstraintViolationException e) {
        String texto = e.getConstraintViolations().stream()
                .map(v -> {
                    String ruta = v.getPropertyPath().toString();
                    String campo = ruta.contains(".") ? ruta.substring(ruta.lastIndexOf('.') + 1) : ruta;
                    return campo + ": " + v.getMessage();
                })
                .sorted()
                .collect(Collectors.joining("; "));
        return responder(HttpStatus.BAD_REQUEST, texto.isEmpty() ? "Los datos enviados no son válidos" : texto);
    }

    /** JSON mal formado o con valores ilegibles (por ejemplo una fecha con formato inválido): 400. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Mensaje> cuerpoIlegible(HttpMessageNotReadableException e) {
        return responder(HttpStatus.BAD_REQUEST, "El cuerpo de la petición no es válido");
    }

    /** Un parámetro o variable de ruta tiene un tipo incorrecto: 400 indicando cuál. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Mensaje> tipoIncorrecto(MethodArgumentTypeMismatchException e) {
        return responder(HttpStatus.BAD_REQUEST, "El parámetro '" + e.getName() + "' no tiene un valor válido");
    }

    /** Falta un parámetro obligatorio de la consulta: 400 indicando cuál. */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Mensaje> parametroFaltante(MissingServletRequestParameterException e) {
        return responder(HttpStatus.BAD_REQUEST, "El parámetro '" + e.getParameterName() + "' es obligatorio");
    }

    /** El método HTTP no está permitido en esa ruta: 405. */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Mensaje> metodoNoPermitido(HttpRequestMethodNotSupportedException e) {
        return responder(HttpStatus.METHOD_NOT_ALLOWED, "Método " + e.getMethod() + " no permitido en esta ruta");
    }

    /** Cualquier otro error: 500 con un mensaje genérico; el detalle solo se registra en el log. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Mensaje> inesperado(Exception e) {
        LOG.error("Error inesperado en la API REST", e);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado");
    }
}
