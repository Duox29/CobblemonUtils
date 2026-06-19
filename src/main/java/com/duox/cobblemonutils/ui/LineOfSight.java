package com.duox.cobblemonutils.ui;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class LineOfSight {
    private static final Map<Integer, Boolean> CACHE = new HashMap<>();
    private static long cacheTick = Long.MIN_VALUE;

    private LineOfSight() {}

    public static boolean isVisible(Entity entity) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && client.level != null) {
            long tick = client.level.getGameTime();
            if (tick != cacheTick) {
                CACHE.clear();
                cacheTick = tick;
            }
            return CACHE.computeIfAbsent(entity.getId(), (id) -> compute(client, entity));
        } else {
            return false;
        }
    }

    private static boolean compute(Minecraft client, Entity entity) {
        Entity viewer = (client.getCameraEntity() != null ? client.getCameraEntity() : client.player);
        Vec3 from = viewer.getEyePosition();
        Vec3 center = entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D);
        return clearPath(viewer, from, entity.getEyePosition()) || clearPath(viewer, from, center);
    }

    private static boolean clearPath(Entity viewer, Vec3 from, Vec3 to) {
        BlockHitResult hit = viewer.level().clip(new ClipContext(from, to, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, viewer));
        return hit.getType() == HitResult.Type.MISS;
    }
}