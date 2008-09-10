package com.dgrid.test.plugins;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SimpleJunit4TestCase {

	private boolean setUpCalled = false;

	@Before
	public void beforeSetUp() {
		setUpCalled = true;
	}

	@After
	public void afterTearDown() {
	}

	@Test
	public void myTestCase1() {
	}

	@Test
	public void myTestCase2() {
		// should fail
		Assert.assertFalse(setUpCalled);
	}
}
