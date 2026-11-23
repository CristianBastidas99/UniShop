package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.dto.CantidadDTO;
import co.edu.uniquindio.proyecto.dto.MedioPagoDTO;
import co.edu.uniquindio.proyecto.dto.PromedioDTO;
import co.edu.uniquindio.proyecto.dto.VentaMesDTO;
import co.edu.uniquindio.proyecto.entidades.MedioPago;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.ReporteServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Pruebas unitarias del bean de reportes: sin base de datos, sin Spring y sin FacesContext. */
class ReportesAdminBeanTest {

    private ReporteServicio servicio;
    private ReportesAdminBean bean;

    @BeforeEach
    void preparar() throws NegocioException {
        servicio = mock(ReporteServicio.class);
        when(servicio.productosPorCategoria()).thenReturn(Collections.emptyList());
        when(servicio.ventasPorMes()).thenReturn(Collections.emptyList());
        when(servicio.topVendedores()).thenReturn(Collections.emptyList());
        when(servicio.mejorCalificados()).thenReturn(Collections.emptyList());
        when(servicio.comprasPorMedioPago()).thenReturn(Collections.emptyList());
        bean = new ReportesAdminBean(servicio);
    }

    private static List<?> etiquetas(org.primefaces.model.charts.ChartModel modelo) {
        return (List<?>) ((org.primefaces.model.charts.ChartData) modelo.getData()).getLabels();
    }

    @Test
    void conListasVaciasNoHayGraficosNiErrores() {
        List<ReportesAdminBean.Aviso> avisos = bean.cargarDatos();

        assertTrue(avisos.isEmpty());
        assertFalse(bean.isHayCategorias());
        assertFalse(bean.isHayVentas());
        assertFalse(bean.isHayVendedores());
        assertFalse(bean.isHayCalificados());
        assertFalse(bean.isHayMediosPago());
        assertNull(bean.getVentasModel());
        assertEquals(0, bean.getTotalVendido());
        assertEquals(0, bean.getTotalCompras());
        assertEquals(0, bean.getCategoriasConProductos());
    }

    @Test
    void rellenaConCeroLosMesesSinVentasIncluidoElCambioDeAnio() throws NegocioException {
        when(servicio.ventasPorMes()).thenReturn(Arrays.asList(
                new VentaMesDTO(2025, 11, 100000.0),
                new VentaMesDTO(2026, 2, 50000.0)));

        bean.cargarDatos();

        assertTrue(bean.isHayVentas());
        assertEquals(Arrays.asList("Nov 2025", "Dic 2025", "Ene 2026", "Feb 2026"), etiquetas(bean.getVentasModel()));
        assertEquals("reporteMoneda", bean.getVentasModel().getExtender());
        List<ReportesAdminBean.FilaVenta> filas = bean.getVentas();
        assertEquals(4, filas.size());
        assertEquals(100000.0, filas.get(0).getTotal());
        assertEquals(0.0, filas.get(1).getTotal());
        assertEquals(0.0, filas.get(2).getTotal());
        assertEquals(50000.0, filas.get(3).getTotal());
        assertEquals(150000.0, bean.getTotalVendido());
    }

    @Test
    void elPastelExcluyeLasCategoriasSinProductosPeroLasDejaEnLaTabla() throws NegocioException {
        when(servicio.productosPorCategoria()).thenReturn(Arrays.asList(
                new CantidadDTO("Libros", 4L),
                new CantidadDTO("Ropa", 0L),
                new CantidadDTO("Tecnología", 2L)));

        bean.cargarDatos();

        assertTrue(bean.isHayCategorias());
        assertEquals(Arrays.asList("Libros", "Tecnología"), etiquetas(bean.getCategoriasModel()));
        assertEquals("reporteAjustarTamano", bean.getCategoriasModel().getExtender());
        assertEquals(3, bean.getCategorias().size());
        assertEquals(2, bean.getCategoriasConProductos());
    }

    @Test
    void siTodasLasCategoriasEstanVaciasNoSePintaElPastel() throws NegocioException {
        when(servicio.productosPorCategoria()).thenReturn(Arrays.asList(new CantidadDTO("Ropa", 0L)));

        bean.cargarDatos();

        assertFalse(bean.isHayCategorias());
        assertNull(bean.getCategoriasModel());
        assertEquals(1, bean.getCategorias().size());
    }

    @Test
    void laDonaUsaLasEtiquetasEnEspanolYSumaLasCompras() throws NegocioException {
        when(servicio.comprasPorMedioPago()).thenReturn(Arrays.asList(
                new MedioPagoDTO(MedioPago.TARJETA, 3L),
                new MedioPagoDTO(MedioPago.PAYPAL, 2L)));

        bean.cargarDatos();

        assertTrue(bean.isHayMediosPago());
        assertEquals(Arrays.asList(MedioPago.TARJETA.getEtiqueta(), MedioPago.PAYPAL.getEtiqueta()),
                etiquetas(bean.getMedioPagoModel()));
        assertEquals("reporteAjustarTamano", bean.getMedioPagoModel().getExtender());
        assertEquals("Tarjeta", bean.getMediosPago().get(0).getNombre());
        assertEquals(5, bean.getTotalCompras());
    }

    @Test
    void lasBarrasHorizontalesTienenNombresYPromedios() throws NegocioException {
        when(servicio.mejorCalificados()).thenReturn(Arrays.asList(
                new PromedioDTO("Bicicleta", 4.8),
                new PromedioDTO("Libro", 4.5)));

        bean.cargarDatos();

        assertTrue(bean.isHayCalificados());
        assertEquals(Arrays.asList("Bicicleta", "Libro"), etiquetas(bean.getCalificadosModel()));
        assertEquals("reporteBarrasHorizontales", bean.getCalificadosModel().getExtender());
        assertEquals(4.8, bean.getCalificados().get(0).getPromedio());
        assertEquals("Libro", bean.getCalificados().get(1).getNombre());
    }

    @Test
    void lasBarrasDeVendedoresTienenUnaFilaPorVendedor() throws NegocioException {
        when(servicio.topVendedores()).thenReturn(Arrays.asList(
                new CantidadDTO("Ana", 7L), new CantidadDTO("Luis", 3L)));

        bean.cargarDatos();

        assertTrue(bean.isHayVendedores());
        assertEquals(Arrays.asList("Ana", "Luis"), etiquetas(bean.getVendedoresModel()));
        assertEquals("reporteAjustarTamano", bean.getVendedoresModel().getExtender());
        assertEquals(7, bean.getVendedores().get(0).getCantidad());
    }

    @Test
    void unReporteQueFallaNoImpideLosDemas() throws NegocioException {
        when(servicio.topVendedores()).thenThrow(new NegocioException("Límite inválido"));
        when(servicio.ventasPorMes()).thenThrow(new IllegalStateException("fallo de base"));
        when(servicio.comprasPorMedioPago()).thenReturn(Arrays.asList(new MedioPagoDTO(MedioPago.PSE, 1L)));

        List<ReportesAdminBean.Aviso> avisos = bean.cargarDatos();

        assertEquals(2, avisos.size());
        assertTrue(avisos.get(0).texto.startsWith("No se pudieron cargar los reportes"));
        assertEquals("Límite inválido", avisos.get(1).texto);
        assertTrue(bean.isHayMediosPago());
        assertFalse(bean.isHayVendedores());
        assertFalse(bean.isHayVentas());
    }

    @Test
    void etiquetaDeMesEnEspanol() {
        assertEquals("Ene 2026", ReportesAdminBean.etiquetaMes(2026, 1));
        assertEquals("Dic 2025", ReportesAdminBean.etiquetaMes(2025, 12));
    }
}
