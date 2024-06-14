package com.softwareag.adabas.jas.junit;

import static org.junit.Assert.*;

import org.junit.Test;

import com.softwareag.adabas.jas.AdabasControlBlock;
import com.softwareag.adabas.jas.AdabasException;

/**
 * @author usadva
 * @author usarc
 */

/* 
 * Copyright (c) 1998-2021 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, 
 * and/or its subsidiaries and/or its affiliates and/or their licensors. Use, reproduction, transfer, 
 * publication or disclosure is prohibited except as specifically provided for in your License Agreement 
 * with Software AG.
 */

public class AdabasControlBlockTest extends JUnitCommon {

	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlock#setAcbCMD()},
	 *                 {@link com.softwareag.adabas.jas.AdabasControlBlock#setAcbDBID()},
	 *                 {@link com.softwareag.adabas.jas.AdabasControlBlock#setAcbFNR()},
	 *                 {@link com.softwareag.adabas.jas.AdabasControlBlock#toString()}.
	 * 
	 * Tests basic functionality for setting up an OP command.
	 */
	@Test
	public final void testAdabasControlBlockOP() throws AdabasException {
		
		AdabasControlBlock acb = new AdabasControlBlock();
		
		acb.setAcbCMD("OP");
		acb.setAcbDBID(700);
		acb.setAcbDBID(1);
		System.out.println(acb.toString());

	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlock#setACBArray()}
	 */
	@Test
	public final void testSetAcbArray() {
		
		AdabasControlBlock	acb	= new AdabasControlBlock();

		byte[] ba	= new byte[16];
		try {
			acb.setACBArray(ba);
		}
		catch (AdabasException ae) {
			System.out.println("Correct AdabasException thrown: " + ae.getMessage());
		}
	}

	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlock#setAcbCMD()}.
	 * 
	 * Test CMD values null, "OP", "O", "", "OPN".
	 */
	@Test
	public final void testSetCMD() throws AdabasException {
		
		AdabasControlBlock acb = new AdabasControlBlock();
		
		acb.setAcbCMD(null);
		acb.setAcbCMD("OP");
		acb.setAcbCMD("O");
		acb.setAcbCMD("");

		try {
			acb.setAcbCMD("OPN");
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbCMD length GT 2")))
				fail("AdabasControlBlock.setAcbCMD(\"OPN\") did not generate AdabasException acbCMD GT 2");
		}
	}

	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlock#setAcbCID(String)}.
	 * 
	 * Test CID values null, "1234", "123", "12", "1", "", and "12345".
	 */
	@Test
	public final void testSetCIDString() throws AdabasException {
		
		AdabasControlBlock acb = new AdabasControlBlock();
		
		String s = null;
		
		acb.setAcbCID(s);
		acb.setAcbCID("1234");
		acb.setAcbCID("123");
		acb.setAcbCID("12");
		acb.setAcbCID("1");
		acb.setAcbCID("");
		
		try {
			acb.setAcbCID("12345");
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbCID length GT 4")))
				fail("AdabasControlBlock.setAcbCID(\"12345\") did not generate AdabasException acbCID length GT 4");
		}
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlock#setAcbCID(byte[])}.
	 * 
	 * Test CID values 0xfffefdfc, 0x01020304, 0x010203, 0x0102, 0x01, null, "0x0102030405".
	 */
	@Test
	public final void testSetCIDByteArray() throws AdabasException {
		
		AdabasControlBlock acb = new AdabasControlBlock();
		
		byte [] b = null;
		
		acb.setAcbCID(new byte[] {(byte) 0xff, (byte) 0xfe, (byte) 0xfd, (byte) 0xfc});
		acb.setAcbCID(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04 });
		acb.setAcbCID(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03 });
		acb.setAcbCID(new byte[] {(byte) 0x01, (byte) 0x02 });
		acb.setAcbCID(new byte[] {(byte) 0x01});
		acb.setAcbCID(b);
	
		try {
			acb.setAcbCID(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05 });
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbCID length GT 4")))
				fail("AdabasControlBlock.setAcbCID(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03," +
						" (byte) 0x04, (byte) 0x05 }) did not generate AdabasException acbCID length GT 4");
		}
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlock#getAcbCIDString()}.
	 *
	 * Test get of String CID values.
	 * Test get of hex CID values that are invalid as a string.
	 */
	@Test
	public final void testGetCIDString() throws AdabasException {
		
		AdabasControlBlock acb = new AdabasControlBlock();
		
		acb.setAcbCID("ABCD");
		acb.getAcbCIDString();

		acb.setAcbCID(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04 });
		acb.getAcbCIDString();
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlock#setAcbDBID()}.
	 * 
	 * Test DBID values -5, 70000, 70, 44444.
	 */
	@Test
	public final void testSetDBID() throws AdabasException {
		
		AdabasControlBlock acb = new AdabasControlBlock();
		
		try {
			acb.setAcbDBID(-5);
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("DBID LT 1 or GT 65535")))
				fail("AdabasControlBlock.setAcbDBID(-5) did not generate AdabasException DBID LT 1 or GT 65535");
		}
		try {
			acb.setAcbDBID(70000);
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("DBID LT 1 or GT 65535")))
				fail("AdabasControlBlock.setAcbDBID(70000) did not generate AdabasException DBID LT 1 or GT 65535");
		}
		acb.setAcbDBID(70);
		acb.setAcbDBID(44444);
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlock#setAcbFNR()}.
	 * 
	 * Test FNR values -100, 32001, 1, 32000.
	 */
	@Test
	public final void testSetFNR() throws AdabasException {
		
		AdabasControlBlock acb = new AdabasControlBlock();
		
		try {
			acb.setAcbFNR((short) -100);
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("FNR LT 1 or GT 32000")))
				fail("AdabasControlBlock.setAcbFNR(-100) did not generate AdabasException FNR LT 1 or GT 32000");
		}
		try {
			acb.setAcbFNR((short) 32001);
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("FNR LT 1 or GT 32000")))
				fail("AdabasControlBlock.setAcbFNR(32001) did not generate AdabasException FNR LT 1 or GT 32000");
		}
		acb.setAcbFNR((short) 1);
		acb.setAcbFNR((short) 32000);
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlock#setAcbISN()}.
	 * 
	 * Test ISN values -100, 4294967296, 0, 4294967295.
	 */
	@Test
	public final void testSetISN() throws AdabasException {
		
		AdabasControlBlock acb = new AdabasControlBlock();
		
		try {
			acb.setAcbISN(-100L);
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("ISN LT 0 or GT 4294967295")))
				fail("AdabasControlBlock.setAcbISN(-100) did not generate AdabasException ISN LT 0 or GT 4294967295");
		}
		
		try {
			acb.setAcbISN(4294967296L);
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("ISN LT 0 or GT 4294967295")))
				fail("AdabasControlBlock.setAcbISN(4294967296) did not generate AdabasException ISN LT 0 or GT 4294967295");
		}
		acb.setAcbISN(0L);
		acb.setAcbISN(4294967295L);
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlock#setAcbFBL()}.
	 * 
	 * Test FBL values -100, 32768, 0, 32767.
	 */
	@Test
	public final void testSetFBL() throws AdabasException {
		
		AdabasControlBlock acb = new AdabasControlBlock();
		
		try {
			acb.setAcbFBL((short)-100);
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("FBL LT 0")))
				fail("AdabasControlBlock.setAcbFBL(-100) did not generate AdabasException FBL LT 0");
		}
		try {
			acb.setAcbFBL((short)32768);           // This will result in a negative 32768
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("FBL LT 0")))
				fail("AdabasControlBlock.setAcbFBL(32768) did not generate AdabasException FBL LT 0");
		}
		acb.setAcbFBL((short)0);
		acb.setAcbFBL((short)32767);
	}

	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlock#setAcbIBL()}.
	 * 
	 * Test FBL values -100, 32768, 0, 32767.
	 */
	@Test
	public final void testSetIBL() throws AdabasException {
		
		AdabasControlBlock acb = new AdabasControlBlock();
		
		try {
			acb.setAcbIBL((short)-100);
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("IBL LT 0")))
				fail("AdabasControlBlock.setAcbIBL(-100) did not generate AdabasException FBL LT 0");
		}
		try {
			acb.setAcbIBL((short)32768);           // This will result in a negative 32768
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("IBL LT 0")))
				fail("AdabasControlBlock.setAcbIBL(32768) did not generate AdabasException IBL LT 0");
		}
		acb.setAcbIBL((short)0);
		acb.setAcbIBL((short)32767);
	}

	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlock#setAcbRBL()}.
	 * 
	 * Test RBL values -100, 32768, 0, 32767.
	 */
	@Test
	public final void testSetRBL() throws AdabasException {
		
		AdabasControlBlock acb = new AdabasControlBlock();
		
		try {
			acb.setAcbRBL((short)-100);
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("RBL LT 0")))
				fail("AdabasControlBlock.setAcbRBL(-100) did not generate AdabasException RBL LT 0");
		}
		
		try {
			acb.setAcbRBL((short)32768);           // This will result in a negative 32768
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("RBL LT 0")))
				fail("AdabasControlBlock.setAcbRBL(32768) did not generate AdabasException RBL LT 0");
		}
		acb.setAcbRBL((short)0);
		acb.setAcbRBL((short)32767);
	}

	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlock#setAcbSBL()}.
	 * 
	 * Test SBL values -100, 32768, 0, 32767.
	 */
	@Test
	public final void testSetSBL() throws AdabasException {
		
		AdabasControlBlock acb = new AdabasControlBlock();
		
		try {
			acb.setAcbSBL((short)-100);
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("SBL LT 0")))
				fail("AdabasControlBlock.setAcbSBL(-100) did not generate AdabasException SBL LT 0");
		}
		try {
			acb.setAcbSBL((short)32768);           // This will result in a negative 32768
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("SBL LT 0")))
				fail("AdabasControlBlock.setAcbSBL(32768) did not generate AdabasException SBL LT 0");
		}
		acb.setAcbSBL((short)0);
		acb.setAcbSBL((short)32767);
	}

	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlock#setAcbVBL()}.
	 * 
	 * Test VBL values -100, 32768, 0, 32767.
	 */
	@Test
	public final void testSetVBL() throws AdabasException {
		
		AdabasControlBlock acb = new AdabasControlBlock();
		
		try {
			acb.setAcbVBL((short)-100);
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("VBL LT 0")))
				fail("AdabasControlBlock.setAcbVBL(-100) did not generate AdabasException VBL LT 0");
		}
		try {
			acb.setAcbVBL((short)32768);           // This will result in a negative 32768
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("VBL LT 0")))
				fail("AdabasControlBlock.setAcbVBL(32768) did not generate AdabasException VBL LT 0");
		}
		acb.setAcbVBL((short)0);
		acb.setAcbVBL((short)32767);
	}

	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlock#setAcbADD1(String)}.
	 * 
	 * Test ADD1 values null, "12345678", "1234567", "123456", "12345", "1234", "123", "12",
	 *  "1", "", and "123456789".
	 */
	@Test
	public final void testSetADD1String() throws AdabasException {
		
		AdabasControlBlock acb = new AdabasControlBlock();
		
		String s = null;
		
		acb.setAcbADD1(s);
		acb.setAcbADD1("12345678");
		acb.setAcbADD1("1234567");
		acb.setAcbADD1("123456");
		acb.setAcbADD1("12345");
		acb.setAcbADD1("1234");
		acb.setAcbADD1("123");
		acb.setAcbADD1("12");
		acb.setAcbADD1("1");
		acb.setAcbADD1("");

		try {
			acb.setAcbADD1("123456789");
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbADD1 length GT 8")))
				fail("AdabasControlBlock.setAcbADD1(\"123456789\") did not generate AdabasException acbADD1 length GT 8");
		}
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlock#setAcbADD1(byte [])}.
	 * 
	 * Test ADD1 values 0xfffefdfc, 0x0102030405060708, 0x01020304050607, 0x010203040506, 0x0102030405, 
	 *  0x01020304, 0x010203, 0x0102, 0x01,null, 0x010203040506070809.
	 */
	@Test
	public final void testSetADD1ByteArray() throws AdabasException {
		
		AdabasControlBlock acb = new AdabasControlBlock();
		
		byte [] b = null;
		
		acb.setAcbADD1(new byte[] {(byte) 0xff, (byte) 0xfe, (byte) 0xfd, (byte) 0xfc});
		acb.setAcbADD1(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06, (byte) 0x07, (byte) 0x08});
		acb.setAcbADD1(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06, (byte) 0x07});
		acb.setAcbADD1(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06});
		acb.setAcbADD1(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05});
		acb.setAcbADD1(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04 });
		acb.setAcbADD1(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03 });
		acb.setAcbADD1(new byte[] {(byte) 0x01, (byte) 0x02 });
		acb.setAcbADD1(new byte[] {(byte) 0x01});
		acb.setAcbADD1(b);
		
		try {
			acb.setAcbADD1(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
					(byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09});
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbADD1 length GT 8")))
				fail("AdabasControlBlock.setAcbADD1(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03," +
						" (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09})" +
						" did not generate AdabasException acbADD1 length GT 8");
		}
	}	
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlock#getAcbADD1String()}.
	 *
	 * Test get of String ADD1 values.
	 * Test get of hex ADD1 values that are invalid as a string.
	 */
	@Test
	public final void testGetADD1String() throws AdabasException {
		
		AdabasControlBlock acb = new AdabasControlBlock();
		
		acb.setAcbADD1("ABCDEFGH");
		acb.getAcbADD1String();

		acb.setAcbADD1(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08 });
		acb.getAcbADD1String();
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlock#setAcbADD2(String)}.
	 * 
	 * Test ADD2 values null, "12345", "1234", "123", "12", "1", "", and "12345".
	 */
	@Test
	public final void testSetADD2String() throws AdabasException {
		
		AdabasControlBlock acb = new AdabasControlBlock();
		
		String s = null;
		
		acb.setAcbADD2(s);
		acb.setAcbADD2("1234");
		acb.setAcbADD2("123");
		acb.setAcbADD2("12");
		acb.setAcbADD2("1");
		acb.setAcbADD2("");

		try {
			acb.setAcbADD2("12345");
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbADD2 length GT 4")))
				fail("AdabasControlBlock.setAcbADD2(\"12345\") did not generate AdabasException acbADD2 length GT 4");
		}
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlock#setAcbADD2(byte [])}.
	 * 
	 * Test ADD2 values 0xfffefdfc, 0x01020304, 0x010203, 0x0102, 0x01,null, 0x0102030405.
	 */
	@Test
	public final void testSetADD2ByteArray() throws AdabasException {
		
		AdabasControlBlock acb = new AdabasControlBlock();
		
		byte [] b = null;
		
		acb.setAcbADD2(new byte[] {(byte) 0xff, (byte) 0xfe, (byte) 0xfd, (byte) 0xfc});
		acb.setAcbADD2(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04 });
		acb.setAcbADD2(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03 });
		acb.setAcbADD2(new byte[] {(byte) 0x01, (byte) 0x02 });
		acb.setAcbADD2(new byte[] {(byte) 0x01});
		acb.setAcbADD2(b);
		
		try {
			acb.setAcbADD2(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05});
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbADD2 length GT 4")))
				fail("AdabasControlBlock.setAcbADD2(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03," +
						" (byte) 0x04, (byte) 0x05})" +
						" did not generate AdabasException acbADD2 length GT 4");
		}
	}	
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlock#setAcbADD2(long)}.
	 * 
	 * Test ISN values -100, 4294967296, 0, 4294967295.
	 */
	@Test
	public final void testSetAcbADD2Long() throws AdabasException {
		
		AdabasControlBlock acb = new AdabasControlBlock();
		
		try {
			acb.setAcbADD2(-100L);
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbADD2 LT 0 or GT 4294967295")))
				fail("AdabasControlBlock.setAcbADD2(-100) did not generate AdabasException acbADD2 LT 0 or GT 4294967295");
		}
		
		try {
			acb.setAcbADD2(4294967296L);
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbADD2 LT 0 or GT 4294967295")))
				fail("AdabasControlBlock.setAcbADD2(4294967296) did not generate AdabasException acbADD2 LT 0 or GT 4294967295");
		}
		acb.setAcbADD2(0L);
		acb.setAcbADD2(4294967295L);
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlock#getAcbADD2String()}.
	 *
	 * Test get of String ADD2 values.
	 * Test get of hex ADD2 values that are invalid as a string.
	 */
	@Test
	public final void testGetADD2String() throws AdabasException {
		
		AdabasControlBlock acb = new AdabasControlBlock();
		
		acb.setAcbADD2("ABCD");
		acb.getAcbADD2String();

		acb.setAcbADD2(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04 });
		acb.getAcbADD2String();
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlock#setAcbADD3(String)}.
	 * 
	 * Test ADD3 values null, "12345678", "1234567", "123456", "12345", "1234", "123", "12",
	 *  "1", "", and "123456789".
	 */
	@Test
	public final void testSetADD3String() throws AdabasException {
		
		AdabasControlBlock acb = new AdabasControlBlock();
		
		String s = null;
		
		acb.setAcbADD3(s);
		acb.setAcbADD3("12345678");
		acb.setAcbADD3("1234567");
		acb.setAcbADD3("123456");
		acb.setAcbADD3("12345");
		acb.setAcbADD3("1234");
		acb.setAcbADD3("123");
		acb.setAcbADD3("12");
		acb.setAcbADD3("1");
		acb.setAcbADD3("");
		
		try {
			acb.setAcbADD3("123456789");
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbADD3 length GT 8")))
				fail("AdabasControlBlock.setAcbADD3(\"123456789\") did not generate AdabasException acbADD3 length GT 8");
		}
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlock#setAcbADD3(byte [])}.
	 * 
	 * Test ADD3 values 0xfffefdfc, 0x0102030405060708, 0x01020304050607, 0x010203040506, 0x0102030405, 
	 *  0x01020304, 0x010203, 0x0102, 0x01,null, 0x010203040506070809.
	 */
	@Test
	public final void testSetADD3ByteArray() throws AdabasException {
		
		AdabasControlBlock acb = new AdabasControlBlock();
		
		byte [] b = null;
		
		acb.setAcbADD3(new byte[] {(byte) 0xff, (byte) 0xfe, (byte) 0xfd, (byte) 0xfc});
		acb.setAcbADD3(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06, (byte) 0x07, (byte) 0x08});
		acb.setAcbADD3(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06, (byte) 0x07});
		acb.setAcbADD3(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06});
		acb.setAcbADD3(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05});
		acb.setAcbADD3(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04 });
		acb.setAcbADD3(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03 });
		acb.setAcbADD3(new byte[] {(byte) 0x01, (byte) 0x02 });
		acb.setAcbADD3(new byte[] {(byte) 0x01});
		acb.setAcbADD3(b);
		
		try {
			acb.setAcbADD3(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
					(byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09});
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbADD3 length GT 8")))
				fail("AdabasControlBlock.setAcbADD3(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03," +
						" (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09})" +
						" did not generate AdabasException acbADD3 length GT 8");
		}
	}	

	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlock#getAcbADD3String()}.
	 *
	 * Test get of String ADD3 values.
	 * Test get of hex ADD3 values that are invalid as a string.
	 */
	@Test
	public final void testGetADD3String() throws AdabasException {
		
		AdabasControlBlock acb = new AdabasControlBlock();
		
		acb.setAcbADD3("ABCDEFGH");
		acb.getAcbADD3String();

		acb.setAcbADD3(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08 });
		acb.getAcbADD3String();
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlock#setAcbADD4(String)}.
	 * 
	 * Test ADD4 values null, "12345678", "1234567", "123456", "12345", "1234", "123", "12",
	 *  "1", "", and "123456789".
	 */
	@Test
	public final void testSetADD4String() throws AdabasException {
		
		AdabasControlBlock acb = new AdabasControlBlock();
		
		String s = null;
		
		acb.setAcbADD4(s);
		acb.setAcbADD4("12345678");
		acb.setAcbADD4("1234567");
		acb.setAcbADD4("123456");
		acb.setAcbADD4("12345");
		acb.setAcbADD4("1234");
		acb.setAcbADD4("123");
		acb.setAcbADD4("12");
		acb.setAcbADD4("1");
		acb.setAcbADD4("");
		
		try {
			acb.setAcbADD4("123456789");
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbADD4 length GT 8")))
				fail("AdabasControlBlock.setAcbADD4(\"123456789\") did not generate AdabasException acbADD4 length GT 8");
		}
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlock#setAcbADD4(byte [])}.
	 * 
	 * Test ADD4 values 0xfffefdfc, 0x0102030405060708, 0x01020304050607, 0x010203040506, 0x0102030405, 
	 *  0x01020304, 0x010203, 0x0102, 0x01,null, 0x010203040506070809.
	 */
	@Test
	public final void testSetADD4ByteArray() throws AdabasException {
		
		AdabasControlBlock acb = new AdabasControlBlock();
		
		byte [] b = null;
		
		acb.setAcbADD4(new byte[] {(byte) 0xff, (byte) 0xfe, (byte) 0xfd, (byte) 0xfc});
		acb.setAcbADD4(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06, (byte) 0x07, (byte) 0x08});
		acb.setAcbADD4(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06, (byte) 0x07});
		acb.setAcbADD4(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06});
		acb.setAcbADD4(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05});
		acb.setAcbADD4(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04 });
		acb.setAcbADD4(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03 });
		acb.setAcbADD4(new byte[] {(byte) 0x01, (byte) 0x02 });
		acb.setAcbADD4(new byte[] {(byte) 0x01});
		acb.setAcbADD4(b);
		
		try {
			acb.setAcbADD4(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
					(byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09});
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbADD4 length GT 8")))
				fail("AdabasControlBlock.setAcbADD4(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03," +
						" (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09})" +
						" did not generate AdabasException acbADD4 length GT 8");
		}
	}	

	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlock#getAcbADD4String()}.
	 *
	 * Test get of String ADD4 values.
	 * Test get of hex ADD4 values that are invalid as a string.
	 */
	@Test
	public final void testGetADD4String() throws AdabasException {
		
		AdabasControlBlock acb = new AdabasControlBlock();
		
		acb.setAcbADD4("ABCDEFGH");
		acb.getAcbADD4String();

		acb.setAcbADD4(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08 });
		acb.getAcbADD4String();
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlock#setAcbADD5(String)}.
	 * 
	 * Test ADD5 values null, "12345678", "1234567", "123456", "12345", "1234", "123", "12",
	 *  "1", "", and "123456789".
	 */
	@Test
	public final void testSetADD5String() throws AdabasException {
		
		AdabasControlBlock acb = new AdabasControlBlock();
		
		String s = null;
		
		acb.setAcbADD5(s);
		acb.setAcbADD5("12345678");
		acb.setAcbADD5("1234567");
		acb.setAcbADD5("123456");
		acb.setAcbADD5("12345");
		acb.setAcbADD5("1234");
		acb.setAcbADD5("123");
		acb.setAcbADD5("12");
		acb.setAcbADD5("1");
		acb.setAcbADD5("");
		
		try {
			acb.setAcbADD5("123456789");
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbADD5 length GT 8")))
				fail("AdabasControlBlock.setAcbADD5(\"123456789\") did not generate AdabasException acbADD5 length GT 8");
		}
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlock#setAcbADD5(byte [])}.
	 * 
	 * Test ADD5 values 0xfffefdfc, 0x0102030405060708, 0x01020304050607, 0x010203040506, 0x0102030405, 
	 *  0x01020304, 0x010203, 0x0102, 0x01,null, 0x010203040506070809.
	 */
	@Test
	public final void testSetADD5ByteArray() throws AdabasException {
		
		AdabasControlBlock acb = new AdabasControlBlock();
		
		byte [] b = null;
		
		acb.setAcbADD5(new byte[] {(byte) 0xff, (byte) 0xfe, (byte) 0xfd, (byte) 0xfc});
		acb.setAcbADD5(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06, (byte) 0x07, (byte) 0x08});
		acb.setAcbADD5(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06, (byte) 0x07});
		acb.setAcbADD5(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06});
		acb.setAcbADD5(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05});
		acb.setAcbADD5(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04 });
		acb.setAcbADD5(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03 });
		acb.setAcbADD5(new byte[] {(byte) 0x01, (byte) 0x02 });
		acb.setAcbADD5(new byte[] {(byte) 0x01});
		acb.setAcbADD5(b);
					
		try {
			acb.setAcbADD5(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
					(byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09});
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbADD5 length GT 8")))
				fail("AdabasControlBlock.setAcbADD5(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03," +
						" (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09})" +
						" did not generate AdabasException acbADD5 length GT 8");
		}
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlock#getAcbADD5String()}.
	 *
	 * Test get of String ADD5 values.
	 * Test get of hex ADD5 values that are invalid as a string.
	 */
	@Test
	public final void testGetADD5String() throws AdabasException {
		
		AdabasControlBlock acb = new AdabasControlBlock();
		
		acb.setAcbADD5("ABCDEFGH");
		acb.getAcbADD5String();

		acb.setAcbADD5(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08 });
		acb.getAcbADD5String();
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlock#setAcbCMDT()}.
	 * 
	 * Test CMDT values -100, 4294967296, 0, 4294967295.
	 */
	@Test
	public final void testSetCMDT() throws AdabasException {
		
		AdabasControlBlock acb = new AdabasControlBlock();
		
		try {
			acb.setAcbCMDT(-100l);
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbCMDT LT 0 or GT 4294967295")))
				fail("AdabasControlBlock.setAcbCMDT(-100) did not generate AdabasException acbCMDT LT 0 or GT 4294967295");
		}
		
		try {
			acb.setAcbCMDT(4294967296l);
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbCMDT LT 0 or GT 4294967295")))
				fail("AdabasControlBlock.setAcbCMDT(4294967296) did not generate AdabasException acbCMDT LT 0 or GT 4294967295");
		}
		acb.setAcbCMDT(0l);
		acb.setAcbCMDT(4294967295l);
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlock#setAcbUSER()}.
	 * 
	 * Test USER values 0xfffefdfc, 0x01020304, 0x010203, 0x0102, 0x01, null, "0x0102030405".
	 */
	@Test
	public final void testSetAcbUser() throws AdabasException {
		
		AdabasControlBlock acb = new AdabasControlBlock();
		
		byte [] b = null;
		
		acb.setAcbUSER(new byte[] {(byte) 0xff, (byte) 0xfe, (byte) 0xfd, (byte) 0xfc});
		acb.setAcbUSER(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04 });
		acb.setAcbUSER(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03 });
		acb.setAcbUSER(new byte[] {(byte) 0x01, (byte) 0x02 });
		acb.setAcbUSER(new byte[] {(byte) 0x01});
		acb.setAcbUSER(b);

		try {
			acb.setAcbUSER(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05 });
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbUSER length GT 4")))
				fail("AdabasControlBlock.setAcbUSER(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03," +
						" (byte) 0x04, (byte) 0x05 }) did not generate AdabasException acbUSER length GT 4");
		}
	}
	
	/**
	 * Test method for all ACB get methods {@link com.softwareag.adabas.jas.AdabasControlBlock}.
	 */
	@Test
	public final void testSetGetAcbFields() throws AdabasException {
		
		AdabasControlBlock acb = new AdabasControlBlock();
	
		System.out.println("Test Set and Get methods for ACB fields:");
		
		acb.setAcbCMD("OP");
		System.out.println("acbCMD = " + acb.getAcbCMD());
		
		acb.setAcbRSP((short) 148);
		System.out.println(String.format("acbRSP = %d", acb.getAcbRSP()));
		
		acb.setAcbCID(new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff});
		System.out.println(String.format("acbCID = 0x%s", byteArrayToHex(acb.getAcbCID())));

		acb.setAcbDBID(65535);
		System.out.println(String.format("acbDBID = %d", acb.getAcbDBID()));
	
		acb.setAcbFNR(32000);
		System.out.println(String.format("acbFNR = %d", acb.getAcbFNR()));

		acb.setAcbISN(4294967295l);
		System.out.println(String.format("acbISN = %d", acb.getAcbISN()));
		
		acb.setAcbISL(4294967295l);
		System.out.println(String.format("acbISL = %d", acb.getAcbISL()));
		
		acb.setAcbISQ(4294967295l);
		System.out.println(String.format("acbISQ = %d", acb.getAcbISQ()));
		
		acb.setAcbFBL((short)32767);
		System.out.println(String.format("acbFBL = %d", acb.getAcbFBL()));
		
		acb.setAcbRBL((short)32767);
		System.out.println(String.format("acbRBL = %d", acb.getAcbRBL()));
		
		acb.setAcbSBL((short)32767);
		System.out.println(String.format("acbSBL = %d", acb.getAcbSBL()));
		
		acb.setAcbVBL((short)32767);
		System.out.println(String.format("acbVBL = %d", acb.getAcbVBL()));
		
		acb.setAcbIBL((short)32767);
		System.out.println(String.format("acbIBL = %d", acb.getAcbIBL()));

		acb.setAcbCOP1((byte) 0x41);
		System.out.println(String.format("acbCOP1 = 0x%x", acb.getAcbCOP1()));

		acb.setAcbCOP2((byte) 0x42);
		System.out.println(String.format("acbCOP2 = 0x%x", acb.getAcbCOP2()));

		acb.setAcbADD1(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06, (byte) 0x07, (byte) 0x08});
		System.out.println(String.format("acbADD1 = 0x%s", byteArrayToHex(acb.getAcbADD1())));

		acb.setAcbADD2(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04});
		System.out.println(String.format("acbADD2 = 0x%s", byteArrayToHex(acb.getAcbADD2())));

		acb.setAcbADD3(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06, (byte) 0x07, (byte) 0x08});
		System.out.println(String.format("acbADD3 = 0x%s", byteArrayToHex(acb.getAcbADD3())));

		acb.setAcbADD4(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06, (byte) 0x07, (byte) 0x08});
		System.out.println(String.format("acbADD4 = 0x%s", byteArrayToHex(acb.getAcbADD4())));

		acb.setAcbADD5(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06, (byte) 0x07, (byte) 0x08});
		System.out.println(String.format("acbADD5 = 0x%s", byteArrayToHex(acb.getAcbADD5())));

		acb.setAcbCMDT(4294967295l);
		System.out.println(String.format("acbCMDT = %d", acb.getAcbCMDT()));
		
		acb.setAcbUSER(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04});
		System.out.println(String.format("acbUSER = 0x%s", byteArrayToHex(acb.getAcbUSER())));

		System.out.println(acb.toString());
	}
	
	/**
	 * Convert a byte array to a string of display hex characters
	 * 
	 * @param a byte array
	 * @return 
	 */
	public String byteArrayToHex(byte[] a) {
	   StringBuilder sb = new StringBuilder();
	   for(byte b: a)
	      sb.append(String.format("%02x", b&0xff));
	   return sb.toString();
	}
}
