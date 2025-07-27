package net.szum123321.ariadne_glasses.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.szum123321.ariadne_glasses.client.OverlayRenderCallback;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Final
    @Shadow
    private MinecraftClient client;

    /*@Shadow private int scaledWidth;

    @Shadow private int scaledHeight;

    @Inject(method = "render", at = @At(value = "FIELD", ordinal = 0, opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/client/gui/hud/InGameHud;client:Lnet/minecraft/client/MinecraftClient;"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderPortalOverlay(F)V")))
    public void overlay(DrawContext context, float tickDelta, CallbackInfo ci) {
        ///OverlayRenderCallback.EVENT.invoker().onOverlayRender(matrixStack, tickDelta, client, scaledWidth, scaledHeight);
    }*/
}
