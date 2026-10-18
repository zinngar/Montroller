package com.montroller.mixin;

import com.montroller.ControllerManager;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Shadow private double accumulatedDX;
    @Shadow private double accumulatedDY;

    @Shadow public abstract boolean isMouseGrabbed();

    // Runs once per frame, just before the game turns the camera by the accumulated mouse movement.
    @Inject(method = "handleAccumulatedMovement", at = @At("HEAD"))
    private void montroller$addControllerLook(CallbackInfo ci) {
        double[] look = ControllerManager.consumeLook();
        if (isMouseGrabbed()) {
            accumulatedDX += look[0];
            accumulatedDY += look[1];
        }
    }
}
