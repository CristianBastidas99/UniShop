/*
 * Ajustes de Chart.js para los gráficos de admin/reportes.xhtml (atributo "extender" de PrimeFaces).
 * PrimeFaces llama a la función con el widget como "this"; la configuración de Chart.js está en this.cfg.config.
 */
(function () {
    var formatoMoneda = new Intl.NumberFormat('es-CO', {maximumFractionDigits: 0});

    function opciones(widget) {
        var config = widget.cfg.config;
        config.options = config.options || {};
        return config.options;
    }

    /** Ocupa toda la altura del contenedor (.reporte-grafico) en vez de mantener la proporción del lienzo. */
    window.reporteAjustarTamano = function () {
        var o = opciones(this);
        o.responsive = true;
        o.maintainAspectRatio = false;
    };

    /**
     * Barras horizontales: igual que reporteAjustarTamano y además recorta con "…" los nombres largos del eje
     * para que no se corten en el celular. El nombre completo sigue en el tooltip y en la tabla.
     */
    window.reporteBarrasHorizontales = function () {
        window.reporteAjustarTamano.call(this);
        var o = opciones(this);
        var maximo = window.innerWidth < 600 ? 14 : 24;
        o.scales = o.scales || {};
        o.scales.yAxes = o.scales.yAxes && o.scales.yAxes.length ? o.scales.yAxes : [{}];
        o.scales.yAxes.forEach(function (eje) {
            eje.ticks = eje.ticks || {};
            eje.ticks.callback = function (valor) {
                var texto = String(valor);
                return texto.length > maximo ? texto.substring(0, maximo - 1) + '…' : texto;
            };
        });
    };

    /** Igual que reporteAjustarTamano y además muestra el eje Y y los tooltips como dinero ($ 1.234.567). */
    window.reporteMoneda = function () {
        window.reporteAjustarTamano.call(this);
        var o = opciones(this);
        o.scales = o.scales || {};
        o.scales.yAxes = o.scales.yAxes && o.scales.yAxes.length ? o.scales.yAxes : [{}];
        o.scales.yAxes.forEach(function (eje) {
            eje.ticks = eje.ticks || {};
            eje.ticks.callback = function (valor) {
                return '$ ' + formatoMoneda.format(valor);
            };
        });
        o.tooltips = o.tooltips || {};
        o.tooltips.callbacks = o.tooltips.callbacks || {};
        o.tooltips.callbacks.label = function (item) {
            return 'Ventas: $ ' + formatoMoneda.format(item.yLabel);
        };
    };
})();
