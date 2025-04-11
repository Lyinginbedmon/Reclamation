package com.lying.config;

import java.io.FileWriter;
import java.util.Properties;

public class ServerConfig extends Config
{
	private static final Properties DEFAULT_SETTINGS = new Properties();
	
	private int rate = 3;
	private int radius = 50;
	
	public ServerConfig(String fileIn)
	{
		super(fileIn);
	}
	
	protected Properties getDefaults() { return DEFAULT_SETTINGS; }
	
	protected void readValues(Properties valuesIn)
	{
		rate = parseIntOr(valuesIn.getProperty("NaturalDecaySpeed"), 3);
		radius = parseIntOr(valuesIn.getProperty("NaturalDecayRadius"), 50);
	}
	
	protected void writeValues(FileWriter writer)
	{
		writeInt(writer, "NaturalDecaySpeed", rate);
		writeInt(writer, "NaturalDecayRadius", radius);
	}
	
	/** How many blocks are tested for natural decay each tick */
	public int naturalDecaySpeed() { return rate; }
	
	/** How far away from a player a block updated by natural decay can be */
	public int naturalDecayRadius() { return radius; }
	
	static
	{
		DEFAULT_SETTINGS.setProperty("NaturalDecaySpeed", "3");
		DEFAULT_SETTINGS.setProperty("NaturalDecayRadius", "50");
	}
}
