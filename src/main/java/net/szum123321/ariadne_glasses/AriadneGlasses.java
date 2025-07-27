package net.szum123321.ariadne_glasses;

import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.szum123321.ariadne_glasses.network.AriadneResetPacket;
import org.ladysnake.cca.api.v3.component.*;
import org.ladysnake.cca.api.v3.entity.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.szum123321.ariadne_glasses.component.StepMapComponent;
import net.szum123321.ariadne_glasses.server.DifficultySetCallback;

import java.util.concurrent.atomic.AtomicInteger;

public class AriadneGlasses implements ModInitializer, EntityComponentInitializer {
    public static final String MOD_ID = "ariadne_glasses";
    public static final Item GLASSES = new Item(new Item.Settings().equipmentSlot((entity, stack) -> EquipmentSlot.HEAD));
    public static final ComponentKey<StepMapComponent> STEP_MAP = ComponentRegistry.getOrCreate(new Identifier(MOD_ID, "step_map"), StepMapComponent.class);
    public static final Identifier ARIADNE_RESET_PACKET = new Identifier(MOD_ID, "packet_reset");
    public static AtomicInteger STEP_RADIUS = new AtomicInteger(3), MERGE_RADIUS = new AtomicInteger(8);

    @Override
    public void onInitialize() {
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "glasses"), GLASSES);

        PayloadTypeRegistry.playC2S().register(AriadneResetPacket.ID, AriadneResetPacket.CODEC);

        ServerLifecycleEvents.SERVER_STARTED.register(server -> STEP_RADIUS.set((server.getSaveProperties().getDifficulty().getId() + 1) * 2));

        DifficultySetCallback.EVENT.register((server, difficulty) -> STEP_RADIUS.set((difficulty.getId() + 1) * 2));

        ServerPlayNetworking.registerGlobalReceiver(AriadneResetPacket.ID,
                (payload, context) -> context.server().execute(() -> STEP_MAP.maybeGet(context.player()).ifPresent(StepMapComponent::clear)));
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(AriadneGlasses.STEP_MAP, StepMapComponent::new, RespawnCopyStrategy.LOSSLESS_ONLY);
    }
}
