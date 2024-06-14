package com.softwareag.adabas.jas;

import java.io.UnsupportedEncodingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main trace / logging class.
 * 
 * Normal usage:
 * 
 * 1) set property JASLOGPROP/LOG4GPROP to point to log4j properties file.
 * 2) Logger logger = AdabasTrace.getlogger(loggerName);
 * 3) logger.debug(...logging/trace...) etc.
 *
 * @author usadva
 */

/* 
 * Copyright (c) 1998-2021 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, 
 * and/or its subsidiaries and/or its affiliates and/or their licensors. Use, reproduction, transfer, 
 * publication or disclosure is prohibited except as specifically provided for in your License Agreement 
 * with Software AG.
 */

/*
 * Thanks to Thorsten Knieling whose original JDC package provided many of the
 * ideas used to create JAS.
 */

public class AdabasTrace {
	
	/**
	 * Get log4j logger. Simple pass through method.
	 * 
	 * @param 	name	Logger name.
	 * @return 			log4j Logger.
	 */
	public static Logger getLogger(String name) {		
		return LogManager.getLogger(name);
	}
	
	/**
	 * Dump a byte array into a string in a format suitable for debug / trace.
	 * 
	 * @param 	header 	Header line string.
	 * @param 	buffer 	Byte array containing data to dump.
	 * 
	 * @return String containing dump suitable for debug / trace.
	 */
	
	/*
	 * Format of dump includes hex addresses, hex display of each byte, and
	 * ASCII and EBCDIC character display of each byte (e.g.):
	 *                                                                  ASCII                  EBCDIC
	 * ReptorTrace: b = 
	 * 0x00000000  E4 C1 C2 C8 00 00 00 80   00 00 00 40 F0 F1 00 01   {........ ...@....}    {UABH.... ... 01..}
	 * 0x00000010  D1 A1 E4 50 3D E6 BA 11   C1 D5 E2 C5 D9 E5 C5 D9   {...P=... ........}    {J~U&.W.. ANSERVER}
	 * 0x00000020  00 00 00 01 00 00 02 BD   00 00 00 00 00 00 00 00   {........ ........}    {........ ........}
	 * 0x00000030  00 00 00 00 00 00 00 00   00 00 00 00 00 00 00 00   {........ ........}    {........ ........}
	 * 0x00000040  E4 C1 C2 C9 00 00 00 40   00 00 00 40 00 00 00 00   {.......@ ...@....}    {UABI...  ... ....}
	 * 0x00000050  E2 E3 D9 D5 00 00 00 00   00 00 00 00 00 00 00 00   {........ ........}    {STRN.... ........}
	 * 0x00000060  D1 A1 E4 50 3D 68 50 12   D1 A1 E4 50 3D 68 50 12   {...P=hP. ...P=hP.}    {J~U&..&. J~U&..&.}
	 * 0x00000070  40 40 40 40 40 40 40 40   00 00 00 00 00 00 00 00   {@@@@@@@@ ........}    {         ........}
	 * 
	 */
	public static String dumpBuffer(String header, byte[] buffer) {
		
		StringBuffer 	result		= new StringBuffer(header);			// copy header string to result
		StringBuffer 	lastLine 	= new StringBuffer();				// working last line
		StringBuffer 	thisLine 	= new StringBuffer();				// working current line
		byte[]			baEBCDIC;
		String 			sEBCDIC 	= null;								// EBCDIC character display
		int				sameLines	= 0;								// number of identical lines
		
		int		lines 	= buffer.length / 16;							// 16 bytes per line
		int		missing	= 0;											// possibly missing bytes in last line
		if (buffer.length % 16 > 0) {									// calculate missing bytes
			lines++;													// need one extra line
			missing = 16 - (buffer.length % 16);						// and number missing bytes
		}
		
		for (int i=0; i < lines; i++) {									// loop through lines
			thisLine.append("\n");										// add line feed
			thisLine.append(String.format("0x%08X", i*16));				// add address prefix
			thisLine.append("  ");										// add spacing
			
			if ((i*16)+8 <= buffer.length) {							// if we have complete 1st 8 bytes
				thisLine.append(String.format("%02X %02X %02X %02X %02X %02X %02X %02X ", 	// use one hex format for performance
										  	  buffer[(i*16)+0],
										  	  buffer[(i*16)+1],
										  	  buffer[(i*16)+2],
										  	  buffer[(i*16)+3],
										  	  buffer[(i*16)+4],
										  	  buffer[(i*16)+5],
										  	  buffer[(i*16)+6],
										  	  buffer[(i*16)+7]));
			}
			else {																// else      partial 1st 8 bytes
				for (int j=0; j<8; j++) {										// loop thru partial 1st 8 bytes
					if ((i*16)+j >= buffer.length) break;						// check for end
					thisLine.append(String.format("%02X ", buffer[(i*16)+j]));	// add 1st 8 bytes in hex at this address
				}
			}
			
			thisLine.append("  ");										// add spacing between 1st and 2nd 8 bytes
			
			if ((i*16)+16 <= buffer.length){							// if we have complete 2nd 8 bytes
				thisLine.append(String.format("%02X %02X %02X %02X %02X %02X %02X %02X ", 	// use one hex format for performance
					  	  					  buffer[(i*16)+8],
					  	  					  buffer[(i*16)+9],
					  	  					  buffer[(i*16)+10],
					  	  					  buffer[(i*16)+11],
					  	  					  buffer[(i*16)+12],
					  	  					  buffer[(i*16)+13],
					  	  					  buffer[(i*16)+14],
					  	  					  buffer[(i*16)+15]));
			}
			else {																// else      partial 2nd 8 bytes
				for (int j=8; j<16; j++) {										// loop thru partial 2nd 8 bytes
					if ((i*16)+j >= buffer.length) break;						// check for end
					thisLine.append(String.format("%02X ", buffer[(i*16)+j]));	// add 2nd 8 bytes in hex at this address
				}
			}
			
			thisLine.append("  ");										// add spacing after 2nd 8 bytes
			
			if (missing > 0 && i == lines - 1) {						// oddball size / last line
				for (int j=0; j < missing; j++) {						// loop thru missing bytes
					thisLine.append("   ");								// substitute blanks
				}
			}
			
			// now do the same for character representation
			
			thisLine.append("{");										// enclose in braces
			
			if ((i*16)+8 <= buffer.length) {							// if we have complete 1st 8 bytes
				thisLine.append(String.format("%c%c%c%c%c%c%c%c", 		// use one char format for performance
											  tranAsciiByte(buffer[(i*16)+0]),
											  tranAsciiByte(buffer[(i*16)+1]),
											  tranAsciiByte(buffer[(i*16)+2]),
											  tranAsciiByte(buffer[(i*16)+3]),
											  tranAsciiByte(buffer[(i*16)+4]),
											  tranAsciiByte(buffer[(i*16)+5]),
											  tranAsciiByte(buffer[(i*16)+6]),
											  tranAsciiByte(buffer[(i*16)+7])));
			}
			else {														// else      partial 1st 8 bytes
				for (int j=0; j<8; j++) {								// loop thru partial 1st 8 bytes for char
					if ((i*16)+j >= buffer.length) {
						thisLine.append(" ");
					}
					else {
						thisLine.append(String.format("%c", tranAsciiByte(buffer[(i*16)+j])));	// add 1st 8 bytes as char
					}
				}
			}
			
			thisLine.append(" ");										// add spacing between 1st and 2nd 8 bytes
			
			if ((i*16)+16 <= buffer.length) {							// if we have complete 2nd 8 bytes
				thisLine.append(String.format("%c%c%c%c%c%c%c%c", 		// use one char format for performance
											  tranAsciiByte(buffer[(i*16)+8]),
											  tranAsciiByte(buffer[(i*16)+9]),
											  tranAsciiByte(buffer[(i*16)+10]),
											  tranAsciiByte(buffer[(i*16)+11]),
											  tranAsciiByte(buffer[(i*16)+12]),
											  tranAsciiByte(buffer[(i*16)+13]),
											  tranAsciiByte(buffer[(i*16)+14]),
											  tranAsciiByte(buffer[(i*16)+15])));
			}
			else {														// else         partial 2nd 8 bytes
				for (int j=8; j<16; j++) {								// loop through partial 2nd 8 bytes for char
					if ((i*16)+j >= buffer.length) {
						thisLine.append(" ");
					}
					else {
						thisLine.append(String.format("%c", tranAsciiByte(buffer[(i*16)+j])));	// add 2nd 8 bytes as char
					}
				}
			}
			
			thisLine.append("}");											// enclose in braces
			
			thisLine.append("    ");										// add spacing between ASCII and EBCDIC string
			
			// now do the same for EBCDIC character representation
			
			thisLine.append("{");											// enclose in braces
			
			if ((i*16)+8 <= buffer.length) {								// if we have complete 1st 8 bytes
				baEBCDIC = tranEbcdicBytes(	buffer, 	i*16, 8);
				try {sEBCDIC = new String(	baEBCDIC,	i*16, 8, "cp037");} catch (UnsupportedEncodingException e) {}
				thisLine.append(sEBCDIC);
			}
			else {															// else      partial 1st 8 bytes
				for (int j=0; j<8; j++) {									// loop thru partial 1st 8 bytes for char
					if ((i*16)+j >= buffer.length) break;					// check for end
					baEBCDIC = tranEbcdicBytes(	buffer, 	(i*16) + j, 1);
					try {sEBCDIC = new String(	baEBCDIC,	(i*16) + j, 1, "cp037");} catch (UnsupportedEncodingException e) {}
					thisLine.append(sEBCDIC);
				}
			}
			
			thisLine.append(" ");											// add spacing between 1st and 2nd 8 bytes
			
			if ((i*16)+16 <= buffer.length) {								// if we have complete 2nd 8 bytes
				baEBCDIC = tranEbcdicBytes(	buffer, 	(i*16) + 8, 8);
				try {sEBCDIC = new String(	baEBCDIC,	(i*16) + 8, 8, "cp037");} catch (UnsupportedEncodingException e) {}
				thisLine.append(sEBCDIC);
			}
			else {															// else         partial 2nd 8 bytes
				for (int j=8; j<16; j++) {									// loop through partial 2nd 8 bytes for char
					if ((i*16)+j >= buffer.length) break;					// check for end
					baEBCDIC = tranEbcdicBytes(	buffer, 	(i*16) + j, 1);
					try {sEBCDIC = new String(	baEBCDIC, 	(i*16) + j, 1, "cp037");} catch (UnsupportedEncodingException e) {}
					thisLine.append(sEBCDIC);
				}
			}
			
			thisLine.append("}");																		// enclose in braces
			
			if (lastLine.length() > 0 &&																// previous line and
				thisLine.toString().substring(12).compareTo(lastLine.toString().substring(12)) == 0) {	// this one duplicate without addr ?
				sameLines++;																			// just bump counter
				thisLine = new StringBuffer();															// and get new current line
			}
			else {																						// else new line different content
				if (sameLines > 0) {																	// any earlier repeated lines ?
					result.append("\n            ");													// append empty addr prefix
					result.append(sameLines + " lines same as above ..");								// append same lines message
					sameLines = 0;																		// reset counter
				}
				result.append(thisLine.toString());														// append new line to result
				lastLine = new StringBuffer(thisLine.toString());										// remember this line as last
				thisLine = new StringBuffer();															// and get new current line
			}
			
//			possible alternate same lines algorithm shows no substantial performance gain - usadva 2013.01.08
			
//			if (i == 0) {																// if 1st line
//				result.append(thisLine.toString());										// just append it
//				thisLine = new StringBuffer();											// and get new current line
//			}
//			else {																		// else subsequent line
//				boolean different = false;												// difference flag
//				for (int k=0; k<16; k++) {												// loop through bytes in line
//					if ((i*16)+k >= buffer.length) break;								// check for end of buffer
//					if (buffer[(i*16)+k] != buffer[((i-1)*16)+k]) {						// check these bytes against previous line
//						different = true;												// if any one different set flag
//						if (sameLines > 0) {											// any earlier repeated lines?
//							result.append("\n            ");							// append empty addr prefix
//							result.append(sameLines + " lines same as above ..");		// append same lines message
//							sameLines = 0;												// reset counter
//						}
//						result.append(thisLine.toString());								// append new line to result
//						thisLine = new StringBuffer();									// and get new current line
//						break;															// break out of loop
//					}
//				}
//				if (different == false) {												// different line ?
//					sameLines++;														// bump counter
//					thisLine = new StringBuffer();										// and get new current line
//				}
//			}
		}

		if (sameLines > 0) {
			result.append("\n            ");
			result.append(sameLines + " lines same as above ..");
			sameLines = 0;
		}
		
		return result.toString();											// final result
	}

	/**
	 * Translate non-displayable ASCII bytes to '.' for buffer dumps.
	 * Anything less than a space (0x20) or greater than a tilde ('~' 0x7e)
	 * is translated to a period ('.' 0x46).
	 * 
	 * @param b Byte to translate.
	 * @return Translated byte.
	 */
	private static byte tranAsciiByte(byte b) {
		
		if (b < 0x20 || b > 0x7e)									// if not a displayable US ASCII character
			return '.';												// translate to period
		else														// else
			return b;												// leave it alone
	}
	
	/**
	 * Translate byte array containing non-displayable EBCDIC bytes to '.' for buffer dumps.
	 * 
	 * @param ba		Input byte array to translate.
	 * @param offset	Offset into byte array.
	 * @param length	Length to translate.
	 * 
	 * @return			Translated byte array.
	 */
	private static byte[] tranEbcdicBytes(byte[] ba, int offset, int length) {
		
		byte[]	 buffer = new byte[offset + length];
		
		for (int i=offset; i < offset + length; i++) {				// loop through byte array
			short s = (short) (ba[i] & 0xFF);
			if ((s < 0x40)				||							// if not a valid EBCDIC character
				(s > 0x40 && s < 0x4A) 	||
				(s > 0x50 && s < 0x5A)	||
				(s > 0x61 && s < 0x6A)	||
				(s > 0x6F && s < 0x79)	||
				(s > 0x7F && s < 0x81)	||
				(s > 0x89 && s < 0x91)	||
				(s > 0x99 && s < 0xA1)	||
				(s > 0xA9 && s < 0xC0)	||
				(s > 0xC9 && s < 0xD0)	||
				(s > 0xC9 && s < 0xD0)	||
				(s > 0xD9 && s < 0xE0)	||
				(s > 0xD9 && s < 0xE0)	||
				(s > 0xE0 && s < 0xE2)	||
				(s > 0xE9 && s < 0xF0)	||
				(s > 0xF9)) {
				
				buffer[i] = 0x4B;							// translate to period
			}
			else {
				buffer[i] = ba[i];
			}
		}
		
		return buffer;
	}
}
