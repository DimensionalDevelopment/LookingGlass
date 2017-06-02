package com.xcompwiz.lookingglass.imc;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.xcompwiz.lookingglass.log.LoggerUtils;
import net.minecraftforge.fml.common.event.FMLInterModComms;

public class IMCHandler {
	public interface IMCProcessor {
		public void process(FMLInterModComms.IMCMessage message);
	}

	private static Map<String, IMCProcessor>	processors	= new HashMap<String, IMCProcessor>();

	static {
		registerProcessor("api", new IMCAPIRegister());
	}

	private static void registerProcessor(String key, IMCProcessor processor) {
		processors.put(key.toLowerCase(), processor);
	}

	public static void process(ImmutableList<FMLInterModComms.IMCMessage> messages) {
		for (FMLInterModComms.IMCMessage message : messages) {
			String key = message.key.toLowerCase();
			IMCProcessor process = processors.get(key);
			if (process == null) {
				LoggerUtils.error("IMC message '%s' from [%s] unrecognized", key, message.getSender());
			}
			try {
				process.process(message);
			} catch (Exception e) {
				LoggerUtils.error("Failed to process IMC message '%s' from [%s]", key, message.getSender());
				e.printStackTrace();
			}
		}
	}
}
