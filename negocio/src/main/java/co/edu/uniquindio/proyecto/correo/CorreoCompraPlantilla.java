package co.edu.uniquindio.proyecto.correo;

import co.edu.uniquindio.proyecto.entidades.Compra;
import co.edu.uniquindio.proyecto.entidades.DetalleCompra;
import co.edu.uniquindio.proyecto.entidades.MedioPago;
import co.edu.uniquindio.proyecto.servicios.ProductoServicio;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Arma el correo HTML con el detalle de una compra. Todo dato que viene del usuario (nombres, cupón) se escapa
 * con {@link HtmlUtils#htmlEscape(String)}. Se usa dentro de la transacción de la compra, cuando ya existen la
 * compra, sus detalles y el cupón.
 */
@Component
public class CorreoCompraPlantilla {

    private static final String ZONA = "America/Bogota";
    private static final String TD = "style=\"padding:8px;border-bottom:1px solid #e5e5e5;\"";
    private static final String TD_DER = "style=\"padding:8px;border-bottom:1px solid #e5e5e5;text-align:right;\"";

    private final ProductoServicio productoServicio;

    public CorreoCompraPlantilla(ProductoServicio productoServicio) {
        this.productoServicio = productoServicio;
    }

    /**
     * Arma el correo de una compra.
     *
     * @param compra   compra ya guardada (con usuario y, si hubo, cupón)
     * @param detalles detalles de la compra
     * @param total    total de la compra (el mismo que muestra la web, {@code calcularTotal})
     * @param subasta  {@code true} si la compra viene de una subasta ganada
     * @return correo listo para publicar
     */
    public CorreoPendienteEvent armar(Compra compra, List<DetalleCompra> detalles, double total, boolean subasta) {
        String asunto = "Tu compra #" + compra.getId() + " en UniShop";
        StringBuilder h = new StringBuilder(2048);
        h.append("<div style=\"font-family:Arial,Helvetica,sans-serif;color:#333;max-width:640px;margin:auto;\">");
        h.append("<h2 style=\"color:#1a73e8;\">UniShop</h2>");
        h.append("<p>Hola ").append(esc(compra.getUsuario().getNombre())).append(", gracias por tu compra.</p>");
        if (subasta) {
            h.append("<p style=\"background:#fff8e1;padding:10px;border-radius:4px;\">")
                    .append("Esta compra se generó porque <b>ganaste la subasta</b> de este producto.</p>");
        }
        h.append("<p><b>Compra N.&ordm;:</b> ").append(compra.getId()).append("<br>")
                .append("<b>Fecha:</b> ").append(formatearFecha(compra)).append("<br>")
                .append("<b>Medio de pago:</b> ").append(esc(etiqueta(compra.getMedioPago()))).append("</p>");

        h.append("<table style=\"width:100%;border-collapse:collapse;font-size:14px;\">")
                .append("<tr style=\"background:#f1f3f4;text-align:left;\">")
                .append("<th style=\"padding:8px;\">Producto</th><th style=\"padding:8px;\">Unidades</th>")
                .append("<th style=\"padding:8px;text-align:right;\">Precio unitario</th>")
                .append("<th style=\"padding:8px;text-align:right;\">Subtotal</th></tr>");
        BigDecimal sinCupon = BigDecimal.ZERO;
        for (DetalleCompra d : detalles) {
            BigDecimal precio = BigDecimal.valueOf(d.getPrecioProducto());
            BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(d.getUnidades()));
            h.append("<tr><td ").append(TD).append(">").append(esc(d.getProducto().getNombre())).append("</td>")
                    .append("<td ").append(TD).append(">").append(d.getUnidades()).append("</td>")
                    .append("<td ").append(TD_DER).append(">").append(moneda(precio)).append("</td>")
                    .append("<td ").append(TD_DER).append(">").append(moneda(subtotal)).append("</td></tr>");
            // Subtotal antes del cupón: precio del producto con su descuento propio
            sinCupon = sinCupon.add(BigDecimal.valueOf(productoServicio.calcularPrecioConDescuento(d.getProducto()))
                    .multiply(BigDecimal.valueOf(d.getUnidades())));
        }
        h.append("</table>");

        BigDecimal totalPagado = BigDecimal.valueOf(total);
        h.append("<p style=\"text-align:right;font-size:14px;\">");
        if (compra.getCupon() != null) {
            BigDecimal descuento = sinCupon.subtract(totalPagado).max(BigDecimal.ZERO);
            h.append("Subtotal: ").append(moneda(sinCupon)).append("<br>")
                    .append("Cupón ").append(esc(compra.getCupon().getCodigo())).append(": -")
                    .append(moneda(descuento)).append("<br>");
        }
        h.append("<span style=\"font-size:18px;\"><b>Total: ").append(moneda(totalPagado)).append("</b></span></p>");
        h.append("<p style=\"color:#777;font-size:12px;\">Este correo es informativo, por favor no lo respondas.</p>");
        h.append("</div>");
        return new CorreoPendienteEvent(compra.getUsuario().getEmail(), asunto, h.toString());
    }

    private String etiqueta(MedioPago medio) {
        return medio == null ? "-" : medio.getEtiqueta();
    }

    private String formatearFecha(Compra compra) {
        if (compra.getFechaCompra() == null) {
            return "-";
        }
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        formato.setTimeZone(TimeZone.getTimeZone(ZONA));
        return formato.format(compra.getFechaCompra());
    }

    /** Formato colombiano: {@code $ 1.234.567}; con decimales solo si no son ,00 ({@code $ 1.234,50}). */
    static String moneda(BigDecimal valor) {
        BigDecimal v = valor.setScale(2, RoundingMode.HALF_UP);
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(new Locale("es", "CO"));
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');
        boolean entero = v.stripTrailingZeros().scale() <= 0;
        return "$ " + new DecimalFormat(entero ? "#,##0" : "#,##0.00", simbolos).format(v);
    }

    private String esc(String texto) {
        return HtmlUtils.htmlEscape(texto == null ? "" : texto);
    }
}
