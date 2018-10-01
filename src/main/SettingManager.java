package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.ProtectionDomain;
import java.util.Properties;

public class SettingManager {

	synchronized public static void ReadConfiguration(Class<?> mainClass) throws IOException {
		if (conf != null) {
			return;
		}
		conf = new Properties();

		URI uri = null;
		try {
			uri = mainClass.getProtectionDomain().getCodeSource().getLocation().toURI();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Path path = Paths.get(uri).getParent().toAbsolutePath();

		conf.load(new FileInputStream(path + File.separator + filename));
	}

	private static Properties conf = null;
	private static final String filename = "config.txt";
	// private static SettingManager instance = null;

	synchronized public static Properties getProperties() {
		return conf;
	}
}
