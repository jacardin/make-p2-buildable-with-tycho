/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.p2.tests;

import junit.framework.*;

/**
 * This is the master test suite for all automated provisioning tests. It runs every test
 * that is suitable for running in an automated fashion as part of a build. Some
 * tests may be excluded if they require special setup (such as generating metadata).
 */
public class AutomatedTests extends TestCase {
	public static Test suite() {
		TestSuite suite = new TestSuite(AutomatedTests.class.getName());
		suite.addTest(org.eclipse.equinox.p2.tests.artifact.repository.AllTests.suite());
		suite.addTest(org.eclipse.equinox.p2.tests.core.AllTests.suite());
		suite.addTest(org.eclipse.equinox.p2.tests.director.AllTests.suite());
		suite.addTest(org.eclipse.equinox.p2.tests.download.AllTests.suite());
		suite.addTest(org.eclipse.equinox.p2.tests.engine.AllTests.suite());
		suite.addTest(org.eclipse.equinox.p2.tests.generator.AllTests.suite());
		suite.addTest(org.eclipse.equinox.p2.tests.metadata.AllTests.suite());
		suite.addTest(org.eclipse.equinox.p2.tests.metadata.repository.AllTests.suite());
		suite.addTest(org.eclipse.equinox.p2.tests.planner.AllTests.suite());
		suite.addTest(org.eclipse.equinox.p2.tests.directorywatcher.AllTests.suite());
		suite.addTest(org.eclipse.equinox.p2.tests.simpleconfigurator.SimpleConfiguratorTests.suite());
		suite.addTest(org.eclipse.equinox.p2.tests.updatesite.AllTests.suite());
		suite.addTest(org.eclipse.equinox.p2.tests.extensionlocation.AllTests.suite());
		suite.addTest(org.eclipse.equinox.p2.tests.touchpoint.eclipse.AllTests.suite());
		return suite;
	}

}
