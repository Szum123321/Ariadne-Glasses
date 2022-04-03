package net.szum123321.ariadne_glasses.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.SaveProperties;
import net.szum123321.ariadne_glasses.server.DifficultySetCallback;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Shadow @Final protected SaveProperties saveProperties;

    @Inject(method = "setDifficulty", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/SaveProperties;setDifficulty(Lnet/minecraft/world/Difficulty;)V"))
    void onDifficultySet(Difficulty difficulty, boolean forceUpdate, CallbackInfo ci) {
        DifficultySetCallback.EVENT.invoker().onDifficultySet((MinecraftServer)(Object)this, saveProperties.isHardcore() ? Difficulty.HARD : difficulty);
    }
}
