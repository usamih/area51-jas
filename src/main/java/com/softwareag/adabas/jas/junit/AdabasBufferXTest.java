package com.softwareag.adabas.jas.junit;

import static org.junit.Assert.fail;

import org.junit.Test;

import com.softwareag.adabas.jas.AdabasBufferX;
import com.softwareag.adabas.jas.AdabasException;

/**
 * JUnit tests for AdabasBufferX
 *
 * @author usadva
 * @author usarc
 *
 */

/* 
 * Copyright (c) 1998-2021 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, 
 * and/or its subsidiaries and/or its affiliates and/or their licensors. Use, reproduction, transfer, 
 * publication or disclosure is prohibited except as specifically provided for in your License Agreement 
 * with Software AG.
 */

public class AdabasBufferXTest {

	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasBufferX#AdabasBufferX(int, byte)}.
	 * Test method for {@link com.softwareag.adabas.jas.AdabasBufferX#AdabasBufferX(byte[], byte)}.
	 * Test method for {@link com.softwareag.adabas.jas.AdabasBufferX#AdabasBufferX(String, byte)}.
	 */
	@Test
	public final void testAdabasBufferX() throws AdabasException {
		
		try {
			new AdabasBufferX(-100, AdabasBufferX.FB);
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("SIZE LT 0")))
				fail("AdabasBufferX(-100), AdabasBufferX.FB) did not generate AdabasException SIZE LT 0");
		}
		
		try {
			new AdabasBufferX(2147483647, (byte) 0x47);
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("Invalid ABD type")))
				fail("AdabasBufferX(2147483647), (byte) 0x47) did not generate AdabasException Invalid ABD type");
		}
		
		/**
		 * You can define a negative array, but you will get a NegativeAarraySizeException.
		 * If you define a valid array that is too large, you will get an OutOfMemoryError.
		 */
		byte [] contentBa= new byte[12096];
		try {
			new AdabasBufferX(contentBa, ( byte) 0x88);
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("Invalid ABD type")))
				fail("AdabasBufferX(12096), (byte) 0x88) did not generate AdabasException Invalid ABD type");
		}

		try {
			new AdabasBufferX("UTI.", ( byte) 0x68);
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("Invalid ABD type")))
				fail("AdabasBufferX('UTI.'), (byte) 0x68) did not generate AdabasException Invalid ABD type");
		}
	
		new AdabasBufferX(0, AdabasBufferX.RB);
		new AdabasBufferX(147483647, AdabasBufferX.RB);
		new AdabasBufferX(contentBa, AdabasBufferX.RB);
		new AdabasBufferX("UTI.", AdabasBufferX.RB);
		
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasBufferX#setAbdSIZE(int)}.
	 */
	@Test
	public final void testsetAbdSIZE() throws AdabasException {
		
		try {
			AdabasBufferX abdx = new AdabasBufferX(12096, AdabasBufferX.RB);
			abdx.setAbdSIZE(-100);
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("SIZE LT 0")))
				fail("abdx.setAbdSIZE(-100), AdabasBufferX.RB) did not generate AdabasException SIZE LT 0");
		}
		
		try {
			AdabasBufferX abdx = new AdabasBufferX(12096, AdabasBufferX.RB);
			abdx.setAbdSIZE(16384);
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("SIZE GT allocated buffer length")))
				fail("abdx.setAbdSIZE(16384), AdabasBufferX.RB) did not generate AdabasException SIZE GT allocated buffer length");
		}
	
		AdabasBufferX abdx = new AdabasBufferX(147483647, AdabasBufferX.RB);
		abdx.setAbdSIZE(147483640);
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasBufferX#setAbdSEND(int)}.
	 */
	@Test
	public final void testsetAbdSEND() throws AdabasException {
		
		try {
			AdabasBufferX abdx = new AdabasBufferX(12096, AdabasBufferX.RB);
			abdx.setAbdSEND(-100);
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("SEND LT 0")))
				fail("abdx.setAbdSEND(-100), AdabasBufferX.RB) did not generate AdabasException SEND LT 0");
		}
		
		try {
			AdabasBufferX abdx = new AdabasBufferX(12096, AdabasBufferX.RB);
			abdx.setAbdSEND(16384);
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("SEND GT allocated buffer length")))
				fail("abdx.setAbdSEND(16384), AdabasBufferX.RB) did not generate AdabasException SEND GT allocated buffer length");
		}
		
		AdabasBufferX abdx = new AdabasBufferX(147483647, AdabasBufferX.RB);
		abdx.setAbdSEND(147483640);
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasBufferX#setAbdRECV(int)}.
	 */
	@Test
	public final void testsetAbdRECV() throws AdabasException {
		
		try {
			AdabasBufferX abdx = new AdabasBufferX(12096, AdabasBufferX.RB);
			abdx.setAbdRECV(-100);
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("RECV LT 0")))
				fail("abdx.setAbdRECV(-100), AdabasBufferX.RB) did not generate AdabasException RECV LT 0");
		}
		
		try {
			AdabasBufferX abdx = new AdabasBufferX(12096, AdabasBufferX.RB);
			abdx.setAbdRECV(16384);
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("RECV GT allocated buffer length")))
				fail("abdx.setAbdRECV(16384), AdabasBufferX.RB) did not generate AdabasException RECV GT allocated buffer length");
		}
	
		AdabasBufferX abdx = new AdabasBufferX(147483647, AdabasBufferX.RB);
		abdx.setAbdRECV(147483640);
	}

}
