package net.szum123321.ariadne_glasses.client;


import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;

public interface OverlayRenderCallback {
    Event<OverlayRenderCallback> EVENT = EventFactory.createArrayBacked(OverlayRenderCallback.class, (listeners) -> (matrixStack, delta, client, scaledWidth, scaledHeight) -> {
        for (OverlayRenderCallback event : listeners) {
            event.onOverlayRender(matrixStack, delta, client, scaledWidth, scaledHeight);
        }
    });

    void onOverlayRender(MatrixStack matrixStack, float tickDelta, MinecraftClient client, float scaledWidth, float scaledHeight);

}
