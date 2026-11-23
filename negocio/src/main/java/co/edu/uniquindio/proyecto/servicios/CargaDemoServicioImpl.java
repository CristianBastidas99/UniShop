package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.dto.ItemCompraDTO;
import co.edu.uniquindio.proyecto.dto.demo.CatalogoDemo;
import co.edu.uniquindio.proyecto.dto.demo.CatalogoDemo.PersonaDemo;
import co.edu.uniquindio.proyecto.dto.demo.CatalogoDemo.ProductoDemo;
import co.edu.uniquindio.proyecto.dto.demo.ResumenCargaDemo;
import co.edu.uniquindio.proyecto.entidades.Administrador;
import co.edu.uniquindio.proyecto.entidades.Categoria;
import co.edu.uniquindio.proyecto.entidades.Ciudad;
import co.edu.uniquindio.proyecto.entidades.Comentario;
import co.edu.uniquindio.proyecto.entidades.Compra;
import co.edu.uniquindio.proyecto.entidades.Cupon;
import co.edu.uniquindio.proyecto.entidades.MedioPago;
import co.edu.uniquindio.proyecto.entidades.MotivoReporte;
import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.entidades.ReporteProducto;
import co.edu.uniquindio.proyecto.entidades.Subasta;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.repositorios.CompraRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Implementación de {@link CargaDemoServicio}. La actividad es determinista (semilla fija) y todo se crea
 * con los servicios, salvo la fecha de las compras (ver {@link #crearCompras}).
 */
@Service
public class CargaDemoServicioImpl implements CargaDemoServicio {

    private static final Logger LOG = LoggerFactory.getLogger(CargaDemoServicioImpl.class);

    private static final String RECURSO = "demo/catalogo.json";
    private static final String RUTA_IMAGENES = "resources/img/demo/";
    private static final long DIA_MS = 24L * 60 * 60 * 1000;
    private static final int TOTAL_COMPRAS = 24;
    private static final int TOTAL_COMENTARIOS = 30;
    private static final int TOTAL_RESPUESTAS = 10;

    /** Posición (en el catálogo) de los tres productos que se subastan, de tres vendedores distintos. */
    private static final int[] PRODUCTOS_SUBASTA = {0, 14, 29};
    /** Duración en horas de cada subasta (4, 7 y 9 días). */
    private static final int[] HORAS_SUBASTA = {96, 168, 216};
    private static final int[] PUJAS_POR_SUBASTA = {3, 2, 4};

    private static final String[][] CUPONES = {{"UNISHOP10", "0.10"}, {"DEMO15", "0.15"}, {"FERIA20", "0.20"}};

    private static final String[] COMENTARIOS_5 = {
            "Excelente producto, llegó en perfecto estado y tal como se describe. Lo recomiendo.",
            "Superó mis expectativas. Muy buena calidad por el precio.",
            "Todo perfecto, el vendedor fue muy atento y respondió rápido mis dudas.",
            "Me encantó, funciona de maravilla. Volvería a comprarlo sin dudarlo.",
            "Muy buena compra, el empaque llegó impecable y el producto es idéntico a las fotos."};
    private static final String[] COMENTARIOS_4 = {
            "Buen producto, cumple con lo prometido. El envío tardó un poco más de lo esperado.",
            "Muy bueno en general, solo le faltaría un manual más claro.",
            "Buena relación calidad-precio. Recomendado.",
            "Es bonito y funcional. Lo único es que el color es un poco distinto al de la foto."};
    private static final String[] COMENTARIOS_3 = {
            "Cumple, aunque esperaba un acabado un poco mejor.",
            "Es aceptable para el precio, pero la calidad podría mejorar."};
    private static final String[] COMENTARIOS_2 = {
            "No era lo que esperaba, la calidad es inferior a lo que muestran las fotos.",
            "Llegó con un pequeño defecto y tardaron en responder. Regular."};
    private static final String[] RESPUESTAS = {
            "¡Muchas gracias por tu compra y por tu comentario!",
            "Gracias por confiar en nosotros, nos alegra que te haya gustado.",
            "Gracias por tu opinión, la tendremos en cuenta para mejorar.",
            "Lamentamos el inconveniente, escríbenos por el chat y lo resolvemos."};

    private final UsuarioServicio usuarioServicio;
    private final AdministradorServicio administradorServicio;
    private final CiudadServicio ciudadServicio;
    private final CategoriaServicio categoriaServicio;
    private final ProductoServicio productoServicio;
    private final CuponServicio cuponServicio;
    private final CompraServicio compraServicio;
    private final ComentarioServicio comentarioServicio;
    private final SubastaServicio subastaServicio;
    private final SubastaUsuarioServicio subastaUsuarioServicio;
    private final ReporteProductoServicio reporteServicio;
    private final CompraRepo compraRepo;

    public CargaDemoServicioImpl(UsuarioServicio usuarioServicio, AdministradorServicio administradorServicio,
                                 CiudadServicio ciudadServicio, CategoriaServicio categoriaServicio,
                                 ProductoServicio productoServicio, CuponServicio cuponServicio,
                                 CompraServicio compraServicio, ComentarioServicio comentarioServicio,
                                 SubastaServicio subastaServicio, SubastaUsuarioServicio subastaUsuarioServicio,
                                 ReporteProductoServicio reporteServicio, CompraRepo compraRepo) {
        this.usuarioServicio = usuarioServicio;
        this.administradorServicio = administradorServicio;
        this.ciudadServicio = ciudadServicio;
        this.categoriaServicio = categoriaServicio;
        this.productoServicio = productoServicio;
        this.cuponServicio = cuponServicio;
        this.compraServicio = compraServicio;
        this.comentarioServicio = comentarioServicio;
        this.subastaServicio = subastaServicio;
        this.subastaUsuarioServicio = subastaUsuarioServicio;
        this.reporteServicio = reporteServicio;
        this.compraRepo = compraRepo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResumenCargaDemo cargar() throws NegocioException {
        CatalogoDemo catalogo = leerCatalogo();
        ResumenCargaDemo resumen = new ResumenCargaDemo();
        if (catalogo.getVendedores().isEmpty() || catalogo.getCompradores().size() < 3
                || catalogo.getProductos().size() <= PRODUCTOS_SUBASTA[2]) {
            throw new NegocioException("El catálogo de demostración está incompleto");
        }

        String emailMarca = catalogo.getVendedores().get(0).getEmail().trim().toLowerCase(Locale.ROOT);
        boolean existe = usuarioServicio.listarUsuarios().stream()
                .anyMatch(u -> emailMarca.equals(u.getEmail()));
        if (existe) {
            resumen.setYaCargado(true);
            LOG.info("{}", resumen.describir());
            return resumen;
        }

        Map<String, Ciudad> ciudades = new HashMap<>();
        for (Ciudad c : ciudadServicio.listarCiudades()) {
            ciudades.put(clave(c.getNombre()), c);
        }
        Map<String, Categoria> categorias = new HashMap<>();
        for (Categoria c : categoriaServicio.listarCategoria()) {
            categorias.put(clave(c.getNombre()), c);
        }

        String password = catalogo.getPassword();
        for (PersonaDemo a : catalogo.getAdministradores()) {
            Administrador admin = new Administrador();
            admin.setNombre(a.getNombre());
            admin.setEmail(a.getEmail());
            admin.setPassword(password);
            administradorServicio.registrarAdministrador(admin);
            resumen.setAdministradores(resumen.getAdministradores() + 1);
        }
        Map<String, Usuario> vendedores = crearUsuarios(catalogo.getVendedores(), password, ciudades);
        List<Usuario> compradores = new ArrayList<>(
                crearUsuarios(catalogo.getCompradores(), password, ciudades).values());
        resumen.setVendedores(vendedores.size());
        resumen.setCompradores(compradores.size());

        List<Producto> productos = crearProductos(catalogo, vendedores, ciudades, categorias);
        resumen.setProductos(productos.size());

        Random azar = new Random(2026);
        List<Cupon> cupones = crearCupones();
        resumen.setCupones(cupones.size());
        Set<Long> enSubasta = new HashSet<>();
        for (int posicion : PRODUCTOS_SUBASTA) {
            enSubasta.add(productos.get(posicion).getId());
        }
        crearCompras(productos, enSubasta, compradores, cupones, azar, resumen);
        crearComentarios(productos, compradores, azar, resumen);
        crearFavoritos(productos, compradores, azar, resumen);
        crearSubastas(productos, compradores, resumen);
        crearReportes(productos, compradores, resumen);

        LOG.info("Datos de demostración cargados: {}", resumen.describir());
        return resumen;
    }

    private CatalogoDemo leerCatalogo() throws NegocioException {
        try (InputStream entrada = new ClassPathResource(RECURSO).getInputStream()) {
            return new ObjectMapper().readValue(entrada, CatalogoDemo.class);
        } catch (IOException e) {
            throw new NegocioException("No se pudo leer el catálogo de demostración: " + e.getMessage());
        }
    }

    /**
     * Clave para buscar ciudades y categorías por nombre sin distinguir mayúsculas ni tildes, igual que la
     * intercalación de MySQL (utf8mb4_0900_ai_ci): "Medellín" y "Medellin" son la misma ciudad para la base, y
     * crear la segunda fallaría por nombre repetido.
     */
    private static String clave(String nombre) {
        String sinTildes = java.text.Normalizer.normalize(nombre.trim(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase(Locale.ROOT);
    }

    private Ciudad ciudadPorNombre(String nombre, Map<String, Ciudad> ciudades) throws NegocioException {
        Ciudad ciudad = ciudades.get(clave(nombre));
        if (ciudad == null) {
            Ciudad nueva = new Ciudad();
            nueva.setNombre(nombre);
            ciudad = ciudadServicio.registrarCiudad(nueva);
            ciudades.put(clave(nombre), ciudad);
        }
        return ciudad;
    }

    private Categoria categoriaPorNombre(String nombre, Map<String, Categoria> categorias) throws NegocioException {
        Categoria categoria = categorias.get(clave(nombre));
        if (categoria == null) {
            Categoria nueva = new Categoria();
            nueva.setNombre(nombre);
            categoria = categoriaServicio.registrarCategoria(nueva);
            categorias.put(clave(nombre), categoria);
        }
        return categoria;
    }

    /** Registra los usuarios por el servicio; devuelve el mapa clave -> usuario en orden del catálogo. */
    private Map<String, Usuario> crearUsuarios(List<PersonaDemo> personas, String password,
                                               Map<String, Ciudad> ciudades) throws NegocioException {
        Map<String, Usuario> resultado = new LinkedHashMap<>();
        for (PersonaDemo p : personas) {
            Usuario u = new Usuario();
            u.setNombre(p.getNombre());
            u.setEmail(p.getEmail());
            u.setPassword(password);
            u.setCiudad(ciudadPorNombre(p.getCiudad(), ciudades));
            u.setNumeroTelefono(new ArrayList<>(p.getTelefonos()));
            resultado.put(p.getClave(), usuarioServicio.registrarUsuario(u));
        }
        return resultado;
    }

    private List<Producto> crearProductos(CatalogoDemo catalogo, Map<String, Usuario> vendedores,
                                          Map<String, Ciudad> ciudades, Map<String, Categoria> categorias)
            throws NegocioException {
        List<Producto> creados = new ArrayList<>();
        long ahora = System.currentTimeMillis();
        int i = 0;
        for (ProductoDemo d : catalogo.getProductos()) {
            Usuario vendedor = vendedores.get(d.getVendedor());
            if (vendedor == null) {
                throw new NegocioException("El vendedor " + d.getVendedor() + " del producto "
                        + d.getNombre() + " no existe en el catálogo");
            }
            Producto p = new Producto();
            p.setNombre(d.getNombre());
            p.setDescripcion(d.getDescripcion());
            p.setPrecio(d.getPrecio());
            p.setUnidades(d.getUnidades());
            p.setDescuento(d.getDescuento());
            p.setUsuario(vendedor);
            p.setCiudad(ciudadPorNombre(d.getCiudad(), ciudades));
            p.setMisImagenes(new ArrayList<>(Collections.singletonList(RUTA_IMAGENES + d.getImagen())));
            // Fecha límite entre 60 y 180 días desde hoy, variada de forma determinista
            p.setFechaLimite(new Date(ahora + (60 + (i * 37L) % 121) * DIA_MS));
            List<Long> idsCategorias = new ArrayList<>();
            for (String nombre : d.getCategorias()) {
                idsCategorias.add(categoriaPorNombre(nombre, categorias).getId());
            }
            creados.add(productoServicio.publicarProducto(p, idsCategorias));
            i++;
        }
        return creados;
    }

    private List<Cupon> crearCupones() throws NegocioException {
        List<Cupon> cupones = new ArrayList<>();
        Date vencimiento = new Date(System.currentTimeMillis() + 90 * DIA_MS);
        for (String[] datos : CUPONES) {
            Cupon c = new Cupon();
            c.setCodigo(datos[0]);
            c.setPorcentaje(Double.parseDouble(datos[1]));
            c.setFechaVencimiento(vencimiento);
            c.setUsosMaximos(50);
            cupones.add(cuponServicio.registrarCupon(c));
        }
        return cupones;
    }

    /**
     * Crea las compras con {@code CompraServicio.comprar} y reparte sus fechas en los últimos 6 meses.
     * Excepción a la regla "todo por servicios": {@code comprar} siempre pone la fecha actual y ningún servicio
     * permite cambiarla, así que la fecha se ajusta con {@code CompraRepo}. Se hace para que el reporte de
     * ventas por mes tenga datos de varios meses.
     */
    private void crearCompras(List<Producto> productos, Set<Long> enSubasta, List<Usuario> compradores,
                              List<Cupon> cupones, Random azar, ResumenCargaDemo resumen) throws NegocioException {
        MedioPago[] medios = MedioPago.values();
        Map<Long, Integer> disponibles = new HashMap<>();
        List<Producto> comprables = new ArrayList<>();
        for (Producto p : productos) {
            if (!enSubasta.contains(p.getId())) {
                comprables.add(p);
                disponibles.put(p.getId(), p.getUnidades());
            }
        }
        long ahora = System.currentTimeMillis();
        for (int i = 0; i < TOTAL_COMPRAS; i++) {
            Usuario comprador = compradores.get(i % compradores.size());
            int cantidadProductos = 1 + azar.nextInt(3);
            List<ItemCompraDTO> items = new ArrayList<>();
            Set<Long> elegidos = new HashSet<>();
            int intentos = 0;
            while (items.size() < cantidadProductos && intentos++ < 50) {
                Producto p = comprables.get(azar.nextInt(comprables.size()));
                int unidades = 1 + azar.nextInt(2);
                // Nunca se agota el stock: siempre queda al menos una unidad
                if (elegidos.contains(p.getId()) || disponibles.get(p.getId()) - unidades < 1) {
                    continue;
                }
                elegidos.add(p.getId());
                disponibles.merge(p.getId(), -unidades, Integer::sum);
                items.add(new ItemCompraDTO(p.getId(), unidades));
            }
            if (items.isEmpty()) {
                continue;
            }
            boolean conCupon = i % 5 == 2;
            String codigo = conCupon ? cupones.get((i / 5) % cupones.size()).getCodigo() : null;
            Compra compra = compraServicio.comprar(comprador.getId(), items, medios[i % medios.length], codigo);
            // De 176 a 8 días atrás, con hora variable: abarca 6 meses distintos
            long dias = 176 - (long) i * 168 / (TOTAL_COMPRAS - 1);
            compra.setFechaCompra(new Date(ahora - dias * DIA_MS - azar.nextInt(10 * 3600) * 1000L));
            compraRepo.save(compra);
            resumen.setCompras(resumen.getCompras() + 1);
            if (conCupon) {
                resumen.setComprasConCupon(resumen.getComprasConCupon() + 1);
            }
        }
    }

    private String elegir(String[] opciones, int semilla) {
        return opciones[semilla % opciones.length];
    }

    private void crearComentarios(List<Producto> productos, List<Usuario> compradores, Random azar,
                                  ResumenCargaDemo resumen) throws NegocioException {
        // 18 productos al azar: los 12 primeros reciben 2 comentarios y los 6 siguientes 1 (30 en total)
        List<Producto> mezclados = new ArrayList<>(productos);
        Collections.shuffle(mezclados, azar);
        int numero = 0;
        for (int k = 0; k < 18 && numero < TOTAL_COMENTARIOS; k++) {
            Producto producto = mezclados.get(k);
            int cantidad = k < 12 ? 2 : 1;
            for (int j = 0; j < cantidad; j++) {
                Usuario autor = compradores.get((k + j) % compradores.size());
                int sorteo = azar.nextInt(10);
                int calificacion = sorteo < 5 ? 5 : sorteo < 8 ? 4 : sorteo < 9 ? 3 : 2;
                String[] textos = calificacion == 5 ? COMENTARIOS_5 : calificacion == 4 ? COMENTARIOS_4
                        : calificacion == 3 ? COMENTARIOS_3 : COMENTARIOS_2;
                Comentario c = new Comentario();
                c.setUsuario(autor);
                c.setProducto(producto);
                c.setCalificacion(calificacion);
                c.setMensaje(elegir(textos, azar.nextInt(100)));
                c = comentarioServicio.comentarProducto(c);
                resumen.setComentarios(resumen.getComentarios() + 1);
                numero++;
                // Cada tercer comentario recibe respuesta del vendedor (10 en total)
                if (numero % 3 == 0 && resumen.getRespuestas() < TOTAL_RESPUESTAS) {
                    String respuesta = calificacion <= 3 ? RESPUESTAS[2 + numero % 2] : RESPUESTAS[numero % 2];
                    comentarioServicio.responderComentario(c.getId(), respuesta, producto.getUsuario().getId());
                    resumen.setRespuestas(resumen.getRespuestas() + 1);
                }
            }
        }
    }

    private void crearFavoritos(List<Producto> productos, List<Usuario> compradores, Random azar,
                                ResumenCargaDemo resumen) throws NegocioException {
        for (Usuario comprador : compradores) {
            List<Producto> mezclados = new ArrayList<>(productos);
            Collections.shuffle(mezclados, azar);
            int cantidad = 4 + azar.nextInt(3);
            for (int i = 0; i < cantidad; i++) {
                productoServicio.agregarFavorito(mezclados.get(i).getId(), comprador.getId());
                resumen.setFavoritos(resumen.getFavoritos() + 1);
            }
        }
    }

    private void crearSubastas(List<Producto> productos, List<Usuario> compradores, ResumenCargaDemo resumen)
            throws NegocioException {
        for (int s = 0; s < PRODUCTOS_SUBASTA.length; s++) {
            Producto producto = productos.get(PRODUCTOS_SUBASTA[s]);
            Subasta subasta = subastaServicio.crearSubasta(producto.getId(), producto.getUsuario().getId(),
                    HORAS_SUBASTA[s]);
            resumen.setSubastas(resumen.getSubastas() + 1);
            for (int k = 0; k < PUJAS_POR_SUBASTA[s]; k++) {
                // Pujas crecientes desde el 60 % del precio, de a 7 % más, redondeadas a miles
                double valor = Math.round(producto.getPrecio() * (0.60 + 0.07 * k) / 1000.0) * 1000.0;
                Usuario postor = compradores.get((s + k) % compradores.size());
                subastaUsuarioServicio.pujar(subasta.getId(), postor.getId(), valor);
                resumen.setPujas(resumen.getPujas() + 1);
            }
        }
    }

    private void crearReportes(List<Producto> productos, List<Usuario> compradores, ResumenCargaDemo resumen)
            throws NegocioException {
        Object[][] reportes = {
                {6, 0, MotivoReporte.INFORMACION_FALSA,
                        "La descripción dice que incluye película, pero en la publicación no se ve por ningún lado."},
                {26, 1, MotivoReporte.FRAUDE,
                        "El precio es muy bajo para ser un perfume original, me parece sospechoso."}};
        for (Object[] datos : reportes) {
            ReporteProducto r = new ReporteProducto();
            r.setProducto(productos.get((Integer) datos[0]));
            r.setUsuario(compradores.get((Integer) datos[1]));
            r.setMotivo((MotivoReporte) datos[2]);
            r.setDescripcion((String) datos[3]);
            reporteServicio.reportarProducto(r);
            resumen.setReportes(resumen.getReportes() + 1);
        }
    }
}
