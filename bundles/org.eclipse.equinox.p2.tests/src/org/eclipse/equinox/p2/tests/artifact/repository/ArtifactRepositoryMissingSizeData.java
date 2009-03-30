package org.eclipse.equinox.p2.tests.artifact.repository;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.internal.provisional.p2.artifact.repository.IArtifactRepository;
import org.eclipse.equinox.internal.provisional.p2.artifact.repository.IArtifactRepositoryManager;
import org.eclipse.equinox.internal.provisional.p2.core.ProvisionException;
import org.eclipse.equinox.internal.provisional.p2.core.VersionRange;
import org.eclipse.equinox.internal.provisional.p2.director.*;
import org.eclipse.equinox.internal.provisional.p2.engine.*;
import org.eclipse.equinox.internal.provisional.p2.engine.phases.Sizing;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.InstallableUnitQuery;
import org.eclipse.equinox.internal.provisional.p2.metadata.repository.IMetadataRepository;
import org.eclipse.equinox.internal.provisional.p2.metadata.repository.IMetadataRepositoryManager;
import org.eclipse.equinox.internal.provisional.p2.query.Collector;
import org.eclipse.equinox.p2.tests.AbstractProvisioningTest;
import org.eclipse.equinox.p2.tests.TestActivator;

public class ArtifactRepositoryMissingSizeData extends AbstractProvisioningTest {
	private static final String testDataLocation = "testData/artifactRepo/missingArtifact";
	IArtifactRepository source = null;
	IMetadataRepository metaRepo;
	IInstallableUnit missingArtifactIU, missingSizeIU;
	IEngine engine;

	protected void setUp() throws Exception {
		super.setUp();
		IMetadataRepositoryManager mmgr = getMetadataRepositoryManager();
		metaRepo = mmgr.loadRepository((getTestData("MissingArtifact repo", testDataLocation).toURI()), null);

		missingArtifactIU = (IInstallableUnit) metaRepo.query(new InstallableUnitQuery("javax.wsdl", new VersionRange("[1.5, 1.6)")), new Collector(), null).iterator().next();
		missingSizeIU = (IInstallableUnit) metaRepo.query(new InstallableUnitQuery("javax.wsdl", new VersionRange("[1.4, 1.5)")), new Collector(), null).iterator().next();

		IArtifactRepositoryManager mgr = getArtifactRepositoryManager();
		source = mgr.loadRepository((getTestData("MissingArtifact repo", testDataLocation).toURI()), null);

		engine = (IEngine) TestActivator.getContext().getService(TestActivator.getContext().getServiceReference(IEngine.SERVICE_NAME));
	}

	public void testMissingArtifact() {
		IProfile profile1 = createProfile("TestProfile." + getName());
		ProfileChangeRequest req = new ProfileChangeRequest(profile1);
		req.addInstallableUnits(new IInstallableUnit[] {missingArtifactIU});
		req.setInstallableUnitInclusionRules(missingArtifactIU, PlannerHelper.createStrictInclusionRule(missingArtifactIU));

		ProvisioningPlan plan = createPlanner().getProvisioningPlan(req, null, null);
		assertEquals(IStatus.OK, plan.getStatus().getSeverity());

		Sizing sizing = new Sizing(100, "");
		PhaseSet set = new SPhaseSet(sizing);

		IStatus status = engine.perform(profile1, set, plan.getOperands(), null, new NullProgressMonitor());
		if (!status.matches(IStatus.ERROR)) {
			fail("Incorrect status for missing artifact during Sizing.");
		}
	}

	public void testMissingSize() {
		IProfile profile1 = createProfile("TestProfile." + getName());
		ProfileChangeRequest req = new ProfileChangeRequest(profile1);
		req.addInstallableUnits(new IInstallableUnit[] {missingSizeIU});
		req.setInstallableUnitInclusionRules(missingSizeIU, PlannerHelper.createStrictInclusionRule(missingSizeIU));

		ProvisioningPlan plan = createPlanner().getProvisioningPlan(req, null, null);
		assertEquals(IStatus.OK, plan.getStatus().getSeverity());

		Sizing sizing = new Sizing(100, "");
		PhaseSet set = new SPhaseSet(sizing);

		IStatus status = engine.perform(profile1, set, plan.getOperands(), null, new NullProgressMonitor());
		if (!status.matches(IStatus.WARNING) && status.getCode() != ProvisionException.ARTIFACT_INCOMPLETE_SIZING) {
			fail("Incorrect status for missing file size during Sizing");
		}
	}

	private class SPhaseSet extends PhaseSet {
		public SPhaseSet(Phase set) {
			super(new Phase[] {set});
		}
	}
}