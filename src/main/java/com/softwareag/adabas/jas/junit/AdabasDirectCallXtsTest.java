package com.softwareag.adabas.jas.junit;

import static org.junit.Assert.*;

import org.junit.Test;

import com.softwareag.adabas.jas.AdabasBufferX;
import com.softwareag.adabas.jas.AdabasControlBlock;
import com.softwareag.adabas.jas.AdabasControlBlockX;
import com.softwareag.adabas.jas.AdabasDirectCall;
import com.softwareag.adabas.jas.AdabasDirectCallX;
import com.softwareag.adabas.jas.AdabasException;
import com.softwareag.adabas.xts.XTSException;

/**
 * Manual tests for JAS/XTS Communication.
 * 
 * Many of these tests must be run manually in the Eclipse debugger with
 * breakpoints set to force error conditions and cause exceptions to be thrown.
 * See each test for instructions. 
 *
 * @author usadva
 */

/* 
 * Copyright (c) 1998-2021 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, 
 * and/or its subsidiaries and/or its affiliates and/or their licensors. Use, reproduction, transfer, 
 * publication or disclosure is prohibited except as specifically provided for in your License Agreement 
 * with Software AG.
 */

public class AdabasDirectCallXtsTest extends JUnitCommon {

	/**
	 * Manual test for no XTS directory server found.
	 * @throws Exception
	 * 
	 * Should never happen because XTS always includes a default dir server entry.
	 * This test is meant to be run manually in the Eclipse debugger. It is intended to verify
	 * the exception thrown when AdabasDirectCallXts.initDsUrl() receives a null ADI from
	 * XTS.getDirectory(). Set a breakpoint in AdabasDirectCallXts.initDsUrl() and manually
	 * force this condition.
	 */
	@Test
	public final void testNoXtsDirServer() throws Exception {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		AdabasDirectCallX	adcx = new AdabasDirectCallX(acbx);
		acbx.setAcbDBID(dbid);				//Default in JUnitCommon										//Default in JUnitCommon
		acbx.setAcbCMD("OP");
		
		AdabasBufferX opFB = new AdabasBufferX(".", AdabasBufferX.FB);
		adcx.setFB(opFB);
		
		AdabasBufferX opRB = new AdabasBufferX("UPD=9.", AdabasBufferX.RB);
		adcx.setRB(opRB);

		try {
			adcx.callAdabas();
		}
		catch (AdabasException e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("No XTS Directory Server found.")))
				fail("callAdabas() DBID = default did not generate AdabasException No XTS Directory Server found.");
		}
		
	}

	/**
	 * Manual test for XTS sendAndWait() error using A1 message.
	 * @throws Exception
	 * 
	 * This test is meant to be run manually in the Eclipse debugger. It is intended to verify
	 * the exception thrown when XTS.sendAndWait() returns an A1_ERRORREPLY. Set a breakpoint
	 * in AdabasDirectCallXts.callAdabasXtsA1() and manually force this condition after the
	 * XTS.sendAndWait() call.
	 */
	@Test
	public final void testXtsSendAndWaitA1() throws Exception {
		
		AdabasControlBlock  acb	= new AdabasControlBlock();
		AdabasDirectCall	adc = new AdabasDirectCall(acb);
		acb.setAcbDBID(dbid);				//Default in JUnitCommon										//Default in JUnitCommon
		acb.setAcbFNR(9);
		acb.setAcbCMD("OP");
		
		String opFB = ".";
		adc.setFB(opFB.getBytes());
		acb.setAcbFBL((short) opFB.length());

		String opRB = "UPD=9.";
		adc.setRB(opRB.getBytes());
		acb.setAcbRBL((short) opRB.length());

		try {
			adc.callAdabas();
		}
		catch (AdabasException e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("XTS.sendAndWait() returned ERRORREPLY")))
				fail("callAdabas() did not generate AdabasException XTS.sendAndWait() returned ERRORREPLY");
		}
		
	}
	
	/**
	 * Test no entry in XTS directory server for database 2.
	 * @throws Exception
	 */
	@Test
	public final void testXtsNODIRENTRY() throws Exception {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		AdabasDirectCallX	adcx = new AdabasDirectCallX(acbx);
		acbx.setAcbDBID(2);
		acbx.setAcbCMD("OP");
		
		AdabasBufferX opFB = new AdabasBufferX(".", AdabasBufferX.FB);
		adcx.setFB(opFB);
		
		AdabasBufferX opRB = new AdabasBufferX("UPD=9.", AdabasBufferX.RB);
		adcx.setRB(opRB);

		try {
			adcx.callAdabas();
		}
		catch (XTSException e) {
			if (!(e.getMessage().contains("No directory entry found for")))
				fail("callAdabas() did not generate XTSException No directory entry found for.");
		}
		
	}
	
	/**
	 * Test bad XTS directory server.
	 * @throws Exception
	 */
	@Test
	public final void testXtsBadDirServer() throws Exception {
		
		System.setProperty("XTSDSURL", "http://foo:1");						// set bad dir server URL
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		AdabasDirectCallX	adcx = new AdabasDirectCallX(acbx);
		acbx.setAcbDBID(1);
		acbx.setAcbCMD("OP");
		
		AdabasBufferX opFB = new AdabasBufferX(".", AdabasBufferX.FB);
		adcx.setFB(opFB);
		
		AdabasBufferX opRB = new AdabasBufferX("UPD=9.", AdabasBufferX.RB);
		adcx.setRB(opRB);

		try {
			adcx.callAdabas();
		}
		catch (Exception e) {
			if (!(e instanceof XTSException && e.getMessage().contains("Unknown Host Exception")))
				fail("callAdabas() did not generate XTSException Unknown Host Exception");
		}
		
	}
	
	/**
	 * Manual test for XTS establish context error.
	 * @throws Exception
	 * 
	 * This test is meant to be run manually in the Eclipse debugger. It is intended to verify
	 * the exception thrown when the attempt to establish context using an initial XTS.sendAndWait() 
	 * returns an A1_ERRORREPLY. Set a breakpoint in AdabasDirectCallXts.connectXts() and manually 
	 * force this condition after the XTS.sendAndWait() call.
	 */
	@Test
	public final void testXtsEstablishContext() throws Exception {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		AdabasDirectCallX	adcx = new AdabasDirectCallX(acbx);
		acbx.setAcbDBID(dbid);				//Default in JUnitCommon										//Default in JUnitCommon
		acbx.setAcbCMD("OP");
		
		AdabasBufferX opFB = new AdabasBufferX(".", AdabasBufferX.FB);
		adcx.setFB(opFB);
		
		AdabasBufferX opRB = new AdabasBufferX("UPD=9.", AdabasBufferX.RB);
		adcx.setRB(opRB);

		try {
			adcx.callAdabas();
		}
		catch (AdabasException e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("XTS A1 establish context")))
				fail("callAdabas() did not generate AdabasException XTS A1 establish context");
		}
		
	}
	
	/**
	 * Manual test for XTS destroy context error.
	 * @throws Exception
	 * 
	 * This test is meant to be run manually in the Eclipse debugger. It is intended to verify
	 * the exception thrown when the attempt to destroy context using an XTS.sendAndWait() 
	 * following a successful CL command returns an A1_ERRORREPLY. Set a breakpoint in 
	 * AdabasDirectCallXts.disconnectXts() and manually force this condition after the 
	 * XTS.sendAndWait() call.
	 */
	@Test
	public final void testXtsDestroyContext() throws Exception {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		AdabasDirectCallX	adcx = new AdabasDirectCallX(acbx);
		acbx.setAcbDBID(dbid);				//Default in JUnitCommon										//Default in JUnitCommon
		acbx.setAcbCMD("OP");
		
		AdabasBufferX opFB = new AdabasBufferX(".", AdabasBufferX.FB);
		adcx.setFB(opFB);
		
		AdabasBufferX opRB = new AdabasBufferX("UPD=9.", AdabasBufferX.RB);
		adcx.setRB(opRB);

		adcx.callAdabas();

		acbx.setAcbCMD("CL");
		adcx.setFB(null);
		adcx.setRB(null);

		try {
			adcx.callAdabas();
		}
		catch (AdabasException e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("XTS A1 destroy context")))
				fail("callAdabas() did not generate AdabasException XTS A1 destroy context");
		}
		
	}
	
	/**
	 * Manual test for XTS sendAndWait() error using A2 message.
	 * @throws Exception
	 * 
	 * This test is meant to be run manually in the Eclipse debugger. It is intended to verify
	 * the exception thrown when XTS.sendAndWait() returns an A1_ERRORREPLY. Set a breakpoint
	 * in AdabasDirectCallXts.callAdabasXtsA2() and manually force this condition after the
	 * XTS.sendAndWait() call.
	 */
	@Test
	public final void testXtsSendAndWaitA2() throws Exception {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		AdabasDirectCallX	adcx = new AdabasDirectCallX(acbx);
		acbx.setAcbDBID(dbid);				//Default in JUnitCommon
		acbx.setAcbCMD("OP");
		
		AdabasBufferX opFB = new AdabasBufferX(".", AdabasBufferX.FB);
		adcx.setFB(opFB);
		
		AdabasBufferX opRB = new AdabasBufferX("UPD=9.", AdabasBufferX.RB);
		adcx.setRB(opRB);

		try {
			adcx.callAdabas();
		}
		catch (AdabasException e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("XTS.sendAndWait() returned ERRORREPLY")))
				fail("callAdabas() did not generate AdabasException XTS.sendAndWait() returned ERRORREPLY");
		}
		
	}
	
}
