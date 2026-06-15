# Damage System

## Archivos modificados
- `RasenganEntity.java` — 1 línea añadida
- `RasenganMod.java` — sistema de timer

## Cómo funciona

1. La `RasenganEntity` vuela hacia el objetivo.
2. Cuando choca contra él (`dist <= 1.8`), activa `grappling = true` y llama a `RasenganMod.onGrappleStart(target)`.
3. `onGrappleStart()` mete al jugador en un mapa `GRAPPLE_TIMERS` con 300 ticks restantes.
4. Cada tick del servidor, `onServerTick()` itera `GRAPPLE_TIMERS`:
   - Si el timer llega a 0 → se elimina (el grapple terminó)
   - Si el jugador murió o se desconectó → se elimina
   - Si el jugador está vivo → `target.hurt(outOfBorder, 75f)`
5. Un `LivingDamageEvent` handler resetea `invulnerableTime = 0` después de cada golpe.

## Código clave

### RasenganEntity.java:209
```java
if (target instanceof ServerPlayer sp) RasenganMod.onGrappleStart(sp);
```

### RasenganMod.java — onServerTick
```java
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
```

### RasenganMod.java — onLivingDamage
```java
if (INVULN_FIELD != null && event.getSource().is(DamageTypeTags.BYPASSES_COOLDOWN)) {
    try { INVULN_FIELD.setInt(event.getEntity(), 0); } catch (Exception ignored) {}
}
```

## Por qué funciona
- Sin escaneo de entidades, sin eventos de join/leave, sin tracking por ID
- El daño empieza en el único momento correcto: cuando el Rasengan impacta
- `outOfBorder` bypasea el cooldown de daño nativamente
- El `LivingDamageEvent` resetea el contador de invulnerabilidad que `hurt()` pone a 10
- 300 ticks = 15 segundos de daño continuo a 75 por tick
