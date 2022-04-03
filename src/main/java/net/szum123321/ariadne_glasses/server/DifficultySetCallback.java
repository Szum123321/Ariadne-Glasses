package net.szum123321.ariadne_glasses.server;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;

public interface DifficultySetCallback {
    Event<DifficultySetCallback> EVENT = EventFactory.createArrayBacked(DifficultySetCallback.class, (listeners) -> (server, difficulty) -> {
        for (DifficultySetCallback event : listeners) {
            event.onDifficultySet(server, difficulty);
        }
    });

    void onDifficultySet(MinecraftServer server, Difficulty difficulty);
}
