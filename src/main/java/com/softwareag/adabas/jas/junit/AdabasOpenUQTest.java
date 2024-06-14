package com.softwareag.adabas.jas.junit;

import org.junit.Test;

import com.softwareag.adabas.jas.AdabasBufferX;
import com.softwareag.adabas.jas.AdabasControlBlockX;
import com.softwareag.adabas.jas.AdabasDirectCallX;

/**
 * Test class to create Adabas User Queue and leave it around.
 * 
 * @author usadva
 */

/* 
 * Copyright (c) 1998-2021 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, 
 * and/or its subsidiaries and/or its affiliates and/or their licensors. Use, reproduction, transfer, 
 * publication or disclosure is prohibited except as specifically provided for in your License Agreement 
 * with Software AG.
 */

public class AdabasOpenUQTest extends JUnitCommon {

	@Test
	public void openUQTest() throws Exception {

		AdabasControlBlockX acbx = new AdabasControlBlockX();
		AdabasDirectCallX	adcx = new AdabasDirectCallX(acbx);
		
		acbx.setAcbDBID(dbid);										// default in JUnitCommon
		acbx.setAcbFNR(1);
		acbx.setAcbCMD("OP");
		acbx.setAcbADD1("JASUQTST");
		
		AdabasBufferX opFB = new AdabasBufferX(".", AdabasBufferX.FB);
		adcx.setFB(opFB);
		AdabasBufferX opRB = new AdabasBufferX("UPD=9,12,13.", AdabasBufferX.RB);
		adcx.setRB(opRB);
		adcx.callAdabas();
		
		System.out.println("OP successful.");
		
		short 	held 	= 0;
		short	rc		= 0;
		long	isn		= 1;
		
		do {
			acbx.setAcbFNR(9);
			acbx.setAcbCMD("L4");
			acbx.setAcbISN(isn);
			AdabasBufferX L4FB = new AdabasBufferX("AA.", AdabasBufferX.FB);
			adcx.setFB(L4FB);
			AdabasBufferX L4RB = new AdabasBufferX(1000, AdabasBufferX.RB);
			adcx.setRB(L4RB);
			adcx.callAdabas();
			
			rc 	= acbx.getAcbRSP();
			if (rc != 0) {
				isn++;
				continue;
			}
			
			System.out.println("L4 ISN = " + isn + " successful.");
			isn++;
			held++;
			
		} while (held <= 5);
		
		System.out.println("No CL/RC issued. User queue entry remains ..");
		
	}
	
	/*
	 * Run this test after openUQTest() to create a waiting for ISN CQ entry 
	 * Should fail with an XTS timeout.
	 */
	@Test
	public void openUQTest2() throws Exception {

		AdabasControlBlockX acbx = new AdabasControlBlockX();
		AdabasDirectCallX	adcx = new AdabasDirectCallX(acbx);
		
		acbx.setAcbDBID(dbid);										// default in JUnitCommon
		acbx.setAcbFNR(1);
		acbx.setAcbCMD("OP");
		acbx.setAcbADD1("JASUQTS2");
		
		AdabasBufferX opFB = new AdabasBufferX(".", AdabasBufferX.FB);
		adcx.setFB(opFB);
		AdabasBufferX opRB = new AdabasBufferX("UPD=9,12,13.", AdabasBufferX.RB);
		adcx.setRB(opRB);
		adcx.callAdabas();
		
		System.out.println("OP successful.");
		
		short 	held 	= 0;
		short	rc		= 0;
		long	isn		= 1;
		
		do {
			acbx.setAcbFNR(9);
			acbx.setAcbCMD("L4");
			acbx.setAcbISN(isn);
			AdabasBufferX L4FB = new AdabasBufferX("AA.", AdabasBufferX.FB);
			adcx.setFB(L4FB);
			AdabasBufferX L4RB = new AdabasBufferX(1000, AdabasBufferX.RB);
			adcx.setRB(L4RB);
			adcx.callAdabas();
			
			rc 	= acbx.getAcbRSP();
			if (rc != 0) {
				isn++;
				continue;
			}
			
			System.out.println("L4 ISN = " + isn + " successful.");
			isn++;
			held++;
			
		} while (held <= 5);
		
		System.out.println("No CL/RC issued. User queue entry remains ..");
		
	}
}
