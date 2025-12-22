package com.montroller;

import net.fabricmc.api.ClientModInitializer;

public class MontrollerClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		ControllerManager.start();
	}
}