package com.softwareag.adabas.jas.junit;

import org.junit.Test;

import com.softwareag.adabas.jas.AdabasBufferX;
import com.softwareag.adabas.jas.AdabasControlBlockX;
import com.softwareag.adabas.jas.AdabasDirectCallX;

public class TestS1 {

	@Test
	public void testS1() throws Exception {
		
		int	dbid	= 70;
		
		AdabasControlBlockX		acbx	= new AdabasControlBlockX();
		AdabasDirectCallX		adcx	= new AdabasDirectCallX(acbx);
		
		acbx.setAcbCMD("OP");
		acbx.setAcbDBID(dbid);
		
		AdabasBufferX			fbOP	= new AdabasBufferX(".", 		AdabasBufferX.FB);
		AdabasBufferX			rbOP	= new AdabasBufferX("UPD=.",	AdabasBufferX.RB);
		
		adcx.setFB(fbOP);
		adcx.setRB(rbOP);
		
		adcx.callAdabas();
		
		acbx.resetACBX();
		acbx.setAcbCMD("S1");
		acbx.setAcbDBID(dbid);
		acbx.setAcbCID("TTS1");
		acbx.setAcbFNR(1);
		
		AdabasBufferX	fbS1	= new AdabasBufferX("AA.", 		AdabasBufferX.FB);
		AdabasBufferX	rbS1	= new AdabasBufferX(1024,		AdabasBufferX.RB);
		AdabasBufferX	sbS1	= new AdabasBufferX("AA,GE.",	AdabasBufferX.SB);
		AdabasBufferX	vbS1	= new AdabasBufferX("00000001",	AdabasBufferX.VB);
		AdabasBufferX	ibS1	= new AdabasBufferX(16*1024,	AdabasBufferX.IB);
		
		adcx.setFB(fbS1);
		adcx.setRB(rbS1);
		adcx.setSB(sbS1);
		adcx.setVB(vbS1);
		adcx.setIB(ibS1);
		
		adcx.callAdabas();

		acbx.resetACBX();
		acbx.setAcbCMD("CL");
		acbx.setAcbDBID(dbid);
		
		adcx.setFB(null);
		adcx.setRB(null);
		adcx.setSB(null);
		adcx.setVB(null);
		adcx.setIB(null);
		
		adcx.callAdabas();
		
	}

}
