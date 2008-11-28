/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.internal.p2.artifact.repository;

import java.io.*;
import java.net.URI;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import org.eclipse.core.runtime.*;
import org.eclipse.equinox.internal.p2.core.helpers.Tracing;
import org.eclipse.equinox.internal.p2.persistence.CompositeRepositoryIO;
import org.eclipse.equinox.internal.p2.persistence.CompositeRepositoryIO.CompositeRepositoryState;
import org.eclipse.equinox.internal.provisional.p2.artifact.repository.IArtifactRepository;
import org.eclipse.equinox.internal.provisional.p2.core.ProvisionException;
import org.eclipse.equinox.internal.provisional.spi.p2.artifact.repository.ArtifactRepositoryFactory;
import org.eclipse.osgi.util.NLS;

public class CompositeArtifactRepositoryFactory extends ArtifactRepositoryFactory {

	public IArtifactRepository load(URI location, IProgressMonitor monitor) throws ProvisionException {
		final String PROTOCOL_FILE = "file"; //$NON-NLS-1$
		long time = 0;
		final String debugMsg = "Restoring artifact repository "; //$NON-NLS-1$
		if (Tracing.DEBUG_METADATA_PARSING) {
			Tracing.debug(debugMsg + location);
			time = -System.currentTimeMillis();
		}
		File localFile = null;
		boolean local = false;
		try {
			SubMonitor sub = SubMonitor.convert(monitor, 300);
			OutputStream artifacts = null;
			// try with compressed
			boolean compress = true;
			if (PROTOCOL_FILE.equals(location.getScheme())) {
				local = true;
				localFile = new File(CompositeArtifactRepository.getActualLocation(location, true).getPath());
				if (!localFile.exists()) {
					localFile = new File(CompositeArtifactRepository.getActualLocation(location, false).getPath());
					compress = false;
				}
			} else {
				//download to local temp file
				localFile = File.createTempFile(CompositeArtifactRepository.CONTENT_FILENAME, CompositeArtifactRepository.XML_EXTENSION); //$NON-NLS-1$ //$NON-NLS-2$
				try {
					artifacts = new BufferedOutputStream(new FileOutputStream(localFile));
					IStatus status = getTransport().download(CompositeArtifactRepository.getActualLocation(location, compress).toString(), artifacts, sub.newChild(100));
					if (!status.isOK()) {
						// retry uncompressed
						compress = false;
						status = getTransport().download(CompositeArtifactRepository.getActualLocation(location, compress).toString(), artifacts, sub.newChild(100));
						if (!status.isOK())
							throw new ProvisionException(new Status(IStatus.ERROR, Activator.ID, ProvisionException.REPOSITORY_NOT_FOUND, status.getMessage(), null));
					}
				} finally {
					if (artifacts != null)
						artifacts.close();
				}
			}
			InputStream descriptorStream = null;
			try {
				descriptorStream = new BufferedInputStream(new FileInputStream(localFile));
				if (compress) {
					URI actualLocation = CompositeArtifactRepository.getActualLocation(location, false);
					JarInputStream jInStream = new JarInputStream(descriptorStream);
					JarEntry jarEntry = jInStream.getNextJarEntry();
					String filename = URIUtil.lastSegment(actualLocation);
					while (jarEntry != null && filename != null && !(filename.equals(jarEntry.getName()))) {
						jarEntry = jInStream.getNextJarEntry();
					}
					if (jarEntry == null) {
						throw new FileNotFoundException("Repository not found in " + actualLocation.getPath()); //$NON-NLS-1$
					}
					descriptorStream = jInStream;
				}
				CompositeRepositoryIO io = new CompositeRepositoryIO();
				CompositeRepositoryState resultState = io.read(localFile.toURL(), descriptorStream, sub.newChild(100));

				CompositeArtifactRepository result = new CompositeArtifactRepository(resultState);

				result.initializeAfterLoad(location);
				if (Tracing.DEBUG_METADATA_PARSING) {
					time += System.currentTimeMillis();
					Tracing.debug(debugMsg + "time (ms): " + time); //$NON-NLS-1$ 
				}
				return result;
			} finally {
				if (descriptorStream != null)
					descriptorStream.close();
			}
		} catch (FileNotFoundException e) {
			String msg = NLS.bind(Messages.io_failedRead, location);
			throw new ProvisionException(new Status(IStatus.ERROR, Activator.ID, ProvisionException.REPOSITORY_NOT_FOUND, msg, e));
		} catch (IOException e) {
			String msg = NLS.bind(Messages.io_failedRead, location);
			throw new ProvisionException(new Status(IStatus.ERROR, Activator.ID, ProvisionException.REPOSITORY_FAILED_READ, msg, e));
		} finally {
			if (!local && localFile != null && !localFile.delete())
				localFile.deleteOnExit();
		}
	}

	public IArtifactRepository create(URI location, String name, String type, Map properties) {
		return new CompositeArtifactRepository(name, location, properties);
	}

	private Transport getTransport() {
		return ECFTransport.getInstance();
	}
}