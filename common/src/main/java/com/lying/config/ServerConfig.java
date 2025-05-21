package com.lying.config;

import java.io.FileWriter;
import java.util.Properties;

import com.lying.utility.ExteriorUtility;

public class ServerConfig extends Config
{
	private static final Properties DEFAULT_SETTINGS = new Properties();
	
	private int rate = 3;
	private int radius = 50;
	private boolean scrapeableRust = false;
	private int exteriorIterationCap = ExteriorUtility.DEFAULT_MAX_ITERATION_CAP;
	
	public ServerConfig(String fileIn)
	{
		super(fileIn);
	}
	
	protected Properties getDefaults() { return DEFAULT_SETTINGS; }
	
	protected void readValues(Properties valuesIn)
	{
		rate = parseIntOr(valuesIn.getProperty("NaturalDecaySpeed"), 3);
		radius = parseIntOr(valuesIn.getProperty("NaturalDecayRadius"), 50);
		scrapeableRust = parseBoolOr(valuesIn.getProperty("ScrapeableRust"), false);
		exteriorIterationCap = parseIntOr(valuesIn.getProperty("ExteriorScanIterationCap"), ExteriorUtility.DEFAULT_MAX_ITERATION_CAP);
	}
	
	protected void writeValues(FileWriter writer)
	{
		writeInt(writer, "NaturalDecaySpeed", rate);
		writeInt(writer, "NaturalDecayRadius", radius);
		writeBool(writer, "ScrapeableRust", scrapeableRust);
		writeInt(writer, "ExteriorScanIterationCap", exteriorIterationCap);
	}
	
	/** How many blocks are tested for natural decay each tick */
	public int naturalDecaySpeed() { return rate; }
	
	/** How far away from a player a block updated by natural decay can be */
	public int naturalDecayRadius() { return radius; }
	
	/** Whether rust can be scraped off of rusted blocks */
	public boolean isRustScrapeable() { return scrapeableRust; }
	
	/** Returns the upper limit of iterations on scans for exterior positions */
	public int exteriorIterationCap() { return exteriorIterationCap; }
	
	static
	{
		DEFAULT_SETTINGS.setProperty("NaturalDecaySpeed", "3");
		DEFAULT_SETTINGS.setProperty("NaturalDecayRadius", "50");
		DEFAULT_SETTINGS.setProperty("ScrapeableRust", "0");
		DEFAULT_SETTINGS.setProperty("ExteriorScanIterationCap", "10000");
	}
}
