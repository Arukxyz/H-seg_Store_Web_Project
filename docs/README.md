# Documentación técnica · Sistema HÖSÉG

El sistema está formado por dos aplicaciones que comparten una base de datos PostgreSQL en Supabase:

| Sistema | Repositorio | Tecnología |
|---|---|---|
| **Portal web** (clientes) | [`Arukxyz/H-seg_Store_Web_Project`](https://github.com/Arukxyz/H-seg_Store_Web_Project) (este) | Java 21, Spring Boot 3.4, Thymeleaf, Bootstrap 5 |
| **Módulo de escritorio SEGITD-HÖSÉG** (personal interno) | [`Arukxyz/Hoseg_Store`](https://github.com/Arukxyz/Hoseg_Store) | Java 25, Swing (FlatLaf), JDBC |

**Por dónde empezar:** [arquitectura.md](arquitectura.md) explica cómo encajan las dos partes. Después, el README del sistema en el que vayas a trabajar.

## Índice

### Visión del sistema completo
| Documento | Contenido |
|---|---|
| [arquitectura.md](arquitectura.md) | Componentes, capas, modelo de datos, quién escribe cada tabla, ciclo de vida del pedido, reglas de integración, seguridad y estado de implementación |

### Portal web (este repositorio)
| Documento | Contenido |
|---|---|
| [../README.md](../README.md) | Requisitos, configuración de credenciales, ejecución y estructura del código |
| [diagramas/casos-de-uso-web.md](diagramas/casos-de-uso-web.md) | Actores, requerimientos funcionales y matriz de casos de uso del portal (diagrama en `casos-de-uso-web.drawio`) |
| [diagramas/mapas-navegacion.drawio](diagramas/mapas-navegacion.drawio) | Mapas de navegación del portal y del escritorio (se abre en [draw.io](https://app.diagrams.net)) |
| [brief-diseno-home.md](brief-diseno-home.md) | Brief de diseño de la página principal: público, requisitos, estructura y sistema visual |
| [diseno/mapeo-figma-thymeleaf.md](diseno/mapeo-figma-thymeleaf.md) | Del prototipo de Figma a las plantillas Thymeleaf: correspondencia de componentes y decisiones tomadas |
| [guia-plantillas.md](guia-plantillas.md) | Cómo crear una página nueva: fragmentos, clases CSS, rutas y servicios disponibles |
| [prototipo/reparto-prototipo-figma.md](prototipo/reparto-prototipo-figma.md) | Reparto del prototipo del resto de pantallas entre el equipo |

### Módulo de escritorio ([`Hoseg_Store`](https://github.com/Arukxyz/Hoseg_Store))
| Documento | Contenido |
|---|---|
| [README.md](https://github.com/Arukxyz/Hoseg_Store/blob/main/README.md) | Requisitos, scripts de base de datos, configuración, ejecución y archivos que genera |
| [SEGITD-HOSEG.md](https://github.com/Arukxyz/Hoseg_Store/blob/main/SEGITD-HOSEG.md) | Especificación: requisitos RF/RNF, esquema, autenticación, pantallas, reglas de negocio y flujo de demostración |
| [GUIA_PRUEBAS_SISTEMA.md](https://github.com/Arukxyz/Hoseg_Store/blob/main/GUIA_PRUEBAS_SISTEMA.md) | Guía de pruebas del sistema |
| [`src/main/resources/sql/`](https://github.com/Arukxyz/Hoseg_Store/tree/main/src/main/resources/sql) | **Esquema de la base de datos** (`01_schema.sql`) y scripts de datos y migración `02`..`06` |

## Convenciones de la documentación

- Todo en **Markdown**, versionado junto al código: si cambia el comportamiento, se actualiza el documento en el mismo commit.
- Diagramas de arquitectura en **Mermaid** (GitHub los dibuja solo); diagramas de casos de uso y navegación en **draw.io**, con su script generador al lado.
- Nada de credenciales en la documentación: solo nombres de variables y archivos `.example`.
