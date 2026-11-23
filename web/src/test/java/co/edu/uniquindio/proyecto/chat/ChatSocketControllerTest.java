package co.edu.uniquindio.proyecto.chat;

import co.edu.uniquindio.proyecto.config.UsuarioPrincipal;
import co.edu.uniquindio.proyecto.entidades.Chat;
import co.edu.uniquindio.proyecto.entidades.Mensaje;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.MensajeServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/** Pruebas unitarias del controlador del chat: sin base de datos ni contexto de Spring. */
class ChatSocketControllerTest {

    private MensajeServicio mensajeServicio;
    private SimpMessagingTemplate plantilla;
    private ChatSocketController controlador;

    @BeforeEach
    void preparar() {
        mensajeServicio = mock(MensajeServicio.class);
        plantilla = mock(SimpMessagingTemplate.class);
        controlador = new ChatSocketController(mensajeServicio, plantilla);
    }

    private MensajeEntrada entrada(String texto) {
        MensajeEntrada e = new MensajeEntrada();
        e.setTexto(texto);
        return e;
    }

    @Test
    void publicaElDtoEnElTopicDelChatConElRemitenteDelPrincipal() throws Exception {
        Usuario remitente = new Usuario();
        remitente.setId(7L);
        remitente.setNombre("Laura Martínez");
        Chat chat = new Chat();
        chat.setId(45L);
        Mensaje guardado = new Mensaje();
        guardado.setId(123L);
        guardado.setMensaje("Hola");
        // 2026-11-23T00:45:00Z = 22/11/2026 19:45 en Bogotá (UTC-5)
        guardado.setFecha(new Date(1795394700000L));
        guardado.setChat(chat);
        guardado.setRemitente(remitente);
        when(mensajeServicio.enviarMensaje(45L, 7L, "Hola")).thenReturn(guardado);

        controlador.enviar(45L, entrada("Hola"), new UsuarioPrincipal(7L));

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(plantilla).convertAndSend(eq("/topic/chat/45"), captor.capture());
        MensajeChatDTO dto = (MensajeChatDTO) captor.getValue();
        assertEquals(123L, dto.getId());
        assertEquals(45L, dto.getIdChat());
        assertEquals("Hola", dto.getTexto());
        assertEquals(1795394700000L, dto.getFecha());
        assertEquals("22/11/2026 19:45", dto.getFechaTexto());
        assertEquals(7L, dto.getIdRemitente());
        assertEquals("Laura Martínez", dto.getNombreRemitente());
    }

    @Test
    void siElServicioFallaNoSePublicaNada() throws Exception {
        when(mensajeServicio.enviarMensaje(anyLong(), anyLong(), any())).thenThrow(new NegocioException("No participa"));
        assertThrows(NegocioException.class,
                () -> controlador.enviar(45L, entrada("Hola"), new UsuarioPrincipal(7L)));
        verify(plantilla, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    void sinCuerpoSeLlamaAlServicioConTextoNulo() throws Exception {
        when(mensajeServicio.enviarMensaje(45L, 7L, null)).thenThrow(new NegocioException("El mensaje es obligatorio"));
        assertThrows(NegocioException.class, () -> controlador.enviar(45L, null, new UsuarioPrincipal(7L)));
    }

    @Test
    void errorDeNegocioVaAErroresConSuMensaje() {
        assertEquals("No participa", controlador.manejarNegocio(new NegocioException("No participa")).getMensaje());
    }

    @Test
    void errorInesperadoRespondeMensajeGenerico() {
        assertEquals("No se pudo enviar el mensaje", controlador.manejarInesperado(new RuntimeException("x")).getMensaje());
    }
}
