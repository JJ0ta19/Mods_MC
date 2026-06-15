# Cómo agregar sonidos al mod

Basado en el sistema de sonidos del mod Rasengan (Forge 1.20.1, Mojang mappings).

---

## Archivos que necesitas tocar (4 archivos)

| Archivo | Propósito |
|---------|-----------|
| `ModSounds.java` | Registrar el SoundEvent |
| `sounds.json` | Mapear el ID del sonido al archivo .ogg |
| `.ogg` file | El audio en sí (en `assets/modid/sounds/`) |
| Tu código Java | Reproducir el sonido con `playSound()` |

---

## Paso 1: Prepara tu audio

- Formato: **OGG Vorbis** (`.ogg`)
- Herramientas: Audacity (gratis), FFmpeg, o cualquier conversor online
- Ejemplo: `ffmpeg -i input.mp3 output.ogg`

Guarda el archivo en:

```
src/main/resources/assets/<modid>/sounds/entity/<nombre>.ogg
```

Ejemplo del Rasengan: `assets/rasengan/sounds/entity/rasengan_grapple.ogg`

---

## Paso 2: Registra el SoundEvent en ModSounds.java

```java
package com.rasengan;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, RasenganMod.MODID);

    public static final RegistryObject<SoundEvent> RASENGAN_GRAPPLE =
            SOUNDS.register("rasengan_grapple",
                    () -> SoundEvent.createVariableRangeEvent(
                            new ResourceLocation(RasenganMod.MODID, "rasengan_grapple")));

    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }
}
```

**Claves:**
- El primer argumento de `register()` es el **registry name** (ej: `"rasengan_grapple"`)
- `ResourceLocation` usa modid + ese mismo nombre
- `createVariableRangeEvent()` crea un sonido con rango variable (recomendado)

En tu clase principal, registra `ModSounds.SOUNDS` en el mod event bus:

```java
ModSounds.SOUNDS.register(modEventBus);
```

---

## Paso 3: Crea sounds.json

Ubicación: `src/main/resources/assets/<modid>/sounds.json`

```json
{
    "rasengan_grapple": {
        "subtitle": "Rasengan impacta",
        "sounds": [
            "rasengan:entity/rasengan_grapple"
        ]
    }
}
```

**Estructura:**
- La clave `"rasengan_grapple"` debe coincidir con el registry name
- `"subtitle"`: texto que aparece en opciones de sonido
- `"sounds"`: array de rutas al archivo .ogg (sin extensión, con `modid:` al inicio)
  - Ruta relativa a `assets/<modid>/sounds/`
  - Ejemplo: `"rasengan:entity/rasengan_grapple"` → apunta a `sounds/entity/rasengan_grapple.ogg`

---

## Paso 4: Reproduce el sonido en tu código

```java
// En el servidor (ServerLevel), para que todos los jugadores lo escuchen:
level().playSound(null, x, y, z,
        ModSounds.RASENGAN_GRAPPLE.get(), SoundSource.PLAYERS, 3.0f, 1.0f);
```

**Parámetros de `playSound()`:**

| Parámetro | Descripción |
|-----------|-------------|
| `null` | Player (null = todos escuchan) |
| `x, y, z` | Posición 3D del sonido |
| `SoundEvent` | El sonido registrado (`.get()`) |
| `SoundSource` | Categoría (PLAYERS, HOSTILE, WEATHER, MASTER, etc.) |
| `volume` | 1.0 = normal, 3.0 = fuerte |
| `pitch` | 1.0 = normal, 0.5 = grave, 2.0 = agudo |

También puedes usar sonidos **vanilla** directamente:

```java
level().playSound(null, pos.x, pos.y, pos.z,
        SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 4.0f, 0.5f);
```

---

## Resumen del flujo completo

```
1. Tienes un .ogg
       │
       ▼
2. Lo pones en assets/<modid>/sounds/entity/mi_sonido.ogg
       │
       ▼
3. Registras ModSounds.MI_SONIDO en ModSounds.java (registry name = "mi_sonido")
       │
       ▼
4. Agregas "mi_sonido" a sounds.json apuntando a "<modid>:entity/mi_sonido"
       │
       ▼
5. En tu codigo: ModSounds.MI_SONIDO.get() para reproducirlo
```

---

## Notas importantes

- El **registry name** en Java, la **clave en sounds.json**, y el **ResourceLocation** deben ser **idénticos**
- El archivo .ogg debe existir en la ruta exacta que pusiste en sounds.json
- Usa `SoundSource.PLAYERS` para sonidos de entidades/jugadores
- Los sonidos se reproducen en **server side** para que todos los escuchen
- Si solo quieres que un jugador escuche, usa `target.playSound()` en vez de `level().playSound()`
