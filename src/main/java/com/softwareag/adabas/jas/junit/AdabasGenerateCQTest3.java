package com.softwareag.adabas.jas.junit;

import org.junit.Test;

import com.softwareag.adabas.jas.AdabasBufferX;
import com.softwareag.adabas.jas.AdabasControlBlockX;
import com.softwareag.adabas.jas.AdabasDirectCallX;

/**
 * Test class to generate Adabas Command Queue entries by running many commands in a loop.
 * 
 * Reads all records in MISCELLANEOUS in ISN order 10 times using L1 commands.
 * 
 * @author usadva
 */

/* 
 * Copyright (c) 1998-2021 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, 
 * and/or its subsidiaries and/or its affiliates and/or their licensors. Use, reproduction, transfer, 
 * publication or disclosure is prohibited except as specifically provided for in your License Agreement 
 * with Software AG.
 */

public class AdabasGenerateCQTest3 extends JUnitCommon {

	@Test
	public void generateCQTest() throws Exception {

		AdabasControlBlockX acbx = new AdabasControlBlockX();
		AdabasDirectCallX	adcx = new AdabasDirectCallX(acbx);
		
		for (int i=0; i<10; i++) {
			
			acbx.setAcbDBID(dbid);										// default in JUnitCommon
			acbx.setAcbFNR(1);
			acbx.setAcbCMD("OP");
			acbx.setAcbADD1("JASCQTS3");

			AdabasBufferX opFB = new AdabasBufferX(".", AdabasBufferX.FB);
			adcx.setFB(opFB);
			AdabasBufferX opRB = new AdabasBufferX("ACC=9.", AdabasBufferX.RB);
			adcx.setRB(opRB);
			adcx.callAdabas();

			System.out.println("OP successful.");

			short	rc		= 0;
			long	isn		= 1;

			acbx.setAcbFNR(13);
			acbx.setAcbCMD("L1");
			acbx.setAcbCID("CQI3");
			acbx.setAcbCOP2((byte) 'I');
			AdabasBufferX L1FB = new AdabasBufferX("CA.", AdabasBufferX.FB);
			adcx.setFB(L1FB);
			AdabasBufferX L1RB = new AdabasBufferX(1000, AdabasBufferX.RB);
			adcx.setRB(L1RB);

			do {
				acbx.setAcbISN(isn);
				adcx.callAdabas();

				rc 	= acbx.getAcbRSP();
				if (rc != 0) {
					isn++;
					continue;
				}

				System.out.println("L1 ISN = " + isn + " successful.");
				isn++;

			} while (rc < 3);
		
			if (rc == 3) {
				System.out.println("EOF reached");
			}

			acbx.setAcbCMD("CL");
			adcx.setFB(null);
			adcx.setRB(null);
			adcx.callAdabas();

			System.out.println("CL successful.");

		}
		
	}
}
