package co.edu.uniquindio.proyecto.bean;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Construcción de los enlaces de compartir y de los datos de la vista previa (métodos estáticos, sin JSF). */
class DetalleProductoBeanCompartirTest {

    private static final String BASE = "http://localhost:8080";
    private static final String URL_ES_PRODUCTO = "http%3A%2F%2Flocalhost%3A8080%2FdetalleProducto.xhtml%3Fproducto%3D7";

    @Test
    void facebookCodificaLaUrlDelProducto() {
        assertEquals("https://www.facebook.com/sharer/sharer.php?u=" + URL_ES_PRODUCTO,
                DetalleProductoBean.urlFacebook(BASE, 7L));
    }

    @Test
    void twitterIncluyeUrlYTextoConNombre() {
        assertEquals("https://twitter.com/intent/tweet?url=" + URL_ES_PRODUCTO
                        + "&text=Mira%20%C2%AB%C3%81rbol%C2%BB%20en%20UniShop",
                DetalleProductoBean.urlTwitter(BASE, 7L, "Árbol"));
    }

    @Test
    void losEspaciosSeCodificanComoPorcentaje20() {
        String url = DetalleProductoBean.urlTwitter(BASE, 7L, "Silla de oficina");
        assertTrue(url.contains("Silla%20de%20oficina"));
        assertFalse(url.contains("+"));
    }

    @Test
    void acentosYEnieSeCodificanEnUtf8() {
        assertEquals("Caf%C3%A9%20ni%C3%B1o", DetalleProductoBean.codificar("Café niño"));
    }

    @Test
    void caracteresReservadosYComillasNoRompenLaUrl() {
        String url = DetalleProductoBean.urlTwitter(BASE, 7L, "A&B?C#D \"x\" 'y'");
        String texto = url.substring(url.indexOf("&text=") + "&text=".length());
        assertEquals("Mira%20%C2%ABA%26B%3FC%23D%20%22x%22%20%27y%27%C2%BB%20en%20UniShop", texto);
        // Solo hay un "&" separador y ningún "#"
        assertEquals(1, url.chars().filter(c -> c == '&').count());
        assertFalse(url.contains("#"));
    }

    @Test
    void elSignoMasDelNombreSeCodificaYNoSeConfundeConEspacio() {
        assertEquals("C%2B%2B%20y%20Java", DetalleProductoBean.codificar("C++ y Java"));
    }

    @Test
    void laBarraFinalDeLaUrlBaseSeQuita() {
        assertEquals("http://localhost:8080/detalleProducto.xhtml?producto=7",
                DetalleProductoBean.urlProducto("http://localhost:8080/", 7L));
        assertEquals("https://unishop.edu.co/detalleProducto.xhtml?producto=7",
                DetalleProductoBean.urlProducto("https://unishop.edu.co//", 7L));
        assertEquals(DetalleProductoBean.urlFacebook(BASE, 7L), DetalleProductoBean.urlFacebook(BASE + "/", 7L));
    }

    @Test
    void laImagenSeArmaConUrlAbsoluta() {
        assertEquals("http://localhost:8080/uploads/a.jpg", DetalleProductoBean.urlImagen(BASE + "/", "uploads/a.jpg"));
        assertEquals("http://localhost:8080/uploads/a.jpg", DetalleProductoBean.urlImagen(BASE, "/uploads/a.jpg"));
    }

    @Test
    void laDescripcionSeRecortaYSeEnderezaEnUnaLinea() {
        assertEquals("", DetalleProductoBean.recortar(null, 150));
        assertEquals("hola mundo", DetalleProductoBean.recortar("  hola \n  mundo ", 150));
        String largo = String.join("", java.util.Collections.nCopies(400, "a"));
        String r = DetalleProductoBean.recortar(largo, 150);
        assertEquals(150, r.length());
        assertTrue(r.endsWith("…"));
    }

    @Test
    void sinProductoLosEnlacesSonNull() {
        DetalleProductoBean bean = new DetalleProductoBean(null, null, null, null, null, null, null, null, BASE);
        assertEquals(null, bean.getUrlCompartirFacebook());
        assertEquals(null, bean.getUrlCompartirTwitter());
        assertEquals(null, bean.getUrlProducto());
        assertEquals(null, bean.getDescripcionMeta());
        assertEquals(null, bean.getUrlImagenMeta());
    }
}
