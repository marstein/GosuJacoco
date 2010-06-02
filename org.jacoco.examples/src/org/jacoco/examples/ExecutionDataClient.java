/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.examples;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;

/**
 * This example connects to a coverage from agents that run in output mode
 * <code>tcpserver</code> and requests execution data. The collected data is
 * dumped to a local file.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class ExecutionDataClient {

	private static final String DESTFILE = "jacoco-client.exec";

	private static final String ADDRESS = "localhost";

	private static final int PORT = 6300;

	/**
	 * Starts the execution data request.
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException {
		final FileOutputStream localFile = new FileOutputStream(DESTFILE);
		final RemoteControlWriter localWriter = new RemoteControlWriter(
				localFile);

		// Open a socket to the coverage agent:
		final Socket socket = new Socket(InetAddress.getByName(ADDRESS), PORT);
		final RemoteControlWriter writer = new RemoteControlWriter(socket
				.getOutputStream());
		final RemoteControlReader reader = new RemoteControlReader(socket
				.getInputStream());
		reader.setSessionInfoVisitor(localWriter);
		reader.setExecutionDataVisitor(localWriter);

		// Send a dump command and read the response:
		writer.visitDumpCommand(true, false);
		reader.read();

		socket.close();
		localFile.close();
	}

}
