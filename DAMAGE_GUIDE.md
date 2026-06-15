# Cómo implementar daño por tick sin invulnerabilidad

## Requisitos
- Forge 1.20.1 (Mojang mappings)
- Una entidad que detecta un "impacto" o "grapple"

## Paso 1: En tu entidad

Cuando la entidad impacta al jugador (ej: `dist <= IMPACT_DIST`), añade UNA SOLA línea:

```java
if (target instanceof ServerPlayer sp) TuMod.onGrappleStart(sp);
```

## Paso 2: En la clase de tu mod

### 2a. Añade un mapa estático

```java
private static final Map<UUID, Integer> GRAPPLE_TIMERS = new HashMap<>();
```

### 2b. Método estático que activa el daño

```java
public static void onGrappleStart(ServerPlayer target) {
    GRAPPLE_TIMERS.put(target.getUUID(), 300); // 300 ticks = 15 segundos
}
```

### 2c. En el ServerTickEvent

```java
@SubscribeEvent
public void onServerTick(TickEvent.ServerTickEvent event) {
    if (event.phase != TickEvent.Phase.END) return;

    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
    if (server == null) return;

    for (UUID uuid : new ArrayList<>(GRAPPLE_TIMERS.keySet())) {
        int ticks = GRAPPLE_TIMERS.get(uuid);
        if (--ticks <= 0) { GRAPPLE_TIMERS.remove(uuid); continue; }
        GRAPPLE_TIMERS.put(uuid, ticks);

        ServerPlayer target = (ServerPlayer) server.getPlayerList().getPlayer(uuid);
        if (target == null || !target.isAlive() || target.isCreative()) {
            GRAPPLE_TIMERS.remove(uuid);
            continue;
        }

        target.hurt(server.overworld().damageSources().outOfBorder(), 75.0f);
    }
}
```

### 2d. Handler que resetea invulnerabilidad

```java
private static java.lang.reflect.Field INVULN_FIELD;

// En el constructor:
try {
    INVULN_FIELD = LivingEntity.class.getDeclaredField("invulnerableTime");
    INVULN_FIELD.setAccessible(true);
} catch (Exception e) {
    try {
        INVULN_FIELD = LivingEntity.class.getDeclaredField("noDamageTicks");
        INVULN_FIELD.setAccessible(true);
    } catch (Exception ignored) {}
}

// En el handler:
@SubscribeEvent
public void onLivingDamage(LivingDamageEvent event) {
    if (INVULN_FIELD != null && event.getSource().is(DamageTypeTags.BYPASSES_COOLDOWN)) {
        try { INVULN_FIELD.setInt(event.getEntity(), 0); } catch (Exception ignored) {}
    }
}
```

## Explicación

| Componente | Por qué |
|---|---|
| `Map<UUID, Integer>` | Timer por jugador, sin escanear entidades |
| `outOfBorder()` | Daño que bypasea cooldown por tag vanilla |
| `LivingDamageEvent` | Resetea el contador que `hurt()` pone a 10 |
| Llamada desde la entidad | El daño empieza EXACTAMENTE cuando impacta |

## Lo que EVITAMOS
- ❌ Escanear entidades con `getAll()` (falla en Mohist)
- ❌ Trackear entidades por ID numérico (no confiable)
- ❌ Eventos `EntityJoinLevelEvent`/`EntityLeaveLevelEvent` (comportamiento impredecible en hybrids)
- ❌ Bucles de daño sin control (usa timer con límite de ticks)
