# Guía: Cómo Abrir los Mapas en Tiled Map Editor

## 📋 Requisitos Previos

1. **Tiled Map Editor** instalado (descarga gratuita desde: https://www.mapeditor.org/)
2. Todos los archivos del proyecto en sus ubicaciones correctas

## 📁 Estructura de Archivos Necesarios

Para abrir correctamente los mapas, necesitas tener esta estructura:

```
assets/
├── MapaVerdePokemon.tmx       ← Archivo del mapa principal (verde)
├── MapaAzulPokemon.tmx        ← Archivo del mapa secundario (azul)
│
├── TexturasVerde.tsx          ← Tileset de texturas base
├── PokebolaVerde.tsx          ← Tileset de pokebolas
├── NPC.tsx                    ← Tileset de NPCs/personajes
│
└── TileSets/                  ← Carpeta con las imágenes de tilesets
    ├── TilesetPokemon.png     ← Imagen principal (usada por TexturasVerde.tsx)
    ├── PokebolasTiled.png     ← Imagen de pokebolas (usada por PokebolaVerde.tsx)
    └── Personajes.png         ← Imagen de NPCs (usada por NPC.tsx)
```

## 🚀 Pasos para Abrir un Mapa en Tiled

### Opción 1: Abrir directamente (Método Recomendado)

1. **Abre Tiled Map Editor**

2. **Menú**: `File` → `Open File...` (o `Ctrl+O`)

3. **Navega** a la carpeta `assets/` del proyecto

4. **Selecciona** uno de estos archivos:
   - `MapaVerdePokemon.tmx` - Mapa principal verde
   - `MapaAzulPokemon.tmx` - Mapa secundario azul

5. **¡Listo!** Tiled debería cargar automáticamente todos los tilesets referenciados

### Opción 2: Arrastrar y Soltar

1. **Abre Tiled Map Editor**

2. **Arrastra** el archivo `.tmx` desde el explorador de archivos
   - Ejemplo: `assets/MapaVerdePokemon.tmx`

3. **Suéltalo** en la ventana de Tiled

4. **¡Listo!** El mapa se abrirá automáticamente

## ⚠️ Solución de Problemas

### Problema: "Tileset not found" o "Image not found"

**Causa**: Los archivos `.tsx` o las imágenes no están en las rutas correctas.

**Solución**:
1. Verifica que todos los archivos `.tsx` estén en `assets/`
2. Verifica que la carpeta `TileSets/` exista dentro de `assets/`
3. Verifica que las imágenes `.png` estén en `assets/TileSets/`

### Problema: Tilesets aparecen vacíos

**Causa**: Las rutas relativas en los archivos `.tsx` no son correctas.

**Solución**:
1. Abre el archivo `.tsx` (ej: `TexturasVerde.tsx`) en un editor de texto
2. Verifica que la ruta de la imagen sea: `../TileSets/NombreImagen.png`
3. Si necesitas cambiar la ruta, en Tiled: `Tilesets` → Selecciona el tileset → `Edit` → `Image` → Cambia la ruta

## 📝 Información Técnica

### Archivos del Mapa

#### `MapaVerdePokemon.tmx`
- **Tamaño**: 50x40 tiles
- **Tileset principal**: `TexturasVerde.tsx`
- **Tilesets adicionales**: 
  - `PokebolaVerde` (embedded)
  - `NPC.tsx`

#### `MapaAzulPokemon.tmx`
- **Tamaño**: 40x30 tiles
- **Tileset principal**: `TexturasVerde.tsx`
- **Tilesets adicionales**:
  - `PokebolaVerde.tsx`
  - `NPC.tsx`

### Tilesets Externos (.tsx)

Los archivos `.tsx` son **tilesets externos** que contienen:
- Referencia a la imagen del tileset
- Propiedades de los tiles (como `Tipo`, `Item`, `NPC`, etc.)
- Metadata del tileset (tamaño de tile, spacing, margin)

### Rutas Relativas

Las rutas en los archivos `.tsx` son **relativas** al archivo `.tmx`:
- `../TileSets/TilesetPokemon.png` significa: "sube un nivel desde `assets/` y entra a `TileSets/`"

## 🎮 Consejos de Edición

1. **Capa de Patrones 1**: Suelo/base del mapa
2. **Capa de Patrones 2**: Objetos recogibles (pokebolas, items)
3. **Portal**: Capa de objetos para portales entre mapas
4. **NPC**: Capa para colocar NPCs (tiles con propiedad `NPC`)

### Propiedades Importantes de Tiles

- **`tipo`**: Define el comportamiento (ej: "inicio", "recogible", "hierba")
- **`Tipo`**: Tipo de pokeball (ej: "PokeballCura", "PokeballEXP")
- **`Item`**: Nombre del item
- **`NPC`**: Tipo de NPC (ej: "Enemigo", "Civil")

## 📂 Ubicación Completa de Archivos

**Desde la raíz del proyecto:**
```
Pokemon-Arceus-Java/
└── assets/
    ├── MapaVerdePokemon.tmx
    ├── MapaAzulPokemon.tmx
    ├── TexturasVerde.tsx
    ├── PokebolaVerde.tsx
    ├── NPC.tsx
    └── TileSets/
        ├── TilesetPokemon.png
        ├── PokebolasTiled.png
        └── Personajes.png
```

## ✅ Checklist antes de abrir

- [ ] Tiled Map Editor está instalado
- [ ] La carpeta `assets/` existe
- [ ] El archivo `.tmx` que quieres abrir está en `assets/`
- [ ] Los archivos `.tsx` están en `assets/`
- [ ] La carpeta `TileSets/` existe dentro de `assets/`
- [ ] Las imágenes `.png` están en `assets/TileSets/`

---

**Nota**: Si todos los archivos están en sus ubicaciones correctas según la estructura del proyecto, Tiled debería abrir los mapas sin problemas.
