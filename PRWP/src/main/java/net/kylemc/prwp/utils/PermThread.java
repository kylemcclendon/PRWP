package net.kylemc.prwp.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

public class PermThread implements Callable<Set<String>> {
	private final UUID id;
	private final String w;

	public PermThread(UUID pu, String world) {
		id = pu;
		w = world;
	}

	@Override
	public Set<String> call() {
		HashSet<String> perms = new HashSet<String>();
		perms = GetPermissions.collectPermissions(id, w);
		return perms;
	}
}