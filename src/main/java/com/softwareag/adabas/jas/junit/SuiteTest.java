package com.softwareag.adabas.jas.junit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Created : Aug 12, 2013
 * Platform: Java Environments
 * Author  : Jorge Cordovil Lima
 * 
 * Generic Motivation: One class that aggregates all the tests chosen to be executed in a lump.
 * 
 * Upon executing SuiteTest as a JUnit Application, it should run and only run the tests
 * listed inside @SuiteClasses(value={whateverClass.class, whateverClass2..., theLastOne.class})
 * and it should do it in a left to right order. Thus our tests would only invoke SuiteTest, and
 * the sequence there would try to take care of what is invoked next.
 * 
 * Update History:     
 * 
 * According to Dave Van Auken, the tests in AdabasBufferXTest, AdabasControlBlockTest, and 
 * AdabasControlXTest are tests that do not use any AAS.
 * 
 * Also most JUnits are all missing asserts of fails, so unless something threw an Exception they 
 * would always pass.
 * 
 * @author usajlim
 */

/* 
 * Copyright (c) 1998-2021 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, 
 * and/or its subsidiaries and/or its affiliates and/or their licensors. Use, reproduction, transfer, 
 * publication or disclosure is prohibited except as specifically provided for in your License Agreement 
 * with Software AG.
 */

@RunWith(value=org.junit.runners.Suite.class)
@SuiteClasses(value={AdabasBufferXTest.class, 
		AdabasControlBlockTest.class,
		AdabasControlBlockXTest.class
		})

public class SuiteTest {

}
