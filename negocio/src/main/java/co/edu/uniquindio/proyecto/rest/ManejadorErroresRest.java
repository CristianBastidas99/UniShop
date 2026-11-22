package co.edu.uniquindio.proyecto.rest;

import co.edu.uniquindio.proyecto.excepciones.AccesoDenegadoException;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.excepciones.NoEncontradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.validation.BindException;

import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * Convierte las excepciones de la API REST en respuestas JSON {@code { "mensaje": "..." }} con el código HTTP adecuado.
 * <p>
 * Extiende {@link ResponseEntityExceptionHandler}, que ya da el código correcto a todas las excepciones estándar de
 * Spring MVC (400, 405, 406, 415...). Aquí solo se cambia el cuerpo, en {@link #handleExceptionInternal}. No se
 * declaran {@code @ExceptionHandler} para esos tipos: si un tipo queda mapeado dos veces, la aplicación no arranca.
 * <p>
 * No se limita a un paquete: los errores que ocurren antes de elegir el controlador (por ejemplo un método HTTP no
 * permitido, 405) solo llegan a un advice global. No interfiere con la aplicación JSF, porque las páginas las atiende
 * el {@code FacesServlet} y no el {@code DispatcherServlet} de Spring MVC, donde solo están la API REST y la
 * redirección de {@code /}. Las rutas que no existen las cubre {@link ErroresApiAtributos}.
 */
@RestControllerAdvice
public class ManejadorErroresRest extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ManejadorErroresRest.class);

    static final String MENSAJE_INESPERADO = "Ocurrió un error inesperado";

    /**
     * Respuesta de error en JSON. El {@code Content-Type} se fija para que Spring no lo negocie con el
     * {@code Accept} del cliente: si el cliente pide XML y ocurre un error, el error igual sale en JSON
     * (sin esto el handler no podría escribirlo y la excepción original terminaría en un 500).
     */
    private static ResponseEntity<Object> responder(HttpStatus estado, String texto) {
        return ResponseEntity.status(estado).contentType(MediaType.APPLICATION_JSON).body(new Mensaje(texto));
    }

    /** El recurso pedido no existe: 404 con el mensaje de la excepción. */
    @ExceptionHandler(NoEncontradoException.class)
    public ResponseEntity<Object> noEncontrado(NoEncontradoException e) {
        return responder(HttpStatus.NOT_FOUND, e.getMessage());
    }

    /** Quien hace la acción no tiene permiso sobre el recurso (por ejemplo, no es el dueño): 403. */
    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<Object> accesoDenegado(AccesoDenegadoException e) {
        return responder(HttpStatus.FORBIDDEN, e.getMessage());
    }

    /** Regla de negocio incumplida o dato inválido: 400 con el mensaje de la excepción. */
    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<Object> negocio(NegocioException e) {
        return responder(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    /** Falla la validación de parámetros o variables de ruta: 400 con las violaciones unidas por "; ". */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> violaciones(ConstraintViolationException e) {
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

    /** Excepción con un código HTTP propio: ese código y su motivo. */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> conEstado(ResponseStatusException e) {
        String motivo = e.getReason() != null ? e.getReason() : textoGenerico(e.getStatus());
        return responder(e.getStatus(), motivo);
    }

    /** Cualquier otro error: 500 con un mensaje genérico; el detalle solo se registra en el log. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> inesperado(Exception e) {
        LOG.error("Error inesperado en la API REST", e);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, MENSAJE_INESPERADO);
    }

    /**
     * Punto común de las excepciones estándar de Spring MVC: conserva el código y las cabeceras que decide
     * {@link ResponseEntityExceptionHandler} (por ejemplo {@code Allow} en el 405) y pone el cuerpo
     * {@code { "mensaje" }}. El 406 va sin cuerpo: el cliente no acepta JSON y enviarlo volvería a fallar.
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatus status, WebRequest request) {
        if (status == HttpStatus.NOT_ACCEPTABLE) {
            return new ResponseEntity<>(headers, status);
        }
        if (status.is5xxServerError()) {
            LOG.error("Error inesperado en la API REST", ex);
        }
        HttpHeaders cabeceras = new HttpHeaders();
        cabeceras.putAll(headers);
        cabeceras.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(new Mensaje(mensajePara(ex, status)), cabeceras, status);
    }

    /** Texto en español para cada excepción estándar de Spring MVC. */
    private static String mensajePara(Exception ex, HttpStatus status) {
        if (ex instanceof BindException) {
            // Incluye MethodArgumentNotValidException (falla @Valid en el cuerpo)
            String texto = ((BindException) ex).getBindingResult().getFieldErrors().stream()
                    .map(f -> f.getField() + ": " + f.getDefaultMessage())
                    .sorted()
                    .collect(Collectors.joining("; "));
            return texto.isEmpty() ? "Los datos enviados no son válidos" : texto;
        }
        if (ex instanceof HttpMessageNotReadableException) {
            return "El cuerpo de la petición no es válido";
        }
        if (ex instanceof MethodArgumentTypeMismatchException) {
            return "El parámetro '" + ((MethodArgumentTypeMismatchException) ex).getName() + "' no tiene un valor válido";
        }
        if (ex instanceof TypeMismatchException) {
            return "El parámetro '" + ((TypeMismatchException) ex).getPropertyName() + "' no tiene un valor válido";
        }
        if (ex instanceof MissingServletRequestParameterException) {
            return "El parámetro '" + ((MissingServletRequestParameterException) ex).getParameterName()
                    + "' es obligatorio";
        }
        if (ex instanceof HttpRequestMethodNotSupportedException) {
            return "Método " + ((HttpRequestMethodNotSupportedException) ex).getMethod() + " no permitido en esta ruta";
        }
        if (ex instanceof HttpMediaTypeNotSupportedException) {
            return "Tipo de contenido no soportado; envía el cuerpo como application/json";
        }
        if (ex instanceof NoHandlerFoundException) {
            return "La ruta " + ((NoHandlerFoundException) ex).getRequestURL() + " no existe";
        }
        return textoGenerico(status);
    }

    /** Mensaje genérico según el tipo de código (5xx o 4xx). */
    static String textoGenerico(HttpStatus status) {
        return status.is5xxServerError() ? MENSAJE_INESPERADO : "La petición no es válida";
    }
}
