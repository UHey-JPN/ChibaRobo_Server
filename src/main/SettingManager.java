package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
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

	synchronized public static byte[] getNicMac() {
		String nic_mac_str = SettingManager.getProperties().getProperty("NIC_MAC");

		if (nic_mac_str != null) {
			try {
				long nic_mac_long = Long.parseLong(nic_mac_str.replaceAll(":", ""), 16);
				ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
				buffer.putLong(nic_mac_long);
				return Arrays.copyOfRange(buffer.array(), 2, 8);
			} catch (Exception e) {
				// e.printStackTrace();
				// return null;
			}
		}

		return null;
	}

	synchronized public static InetAddress getIpAddr() {
		String nic_addr_str = SettingManager.getProperties().getProperty("IP_ADDR");

		if (nic_addr_str != null) {
			try {
				return InetAddress.getByName(nic_addr_str);
			} catch (UnknownHostException e) {
				// e.printStackTrace();
			}
		}

		return null;
	}

	synchronized public static int getDbPort() {
		String db_port_str = SettingManager.getProperties().getProperty("DB_PORT");

		if (db_port_str != null) {
			try {
				return Integer.parseInt(db_port_str);
			} catch (Exception ex) {

			}
		}

		return 0;
	}
}
