package com.montroller.mixin;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MouseHandler.class)
public interface MouseAccessor {
    @Invoker("onMove")
    void invokeOnMove(long window, double x, double y, double dx, double dy);
}
