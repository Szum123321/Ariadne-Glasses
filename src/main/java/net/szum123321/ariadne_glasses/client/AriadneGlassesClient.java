package net.szum123321.ariadne_glasses.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.monarkhēs.myron.api.Myron;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.*;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.szum123321.ariadne_glasses.AriadneGlasses;
import net.szum123321.ariadne_glasses.component.StepMapComponent;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public class AriadneGlassesClient implements ClientModInitializer {
    private static final KeyBinding TOGGLE_DISPLAY_KEYBINDING = KeyBindingHelper.registerKeyBinding(
            new ToggleStickyTimedKeybinding("render", GLFW.GLFW_KEY_R, "abcd", 900));

    private static final KeyBinding SET_ROOT_KEYBINDING = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("reset", InputUtil.Type.SCANCODE, GLFW.GLFW_KEY_Y, "abcd")
    );

    private static final Identifier ARROW_ID = new Identifier(AriadneGlasses.MOD_ID, "models/misc/arrow");

    private static BakedModel ARROW_MODEL;

    private static final float STEP_PHASE_SHIFT = (float) (Math.PI / 6.0f); //Three steps per quarter
    private static final float FREQ = 0.055f;  //change of angle per tick
    private static final float AMPLITUDE = 0.085f;

    private final int renderDistance = 32;

    float resetTimer = -1;

    boolean hasGoggles = false;

    @Override
    public void onInitializeClient() {
        WorldRenderEvents.AFTER_ENTITIES.register(ctx -> {
            if(!TOGGLE_DISPLAY_KEYBINDING.isPressed() || ! hasGoggles) return;

            ctx.profiler().push("Ariadne_Render");

            ARROW_MODEL = Myron.getModel(ARROW_ID);

            Vec3d cameraPos = ctx.camera().getPos();

            StepMapComponent component = AriadneGlasses.STEP_MAP.get(MinecraftClient.getInstance().player);

            VertexConsumer consumer = ctx.consumers().getBuffer(RenderLayer.getSolid());

            component.getStepMap().query(ctx.camera().getBlockPos(), renderDistance)
                .forEach(i -> {
                    Vec3f pos = component.getStepList().get(i);

                    ctx.matrixStack().push();

                    //Deflection angle
                    float theta = AMPLITUDE * MathHelper.sin(i * STEP_PHASE_SHIFT + (ctx.world().getTime() + ctx.tickDelta()) * FREQ);

                    ctx.matrixStack().translate(
                            pos.getX() - cameraPos.x,
                            pos.getY() - cameraPos.y + 1 + AriadneGlasses.STEP_RADIUS.get() * theta,
                            pos.getZ() - cameraPos.z
                    );

                    //ctx.matrixStack().scale(0.4f, 0.4f, 0.4f);

                    if (i > 0) {
                        Vec3f prev = component.getStepList().get(i - 1);
                        float dx = prev.getX() - pos.getX();
                        float dy = pos.getY() - prev.getY();
                        float dz = prev.getZ() - pos.getZ();

                        ctx.matrixStack().multiply(Vec3f.POSITIVE_Y.getRadialQuaternion((float) ( MathHelper.HALF_PI - MathHelper.atan2(dz, dx))));
                        ctx.matrixStack().multiply(Vec3f.POSITIVE_X.getRadialQuaternion((float) ( MathHelper.HALF_PI + Math.atan(dy * MathHelper.fastInverseSqrt(dx*dx + dz*dz)) + theta )));
                    } else {
                        ctx.matrixStack().multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(180));
                        ctx.matrixStack().multiply(Vec3f.NEGATIVE_Y.getDegreesQuaternion(ctx.world().getTime() + ctx.tickDelta()));
                    }

                    //ctx.consumers().getBuffer(RenderLayer.getTranslucent())

                    //15728880
                    /*MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(
                            Blocks.END_ROD.getDefaultState(),
                            ctx.matrixStack(), ctx.consumers(), 0xFFFFFF, OverlayTexture.DEFAULT_UV);*/

                    MatrixStack.Entry entry = ctx.matrixStack().peek();

                    ARROW_MODEL.getQuads(null, null, ctx.world().random).forEach(quad -> {
                        consumer.quad(entry, quad, 1, 1, 1, 0xFFFFFF, OverlayTexture.DEFAULT_UV);
                    });

                    ctx.matrixStack().pop();
                });

            ctx.profiler().pop();
        });

        OverlayRenderCallback.EVENT.register((matrixStack, tickDelta, client, scaledWidth, scaledHeight) -> {
            if(hasGoggles && TOGGLE_DISPLAY_KEYBINDING.isPressed()) {
                renderTintedOverlay(client, scaledWidth, scaledHeight);
            }
        });

        HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> {
            if(resetTimer >= 0) {
                MinecraftClient client = MinecraftClient.getInstance();
                String text = "Time Left: " + (int)resetTimer;

                int w = client.getWindow().getScaledWidth(), h = client.getWindow().getScaledHeight();

                int n = client.textRenderer.getWidth(text);

                client.textRenderer.draw(matrixStack, text, (float) (w / 2 - n / 2), (float) (h - 60), 0xFFFFFF);
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if(client.player == null)  return;

            this.hasGoggles = client.player.getInventory().getArmorStack(3).isOf(AriadneGlasses.GLASSES);

            if(SET_ROOT_KEYBINDING.isPressed()) {
                if(resetTimer == -1) resetTimer = 15.0f;
                else if(resetTimer > 0) resetTimer = Math.max(0, resetTimer - (client.getLastFrameDuration() / 20.0f));

                if(resetTimer == 0) {
                    ClientPlayNetworking.send(AriadneGlasses.ARIADNE_RESET_PACKET, PacketByteBufs.empty());
                    resetTimer = -2; //this way player has to release the key
                }
            } else resetTimer = -1;
        });
    }

    //This code is a direct copy of InGameHud.renderPortalOverlay, just with tuned values
    private void renderTintedOverlay(MinecraftClient client, float scaledWidth, float scaledHeight) {
        client.getTextureManager().bindTexture(new Identifier("textures/block/lava_still.png"));

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();
        //rgba
        RenderSystem.setShaderColor(0.275f, 0.045f, 1f, 0.4f);
        RenderSystem.setShaderTexture(0, SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        //RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Sprite sprite = client.getBlockRenderManager().getModels().getModelParticleSprite(Blocks.LAVA.getDefaultState());

        float f = sprite.getMinU();
        float g = sprite.getMinV();
        float h = sprite.getMaxU();
        float i = sprite.getMaxV();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(0.0, scaledHeight, -90.0).texture(f, i).next();
        bufferBuilder.vertex(scaledWidth, scaledHeight, -90.0).texture(h, i).next();
        bufferBuilder.vertex(scaledWidth, 0.0, -90.0).texture(h, g).next();
        bufferBuilder.vertex(0.0, 0.0, -90.0).texture(f, g).next();

        tessellator.draw();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
