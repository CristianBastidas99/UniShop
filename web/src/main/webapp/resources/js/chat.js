/*
 * Chat de UniShop (usuario/chat.xhtml). Se conecta por SockJS + STOMP a <contexto>/ws, se suscribe a
 * /topic/chat/{id} y /user/queue/errores, y envía a /app/chat/{id}. El texto de los mensajes se inserta
 * siempre con textContent (nunca innerHTML) para que no se pueda inyectar HTML.
 */
(function () {
    'use strict';

    var MAX_CARACTERES = 1000;
    var ESPERA_INICIAL = 1000;
    var ESPERA_MAXIMA = 30000;

    function iniciar() {
        var raiz = document.getElementById('chat');
        if (!raiz || typeof SockJS === 'undefined' || typeof Stomp === 'undefined') {
            if (raiz) {
                var e = document.getElementById('chat-estado');
                if (e) { e.textContent = 'No se pudo cargar el chat en tiempo real. Recarga la página.'; }
            }
            return;
        }

        var idChat = raiz.getAttribute('data-chat');
        var idUsuario = String(raiz.getAttribute('data-usuario'));
        var contexto = raiz.getAttribute('data-contexto') || '';

        var zonaMensajes = document.getElementById('chat-mensajes');
        var texto = document.getElementById('chat-texto');
        var boton = document.getElementById('chat-enviar');
        var contador = document.getElementById('chat-contador');
        var estado = document.getElementById('chat-estado');
        var zonaError = document.getElementById('chat-error');

        var cliente = null;
        var conectado = false;
        var intentos = 0;
        var temporizador = null;
        var cerrando = false;
        var ultimoEnviado = '';
        var temporizadorError = null;

        // Ids de los mensajes que ya están en pantalla (los del historial y los que llegan por el socket)
        var vistos = {};
        var existentes = zonaMensajes.querySelectorAll('[data-id]');
        for (var i = 0; i < existentes.length; i++) {
            vistos[existentes[i].getAttribute('data-id')] = true;
        }

        function alFinal() {
            zonaMensajes.scrollTop = zonaMensajes.scrollHeight;
        }

        function cercaDelFinal() {
            return zonaMensajes.scrollHeight - zonaMensajes.scrollTop - zonaMensajes.clientHeight < 80;
        }

        function mostrarEstado() {
            estado.className = 'chat-estado ' + (conectado ? 'chat-estado-conectado' : 'chat-estado-conectando');
            estado.textContent = conectado ? 'Conectado' : (intentos === 0 ? 'Conectando…' : 'Reconectando…');
            actualizarBoton();
        }

        function actualizarBoton() {
            boton.disabled = !conectado || texto.value.trim().length === 0;
        }

        function actualizarContador() {
            contador.textContent = String(MAX_CARACTERES - texto.value.length);
            actualizarBoton();
        }

        function mostrarError(mensaje) {
            zonaError.textContent = mensaje;
            zonaError.hidden = false;
            clearTimeout(temporizadorError);
            temporizadorError = setTimeout(function () { zonaError.hidden = true; }, 8000);
        }

        function ocultarError() {
            zonaError.hidden = true;
            zonaError.textContent = '';
        }

        function agregarMensaje(m) {
            if (m == null || m.id == null || vistos[String(m.id)]) {
                return;
            }
            vistos[String(m.id)] = true;
            var propio = String(m.idRemitente) === idUsuario;
            var vacio = zonaMensajes.querySelector('.chat-vacio');
            if (vacio) {
                vacio.parentNode.removeChild(vacio);
            }

            var fila = document.createElement('div');
            fila.className = 'chat-fila ' + (propio ? 'chat-propio' : 'chat-ajeno');
            fila.setAttribute('data-id', String(m.id));
            var burbuja = document.createElement('div');
            burbuja.className = 'chat-burbuja';
            var cuerpo = document.createElement('div');
            cuerpo.className = 'chat-texto';
            cuerpo.textContent = m.texto;
            var meta = document.createElement('div');
            meta.className = 'chat-meta';
            meta.textContent = (propio ? '' : (m.nombreRemitente || '') + ' · ') + (m.fechaTexto || '');
            burbuja.appendChild(cuerpo);
            burbuja.appendChild(meta);
            fila.appendChild(burbuja);

            var seguir = propio || cercaDelFinal();
            zonaMensajes.appendChild(fila);
            if (seguir) {
                alFinal();
            }
        }

        function leerJson(frame) {
            try {
                return JSON.parse(frame.body);
            } catch (err) {
                return null;
            }
        }

        function programarReconexion() {
            if (cerrando || temporizador !== null) {
                return;
            }
            var espera = Math.min(ESPERA_INICIAL * Math.pow(2, intentos), ESPERA_MAXIMA);
            intentos++;
            mostrarEstado();
            temporizador = setTimeout(function () {
                temporizador = null;
                conectar();
            }, espera);
        }

        function conectar() {
            var socket = new SockJS(contexto + '/ws');
            var stomp = Stomp.over(socket);
            stomp.debug = null; // sin registro de depuración en la consola
            cliente = stomp;
            stomp.connect({}, function () {
                if (cliente !== stomp) {
                    return;
                }
                intentos = 0;
                conectado = true;
                stomp.subscribe('/topic/chat/' + idChat, function (frame) {
                    agregarMensaje(leerJson(frame));
                });
                stomp.subscribe('/user/queue/errores', function (frame) {
                    var error = leerJson(frame);
                    mostrarError(error && error.mensaje ? error.mensaje : 'No se pudo enviar el mensaje');
                    if (texto.value === '' && ultimoEnviado !== '') {
                        texto.value = ultimoEnviado; // se devuelve el texto para no perderlo
                        actualizarContador();
                    }
                });
                mostrarEstado();
            }, function () {
                if (cliente !== stomp) {
                    return;
                }
                conectado = false;
                programarReconexion();
            });
        }

        function enviar() {
            var contenido = texto.value.trim();
            if (!conectado || contenido.length === 0) {
                return;
            }
            if (contenido.length > MAX_CARACTERES) {
                mostrarError('El mensaje no puede superar los ' + MAX_CARACTERES + ' caracteres');
                return;
            }
            ocultarError();
            ultimoEnviado = texto.value;
            try {
                cliente.send('/app/chat/' + idChat, {}, JSON.stringify({ texto: contenido }));
            } catch (err) {
                conectado = false;
                mostrarError('No se pudo enviar el mensaje: se perdió la conexión');
                programarReconexion();
                return;
            }
            texto.value = '';
            actualizarContador();
            texto.focus();
        }

        texto.addEventListener('input', actualizarContador);
        texto.addEventListener('keydown', function (ev) {
            // Enter envía; Shift+Enter deja el salto de línea (y no se envía mientras se compone con un IME)
            if (ev.key === 'Enter' && !ev.shiftKey && !ev.isComposing) {
                ev.preventDefault();
                enviar();
            }
        });
        boton.addEventListener('click', enviar);
        window.addEventListener('beforeunload', function () {
            cerrando = true;
            try {
                if (cliente && conectado) { cliente.disconnect(); }
            } catch (err) { /* se está cerrando la página */ }
        });

        alFinal();
        actualizarContador();
        mostrarEstado();
        conectar();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', iniciar);
    } else {
        iniciar();
    }
})();
