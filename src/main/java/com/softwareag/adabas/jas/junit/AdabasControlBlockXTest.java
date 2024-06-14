package com.softwareag.adabas.jas.junit;

import static org.junit.Assert.fail;

import org.junit.Test;

import com.softwareag.adabas.jas.AdabasControlBlockX;
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

public class AdabasControlBlockXTest extends JUnitCommon {

	@Test
	public final void testAdabasControlBlockX() throws AdabasException {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		
		acbx.setAcbCMD("OP");
		acbx.setAcbFNR( 700);
		System.out.println(acbx.toStringBrief());
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlockX#setAcbCMD()}.
	 * 
	 * Test CMD values null, "OP", "O", "", "OPN".
	 */
	@Test
	public final void testSetCMD() throws AdabasException {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		
		acbx.setAcbCMD(null);
		acbx.setAcbCMD("OP");
		acbx.setAcbCMD("O");
		acbx.setAcbCMD("");

		try {
			acbx.setAcbCMD("OPN");
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbxCMD length GT 2")))
				fail("AdabasControlBlockX.setAcbCMD(\"OPN\") did not generate AdabasException acbxCMD GT 2");
		}
	}

	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlockX#setACBArray()}
	 * 
	 * @throws AdabasException	Adabas specific exception. 
	 */
	@Test
	public final void testSetAcbxArray() throws AdabasException {
		
		AdabasControlBlockX	acbx	= new AdabasControlBlockX();

		byte[] ba	= new byte[16];
		try {
			acbx.setACBArray(ba);
		}
		catch (AdabasException ae) {
			System.out.println("Correct AdabasException thrown: " + ae.getMessage());
		}
	}

	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlockX#setAcbCID(String)}.
	 * 
	 * Test CID values null, "1234", "123", "12", "1", "", and "12345".
	 */
	@Test
	public final void testSetCIDString() throws AdabasException {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		
		String s = null;
		
		acbx.setAcbCID(s);
		acbx.setAcbCID("1234");
		acbx.setAcbCID("123");
		acbx.setAcbCID("12");
		acbx.setAcbCID("1");
		acbx.setAcbCID("");
		
		try {
			acbx.setAcbCID("12345");
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbxCID length GT 4")))
				fail("AdabasControlBlockX.setAcbCID(\"12345\") did not generate AdabasException acbxCID length GT 4");
		}
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlockX#setAcbCID(byte[])}.
	 * 
	 * Test CID values 0xfffefdfc, 0x01020304, 0x010203, 0x0102, 0x01, null, "0x0102030405".
	 */
	@Test
	public final void testSetCIDByteArray() throws AdabasException {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		
		byte [] b = null;
		
		acbx.setAcbCID(new byte[] {(byte) 0xff, (byte) 0xfe, (byte) 0xfd, (byte) 0xfc});
		acbx.setAcbCID(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04 });
		acbx.setAcbCID(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03 });
		acbx.setAcbCID(new byte[] {(byte) 0x01, (byte) 0x02 });
		acbx.setAcbCID(new byte[] {(byte) 0x01});
		acbx.setAcbCID(b);
	
		try {
			acbx.setAcbCID(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05 });
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbxCID length GT 4")))
				fail("AdabasControlBlockX.setAcbCID(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03," +
						" (byte) 0x04, (byte) 0x05 }) did not generate AdabasException acbxCID length GT 4");
		}
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlockX#getAcbCIDString()}.
	 *
	 * Test get of String CID values.
	 * Test get of hex CID values that are invalid as a string.
	 */
	@Test
	public final void testGetCIDString() throws AdabasException {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		
		acbx.setAcbCID("ABCD");
		acbx.getAcbCIDString();

		acbx.setAcbCID(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04 });
		acbx.getAcbCIDString();
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlockX#setAcbDBID()}.
	 * 
	 * Test DBID values -5, 70000, 70, 44444.
	 */
	@Test
	public final void testSetDBID() throws AdabasException {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		
		try {
			acbx.setAcbDBID(-5);
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("DBID LT 1 or GT 65535")))
				fail("AdabasControlBlockX.setAcbDBID(-5) did not generate AdabasException DBID LT 1 or GT 65535");
		}
		try {
			acbx.setAcbDBID(70000);
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("DBID LT 1 or GT 65535")))
				fail("AdabasControlBlockX.setAcbDBID(70000) did not generate AdabasException DBID LT 1 or GT 65535");
		}
		acbx.setAcbDBID(70);
		acbx.setAcbDBID(44444);
		System.out.println(acbx.toStringBrief());
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlockX#setAcbFNR()}.
	 * 
	 * Test FNR values -100, 32001, 1, 32000.
	 */
	@Test
	public final void testSetFNR() throws AdabasException {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		
		try {
			acbx.setAcbFNR((short) -100);
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("FNR LT 1 or GT 32000")))
				fail("AdabasControlBlockX.setAcbDBID(-100) did not generate AdabasException FNR LT 1 or GT 32000");
		}
		try {
			acbx.setAcbFNR((short) 32001);
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("FNR LT 1 or GT 32000")))
				fail("AdabasControlBlockX.setAcbDBID(-100) did not generate AdabasException FNR LT 1 or GT 32000");
		}
		acbx.setAcbFNR((short) 1);
		acbx.setAcbFNR((short) 32000);
		System.out.println(acbx.toStringBrief());
	}

	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlockX#setAcbISN()}.
	 * 
	 * Test ISN values -100, 4294967296, 0, 4294967295.
	 */
	@Test
	public final void testSetISN() throws AdabasException {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		
		try {
			acbx.setAcbISN(-100l);
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbxISN LT 0 or GT 4294967295")))
				fail("AdabasControlBlockX.setAcbISN(-100) did not generate AdabasException acbxISN LT 0 or GT 4294967295");
		}
		try {
			acbx.setAcbISN(4294967296l);
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbxISN LT 0 or GT 4294967295")))
				fail("AdabasControlBlockX.setAcbISN(4294967296) did not generate AdabasException acbxISN LT 0 or GT 4294967295");
		}
		acbx.setAcbISN(0l);
		acbx.setAcbISN(4294967295l);
	}

	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlockX#setAcbISL()}.
	 * 
	 * Test ISN values -100, 4294967296, 0, 4294967295.
	 */
	@Test
	public final void testSetISL() throws AdabasException {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		
		try {
			acbx.setAcbISL(-100l);
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbxISL LT 0 or GT 4294967295")))
				fail("AdabasControlBlockX.setAcbISL(-100) did not generate AdabasException acbxISL LT 0 or GT 4294967295");
		}
		try {
			acbx.setAcbISL(4294967296l);
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbxISL LT 0 or GT 4294967295")))
				fail("AdabasControlBlockX.setAcbISL(4294967296) did not generate AdabasException acbxISL LT 0 or GT 4294967295");
		}
		acbx.setAcbISL(0l);
		acbx.setAcbISL(4294967295l);
	}
	

	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlockX#setAcbISQ()}.
	 * 
	 * Test ISN values -100, 4294967296, 0, 4294967295.
	 */
	@Test
	public final void testSetISQ() throws AdabasException {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		
		try {
			acbx.setAcbISQ(-100l);
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbxISQ LT 0 or GT 4294967295")))
				fail("AdabasControlBlockX.setAcbISQ(-100) did not generate AdabasException acbxISQ LT 0 or GT 4294967295");
		}
		try {
			acbx.setAcbISQ(4294967296l);
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbxISQ LT 0 or GT 4294967295")))
				fail("AdabasControlBlockX.setAcbISQ(4294967296) did not generate AdabasException acbxISQ LT 0 or GT 4294967295");
		}
		acbx.setAcbISQ(0l);
		acbx.setAcbISQ(4294967295l);
	}

	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlockX#setAcbADD1(String acbxADD1)}.
	 * 
	 * Test ADD1 values null, "12345678", "1234567", "123456", "12345", "1234", "123", "12",
	 *  "1", "", and "123456789".
	 */
	@Test
	public final void testSetADD1String() throws AdabasException {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		
		String s = null;
		
		acbx.setAcbADD1(s);
		acbx.setAcbADD1("12345678");
		acbx.setAcbADD1("1234567");
		acbx.setAcbADD1("123456");
		acbx.setAcbADD1("12345");
		acbx.setAcbADD1("1234");
		acbx.setAcbADD1("123");
		acbx.setAcbADD1("12");
		acbx.setAcbADD1("1");
		acbx.setAcbADD1("");

		try {
			acbx.setAcbADD1("123456789");
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbxADD1 length GT 8")))
				fail("AdabasControlBlockX.setAcbADD1(\"123456789\") did not generate AdabasException acbxADD1 length GT 8");
		}
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlockX#setAcbADD1(byte [] acbxADD1)}.
	 * 
	 * Test ADD1 values 0xfffefdfc, 0x0102030405060708, 0x01020304050607, 0x010203040506, 0x0102030405, 
	 *  0x01020304, 0x010203, 0x0102, 0x01,null, 0x010203040506070809.
	 */
	@Test
	public final void testSetADD1ByteArray() throws AdabasException {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		
		byte [] b = null;
		
		acbx.setAcbADD1(new byte[] {(byte) 0xff, (byte) 0xfe, (byte) 0xfd, (byte) 0xfc});
		acbx.setAcbADD1(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06, (byte) 0x07, (byte) 0x08});
		acbx.setAcbADD1(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06, (byte) 0x07});
		acbx.setAcbADD1(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06});
		acbx.setAcbADD1(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05});
		acbx.setAcbADD1(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04 });
		acbx.setAcbADD1(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03 });
		acbx.setAcbADD1(new byte[] {(byte) 0x01, (byte) 0x02 });
		acbx.setAcbADD1(new byte[] {(byte) 0x01});
		acbx.setAcbADD1(b);
		
		try {
			acbx.setAcbADD1(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
					(byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09});
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbxADD1 length GT 8")))
				fail("AdabasControlBlockX.setAcbADD1(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03," +
						" (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09})" +
						" did not generate AdabasException acbxADD1 length GT 8");
		}
	}	
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlockX#getAcbADD1String()}.
	 *
	 * Test get of String ADD1 values.
	 * Test get of hex ADD1 values that are invalid as a string.
	 */
	@Test
	public final void testGetADD1String() throws AdabasException {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		
		acbx.setAcbADD1("ABCDEFGH");
		acbx.getAcbADD1String();

		acbx.setAcbADD1(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08 });
		acbx.getAcbADD1String();
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlock#setAcbxADD2(String)}.
	 * 
	 * Test ADD2 values null, "12345", "1234", "123", "12", "1", "", and "12345".
	 */
	@Test
	public final void testSetADD2String() throws AdabasException {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		
		String s = null;
		
		acbx.setAcbADD2(s);
		acbx.setAcbADD2("1234");
		acbx.setAcbADD2("123");
		acbx.setAcbADD2("12");
		acbx.setAcbADD2("1");
		acbx.setAcbADD2("");

		try {
			acbx.setAcbADD2("12345");
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbxADD2 length GT 4")))
				fail("AdabasControlBlock.setAcbADD2(\"12345\") did not generate AdabasException acbxADD2 length GT 4");
		}
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlockX#setAcbADD2(byte [])}.
	 * 
	 * Test ADD2 values 0xfffefdfc, 0x01020304, 0x010203, 0x0102, 0x01,null, 0x0102030405.
	 */
	@Test
	public final void testSetADD2ByteArray() throws AdabasException {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		
		byte [] b = null;
		
		acbx.setAcbADD2(new byte[] {(byte) 0xff, (byte) 0xfe, (byte) 0xfd, (byte) 0xfc});
		acbx.setAcbADD2(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04 });
		acbx.setAcbADD2(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03 });
		acbx.setAcbADD2(new byte[] {(byte) 0x01, (byte) 0x02 });
		acbx.setAcbADD2(new byte[] {(byte) 0x01});
		acbx.setAcbADD2(b);
		
		try {
			acbx.setAcbADD2(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05});
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbxADD2 length GT 4")))
				fail("AdabasControlBlock.setAcbADD2(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03," +
						" (byte) 0x04, (byte) 0x05})" +
						" did not generate AdabasException acbxADD2 length GT 4");
		}
	}	
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlock#setAcbxADD2Long()}.
	 * 
	 * Test ADD2 values -100, 4294967296, 0, 4294967295.
	 */
	@Test
	public final void testSetADD2Long() throws AdabasException {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		
		try {
			acbx.setAcbADD2(-100L);
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbxADD2 LT 0 or GT 4294967295")))
				fail("AdabasControlBlockX.setAcbADD2(-100) did not generate AdabasException acbxADD2 LT 0 or GT 4294967295");
		}
		try {
			acbx.setAcbADD2(4294967296L);
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbxADD2 LT 0 or GT 4294967295")))
				fail("AdabasControlBlockX.setAcbADD2(4294967296) did not generate AdabasException acbxADD2 LT 0 or GT 4294967295");
		}
		acbx.setAcbADD2(0L);
		acbx.setAcbADD2(4294967295L);
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlockX#getAcbADD2String()}.
	 *
	 * Test get of String ADD2 values.
	 * Test get of hex ADD2 values that are invalid as a string.
	 */
	@Test
	public final void testGetADD2String() throws AdabasException {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		
		acbx.setAcbADD2("ABCD");
		acbx.getAcbADD2String();

		acbx.setAcbADD2(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04 });
		acbx.getAcbADD2String();
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlockX#setAcbADD3(String acbxADD3)}.
	 * 
	 * Test ADD3 values null, "12345678", "1234567", "123456", "12345", "1234", "123", "12",
	 *  "1", "", and "123456789".
	 */
	@Test
	public final void testSetADD3String() throws AdabasException {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		
		String s = null;
		
		acbx.setAcbADD3(s);
		acbx.setAcbADD3("12345678");
		acbx.setAcbADD3("1234567");
		acbx.setAcbADD3("123456");
		acbx.setAcbADD3("12345");
		acbx.setAcbADD3("1234");
		acbx.setAcbADD3("123");
		acbx.setAcbADD3("12");
		acbx.setAcbADD3("1");
		acbx.setAcbADD3("");
		
		try {
			acbx.setAcbADD3("123456789");
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbxADD3 length GT 8")))
				fail("AdabasControlBlockX.setAcbADD3(\"123456789\") did not generate AdabasException acbxADD3 length GT 8");
		}
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlockX#setAcbADD3(byte [] acbxADD3)}.
	 * 
	 * Test ADD3 values 0xfffefdfc, 0x0102030405060708, 0x01020304050607, 0x010203040506, 0x0102030405, 
	 *  0x01020304, 0x010203, 0x0102, 0x01,null, 0x010203040506070809.
	 */
	@Test
	public final void testSetADD3ByteArray() throws AdabasException {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		
		byte [] b = null;
		
		acbx.setAcbADD3(new byte[] {(byte) 0xff, (byte) 0xfe, (byte) 0xfd, (byte) 0xfc});
		acbx.setAcbADD3(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06, (byte) 0x07, (byte) 0x08});
		acbx.setAcbADD3(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06, (byte) 0x07});
		acbx.setAcbADD3(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06});
		acbx.setAcbADD3(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05});
		acbx.setAcbADD3(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04 });
		acbx.setAcbADD3(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03 });
		acbx.setAcbADD3(new byte[] {(byte) 0x01, (byte) 0x02 });
		acbx.setAcbADD3(new byte[] {(byte) 0x01});
		acbx.setAcbADD3(b);
		
		try {
			acbx.setAcbADD3(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
					(byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09});
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbxADD3 length GT 8")))
				fail("AdabasControlBlockX.setAcbADD3(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03," +
						" (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09})" +
						" did not generate AdabasException acbxADD3 length GT 8");
		}
	}	

	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlockX#getAcbADD3String()}.
	 *
	 * Test get of String ADD3 values.
	 * Test get of hex ADD3 values that are invalid as a string.
	 */
	@Test
	public final void testGetADD3String() throws AdabasException {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		
		acbx.setAcbADD3("ABCDEFGH");
		acbx.getAcbADD3String();

		acbx.setAcbADD3(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08 });
		acbx.getAcbADD3String();
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlockX#setAcbADD4(String acbxADD4)}.
	 * 
	 * Test ADD4 values null, "12345678", "1234567", "123456", "12345", "1234", "123", "12",
	 *  "1", "", and "123456789".
	 */
	@Test
	public final void testSetADD4String() throws AdabasException {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		
		String s = null;
		
		acbx.setAcbADD4(s);
		acbx.setAcbADD4("12345678");
		acbx.setAcbADD4("1234567");
		acbx.setAcbADD4("123456");
		acbx.setAcbADD4("12345");
		acbx.setAcbADD4("1234");
		acbx.setAcbADD4("123");
		acbx.setAcbADD4("12");
		acbx.setAcbADD4("1");
		acbx.setAcbADD4("");
		
		try {
			acbx.setAcbADD4("123456789");
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbxADD4 length GT 8")))
				fail("AdabasControlBlockX.setAcbADD4(\"123456789\") did not generate AdabasException acbxADD4 length GT 8");
		}
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlockX#setAcbADD4(byte [] acbxADD4)}.
	 * 
	 * Test ADD4 values 0xfffefdfc, 0x0102030405060708, 0x01020304050607, 0x010203040506, 0x0102030405, 
	 *  0x01020304, 0x010203, 0x0102, 0x01,null, 0x010203040506070809.
	 */
	@Test
	public final void testSetADD4ByteArray() throws AdabasException {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		
		byte [] b = null;
		
		acbx.setAcbADD4(new byte[] {(byte) 0xff, (byte) 0xfe, (byte) 0xfd, (byte) 0xfc});
		acbx.setAcbADD4(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06, (byte) 0x07, (byte) 0x08});
		acbx.setAcbADD4(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06, (byte) 0x07});
		acbx.setAcbADD4(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06});
		acbx.setAcbADD4(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05});
		acbx.setAcbADD4(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04 });
		acbx.setAcbADD4(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03 });
		acbx.setAcbADD4(new byte[] {(byte) 0x01, (byte) 0x02 });
		acbx.setAcbADD4(new byte[] {(byte) 0x01});
		acbx.setAcbADD4(b);
		
		try {
			acbx.setAcbADD4(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
					(byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09});
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbxADD4 length GT 8")))
				fail("AdabasControlBlockX.setAcbADD4(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03," +
						" (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09})" +
						" did not generate AdabasException acbxADD4 length GT 8");
		}
	}	

	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlockX#getAcbADD4String()}.
	 *
	 * Test get of String ADD4 values.
	 * Test get of hex ADD4 values that are invalid as a string.
	 */
	@Test
	public final void testGetADD4String() throws AdabasException {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		
		acbx.setAcbADD4("ABCDEFGH");
		acbx.getAcbADD4String();

		acbx.setAcbADD4(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08 });
		acbx.getAcbADD4String();
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlockX#setAcbADD5(String acbxADD5)}.
	 * 
	 * Test ADD5 values null, "12345678", "1234567", "123456", "12345", "1234", "123", "12",
	 *  "1", "", and "123456789".
	 */
	@Test
	public final void testSetADD5String() throws AdabasException {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		
		String s = null;
		
		acbx.setAcbADD5(s);
		acbx.setAcbADD5("12345678");
		acbx.setAcbADD5("1234567");
		acbx.setAcbADD5("123456");
		acbx.setAcbADD5("12345");
		acbx.setAcbADD5("1234");
		acbx.setAcbADD5("123");
		acbx.setAcbADD5("12");
		acbx.setAcbADD5("1");
		acbx.setAcbADD5("");
		
		try {
			acbx.setAcbADD5("123456789");
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbxADD5 length GT 8")))
				fail("AdabasControlBlockX.setAcbADD5(\"123456789\") did not generate AdabasException acbxADD5 length GT 8");
		}
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlockX#setAcbADD5(byte [] acbxADD5)}.
	 * 
	 * Test ADD5 values 0xfffefdfc, 0x0102030405060708, 0x01020304050607, 0x010203040506, 0x0102030405, 
	 *  0x01020304, 0x010203, 0x0102, 0x01,null, 0x010203040506070809.
	 */
	@Test
	public final void testSetADD5ByteArray() throws AdabasException {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		
		byte [] b = null;
		
		acbx.setAcbADD5(new byte[] {(byte) 0xff, (byte) 0xfe, (byte) 0xfd, (byte) 0xfc});
		acbx.setAcbADD5(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06, (byte) 0x07, (byte) 0x08});
		acbx.setAcbADD5(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06, (byte) 0x07});
		acbx.setAcbADD5(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06});
		acbx.setAcbADD5(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05});
		acbx.setAcbADD5(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04 });
		acbx.setAcbADD5(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03 });
		acbx.setAcbADD5(new byte[] {(byte) 0x01, (byte) 0x02 });
		acbx.setAcbADD5(new byte[] {(byte) 0x01});
		acbx.setAcbADD5(b);
					
		try {
			acbx.setAcbADD5(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
					(byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09});
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbxADD5 length GT 8")))
				fail("AdabasControlBlockX.setAcbADD5(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03," +
						" (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09})" +
						" did not generate AdabasException acbxADD5 length GT 8");
		}
	}

	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlockX#getAcbADD5String()}.
	 *
	 * Test get of String ADD5 values.
	 * Test get of hex ADD5 values that are invalid as a string.
	 */
	@Test
	public final void testGetADD5String() throws AdabasException {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		
		acbx.setAcbADD5("ABCDEFGH");
		acbx.getAcbADD5String();

		acbx.setAcbADD5(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08 });
		acbx.getAcbADD5String();
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlockX#setAcbADD6(String acbxADD6)}.
	 * 
	 * Test ADD6 values null, "12345678", "1234567", "123456", "12345", "1234", "123", "12",
	 *  "1", "", and "123456789".
	 */
	@Test
	public final void testSetADD6String() throws AdabasException {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		
		String s = null;
		
		acbx.setAcbADD6(s);
		acbx.setAcbADD6("12345678");
		acbx.setAcbADD6("1234567");
		acbx.setAcbADD6("123456");
		acbx.setAcbADD6("12345");
		acbx.setAcbADD6("1234");
		acbx.setAcbADD6("123");
		acbx.setAcbADD6("12");
		acbx.setAcbADD6("1");
		acbx.setAcbADD6("");
		
		try {
			acbx.setAcbADD6("123456789");
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbxADD6 length GT 8")))
				fail("AdabasControlBlockX.setAcbADD6(\"123456789\") did not generate AdabasException acbxADD6 length GT 8");
		}
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlockX#setAcbADD6(byte [] acbxADD6)}.
	 * 
	 * Test ADD6 values 0xfffefdfc, 0x0102030405060708, 0x01020304050607, 0x010203040506, 0x0102030405, 
	 *  0x01020304, 0x010203, 0x0102, 0x01,null, 0x010203040506070809.
	 */
	@Test
	public final void testSetADD6ByteArray() throws AdabasException {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		
		byte [] b = null;
		
		acbx.setAcbADD6(new byte[] {(byte) 0xff, (byte) 0xfe, (byte) 0xfd, (byte) 0xfc});
		acbx.setAcbADD6(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06, (byte) 0x07, (byte) 0x08});
		acbx.setAcbADD6(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06, (byte) 0x07});
		acbx.setAcbADD6(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06});
		acbx.setAcbADD6(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05});
		acbx.setAcbADD6(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04 });
		acbx.setAcbADD6(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03 });
		acbx.setAcbADD6(new byte[] {(byte) 0x01, (byte) 0x02 });
		acbx.setAcbADD6(new byte[] {(byte) 0x01});
		acbx.setAcbADD6(b);
					
		try {
			acbx.setAcbADD6(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
					(byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09});
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbxADD6 length GT 8")))
				fail("AdabasControlBlockX.setAcbADD6(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03," +
						" (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09})" +
						" did not generate AdabasException acbxADD6 length GT 8");
		}
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlockX#getAcbADD6String()}.
	 *
	 * Test get of String ADD6 values.
	 * Test get of hex ADD6 values that are invalid as a string.
	 */
	@Test
	public final void testGetADD6String() throws AdabasException {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		
		acbx.setAcbADD6("ABCDEFGH");
		acbx.getAcbADD6String();

		acbx.setAcbADD6(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08 });
		acbx.getAcbADD6String();
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlockX#setAcbERRB()}.
	 * 
	 * Test ERRB values null, "ER", "E", "", "ERR".
	 */
	@Test
	public final void testSetERRB() throws AdabasException {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		
		acbx.setAcbERRB(null);
		acbx.setAcbERRB("ER");
		acbx.setAcbERRB("E");
		acbx.setAcbERRB("");

		try {
			acbx.setAcbERRB("ERR");
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbxERRB length GT 2")))
				fail("AdabasControlBlockX.setAcbERRB(\"ERR\") did not generate AdabasException acbxERRB GT 2");
		}
	}

	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlockX#setAcbSUBT(String)}.
	 * 
	 * Test SUBT values null, "1234", "123", "12", "1", "", and "12345".
	 */
	@Test
	public final void testSetSUBTString() throws AdabasException {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		
		String s = null;
		
		acbx.setAcbSUBT(s);
		acbx.setAcbSUBT("1234");
		acbx.setAcbSUBT("123");
		acbx.setAcbSUBT("12");
		acbx.setAcbSUBT("1");
		acbx.setAcbSUBT("");
		
		try {
			acbx.setAcbSUBT("12345");
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbxSUBT length GT 4")))
				fail("AdabasControlBlockX.setAcbSUBT(\"12345\") did not generate AdabasException acbxSUBT length GT 4");
		}
	}
	
	/**
	 * Test method for {@link com.softwareag.adabas.jas.AdabasControlBlockX#setAcbUSER()}.
	 * 
	 * Test USER values 0xfffefdfc, 0x01020304, 0x010203, 0x0102, 0x01, null, "0x0102030405".
	 */
	@Test
	public final void testSetAcbUser() throws AdabasException {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		
		byte [] b = null;
		
		acbx.setAcbUSER(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0A, (byte) 0x0B, 
				(byte) 0x0C, (byte) 0x0D, (byte) 0x0E, (byte) 0x0F, (byte) 0x10});
		acbx.setAcbUSER(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0A, (byte) 0x0B, 
				(byte) 0x0C, (byte) 0x0D, (byte) 0x0E, (byte) 0x0F});
		acbx.setAcbUSER(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0A, (byte) 0x0B, 
				(byte) 0x0C, (byte) 0x0D, (byte) 0x0E});
		acbx.setAcbUSER(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0A, (byte) 0x0B, 
				(byte) 0x0C, (byte) 0x0D});
		acbx.setAcbUSER(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0A, (byte) 0x0B, 
				(byte) 0x0C});
		acbx.setAcbUSER(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0A, (byte) 0x0B});
		acbx.setAcbUSER(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0A});
		acbx.setAcbUSER(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09});
		acbx.setAcbUSER(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06, (byte) 0x07, (byte) 0x08});
		acbx.setAcbUSER(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06, (byte) 0x07});
		acbx.setAcbUSER(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06});
		acbx.setAcbUSER(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05});
		acbx.setAcbUSER(new byte[] {(byte) 0xff, (byte) 0xfe, (byte) 0xfd, (byte) 0xfc});
		acbx.setAcbUSER(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04 });
		acbx.setAcbUSER(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03 });
		acbx.setAcbUSER(new byte[] {(byte) 0x01, (byte) 0x02 });
		acbx.setAcbUSER(new byte[] {(byte) 0x01});
		acbx.setAcbUSER(b);

		try {
			acbx.setAcbUSER(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
					(byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0A, (byte) 0x0B, 
					(byte) 0x0C, (byte) 0x0D, (byte) 0x0E, (byte) 0x0F, (byte) 0x10, (byte) 0x11});
		}
		catch (Exception e) {
			if (!(e instanceof AdabasException && e.getMessage().contains("acbxUSER length GT 16")))
				fail("AdabasControlBlockX.setAcbUSER(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03," +
						" (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09," +
						" (byte) 0x0A, (byte) 0x0B,	(byte) 0x0C, (byte) 0x0D, (byte) 0x0E, (byte) 0x0F," +
						" (byte) 0x10, (byte) 0x11}) did not generate AdabasException acbxUSER length GT 16");
		}
	}
	
	/**
	 * Test method for all ACB Get methods {@link com.softwareag.adabas.jas.AdabasControlBlockX}.
	 */
	@Test
	public final void testSetGetAcbFields() throws AdabasException {
		
		AdabasControlBlockX acbx = new AdabasControlBlockX();
		
		acbx.setAcbTYPE((byte) 0x00);
		System.out.println(String.format("acbxTYPE = 0x%x", acbx.getAcbTYPE()));
		
		byte[] ver = acbx.getAcbVER();
		System.out.println(String.format("acbxVER = 0x%02x%02x", ver[0], ver[1]));
		
		acbx.setAcbCMD("OP");
		System.out.println("acbxCMD = " + acbx.getAcbCMD());
		
		acbx.setAcbRSP((short) 148);
		System.out.println(String.format("acbxRSP = %d", acbx.getAcbRSP()));
		
		acbx.setAcbCID(new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff});
		System.out.println(String.format("acbxCID = 0x%s", byteArrayToHex(acbx.getAcbCID())));

		acbx.setAcbDBID(65535);
		System.out.println(String.format("acbxDBID = %d", acbx.getAcbDBID()));
	
		acbx.setAcbFNR(32000);
		System.out.println(String.format("acbxFNR = %d", acbx.getAcbFNR()));

		acbx.setAcbISN(4294967295l);
		System.out.println(String.format("acbxISN = %d", acbx.getAcbISN()));
		
		acbx.setAcbISL(4294967295l);
		System.out.println(String.format("acbxISL = %d", acbx.getAcbISL()));
		
		acbx.setAcbISQ(4294967295l);
		System.out.println(String.format("acbxISQ = %d", acbx.getAcbISQ()));

		acbx.setAcbCOP1((byte) 0x41);
		System.out.println(String.format("acbxCOP1 = 0x%x", acbx.getAcbCOP1()));

		acbx.setAcbCOP2((byte) 0x42);
		System.out.println(String.format("acbxCOP2 = 0x%x", acbx.getAcbCOP2()));

		acbx.setAcbCOP3((byte) 0x43);
		System.out.println(String.format("acbxCOP3 = 0x%x", acbx.getAcbCOP3()));

		acbx.setAcbCOP4((byte) 0x44);
		System.out.println(String.format("acbxCOP4 = 0x%x", acbx.getAcbCOP4()));

		acbx.setAcbCOP5((byte) 0x45);
		System.out.println(String.format("acbxCOP5 = 0x%x", acbx.getAcbCOP5()));

		acbx.setAcbCOP6((byte) 0x46);
		System.out.println(String.format("acbxCOP6 = 0x%x", acbx.getAcbCOP6()));

		acbx.setAcbCOP7((byte) 0x47);
		System.out.println(String.format("acbxCOP7 = 0x%x", acbx.getAcbCOP7()));

		acbx.setAcbCOP8((byte) 0x48);
		System.out.println(String.format("acbxCOP8 = 0x%x", acbx.getAcbCOP8()));

		acbx.setAcbADD1(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06, (byte) 0x07, (byte) 0x08});
		System.out.println(String.format("acbxADD1 = 0x%s", byteArrayToHex(acbx.getAcbADD1())));

		acbx.setAcbADD2(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04});
		System.out.println(String.format("acbxADD2 = 0x%s", byteArrayToHex(acbx.getAcbADD2())));

		acbx.setAcbADD3(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06, (byte) 0x07, (byte) 0x08});
		System.out.println(String.format("acbxADD3 = 0x%s", byteArrayToHex(acbx.getAcbADD3())));

		acbx.setAcbADD4(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06, (byte) 0x07, (byte) 0x08});
		System.out.println(String.format("acbxADD4 = 0x%s", byteArrayToHex(acbx.getAcbADD4())));

		acbx.setAcbADD5(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06, (byte) 0x07, (byte) 0x08});
		System.out.println(String.format("acbxADD5 = 0x%s", byteArrayToHex(acbx.getAcbADD5())));

		acbx.setAcbADD6(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06, (byte) 0x07, (byte) 0x08});
		System.out.println(String.format("acbxADD6 = 0x%s", byteArrayToHex(acbx.getAcbADD6())));

		acbx.setAcbERRA(4294967295l);
		System.out.println(String.format("acbxERRA = %d", acbx.getAcbERRA()));
		
		acbx.setAcbERRB("EE");
		System.out.println(String.format("acbxERRB = %s", acbx.getAcbERRB()));

		acbx.setAcbERRC((short)65535);
		System.out.println(String.format("acbxERRC = %d", acbx.getAcbERRC()));

		acbx.setAcbERRD((byte) 0xff);
		System.out.println(String.format("acbxERRD = 0x%x", acbx.getAcbERRD()));

		acbx.setAcbERRF((short)65534);
		System.out.println(String.format("acbxERRF = %d", acbx.getAcbERRF()));

		acbx.setAcbSUBR((short)65533);
		System.out.println(String.format("acbxSUBR = %d", acbx.getAcbSUBR()));

		acbx.setAcbSUBS((short)65532);
		System.out.println(String.format("acbxSUBS = %d", acbx.getAcbSUBS()));

		acbx.setAcbSUBT("SUBT");
		System.out.println(String.format("acbxSUBT = %s", acbx.getAcbSUBT()));

		acbx.setAcbLCMP(4294967295l);
		System.out.println(String.format("acbxLCMP = %d", acbx.getAcbLCMP()));

		acbx.setAcbLDEC(4294967295l);
		System.out.println(String.format("acbxLDEC = %d", acbx.getAcbLDEC()));

		acbx.setAcbCMDT(9223372036854775807l);
		System.out.println(String.format("acbxCMDT = %d", acbx.getAcbCMDT()));
		
		acbx.setAcbUSER(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
				(byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0A, (byte) 0x0B, 
				(byte) 0x0C, (byte) 0x0D, (byte) 0x0E, (byte) 0x0F, (byte) 0x10});
		System.out.println(String.format("acbxUSER = 0x%s", byteArrayToHex(acbx.getAcbUSER())));

		System.out.println(acbx.toString());
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
