package com.rosamod;

import net.minecraft.server.level.ServerPlayer;
import java.util.ArrayList;
import java.util.List;

public class RitualManager {
    private static final List<Ritual> activeRituals = new ArrayList<>();

    public static void startRitual(ServerPlayer target) {
        Ritual ritual = new Ritual(target);
        activeRituals.add(ritual);
        ritual.start();
    }

    public static void tick() {
        activeRituals.removeIf(ritual -> {
            ritual.tick();
            return ritual.isFinished();
        });
    }

    public static void removeRitual(Ritual ritual) {
        activeRituals.remove(ritual);
    }
}
