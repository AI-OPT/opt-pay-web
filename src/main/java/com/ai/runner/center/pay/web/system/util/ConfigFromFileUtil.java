package com.ai.runner.center.pay.web.system.util;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ConfigFromFileUtil {

	private static Properties mConfig;
	private static Log log = LogFactory.getLog(ConfigFromFileUtil.class);
	private static String default_config_1 = "/config.properties";

	/**
	 * 获取配置文件属性对应的值
	 * 
	 * @param propertSrc
	 *            eg：/sso-server.properties 资源文件名
	 * @param key
	 * @return keyValue
	 */
	public static String getParameterValue(String propertSrc, String key) {

		mConfig = new Properties();

		try {
			@SuppressWarnings("rawtypes")
			Class config_class = ConfigFromFileUtil.class;
			InputStream is = config_class.getResourceAsStream(propertSrc);
			mConfig.load(is);
			log.info("successfully loaded propertSrc properties...");
			return mConfig.getProperty(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	/**
	 * 获取配置文件属性对应的值    该方法可以接收带"\"的url地址    解决了  getParameterValue中的bug
	 * @param propertSrc  \sso-server.properties 资源文件路径
	 * @param key   属性key
	 * @return key对应的value值
	 * @author 李明顶
	 *
	 * Date: 2014年3月25日 <br>
	 */
	public static String getPropertyValue(final String propertSrc,final String key) {

		mConfig = new Properties();

		try {
			mConfig.load(new FileInputStream(propertSrc));
			return mConfig.getProperty(key);	
		} catch (Exception e1) {
			e1.printStackTrace();
		} 
		return "";
	}

	static {
		mConfig = new Properties();
		InputStream is = null;
		try {
			// we'll need this to get at our properties files in the classpath
			@SuppressWarnings("rawtypes")
			Class config_class = ConfigFromFileUtil.class;

			// first, lets load our default properties
			is = new FileInputStream(new File(config_class.getResource(
					default_config_1).toURI()));
			mConfig.load(is);
			log.info("successfully loaded default properties.");

			// now, see if we can find our custom config
			/*
			 * //is = config_class.getResourceAsStream(default_config_2); is =
			 * new FileInputStream(new
			 * File(config_class.getResource(default_config_2).toURI())); if (is
			 * != null) { mConfig.load(is);
			 * log.info("successfully loaded custom properties file from classpath"
			 * ); } else {
			 * log.info("no custom properties file found in classpath"); }
			 */

			// some debugging for those that want it
			if (log.isDebugEnabled()) {
				log.debug("AiCasConfig looks like this ...");

				String key = null;
				@SuppressWarnings("rawtypes")
				Enumeration keys = mConfig.keys();
				while (keys.hasMoreElements()) {
					key = (String) keys.nextElement();
					log.debug(key + "=" + mConfig.getProperty(key));
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * Retrieve a property value
	 * 
	 * @param key
	 *            Name of the property
	 * @return String Value of property requested, null if not found
	 */
	public static String getProperty(String key) {
		log.debug("Fetching property [" + key + "=" + mConfig.getProperty(key)
				+ "]");
		return mConfig.getProperty(key);
	}

	/**
	 * Retrieve a property value
	 * 
	 * @param key
	 *            Name of the property
	 * @param defaultValue
	 *            Default value of property if not found
	 * @return String Value of property requested or defaultValue
	 */
	public static String getProperty(String key, String defaultValue) {
		log.debug("Fetching property [" + key + "=" + mConfig.getProperty(key)
				+ ",defaultValue=" + defaultValue + "]");
		String value = mConfig.getProperty(key);
		if (value == null)
			return defaultValue;

		return value;
	}

	/**
	 * Retrieve a property as a boolean ... defaults to false if not present.
	 */
	public static boolean getBooleanProperty(String name) {
		return getBooleanProperty(name, false);
	}

	/**
	 * Retrieve a property as a boolean ... with specified default if not
	 * present.
	 */
	public static boolean getBooleanProperty(String name, boolean defaultValue) {
		// get the value first, then convert
		String value = getProperty(name);

		if (value == null)
			return defaultValue;

		return (Boolean.valueOf(value));
	}

	/**
	 * Retrieve a property as an int ... defaults to 0 if not present.
	 */
	public static int getIntProperty(String name) {
		return getIntProperty(name, 0);
	}

	/**
	 * Retrieve a property as a int ... with specified default if not present.
	 */
	public static int getIntProperty(String name, int defaultValue) {
		// get the value first, then convert
		String value = ConfigFromFileUtil.getProperty(name);

		if (value == null)
			return defaultValue;

		return (new Integer(value)).intValue();
	}

	/**
	 * Retrieve all property keys
	 * 
	 * @return Enumeration A list of all keys
	 */
	@SuppressWarnings("rawtypes")
	public static Enumeration keys() {
		return mConfig.keys();
	}

	public static boolean isTrue(String name) {
		return "true".equalsIgnoreCase(getProperty(name) == null ? ""
				: getProperty(name).trim());
	}

	public static void main(String[] args) {
		System.out.println(getProperty("reloadcache.request.plus"));
	}

	@SuppressWarnings("rawtypes")
	public static synchronized void reloadProperties() throws Exception {

		mConfig = new Properties();
		InputStream is = null;
		try {
			// we'll need this to get at our properties files in the classpath
			Class config_class = ConfigFromFileUtil.class;

			// first, lets load our default properties
			// is = config_class.getResourceAsStream(default_config_1);
			is = new FileInputStream(new File(config_class.getResource(
					default_config_1).toURI()));
			mConfig.load(is);
			log.info("successfully loaded default properties.");

			// now, see if we can find our custom config
			// is = config_class.getResourceAsStream(default_config_2);
			/*
			 * is = new FileInputStream(new
			 * File(config_class.getResource(default_config_2).toURI())); if (is
			 * != null) { mConfig.load(is);
			 * log.info("successfully loaded custom properties file from classpath"
			 * ); } else {
			 * log.info("no custom properties file found in classpath"); }
			 */

			// some debugging for those that want it
			if (log.isDebugEnabled()) {
				log.debug("AiCasConfig looks like this ...");

				String key = null;
				Enumeration keys = mConfig.keys();
				while (keys.hasMoreElements()) {
					key = (String) keys.nextElement();
					log.debug(key + "=" + mConfig.getProperty(key));
				}
			}

		} catch (Exception e) {
			throw e;
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	
}
