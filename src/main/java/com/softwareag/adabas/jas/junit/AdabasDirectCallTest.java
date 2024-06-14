/**
 * 
 */
package com.softwareag.adabas.jas.junit;

import static org.junit.Assert.*;

import org.junit.Test;

import com.softwareag.adabas.jas.AdabasControlBlock;
import com.softwareag.adabas.jas.AdabasDirectCall;
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

public class AdabasDirectCallTest extends JUnitCommon {

	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasDirectCall#AdabasDirectCall(com.softwareag.adabas.jas.AdabasControlBlock)}.
	 * @throws AdabasException
	 * 
	 * Test invalid direct call constructor with null control block.
	 */
	@Test
	public final void testAdabasDirectCall() {
		
		try {
			new AdabasDirectCall(null);
		} catch (AdabasException e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("No Adabas control block provided")))
				fail("AdabasDirectCall(null) did not generate AdabasException No Adabas control block provided");
		}
	}

	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasDirectCall#callAdabas()}.
	 * @throws Exception 
	 * 
	 * Test simple OP/CL.
	 */
	@Test
	public final void testCallAdabas() throws Exception {
		
		AdabasControlBlock 	acb = new AdabasControlBlock();
		AdabasDirectCall	adc = new AdabasDirectCall(acb);

		acb.setAcbDBID(dbid);										// default in JUnitCommon
		acb.setAcbFNR(9);
		acb.setAcbCMD("OP");
		
		String opFB = ".";
		adc.setFB(opFB.getBytes());
		acb.setAcbFBL((short) opFB.length());

		String opRB = "UPD=9.";
		adc.setRB(opRB.getBytes());
		acb.setAcbRBL((short) opRB.length());

		adc.callAdabas();
		
		acb.setAcbCMD("CL");
		adc.setFB(null);
		adc.setRB(null);
		acb.setAcbRBL((short) 0);
		acb.setAcbRBL((short) 0);
		
		adc.callAdabas();
	}

	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasDirectCall#callAdabas()}.
	 * @throws Exception 
	 * 
	 * Test bad response code logic by sending OP to inactive database 255.
	 * This test requires that a database 255 is defined and known to AAS and is inactive.
	 */
	@Test
	public final void testRC148() throws Exception {
		
		AdabasControlBlock 	acb = new AdabasControlBlock();
		AdabasDirectCall	adc = new AdabasDirectCall(acb);
		acb.setAcbDBID(255);
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
			if (!(e instanceof AdabasException && e.getMessage().contains("response code = 148"))) {
				fail("callAdabas() DBID = 255 did not generate AdabasException response code = 148");
			}
			return;
		}
		System.out.println(String.format("OP rc = %d; 0x%04x", acb.getAcbRSP(), acb.getAcbRSP()));
		fail("callAdabas() DBID = 255 did not generate AdabasException response code = 148");
		
	}

	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasDirectCall#callAdabas()}.
	 * @throws Exception 
	 * 
	 * Get GCB with U1 command.
	 */
	@Test
	public final void testGetGCB() throws Exception {
		
		AdabasControlBlock 	acb = new AdabasControlBlock();
		AdabasDirectCall	adc = new AdabasDirectCall(acb);

		acb.setAcbDBID(dbid);										// default in JUnitCommon
		acb.setAcbFNR(1);
		
		acb.setAcbCMD("OP");
		String opRB = "UPD=1.";
		adc.setRB(opRB.getBytes());
		acb.setAcbRBL((short) opRB.length());

		adc.callAdabas();

		acb.setAcbCMD("U1");
		acb.setAcbCOP1((byte) '0');
		acb.setAcbCOP2((byte) '0');
		acb.setAcbISN(1);
		String opFB = ".";
		adc.setFB(opFB.getBytes());
		acb.setAcbFBL((short) opFB.length());
		byte[] rb = new byte[1000];
		adc.setRB(rb);
		acb.setAcbRBL((short) rb.length);
		
		acb.setAcbADD5(getPrivAdd5());

		adc.callAdabas();

		acb.setAcbCMD("CL");
		adc.setFB(null);
		adc.setRB(null);
		acb.setAcbRBL((short) 0);
		acb.setAcbRBL((short) 0);
		
		adc.callAdabas();
	}
	
	/**
	 * Get additions 5 for privileged call.
	 * 
	 * @return byte array for additions 5.
	 */
	private byte[] getPrivAdd5() {
		
		long t = System.currentTimeMillis() / 1000;
		byte[] codTime = new byte[4];
		codTime[0] = (byte) (t & 0xff);
		codTime[1] = (byte) ((t >> (2 * 8)) & 0xFF);
		byte x = (byte) ((t >> (3 * 8)) & 0xFF);
		long y = (x >> 4);
		y |= (x << 4);
		codTime[2] = (byte) y;
		codTime[3] = (byte) ((t >> (1 * 8)) & 0xFF);
		
		byte[] add5 = new byte[8];
		System.arraycopy(codTime, 0, add5, 0, 4);
		
		return add5;
	}

}
