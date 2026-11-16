"""
Genera el diagrama entidad-relación de UniShop a partir de las tablas REALES de la base de datos.

1. Lee el esquema de la base `unishop` (information_schema) desde el contenedor de MySQL.
2. Escribe docs/diagrama-er.puml (PlantUML, notación de pata de gallo).
3. Si se indica el .jar de PlantUML, genera docs/diagrama-er.png (usa el motor "smetana",
   que no necesita Graphviz).

Requisitos: Docker con el contenedor de MySQL encendido y la base creada por Hibernate
(por ejemplo, después de `mvn -pl persistencia test`).

Uso:
    python docs/generar-diagrama-er.py [ruta/plantuml.jar]

El .jar de PlantUML se puede bajar con Maven:
    mvn dependency:get -Dartifact=net.sourceforge.plantuml:plantuml:1.2023.13
    (queda en ~/.m2/repository/net/sourceforge/plantuml/plantuml/1.2023.13/)
"""
import subprocess
import sys
from collections import defaultdict
from pathlib import Path

CONTENEDOR = "unimotor-mysql"
BASE = "unishop"
USUARIO = "unishop"
CLAVE = "unishop"
IGNORAR = {"hibernate_sequence"}

CARPETA = Path(__file__).resolve().parent
PUML = CARPETA / "diagrama-er.puml"


def consultar(sql):
    """Ejecuta una consulta en MySQL (dentro del contenedor) y devuelve las filas como listas."""
    salida = subprocess.run(
        ["docker", "exec", CONTENEDOR, "mysql", f"-u{USUARIO}", f"-p{CLAVE}", BASE, "-N", "-B", "-e", sql],
        capture_output=True, text=True, encoding="utf-8", check=True).stdout
    return [linea.split("\t") for linea in salida.splitlines() if linea]


def leer_esquema():
    columnas = defaultdict(list)
    for tabla, columna, tipo, nulo, clave in consultar(
            "select table_name, column_name, column_type, is_nullable, column_key "
            "from information_schema.columns where table_schema = database() "
            "order by table_name, ordinal_position"):
        if tabla not in IGNORAR:
            columnas[tabla].append((columna, tipo, nulo == "YES", clave))

    llaves_foraneas = consultar(
        "select table_name, column_name, referenced_table_name "
        "from information_schema.key_column_usage "
        "where table_schema = database() and referenced_table_name is not null "
        "order by table_name, column_name")

    unicas = {(t, c) for t, c in consultar(
        "select s.table_name, s.column_name from information_schema.statistics s "
        "where s.table_schema = database() and s.non_unique = 0 and s.index_name <> 'PRIMARY' "
        "and (select count(*) from information_schema.statistics s2 where s2.table_schema = s.table_schema "
        "     and s2.table_name = s.table_name and s2.index_name = s.index_name) = 1")}
    return columnas, llaves_foraneas, unicas


def generar_puml(columnas, llaves_foraneas, unicas):
    fks = {(t, c) for t, c, _ in llaves_foraneas}
    lineas = [
        "@startuml diagrama-er",
        "' Generado por docs/generar-diagrama-er.py a partir de la base de datos. No editar a mano.",
        "!pragma layout smetana",
        "hide circle",
        "skinparam shadowing false",
        "skinparam defaultFontName Arial",
        "skinparam entity {",
        "  BackgroundColor #FDFDFD",
        "  BorderColor #555555",
        "}",
        "title UniShop - Diagrama entidad-relacion",
        "",
    ]
    for tabla, cols in sorted(columnas.items()):
        lineas.append(f"entity {tabla} {{")
        pks = [c for c in cols if c[3] == "PRI"]
        resto = [c for c in cols if c[3] != "PRI"]
        for columna, tipo, _, _ in pks:
            marca = " <<FK>>" if (tabla, columna) in fks else ""
            lineas.append(f"  * **{columna}** : {tipo} <<PK>>{marca}")
        lineas.append("  --")
        for columna, tipo, nulo, _ in resto:
            marcas = []
            if (tabla, columna) in fks:
                marcas.append("<<FK>>")
            if (tabla, columna) in unicas:
                marcas.append("<<UK>>")
            obligatorio = "" if nulo else "* "
            lineas.append(f"  {obligatorio}{columna} : {tipo} {' '.join(marcas)}".rstrip())
        lineas.append("}")
        lineas.append("")

    for tabla, columna, referenciada in llaves_foraneas:
        nulo = next(c[2] for c in columnas[tabla] if c[0] == columna)
        lado_padre = "|o" if nulo else "||"
        lineas.append(f"{referenciada} {lado_padre}--o{{ {tabla} : {columna}")

    lineas += [
        "",
        "legend right",
        "  * = obligatorio (NOT NULL)",
        "  PK = llave primaria, FK = llave foranea, UK = valor unico",
        "  ||--o{ : uno a muchos (FK obligatoria)",
        "  |o--o{ : cero o uno a muchos (FK opcional)",
        "endlegend",
        "@enduml",
        "",
    ]
    return "\n".join(lineas)


def main():
    columnas, llaves_foraneas, unicas = leer_esquema()
    PUML.write_text(generar_puml(columnas, llaves_foraneas, unicas), encoding="utf-8")
    print(f"{PUML.name}: {len(columnas)} tablas, {len(llaves_foraneas)} llaves foraneas")

    if len(sys.argv) > 1:
        subprocess.run(["java", "-jar", sys.argv[1], "-tpng", "-charset", "UTF-8", str(PUML)], check=True)
        print("diagrama-er.png generado")


if __name__ == "__main__":
    main()
