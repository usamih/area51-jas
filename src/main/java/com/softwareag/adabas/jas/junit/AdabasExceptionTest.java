package com.softwareag.adabas.jas.junit;

import org.junit.Test;

import com.softwareag.adabas.jas.AIFResponseCodes;
import com.softwareag.adabas.jas.AdabasException;

/**
 * @author usadva
 */

/* 
 * Copyright (c) 1998-2021 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, 
 * and/or its subsidiaries and/or its affiliates and/or their licensors. Use, reproduction, transfer, 
 * publication or disclosure is prohibited except as specifically provided for in your License Agreement 
 * with Software AG.
 */

public class AdabasExceptionTest {

	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasException#printAllRCTexts()}.
	 */
	@Test
	public final void testPrintAllRCTexts() {

		AdabasException.printAllRCTexts();							// print all response code texts
	}

	/**
	 * Test method for {@link com.softwareag.adabas.jas.AIFResponseCodes#printAllRCTexts()}.
	 */
	@Test
	public final void testPrintAllAIFRCTexts() {

		AIFResponseCodes.printAllRCTexts();							// print all AIF response code texts
	}
	
	/**
	 * Test method for AdabasException no args constructor.
	 * 
	 * @throws Exception 
	 */
	@Test
	public final void testThrow1() throws Exception {
		
		AdabasException	ae = new AdabasException();
		
		throw ae;
	}

	/**
	 * Test method for AdabasException(msg) constructor.
	 * 
	 * @throws AdabasException 
	 */
	@Test
	public final void testThrow2() throws AdabasException {
		
		AdabasException	ae = new AdabasException("Test Exception!");
		
		throw ae;
	}

	/**
	 * Test method for AdabasException(dbid, msg) constructor.
	 * 
	 * @throws AdabasException 
	 */
	@Test
	public final void testThrow3() throws AdabasException {
		
		AdabasException	ae = new AdabasException(70, "DBID 70 Test Exception!");
		
		throw ae;
	}

	/**
	 * Test method for AdabasException(dbid, rc) constructor.
	 * 
	 * @throws AdabasException 
	 */
	@Test
	public final void testThrow4() throws AdabasException {
		
		AdabasException	ae = new AdabasException(70, (short) 148);
		
		throw ae;
	}

	/**
	 * Test method for AdabasException(dbid, rc, sc) constructor.
	 * 
	 * @throws AdabasException 
	 */
	@Test
	public final void testThrow5() throws AdabasException {
		
		AdabasException	ae = new AdabasException(70, (short) 1, (short) 1);
		
		throw ae;
	}

	/**
	 * Test method for AdabasException(dbid, rc, sc, src) constructor.
	 * 
	 * @throws AdabasException 
	 */
	@Test
	public final void testThrow6() throws AdabasException {
		
		AdabasException	ae = new AdabasException(70, (short) 2, (short) 5, (short) 999);
		
		throw ae;
	}

	/**
	 * Test method for AdabasException(dbid, cmd, rc, sc) constructor.
	 * 
	 * @throws AdabasException 
	 */
	@Test
	public final void testThrow7() throws AdabasException {
		
		AdabasException	ae = new AdabasException(70, "S1", (short) 9, (short) 66);
		
		throw ae;
	}

	/**
	 * Test method for AdabasException(dbid, cmd, rc, sc, src) constructor.
	 * 
	 * @throws AdabasException 
	 */
	@Test
	public final void testThrow8() throws AdabasException {
		
		AdabasException	ae = new AdabasException(70, "L3", (short) 131, (short) 30, (short) 999);
		
		throw ae;
	}

	/**
	 * Test method for AdabasException(dbid, cmd, rc, sc, src, ssc) constructor.
	 * 
	 * @throws AdabasException 
	 */
	@Test
	public final void testThrow9() throws AdabasException {
		
		AdabasException	ae = new AdabasException(70, "L3", (short) 148, (short) 58, (short) 999, (short) 666);
		
		throw ae;
	}

	/**
	 * Test method for AdabasException(dbid, cmd, rc, sc, src, ssc) constructor with undefined subcode.
	 * 
	 * @throws AdabasException 
	 */
	@Test
	public final void testThrow10() throws AdabasException {
		
		AdabasException	ae = new AdabasException(70, "L3", (short) 148, (short) 30, (short) 999, (short) 666);
		
		throw ae;
	}

}
