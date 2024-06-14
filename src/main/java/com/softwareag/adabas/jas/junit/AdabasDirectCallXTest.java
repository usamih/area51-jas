package com.softwareag.adabas.jas.junit;

import static org.junit.Assert.fail;

import org.junit.Test;

import com.softwareag.adabas.jas.AdabasBufferX;
import com.softwareag.adabas.jas.AdabasControlBlockX;
import com.softwareag.adabas.jas.AdabasDirectCallX;
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

public class AdabasDirectCallXTest extends JUnitCommon {

	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasDirectCallX#AdabasDirectCallX(com.softwareag.adabas.jas.AdabasControlBlockX)}.
	 * @throws AdabasException
	 * 
	 * Test invalid direct call constructor with null control block.
	 */
	@Test
	public final void testAdabasDirectCallX() {
		
		try {
			new AdabasDirectCallX(null);
		} catch (AdabasException e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("No Adabas control block provided")))
				fail("AdabasDirectCallX(null) did not generate AdabasException No Adabas control block provided");
		}
	}

	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasDirectCallX#callAdabas()}.
	 * @throws Exception
	 *  
	 * Test simple OP/CL.
	 */
	@Test
	public final void testCallAdabasX() throws Exception {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		AdabasDirectCallX	adcx = new AdabasDirectCallX(acbx);
		acbx.setAcbDBID(dbid);										// default in JUnitCommon
		acbx.setAcbCMD("OP");
		
		AdabasBufferX opFB = new AdabasBufferX(".", AdabasBufferX.FB);
		adcx.setFB(opFB);
		
		AdabasBufferX opRB = new AdabasBufferX("UPD=9.", AdabasBufferX.RB);
		adcx.setRB(opRB);

		adcx.callAdabas();
		
		acbx.setAcbCMD("CL");
		adcx.setFB(null);
		adcx.setRB(null);
		
		adcx.callAdabas();
	}

	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasDirectCallX#callAdabas()}.
	 * @throws Exception
	 *  
	 * Test OP/L2/CL read Employees sequence.
	 */
	@Test
	public final void testReadEmployeesX() throws Exception {
		
		long 	startTime	= System.currentTimeMillis();
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		AdabasDirectCallX	adcx = new AdabasDirectCallX(acbx);
		acbx.setAcbDBID(70);										// default in JUnitCommon
		acbx.setAcbCMD("OP");
		
		AdabasBufferX opFB = new AdabasBufferX(".", AdabasBufferX.FB);
		adcx.setFB(opFB);
		
		AdabasBufferX opRB = new AdabasBufferX("UPD=9.", AdabasBufferX.RB);
		adcx.setRB(opRB);

		long	startOPTime		= System.currentTimeMillis();
		adcx.callAdabas();
		long	endOPTime		= System.currentTimeMillis();
		long	elapsedOPTime	= endOPTime - startOPTime;
		System.out.println(String.format("elapsedOPTime = %d ms", elapsedOPTime));
		
		acbx.setAcbCMD("L2");
		acbx.setAcbCID("JASR");
		acbx.setAcbFNR(1);
		
		AdabasBufferX l2FB = new AdabasBufferX("AA,AC,AE.", AdabasBufferX.FB);
		adcx.setFB(l2FB);
		
		AdabasBufferX l2RB = new AdabasBufferX(48, AdabasBufferX.RB);
		adcx.setRB(l2RB);
		
		for (int i=0; i < 20; i++) {
			long	startL2Time		= System.currentTimeMillis();
			adcx.callAdabas();
			long	endL2Time		= System.currentTimeMillis();
			long	elapsedL2Time	= endL2Time - startL2Time;
			System.out.println(String.format("elapsedL2Time = %d ms", elapsedL2Time));
		}
		
		acbx.setAcbCMD("CL");
		adcx.setFB(null);
		adcx.setRB(null);
		
		long	startCLTime		= System.currentTimeMillis();
		adcx.callAdabas();
		long	endCLTime		= System.currentTimeMillis();
		long	elapsedCLTime	= endCLTime - startCLTime;
		System.out.println(String.format("elapsedCLTime = %d ms", elapsedCLTime));
		
		long	endTime		= System.currentTimeMillis();
		long	elapsedTime	= endTime - startTime;
		
		System.out.println(String.format("elapsedTime   = %d ms", elapsedTime));
	}

	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasDirectCallX#callAdabas()}.
	 * @throws Exception
	 *  
	 * Test bad response code logic by sending OP to inactive database 255.
	 * This test requires that a database 255 is defined and known to AAS and is inactive.
	 */
	@Test
	public final void testRC148() throws Exception {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		AdabasDirectCallX	adcx = new AdabasDirectCallX(acbx);
		acbx.setAcbDBID(255);
		acbx.setAcbCMD("OP");
		
		AdabasBufferX opFB = new AdabasBufferX(".", AdabasBufferX.FB);
		adcx.setFB(opFB);
		
		AdabasBufferX opRB = new AdabasBufferX("UPD=9.", AdabasBufferX.RB);
		adcx.setRB(opRB);

		try {
			adcx.callAdabas();
		}
		catch (AdabasException e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("rc = 148")))
				fail("callAdabas() DBID = 255 did not generate AdabasException rc = 148");
		}
		
	}

	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasDirectCallX#callAdabas()}.
	 * @throws Exception
	 * 
	 * Get GCB with U1 command using ACBX.
	 */
	@Test
	public final void testGetGCBX() throws Exception {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		AdabasDirectCallX	adcx = new AdabasDirectCallX(acbx);
		
		acbx.setAcbDBID(dbid);										// default in JUnitCommon
		acbx.setAcbFNR(1);
		acbx.setAcbCMD("OP");
		acbx.setAcbADD5(getPrivAdd5());
		
		AdabasBufferX opFB = new AdabasBufferX(".", AdabasBufferX.FB);
		adcx.setFB(opFB);
		AdabasBufferX opRB = new AdabasBufferX("UTI.", AdabasBufferX.RB);
		adcx.setRB(opRB);
		adcx.callAdabas();
		
		acbx.setAcbCMD("U1");
		acbx.setAcbCOP1((byte) '0');
		acbx.setAcbCOP2((byte) '0');
		acbx.setAcbISN(1);
		acbx.setAcbADD5(getPrivAdd5());
		AdabasBufferX u1FB = new AdabasBufferX(".", AdabasBufferX.FB);
		adcx.setFB(u1FB);
		AdabasBufferX u1RB = new AdabasBufferX(1000, AdabasBufferX.RB);
		adcx.setRB(u1RB);
		adcx.callAdabas();
		
		acbx.setAcbCMD("CL");
		adcx.setFB(null);
		adcx.setRB(null);
		adcx.callAdabas();
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

	/**
	 * Mainframe OP/CL test for response 50 debugging for WCT.
	 * @throws Exception
	 *  
	 * Test simple OP/CL.
	 */
	@Test
	public final void testCallAdabasXRsp50() throws Exception {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX(true);
		AdabasDirectCallX	adcx = new AdabasDirectCallX(acbx, true);
		acbx.setAcbDBID(70);
		acbx.setAcbBaCMD("OP".getBytes("cp037"));
		
		AdabasBufferX opFB = new AdabasBufferX(9, AdabasBufferX.FB, true);
		adcx.setFB(opFB);
		
		AdabasBufferX opRB = new AdabasBufferX(32, AdabasBufferX.RB, true);
		System.arraycopy("ACC.".getBytes("cp037"), 0x00, opRB.getDataBytes(), 0x00, 4);
		adcx.setRB(opRB);

		adcx.callAdabas();
		
		acbx.setAcbBaCMD("CL".getBytes("cp037"));
		adcx.setFB(null);
		adcx.setRB(null);
		
		adcx.callAdabas();
	}

	/**
	 * Test simple OP/CL using setCallSource().
	 */
	@Test
	public final void testAMNCallAdabasX() throws Exception {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		AdabasDirectCallX	adcx = new AdabasDirectCallX(acbx);
		
		adcx.setCallSource(A2_AMNUSER);
		acbx.setAcbDBID(70);										// default in JUnitCommon
		acbx.setAcbCMD("OP");
		
		AdabasBufferX opFB = new AdabasBufferX(".", AdabasBufferX.FB);
		adcx.setFB(opFB);
		
		AdabasBufferX opRB = new AdabasBufferX("UPD=9.", AdabasBufferX.RB);
		adcx.setRB(opRB);

		adcx.callAdabas();
		
		acbx.setAcbCMD("CL");
		adcx.setFB(null);
		adcx.setRB(null);
		
		adcx.callAdabas();
	}

	/**
	 * Mainframe OP/CL test using explicit ASCII.
	 * @throws Exception
	 *  
	 * Test simple OP/CL.
	 */
	@Test
	public final void testCallAdabasASCII() throws Exception {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX(false);
		AdabasDirectCallX	adcx = new AdabasDirectCallX(acbx, false);
		acbx.setAcbDBID(70);
		acbx.setAcbBaCMD("OP".getBytes());
		
		AdabasBufferX opFB = new AdabasBufferX(9, AdabasBufferX.FB, false);
		adcx.setFB(opFB);
		
		AdabasBufferX opRB = new AdabasBufferX(32, AdabasBufferX.RB, false);
		System.arraycopy("ACC.".getBytes(), 0x00, opRB.getDataBytes(), 0x00, 4);
		adcx.setRB(opRB);

		adcx.callAdabas();
		
		acbx.setAcbBaCMD("CL".getBytes());
		adcx.setFB(null);
		adcx.setRB(null);
		
		adcx.callAdabas();
	}

}
