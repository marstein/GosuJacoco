/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.util.FileUtils;
import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;

/**
 * Ant task for remotely controlling an application that is running with the
 * tcpserver output mode
 * 
 * @author Brock Janiczak
 * @version $Revision: $
 */
public class DumpTask extends Task {
	private boolean dump = true;
	private boolean reset;
	private File destfile;
	private String address = AgentOptions.DEFAULT_ADDRESS;
	private int port = AgentOptions.DEFAULT_PORT;
	private boolean append;

	/**
	 * Sets the location of the execution data file to write. This parameter is
	 * required when dump is <code>true</code>. Default is
	 * <code>jacoco.exec</code>
	 * 
	 * @param destfile
	 *            Location to write execution data to
	 */
	public void setDestFile(final File destfile) {
		this.destfile = destfile;
	}

	/**
	 * IP Address or hostname to connect to. Defaults to <code>localhost</code>
	 * 
	 * @param address
	 *            IP Address or hostname to connect to
	 */
	public void setAddress(final String address) {
		this.address = address;
	}

	/**
	 * Port number to connect to. Default is <code>6300</code>
	 * 
	 * @param port
	 *            Port to connect to
	 */
	public void setPort(final int port) {
		if (port <= 0) {
			throw new BuildException("Invalid port value");
		}

		this.port = port;
	}

	/**
	 * <code>true</code> if the destination file it to be appended to.
	 * <code>false</code> if the file is to be overwritten
	 * 
	 * @param append
	 *            <code>true</code> if the destination file should be appended
	 *            to
	 */
	public void setAppend(final boolean append) {
		this.append = append;
	}

	/**
	 * Sets whether execution data should be downloaded from the remote host.
	 * Defaults to <code>false</code>
	 * 
	 * @param dump
	 *            <code>true</code> to download execution data
	 */
	public void setDump(final boolean dump) {
		this.dump = dump;
	}

	/**
	 * Sets whether a reset command should be sent after the execution data has
	 * been copied. Defaults to <code>false</code>
	 * 
	 * @param reset
	 *            <code>true</code> to reset execution data
	 */
	public void setReset(final boolean reset) {
		this.reset = reset;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tools.ant.Task#execute()
	 */
	@Override
	public void execute() throws BuildException {

		if (dump && destfile == null) {
			throw new BuildException(
					"Destination file is required when dumping execution data");
		}

		FileOutputStream fileOutput = null;

		try {
			final Socket socket = new Socket(InetAddress.getByName(address),
					port);
			final RemoteControlWriter remoteWriter = new RemoteControlWriter(
					socket.getOutputStream());
			final RemoteControlReader remoteReader = new RemoteControlReader(
					socket.getInputStream());

			if (dump) {
				try {
					FileUtils.getFileUtils().createNewFile(destfile, true);
					fileOutput = new FileOutputStream(destfile, append);

					final ExecutionDataWriter executionDataWriter = new ExecutionDataWriter(
							fileOutput);
					remoteReader.setSessionInfoVisitor(executionDataWriter);
					remoteReader.setExecutionDataVisitor(executionDataWriter);
				} catch (final IOException e) {
					throw new BuildException(
							"Unable to create destination file", e);
				}
			}

			remoteWriter.visitDumpCommand(dump, reset);
			// Read status and/or execution data
			remoteReader.read();

			socket.close();
		} catch (final IOException e) {
			throw new BuildException(
					"Unable to communicate with JaCoCo server", e);
		} finally {
			FileUtils.close(fileOutput);
		}
	}
}
