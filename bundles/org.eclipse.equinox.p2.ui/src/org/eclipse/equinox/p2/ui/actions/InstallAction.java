/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.equinox.p2.ui.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.internal.p2.ui.ProvUIMessages;
import org.eclipse.equinox.internal.p2.ui.actions.ProfileModificationAction;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.director.ProfileChangeRequest;
import org.eclipse.equinox.p2.director.ProvisioningPlan;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.ui.*;
import org.eclipse.equinox.p2.ui.dialogs.InstallWizard;
import org.eclipse.equinox.p2.ui.operations.ProvisioningUtil;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

public class InstallAction extends ProfileModificationAction {

	public InstallAction(ISelectionProvider selectionProvider, String profileId, IProfileChooser chooser, LicenseManager licenseManager, Shell shell) {
		super(ProvUI.INSTALL_COMMAND_LABEL, selectionProvider, profileId, chooser, licenseManager, shell);
		setToolTipText(ProvUI.INSTALL_COMMAND_TOOLTIP);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.actions.SelectionProviderAction#selectionChanged(org.eclipse.jface.viewers.IStructuredSelection)
	 *
	 *  Overridden to enable only on selections with IU's.  Does not validate
	 *  whether the IU is already installed in a particular profile.
	 */
	public void selectionChanged(IStructuredSelection selection) {
		Object[] selectionArray = selection.toArray();
		if (selectionArray.length < 1) {
			setEnabled(false);
		} else {
			for (int i = 0; i < selectionArray.length; i++) {
				IInstallableUnit iu = getIU(selectionArray[i]);
				if (iu == null || ProvisioningUtil.isCategory(iu)) {
					setEnabled(false);
					return;
				}
			}
			setEnabled(true);
		}
	}

	protected String getTaskName() {
		return ProvUIMessages.InstallIUProgress;
	}

	protected void performOperation(IInstallableUnit[] ius, String targetProfileId) {
		InstallWizard wizard = new InstallWizard(targetProfileId, ius, getLicenseManager());
		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		dialog.open();
	}

	protected IStatus validateOperation(IInstallableUnit[] ius, String targetProfileId, IProgressMonitor monitor) {
		try {
			ProfileChangeRequest request = new ProfileChangeRequest(targetProfileId);
			request.addInstallableUnits(ius);
			ProvisioningPlan plan = ProvisioningUtil.getProvisioningPlan(request, monitor);
			return plan.getStatus();
		} catch (ProvisionException e) {
			return ProvUI.handleException(e, null);
		}
	}
}