package net.szum123321.ariadne_glasses;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.szum123321.ariadne_glasses.component.StepMapComponent;
import net.szum123321.ariadne_glasses.server.DifficultySetCallback;

import java.util.concurrent.atomic.AtomicInteger;

public class AriadneGlasses implements ModInitializer, EntityComponentInitializer {
    public static final String MOD_ID = "ariadne_glasses";
    public static final Item GLASSES = new Item(new FabricItemSettings().group(ItemGroup.TOOLS).equipmentSlot(stack -> EquipmentSlot.HEAD));

    public static final ComponentKey<StepMapComponent> STEP_MAP = ComponentRegistry.getOrCreate(new Identifier(MOD_ID, "step_map"), StepMapComponent.class);

    public static final Identifier ARIADNE_RESET_PACKET = new Identifier(MOD_ID, "packet_reset");

    public static AtomicInteger STEP_RADIUS = new AtomicInteger(3), MERGE_RADIUS = new AtomicInteger(8);

    @Override
    public void onInitialize() {
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "glasses"), GLASSES);

        ServerLifecycleEvents.SERVER_STARTED.register(server -> STEP_RADIUS.set((server.getSaveProperties().getDifficulty().getId() + 1) * 2));

        DifficultySetCallback.EVENT.register((server, difficulty) -> STEP_RADIUS.set((difficulty.getId() + 1) * 2));

        ServerPlayNetworking.registerGlobalReceiver(ARIADNE_RESET_PACKET,
                (server, player, handler, buf, responseSender) -> server.execute(() -> STEP_MAP.maybeGet(player).ifPresent(StepMapComponent::clear))
        );
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(AriadneGlasses.STEP_MAP, StepMapComponent::new, RespawnCopyStrategy.LOSSLESS_ONLY);
    }
}
