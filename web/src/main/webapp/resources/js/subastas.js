/* Cuenta regresiva de las subastas. Cada elemento .cuenta-regresiva trae data-restante (milisegundos que
   faltaban cuando el servidor pintó la página). Se calcula el instante de fin con el reloj del navegador la
   primera vez que se ve el elemento, así no depende de que el reloj del cliente esté bien ajustado. */
(function () {
    function dos(n) { return n < 10 ? '0' + n : '' + n; }

    function formato(ms) {
        var s = Math.floor(ms / 1000);
        var d = Math.floor(s / 86400);
        var h = Math.floor((s % 86400) / 3600);
        var m = Math.floor((s % 3600) / 60);
        var seg = s % 60;
        return (d > 0 ? d + ' d ' : '') + (d > 0 || h > 0 ? h + ' h ' : '') + dos(m) + ' min ' + dos(seg) + ' s';
    }

    function actualizar() {
        var elementos = document.querySelectorAll('.cuenta-regresiva');
        for (var i = 0; i < elementos.length; i++) {
            var el = elementos[i];
            if (!el.dataset.fin) {
                el.dataset.fin = String(Date.now() + parseInt(el.dataset.restante, 10));
            }
            var restante = parseInt(el.dataset.fin, 10) - Date.now();
            el.textContent = restante > 0 ? formato(restante) : 'Terminada';
        }
    }

    actualizar();
    setInterval(actualizar, 1000);
})();
