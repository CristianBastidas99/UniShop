package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.dto.CantidadDTO;
import co.edu.uniquindio.proyecto.dto.MedioPagoDTO;
import co.edu.uniquindio.proyecto.dto.PromedioDTO;
import co.edu.uniquindio.proyecto.dto.VentaMesDTO;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.ReporteServicio;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearTicks;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.donut.DonutChartDataSet;
import org.primefaces.model.charts.donut.DonutChartModel;
import org.primefaces.model.charts.donut.DonutChartOptions;
import org.primefaces.model.charts.hbar.HorizontalBarChartDataSet;
import org.primefaces.model.charts.hbar.HorizontalBarChartModel;
import org.primefaces.model.charts.line.LineChartDataSet;
import org.primefaces.model.charts.line.LineChartModel;
import org.primefaces.model.charts.line.LineChartOptions;
import org.primefaces.model.charts.optionconfig.legend.Legend;
import org.primefaces.model.charts.optionconfig.title.Title;
import org.primefaces.model.charts.pie.PieChartDataSet;
import org.primefaces.model.charts.pie.PieChartModel;
import org.primefaces.model.charts.pie.PieChartOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean de {@code admin/reportes.xhtml}: arma los cinco gráficos estadísticos del administrador
 * (pastel, línea, barras, barras horizontales y dona) y las tablas con los valores exactos.
 * Cada reporte se carga por separado: si uno falla, los demás se siguen mostrando.
 */
@Component
@ViewScoped
public class ReportesAdminBean implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(ReportesAdminBean.class);

    /** Paleta fija reutilizada en todos los gráficos. */
    static final List<String> PALETA = List.of("#1565c0", "#ef6c00", "#2e7d32", "#8e24aa",
            "#c62828", "#00838f", "#f9a825", "#5d4037");

    /** Funciones de resources/js/reportes.js que ajustan Chart.js (altura del contenedor y formato de moneda). */
    static final String EXTENDER_TAMANO = "reporteAjustarTamano";
    static final String EXTENDER_MONEDA = "reporteMoneda";
    static final String EXTENDER_BARRAS_H = "reporteBarrasHorizontales";

    private static final String[] MESES = {"Ene", "Feb", "Mar", "Abr", "May", "Jun",
            "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};

    /** Fila de tabla con un nombre y una cantidad entera. */
    public static class FilaCantidad implements Serializable {
        private final String nombre;
        private final long cantidad;

        FilaCantidad(String nombre, long cantidad) {
            this.nombre = nombre;
            this.cantidad = cantidad;
        }

        public String getNombre() { return nombre; }
        public long getCantidad() { return cantidad; }
    }

    /** Fila de tabla con las ventas de un mes. */
    public static class FilaVenta implements Serializable {
        private final String mes;
        private final double total;

        FilaVenta(String mes, double total) {
            this.mes = mes;
            this.total = total;
        }

        public String getMes() { return mes; }
        public double getTotal() { return total; }
    }

    /** Fila de tabla con un nombre y un promedio. */
    public static class FilaPromedio implements Serializable {
        private final String nombre;
        private final double promedio;

        FilaPromedio(String nombre, double promedio) {
            this.nombre = nombre;
            this.promedio = promedio;
        }

        public String getNombre() { return nombre; }
        public double getPromedio() { return promedio; }
    }

    /** Aviso pendiente de mostrar al usuario (se separa de la carga para poder probarla sin JSF). */
    static class Aviso {
        final FacesMessage.Severity severidad;
        final String texto;

        Aviso(FacesMessage.Severity severidad, String texto) {
            this.severidad = severidad;
            this.texto = texto;
        }
    }

    private final ReporteServicio reporteServicio;

    private PieChartModel categoriasModel;
    private LineChartModel ventasModel;
    private BarChartModel vendedoresModel;
    private HorizontalBarChartModel calificadosModel;
    private DonutChartModel medioPagoModel;

    private List<FilaCantidad> categorias = new ArrayList<>();
    private List<FilaVenta> ventas = new ArrayList<>();
    private List<FilaCantidad> vendedores = new ArrayList<>();
    private List<FilaPromedio> calificados = new ArrayList<>();
    private List<FilaCantidad> mediosPago = new ArrayList<>();

    private boolean hayCategorias;
    private boolean hayVentas;
    private boolean hayVendedores;
    private boolean hayCalificados;
    private boolean hayMediosPago;

    private double totalVendido;
    private long totalCompras;
    private long categoriasConProductos;

    public ReportesAdminBean(ReporteServicio reporteServicio) {
        this.reporteServicio = reporteServicio;
    }

    /** Se ejecuta con f:viewAction al abrir la página. */
    public void cargar() {
        for (Aviso a : cargarDatos()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(a.severidad, "Atención", a.texto));
        }
    }

    /**
     * Consulta los reportes y arma los modelos. No usa FacesContext.
     *
     * @return avisos para el usuario (vacío si todo salió bien)
     */
    List<Aviso> cargarDatos() {
        List<Aviso> avisos = new ArrayList<>();
        reiniciar();

        try {
            armarCategorias(reporteServicio.productosPorCategoria());
        } catch (Exception e) {
            registrarFallo("productos por categoría", e, avisos);
        }
        try {
            armarVentas(reporteServicio.ventasPorMes());
        } catch (Exception e) {
            registrarFallo("ventas por mes", e, avisos);
        }
        try {
            armarVendedores(reporteServicio.topVendedores());
        } catch (Exception e) {
            registrarFallo("mejores vendedores", e, avisos);
        }
        try {
            armarCalificados(reporteServicio.mejorCalificados());
        } catch (Exception e) {
            registrarFallo("productos mejor calificados", e, avisos);
        }
        try {
            armarMediosPago(reporteServicio.comprasPorMedioPago());
        } catch (Exception e) {
            registrarFallo("compras por medio de pago", e, avisos);
        }
        return avisos;
    }

    private void registrarFallo(String reporte, Exception e, List<Aviso> avisos) {
        if (e instanceof NegocioException) {
            avisos.add(new Aviso(FacesMessage.SEVERITY_WARN, e.getMessage()));
        } else {
            LOG.error("Error inesperado al cargar el reporte de {}", reporte, e);
            avisos.add(new Aviso(FacesMessage.SEVERITY_ERROR,
                    "No se pudieron cargar los reportes de " + reporte + ". Inténtalo de nuevo más tarde."));
        }
    }

    private void reiniciar() {
        categoriasModel = null;
        ventasModel = null;
        vendedoresModel = null;
        calificadosModel = null;
        medioPagoModel = null;
        categorias = new ArrayList<>();
        ventas = new ArrayList<>();
        vendedores = new ArrayList<>();
        calificados = new ArrayList<>();
        mediosPago = new ArrayList<>();
        hayCategorias = false;
        hayVentas = false;
        hayVendedores = false;
        hayCalificados = false;
        hayMediosPago = false;
        totalVendido = 0;
        totalCompras = 0;
        categoriasConProductos = 0;
    }

    // ---------- Armado de cada reporte ----------

    /** Pastel: las categorías sin productos salen en la tabla pero no en el gráfico. */
    void armarCategorias(List<CantidadDTO> datos) {
        List<String> etiquetas = new ArrayList<>();
        List<Number> valores = new ArrayList<>();
        List<FilaCantidad> filas = new ArrayList<>();
        for (CantidadDTO d : datos) {
            long cantidad = valor(d.getCantidad());
            filas.add(new FilaCantidad(d.getNombre(), cantidad));
            if (cantidad > 0) {
                etiquetas.add(d.getNombre());
                valores.add(cantidad);
            }
        }
        categorias = filas;
        categoriasConProductos = valores.size();
        hayCategorias = !valores.isEmpty();
        if (!hayCategorias) {
            return;
        }
        PieChartDataSet ds = new PieChartDataSet();
        ds.setData(valores);
        ds.setBackgroundColor(colores(valores.size()));
        ChartData datosGrafico = new ChartData();
        datosGrafico.addChartDataSet(ds);
        datosGrafico.setLabels(etiquetas);
        PieChartModel modelo = new PieChartModel();
        modelo.setData(datosGrafico);
        PieChartOptions opciones = new PieChartOptions();
        opciones.setTitle(sinTitulo());
        opciones.setLegend(leyenda(true));
        modelo.setOptions(opciones);
        modelo.setExtender(EXTENDER_TAMANO);
        categoriasModel = modelo;
    }

    /** Línea: rellena con 0 los meses sin ventas entre el primero y el último con ventas. */
    void armarVentas(List<VentaMesDTO> datos) {
        List<FilaVenta> filas = rellenarMeses(datos);
        ventas = filas;
        hayVentas = !filas.isEmpty();
        totalVendido = 0;
        for (FilaVenta f : filas) {
            totalVendido += f.getTotal();
        }
        if (!hayVentas) {
            return;
        }
        List<String> etiquetas = new ArrayList<>();
        List<Object> valores = new ArrayList<>();
        for (FilaVenta f : filas) {
            etiquetas.add(f.getMes());
            valores.add(f.getTotal());
        }
        LineChartDataSet ds = new LineChartDataSet();
        ds.setLabel("Ventas (COP)");
        ds.setData(valores);
        ds.setFill(true);
        ds.setBorderColor(PALETA.get(0));
        ds.setBackgroundColor("rgba(21, 101, 192, 0.12)");
        ds.setLineTension(0.2);
        ChartData datosGrafico = new ChartData();
        datosGrafico.addChartDataSet(ds);
        datosGrafico.setLabels(etiquetas);
        LineChartModel modelo = new LineChartModel();
        modelo.setData(datosGrafico);
        LineChartOptions opciones = new LineChartOptions();
        opciones.setTitle(sinTitulo());
        opciones.setLegend(leyenda(false));
        CartesianScales escalas = new CartesianScales();
        CartesianLinearAxes ejeY = new CartesianLinearAxes();
        CartesianLinearTicks ticks = new CartesianLinearTicks();
        ticks.setBeginAtZero(true);
        ejeY.setTicks(ticks);
        escalas.addYAxesData(ejeY);
        opciones.setScales(escalas);
        modelo.setOptions(opciones);
        modelo.setExtender(EXTENDER_MONEDA);
        ventasModel = modelo;
    }

    /** Barras verticales: unidades vendidas por vendedor, con ticks enteros desde 0. */
    void armarVendedores(List<CantidadDTO> datos) {
        List<String> etiquetas = new ArrayList<>();
        List<Number> valores = new ArrayList<>();
        List<FilaCantidad> filas = new ArrayList<>();
        long maximo = 0;
        for (CantidadDTO d : datos) {
            long cantidad = valor(d.getCantidad());
            filas.add(new FilaCantidad(d.getNombre(), cantidad));
            etiquetas.add(d.getNombre());
            valores.add(cantidad);
            maximo = Math.max(maximo, cantidad);
        }
        vendedores = filas;
        hayVendedores = !filas.isEmpty();
        if (!hayVendedores) {
            return;
        }
        BarChartDataSet ds = new BarChartDataSet();
        ds.setLabel("Unidades vendidas");
        ds.setData(valores);
        ds.setBackgroundColor(colores(valores.size()));
        ChartData datosGrafico = new ChartData();
        datosGrafico.addChartDataSet(ds);
        datosGrafico.setLabels(etiquetas);
        BarChartModel modelo = new BarChartModel();
        modelo.setData(datosGrafico);
        BarChartOptions opciones = new BarChartOptions();
        opciones.setTitle(sinTitulo());
        opciones.setLegend(leyenda(false));
        CartesianScales escalas = new CartesianScales();
        CartesianLinearAxes ejeY = new CartesianLinearAxes();
        CartesianLinearTicks ticks = new CartesianLinearTicks();
        ticks.setBeginAtZero(true);
        ticks.setPrecision(0);
        if (maximo <= 10) {
            ticks.setStepSize(1);
        }
        ejeY.setTicks(ticks);
        escalas.addYAxesData(ejeY);
        opciones.setScales(escalas);
        modelo.setOptions(opciones);
        modelo.setExtender(EXTENDER_TAMANO);
        vendedoresModel = modelo;
    }

    /** Barras horizontales: promedio de calificación con el eje de valores fijo de 0 a 5. */
    void armarCalificados(List<PromedioDTO> datos) {
        List<String> etiquetas = new ArrayList<>();
        List<Number> valores = new ArrayList<>();
        List<FilaPromedio> filas = new ArrayList<>();
        for (PromedioDTO d : datos) {
            double promedio = d.getPromedio() == null ? 0 : d.getPromedio();
            filas.add(new FilaPromedio(d.getNombre(), promedio));
            etiquetas.add(d.getNombre());
            valores.add(promedio);
        }
        calificados = filas;
        hayCalificados = !filas.isEmpty();
        if (!hayCalificados) {
            return;
        }
        HorizontalBarChartDataSet ds = new HorizontalBarChartDataSet();
        ds.setLabel("Calificación promedio");
        ds.setData(valores);
        ds.setBackgroundColor(colores(valores.size()));
        ChartData datosGrafico = new ChartData();
        datosGrafico.addChartDataSet(ds);
        datosGrafico.setLabels(etiquetas);
        HorizontalBarChartModel modelo = new HorizontalBarChartModel();
        modelo.setData(datosGrafico);
        BarChartOptions opciones = new BarChartOptions();
        opciones.setTitle(sinTitulo());
        opciones.setLegend(leyenda(false));
        CartesianScales escalas = new CartesianScales();
        CartesianLinearAxes ejeX = new CartesianLinearAxes();
        CartesianLinearTicks ticks = new CartesianLinearTicks();
        ticks.setMin(0);
        ticks.setMax(5);
        ticks.setStepSize(1);
        ejeX.setTicks(ticks);
        escalas.addXAxesData(ejeX);
        opciones.setScales(escalas);
        modelo.setOptions(opciones);
        modelo.setExtender(EXTENDER_BARRAS_H);
        calificadosModel = modelo;
    }

    /** Dona: compras por medio de pago, con la etiqueta en español. */
    void armarMediosPago(List<MedioPagoDTO> datos) {
        List<String> etiquetas = new ArrayList<>();
        List<Number> valores = new ArrayList<>();
        List<FilaCantidad> filas = new ArrayList<>();
        long total = 0;
        for (MedioPagoDTO d : datos) {
            long cantidad = valor(d.getCantidad());
            String etiqueta = d.getMedioPago() == null ? "Sin dato" : d.getMedioPago().getEtiqueta();
            filas.add(new FilaCantidad(etiqueta, cantidad));
            etiquetas.add(etiqueta);
            valores.add(cantidad);
            total += cantidad;
        }
        mediosPago = filas;
        totalCompras = total;
        hayMediosPago = total > 0;
        if (!hayMediosPago) {
            return;
        }
        DonutChartDataSet ds = new DonutChartDataSet();
        ds.setData(valores);
        ds.setBackgroundColor(colores(valores.size()));
        ChartData datosGrafico = new ChartData();
        datosGrafico.addChartDataSet(ds);
        datosGrafico.setLabels(etiquetas);
        DonutChartModel modelo = new DonutChartModel();
        modelo.setData(datosGrafico);
        DonutChartOptions opciones = new DonutChartOptions();
        opciones.setTitle(sinTitulo());
        opciones.setLegend(leyenda(true));
        modelo.setOptions(opciones);
        modelo.setExtender(EXTENDER_TAMANO);
        medioPagoModel = modelo;
    }

    // ---------- Utilidades ----------

    /**
     * Devuelve una fila por cada mes entre el primero y el último con ventas; los meses sin ventas valen 0.
     * Los datos llegan ordenados por año y mes.
     */
    static List<FilaVenta> rellenarMeses(List<VentaMesDTO> datos) {
        List<FilaVenta> filas = new ArrayList<>();
        if (datos == null || datos.isEmpty()) {
            return filas;
        }
        int indice = -1;
        for (VentaMesDTO d : datos) {
            int nuevo = d.getAnio() * 12 + (d.getMes() - 1);
            if (indice >= 0) {
                for (int vacio = indice + 1; vacio < nuevo; vacio++) {
                    filas.add(new FilaVenta(etiquetaMes(vacio / 12, vacio % 12 + 1), 0));
                }
            }
            filas.add(new FilaVenta(etiquetaMes(d.getAnio(), d.getMes()), d.getTotal() == null ? 0 : d.getTotal()));
            indice = nuevo;
        }
        return filas;
    }

    /** Etiqueta como "Ene 2026". */
    static String etiquetaMes(int anio, int mes) {
        return MESES[mes - 1] + " " + anio;
    }

    private static long valor(Long n) {
        return n == null ? 0 : n;
    }

    private static List<String> colores(int cantidad) {
        List<String> lista = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            lista.add(PALETA.get(i % PALETA.size()));
        }
        return lista;
    }

    private static Title sinTitulo() {
        Title titulo = new Title();
        titulo.setDisplay(false);
        return titulo;
    }

    private static Legend leyenda(boolean visible) {
        Legend leyenda = new Legend();
        leyenda.setDisplay(visible);
        if (visible) {
            leyenda.setPosition("bottom");
        }
        return leyenda;
    }

    public PieChartModel getCategoriasModel() { return categoriasModel; }
    public LineChartModel getVentasModel() { return ventasModel; }
    public BarChartModel getVendedoresModel() { return vendedoresModel; }
    public HorizontalBarChartModel getCalificadosModel() { return calificadosModel; }
    public DonutChartModel getMedioPagoModel() { return medioPagoModel; }
    public List<FilaCantidad> getCategorias() { return categorias; }
    public List<FilaVenta> getVentas() { return ventas; }
    public List<FilaCantidad> getVendedores() { return vendedores; }
    public List<FilaPromedio> getCalificados() { return calificados; }
    public List<FilaCantidad> getMediosPago() { return mediosPago; }
    public boolean isHayCategorias() { return hayCategorias; }
    public boolean isHayVentas() { return hayVentas; }
    public boolean isHayVendedores() { return hayVendedores; }
    public boolean isHayCalificados() { return hayCalificados; }
    public boolean isHayMediosPago() { return hayMediosPago; }
    public double getTotalVendido() { return totalVendido; }
    public long getTotalCompras() { return totalCompras; }
    public long getCategoriasConProductos() { return categoriasConProductos; }
}
