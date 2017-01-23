package usergames.mygo;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Contains the configuration properties and offers some methods for easier access.
 */
public class GoGroupAIConfig
{
	private static GoGroupAIConfig config;
	private Properties properties;

	private GoGroupAIConfig()
	{
		properties = new Properties();
		try
		{
			FileInputStream in = new FileInputStream("GO_Group_AI.properties");
			properties.load(in);
			in.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new IllegalStateException("GOGroupAI: Property file not found! Can't play without a property file.");
		}
	}

	public static GoGroupAIConfig getInstance()
	{
		if (config == null)
			config = new GoGroupAIConfig();
		return config;
	}

	public int getInt(String key)
	{
		return Integer.parseInt(properties.getProperty(key));
	}

	public boolean getBoolean(String key)
	{
		return Boolean.parseBoolean(properties.getProperty(key));
	}

}
