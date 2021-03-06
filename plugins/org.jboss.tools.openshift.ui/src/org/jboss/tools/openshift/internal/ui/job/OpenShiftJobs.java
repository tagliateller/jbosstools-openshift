/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.openshift.internal.ui.job;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.jboss.tools.openshift.common.core.connection.ConnectionsRegistrySingleton;
import org.jboss.tools.openshift.core.connection.Connection;
import org.jboss.tools.openshift.core.connection.ConnectionProperties;
import org.jboss.tools.openshift.core.connection.ConnectionsRegistryUtil;

import com.openshift.restclient.ResourceKind;
import com.openshift.restclient.model.IProject;

/**
 * Utility class to handle OpenShift {@link Job}s
 * 
 * @author Fred Bricon
 */
public class OpenShiftJobs {

	private OpenShiftJobs() {}
	
	/**
	 * Creates a {@link DeleteResourceJob} to delete an OpenShift {@link IProject}.
	 */
	public static DeleteResourceJob createDeleteProjectJob(final IProject project) {
		Assert.isNotNull(project, "A project must not be null");
		DeleteResourceJob deleteProjectJob = new DeleteResourceJob(project) {

			@Override
			protected IStatus doRun(IProgressMonitor monitor) {
				Connection connection = ConnectionsRegistryUtil.getConnectionFor(project);
				List<IProject> oldProjects = connection.getResources(ResourceKind.PROJECT);
				IStatus status = super.doRun(monitor);
				if(status.isOK()) {
					List<IProject> newProjects = new ArrayList<>(oldProjects);
					newProjects.remove(project);
					ConnectionsRegistrySingleton.getInstance().fireConnectionChanged(
							connection , 
							ConnectionProperties.PROPERTY_PROJECTS, 
							oldProjects, 
							newProjects);
				}
				return status;
			}
		};
		
		return deleteProjectJob;
	}
}
