package com.montroller;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

public class MontrollerClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Options (and their key mappings) don't exist yet during client init,
		// so wait until the client has finished starting.
		ClientLifecycleEvents.CLIENT_STARTED.register(client -> ControllerManager.start());
	}
}
