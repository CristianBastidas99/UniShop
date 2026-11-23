"""
Genera las imágenes de los productos de demostración de UniShop.

Lee negocio/src/main/resources/demo/catalogo.json y, por cada producto, dibuja una imagen PNG de 600x600
(fondo degradado con el color de su primera categoría, el ícono del producto, su nombre y la categoría) en
web/src/main/webapp/resources/img/demo/<imagen>. Los productos guardan la ruta resources/img/demo/<imagen>.

Uso (desde la raíz del proyecto):
    python docs/generar-imagenes-demo.py

Requiere Pillow (pip install pillow). Usa las fuentes de Windows Segoe UI y Segoe UI Emoji (íconos a color);
en otro sistema se pueden pasar otras rutas con --fuente, --fuente-negrita y --fuente-emoji.
"""
import argparse
import json
import os

from PIL import Image, ImageDraw, ImageFont

RAIZ = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CATALOGO = os.path.join(RAIZ, "negocio", "src", "main", "resources", "demo", "catalogo.json")
SALIDA = os.path.join(RAIZ, "web", "src", "main", "webapp", "resources", "img", "demo")

TAM = 600
ALTO_ICONO = 210
# Colores (claro, oscuro) del degradado por categoría
COLORES = {
    "Tecnología": ("#4f8ef7", "#1e3a8a"),
    "Hogar": ("#f6b26b", "#b45309"),
    "Ropa": ("#e879a6", "#9d174d"),
    "Deportes": ("#4ade80", "#166534"),
    "Libros": ("#a78bfa", "#5b21b6"),
    "Juguetes": ("#fbbf24", "#c2410c"),
    "Salud y belleza": ("#5eead4", "#0f766e"),
}
COLOR_POR_DEFECTO = ("#94a3b8", "#334155")


def hex_a_rgb(color):
    color = color.lstrip("#")
    return tuple(int(color[i:i + 2], 16) for i in (0, 2, 4))


def degradado(claro, oscuro):
    """Degradado diagonal de la esquina superior izquierda (claro) a la inferior derecha (oscuro)."""
    a, b = hex_a_rgb(claro), hex_a_rgb(oscuro)
    base = Image.new("RGB", (TAM, TAM))
    pixeles = base.load()
    for y in range(TAM):
        for x in range(TAM):
            t = (x + y) / (2 * (TAM - 1))
            pixeles[x, y] = tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(3))
    return base


def partir_lineas(dibujo, texto, fuente, ancho_max):
    """Parte el texto en líneas que quepan en ancho_max (máximo 3 líneas, con '…' si sobra)."""
    palabras, lineas, actual = texto.split(), [], ""
    for palabra in palabras:
        prueba = (actual + " " + palabra).strip()
        if dibujo.textlength(prueba, font=fuente) <= ancho_max:
            actual = prueba
        else:
            lineas.append(actual)
            actual = palabra
    lineas.append(actual)
    if len(lineas) > 3:
        lineas = lineas[:3]
        lineas[2] = lineas[2].rstrip(".,") + "…"
    return lineas


def centrar_texto(dibujo, y, texto, fuente, color):
    ancho = dibujo.textlength(texto, font=fuente)
    dibujo.text(((TAM - ancho) / 2, y), texto, font=fuente, fill=color)


def generar(producto, fuentes):
    categoria = producto["categorias"][0]
    claro, oscuro = COLORES.get(categoria, COLOR_POR_DEFECTO)
    imagen = degradado(claro, oscuro).convert("RGBA")

    # Tarjeta blanca semitransparente
    capa = Image.new("RGBA", (TAM, TAM), (0, 0, 0, 0))
    ImageDraw.Draw(capa).rounded_rectangle((40, 40, TAM - 40, TAM - 40), radius=36, fill=(255, 255, 255, 235))
    imagen = Image.alpha_composite(imagen, capa)
    dibujo = ImageDraw.Draw(imagen)

    # Etiqueta de la categoría
    texto_cat = categoria.upper()
    ancho_cat = dibujo.textlength(texto_cat, font=fuentes["etiqueta"])
    x0 = (TAM - ancho_cat) / 2 - 16
    dibujo.rounded_rectangle((x0, 66, x0 + ancho_cat + 32, 104), radius=19, fill=hex_a_rgb(oscuro))
    centrar_texto(dibujo, 72, texto_cat, fuentes["etiqueta"], "white")

    # Ícono: se dibuja aparte en su tamaño nativo, se recorta a su caja real y se escala para centrarlo
    icono = producto.get("icono", "🛍️")
    lienzo = Image.new("RGBA", (300, 300), (0, 0, 0, 0))
    ImageDraw.Draw(lienzo).text((20, 20), icono, font=fuentes["emoji"], embedded_color=True)
    caja = lienzo.getbbox()
    if caja:
        recorte = lienzo.crop(caja)
        escala = ALTO_ICONO / max(recorte.width, recorte.height)
        recorte = recorte.resize((round(recorte.width * escala), round(recorte.height * escala)), Image.LANCZOS)
        imagen.alpha_composite(recorte, ((TAM - recorte.width) // 2, 250 - recorte.height // 2))

    # Nombre del producto (hasta 3 líneas)
    lineas = partir_lineas(dibujo, producto["nombre"], fuentes["nombre"], TAM - 140)
    y = 395 if len(lineas) < 3 else 380
    for linea in lineas:
        centrar_texto(dibujo, y, linea, fuentes["nombre"], (30, 41, 59))
        y += 44

    # Marca
    centrar_texto(dibujo, TAM - 92, "UniShop · demostración", fuentes["marca"], (100, 116, 139))

    destino = os.path.join(SALIDA, producto["imagen"])
    imagen.convert("RGB").save(destino, "PNG", optimize=True)
    return destino


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--fuente", default="C:/Windows/Fonts/segoeui.ttf")
    parser.add_argument("--fuente-negrita", default="C:/Windows/Fonts/seguisb.ttf")
    parser.add_argument("--fuente-emoji", default="C:/Windows/Fonts/seguiemj.ttf")
    args = parser.parse_args()

    fuentes = {
        "etiqueta": ImageFont.truetype(args.fuente_negrita, 20),
        "nombre": ImageFont.truetype(args.fuente_negrita, 34),
        "marca": ImageFont.truetype(args.fuente, 20),
        # Segoe UI Emoji solo dibuja a color en tamaños de su tabla de mapas de bits (109 es uno de ellos)
        "emoji": ImageFont.truetype(args.fuente_emoji, 109),
    }

    with open(CATALOGO, encoding="utf-8") as archivo:
        productos = json.load(archivo)["productos"]

    os.makedirs(SALIDA, exist_ok=True)
    for producto in productos:
        generar(producto, fuentes)
    print(f"Se generaron {len(productos)} imágenes en {os.path.relpath(SALIDA, RAIZ)}")


if __name__ == "__main__":
    main()
