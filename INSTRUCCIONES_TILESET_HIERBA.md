# Instrucciones para Configurar Tilesets de Hierba

## Configuración en Tiled Map Editor

Para que los Pokémon aparezcan cuando el jugador camina sobre la hierba, necesitas configurar los tilesets en Tiled Map Editor de la siguiente manera:

### Opción 1: Propiedad en el Tile Individual

1. Abre tu mapa `.tmx` en Tiled Map Editor
2. Selecciona el tile de hierba que quieras usar
3. En el panel de propiedades (Properties), agrega una nueva propiedad:
   - **Nombre de la propiedad**: `tipo`
   - **Tipo**: `string`
   - **Valor**: `hierba` o `grass`

### Opción 2: Propiedad en la Capa Completa

1. Abre tu mapa `.tmx` en Tiled Map Editor
2. Selecciona la capa que contiene los tiles de hierba
3. En el panel de propiedades de la capa, agrega:
   - **Nombre de la propiedad**: `tipo`
   - **Tipo**: `string`
   - **Valor**: `hierba` o `grass`

### Opción 3: Propiedad en el Tileset

1. Abre tu tileset `.tsx` en Tiled Map Editor
2. Selecciona el tile de hierba en el tileset
3. En el panel de propiedades del tile, agrega:
   - **Nombre de la propiedad**: `tipo`
   - **Tipo**: `string`
   - **Valor**: `hierba` o `grass`

## Notas Importantes

- El sistema detecta tiles con la propiedad `tipo` igual a `"hierba"` o `"grass"` (no distingue mayúsculas/minúsculas)
- Puedes usar cualquiera de las tres opciones, o combinarlas
- La prioridad es: Tile individual > Capa > Tileset
- El sistema verifica automáticamente cuando el jugador termina de moverse a una nueva posición
- La probabilidad de encuentro es del 15% por cada paso sobre hierba

## Personalización

Si quieres cambiar la probabilidad de encuentro, edita el archivo `SpawnPokemon.java` y modifica la constante:

```java
private static final double PROBABILIDAD_ENCUENTRO = 0.15; // Cambia este valor (0.0 a 1.0)
```

## Ejemplo Visual en Tiled

```
Capa "Hierba" (Layer Properties)
├── tipo: "hierba"
└── Tiles:
    ├── Tile 1 (tipo: "hierba") ← Detectado
    ├── Tile 2 (sin propiedad) ← Detectado por capa
    └── Tile 3 (tipo: "piedra") ← NO detectado
```

¡Listo! Ahora cuando el jugador camine sobre tiles con la propiedad `tipo = "hierba"`, tendrá una probabilidad del 15% de encontrar un Pokémon salvaje.

