package com.dgrid.test.plugins;

import junit.framework.TestCase;

public class SimpleJunit3TestCase extends TestCase {
	private boolean setUpCalled = false;

	private boolean tearDownCalled = false;

	public void setUp() {
		setUpCalled = true;
		tearDownCalled = false;
	}

	public void tearDown() {
		tearDownCalled = true;
	}

	public void testCase1() {
		assertTrue(setUpCalled);
		assertFalse(tearDownCalled);
	}

	public void testCase2() {
		assertTrue(setUpCalled);
		assertFalse(tearDownCalled);
	}
}
