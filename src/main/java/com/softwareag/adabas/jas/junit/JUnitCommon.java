package com.softwareag.adabas.jas.junit;

import java.io.File;
import java.io.IOException;

import org.junit.BeforeClass;

import com.softwareag.adabas.jas.xts.XtsDefinitions;
import com.softwareag.adabas.xts.XTS;
import com.softwareag.adabas.xts.directory.Directory;

/**
 * Common JUnit test infrastructure for JAS testing.
 *
 * @author usadva
 * @author usajlim
 */

/* 
 * Copyright (c) 1998-2021 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, 
 * and/or its subsidiaries and/or its affiliates and/or their licensors. Use, reproduction, transfer, 
 * publication or disclosure is prohibited except as specifically provided for in your License Agreement 
 * with Software AG.
 */

public class JUnitCommon extends XtsDefinitions {
	
	public static final String DEFAULT_DBID= "12";
	
	protected static String dbidStr;	//Becomes DEFAULT_DBID in the lack of -DDBID
	protected static int dbid;			//Becomes an int derived from dbidStr
	
	/**
	 * Set default XTS directory server URL. Defaults to flat file INIDIR located at
	 * config/xtsurl.cfg.
	 * 
	 * @return String containing DS URL
	 * @throws Exception
	 */
	protected static String setDefaultXTSDSURL() throws Exception {
		
//		if (new File("config/xtsurl.cfg").exists())
//			System.setProperty("XTSDSURL", "file:config/xtsurl.cfg");
//		else {
//			System.err.println("missing XTSDSURL flat file at " +  new File(".").getCanonicalPath() + "/config/xtsurl.cfg");
//			throw new Exception("missing XTSDSURL flat file");
//		}
		System.setProperty("XTSDSURL", "http://localhost:4952");	// default to local AAS
		return System.getProperty("XTSDSURL");
	}
	
	/**
	 * Set default location and name for log4j properties file.
	 * Defaults to config/log4j.properties.
	 * 
	 * @return String containing log4j properties file location.
	 * @throws IOException 
	 */
	protected static String setDefaultLog4jProperties() throws IOException {
		
		String	propFile = new File(".").getCanonicalPath() + "/config/log4j.properties";
		System.setProperty("JASLOGPROP", propFile);
		return propFile;
	}

	/**
	 * Common prepare routine to be run before each JUnit class.
	 * Activate by adding 'extends JUnitCommon' to your class.
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void prepare() throws Exception {
		
		dbidStr = System.getProperty("DBID", DEFAULT_DBID);	//The value of -DDBID or DEFAULT_DBID;
		dbid = Integer.parseInt(dbidStr);					//May throw NumberFormatException
		
		String xtsDsUrl = System.getProperty("XTSDSURL");
		if (xtsDsUrl == null || xtsDsUrl.length() == 0)
			xtsDsUrl = setDefaultXTSDSURL();
		
//		Directory 		directory	= XTS.getDirectory();					// get directory server
//		directory.setPartition(ADI_ADMIN);									// point to admin partition

		String log4jPropFile = System.getProperty("JASLOGPROP");
		if (log4jPropFile == null || log4jPropFile.length() == 0)
			setDefaultLog4jProperties();

		File cur = new File(".");
		System.out.println("JUnit path: " + cur.getCanonicalPath());

//		System.setProperty("XTSTRACE", "0xffff");
//		System.setProperty("XTSDIR", "/tmp");
	}

}
