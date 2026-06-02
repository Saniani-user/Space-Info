package com.alex.app;

import java.lang.instrument.Instrumentation;

import net.bytebuddy.agent.ByteBuddyAgent;

public class MemoryMeasurer {
	private static Instrumentation instrumentation;
	static {
		instrumentation = ByteBuddyAgent.install();
	}
	
	public static long getObjectSize(Object obj) {
		if (instrumentation == null) {
			throw new IllegalStateException("Instrumentation не инициализирован");
		}
		return instrumentation.getObjectSize(obj);
	}
}
