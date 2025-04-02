package com.lying.config;

import java.io.FileWriter;
import java.util.Properties;

public class ServerConfig extends Config
{
	private static final Properties DEFAULT_SETTINGS = new Properties();
	
	// TODO Change config values to default settings of corresponding gamerules
	private int rate = 3;
	private int radius = 50;
	
	public ServerConfig(String fileIn)
	{
		super(fileIn);
	}
	
	protected Properties getDefaults() { return DEFAULT_SETTINGS; }
	
	protected void readValues(Properties valuesIn)
	{
		rate = parseIntOr(valuesIn.getProperty("NaturalDecayRate"), 3);
		radius = parseIntOr(valuesIn.getProperty("NaturalDecayRadius"), 50);
	}
	
	protected void writeValues(FileWriter writer)
	{
		writeInt(writer, "NaturalDecayRate", rate);
		writeInt(writer, "NaturalDecayRadius", radius);
	}
	
	/** How many blocks are tested for natural decay each tick */
	public int naturalDecayRate() { return rate; }
	
	/** How far away from a player a block updated by natural decay can be */
	public int naturalDecayRadius() { return radius; }
	
	public void resetRate() { setRate(Integer.valueOf(DEFAULT_SETTINGS.getProperty("NaturalDecayRate"))); }
	
	public void resetRadius() { setRadius(Integer.valueOf(DEFAULT_SETTINGS.getProperty("NaturalDecayRadius"))); }
	
	public void setRate(int rateIn)
	{
		rate = rateIn;
		save();
	}
	
	public void setRadius(int radiusIn)
	{
		radius = radiusIn;
		save();
	}
	
	static
	{
		DEFAULT_SETTINGS.setProperty("NaturalDecayRate", "3");
		DEFAULT_SETTINGS.setProperty("NaturalDecayRadius", "50");
	}
}
