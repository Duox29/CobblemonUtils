package com.duox.cobblemonutils.ui;

import com.duox.cobblemonutils.config.Config;
import com.duox.cobblemonutils.config.ConfigManager;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Matrix4f;

public class PokeFinderRenderer {

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        // Render sau khi các block trong suốt và entity đã được render xong
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Config config = ConfigManager.getConfig();
        if (!config.enablePokeFinder) return;
        if (!config.enableTraceRay && !config.enableBeaconBeam) return;

        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) return;

        Vec3 cameraPos = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer lineBuilder = client.renderBuffers().bufferSource().getBuffer(RenderType.lines());

        for (Entity entity : client.level.entitiesForRendering()) {
            if (entity instanceof PokemonEntity pokemonEntity) {
                int color = PokeFinderFilter.getHighlightColor(pokemonEntity);
                if (color != 0) {
                    float r = ((color >> 16) & 0xFF) / 255f;
                    float g = ((color >> 8) & 0xFF) / 255f;
                    float b = (color & 0xFF) / 255f;

                    Vec3 entityPos = entity.getPosition(event.getPartialTick());

                    if (config.enableTraceRay) {
                        Vec3 eyePos = client.player.getEyePosition(event.getPartialTick());
                        Vec3 lookVec = client.player.getLookAngle();
                        Vec3 startPos = eyePos.add(lookVec.scale(2.0));
                        lineBuilder.vertex(matrix, (float) startPos.x, (float) startPos.y, (float) startPos.z)
                                .color(r, g, b, 1.0f).normal(0, 1, 0).endVertex();

                        lineBuilder.vertex(matrix, (float) entityPos.x, (float) (entityPos.y + entity.getBbHeight() / 2.0), (float) entityPos.z)
                                .color(r, g, b, 1.0f).normal(0, 1, 0).endVertex();
                    }

                    if (config.enableBeaconBeam) {
                        lineBuilder.vertex(matrix, (float) entityPos.x, (float) entityPos.y, (float) entityPos.z)
                                .color(r, g, b, 1.0f).normal(0, 1, 0).endVertex();

                        lineBuilder.vertex(matrix, (float) entityPos.x, (float) (entityPos.y + 320), (float) entityPos.z)
                                .color(r, g, b, 1.0f).normal(0, 1, 0).endVertex();
                    }
                }
            }
        }

        client.renderBuffers().bufferSource().endBatch(RenderType.lines());
        poseStack.popPose();
    }
}