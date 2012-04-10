/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.agent.rt;

import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.jacoco.core.runtime.WildcardMatcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.logging.Logger;

import static java.lang.String.format;

/**
 * Class file transformer to instrument classes for code coverage analysis.
 */
public class CoverageTransformer implements ClassFileTransformer {

  private static final String AGENT_PREFIX;

  private static Logger log = Logger.getLogger("transformer");

  static {
    final String name = CoverageTransformer.class.getName();
    AGENT_PREFIX = toVMName(name.substring(0, name.lastIndexOf('.')));
  }

  private final IExceptionLogger logger;
  private final Instrumenter instrumenter;
  private final WildcardMatcher includes;
  private final WildcardMatcher excludes;
  private final WildcardMatcher exclClassloader;
  private final File classDumpDir;

  /**
   * New transformer with the given delegates.
   *
   * @param generator generator for runtime specific access code
   * @param options   configuration options for the generator
   * @param logger    logger for exceptions during instrumentation
   */
  public CoverageTransformer(final IExecutionDataAccessorGenerator generator,
                             final AgentOptions options, final IExceptionLogger logger) {
    this.instrumenter = new Instrumenter(generator);
    this.logger = logger;
    // Class names will be reported in VM notation:
    includes = new WildcardMatcher(toWildcard(toVMName(options.getIncludes())));
    excludes = new WildcardMatcher(toWildcard(toVMName(options.getExcludes())));
    exclClassloader = new WildcardMatcher(toWildcard(options.getExclClassloader()));
    classDumpDir = options.getClassDumpDirectory();
  }

  public byte[] transform(final ClassLoader loader, final String classname,
                          final Class<?> classBeingRedefined,
                          final ProtectionDomain protectionDomain,
                          final byte[] classfileBuffer) throws IllegalClassFormatException {

    if (!filter(loader, classname)) {
      return null;
    }

    log.info("Transforming class " + classname + " size " + classfileBuffer.length + " bytes filtered=" + filter(loader, classname));
    possiblyDump(classname, ".class", classfileBuffer, isGosuClass(classBeingRedefined));

    try {
      return instrumenter.instrument(classfileBuffer);
    } catch (final Throwable t) {
      final String msg = "Error while instrumenting class %s.";
      final IllegalClassFormatException ex = new IllegalClassFormatException(format(msg, classname));
      // Report this, as the exception is ignored by the JVM:
      logger.logExeption(ex);
      throw (IllegalClassFormatException) ex.initCause(t);
    }
  }

  // Check if the class is dynamically generated without a class file.
  private boolean isGosuClass(Class<?> classBeingRedefined) {
    if (classBeingRedefined == null) return false;
    for(java.lang.Class interfaceClass: classBeingRedefined.getInterfaces()) {
      if(interfaceClass.getName().contains("IGosu")) return true;
    }
    return false;
  }


  /**
   * Dump bytecode if configured with location to write to.
   * TODO: do not dump if class file already exists.
   */
  private void possiblyDump(String name, String suffix, byte[] bytecode, boolean gosuClass) {
    if (classDumpDir != null && gosuClass) {
      File file = new File(classDumpDir, name + suffix);
      file.getParentFile().mkdirs();
      OutputStream out = null;
      try {
        out = new FileOutputStream(file);
        out.write(bytecode);
      } catch (IOException e) {
        logger.logExeption(e);
      } finally {
        if (out != null) {
          try {
            out.close();
          } catch (IOException e) {
            logger.logExeption(e);
          }
        }
      }
    }
  }


  /**
   * Checks whether this class should be instrumented.
   *
   * @param loader    loader for the class
   * @param classname VM name of the class to check
   * @return <code>true</code> if the class should be instrumented
   */
  protected boolean filter(final ClassLoader loader, final String classname) {
    // Don't instrument classes of the bootstrap loader:
    return loader != null &&
            !classname.startsWith(AGENT_PREFIX) &&
            // TODO: ignore blocks
            !classname.contains("$block_") &&
            !exclClassloader.matches(loader.getClass().getName()) &&
            includes.matches(classname) &&
            !excludes.matches(classname);
  }

  private String toWildcard(final String src) {
    if (src.indexOf('|') != -1) {
      final IllegalArgumentException ex = new IllegalArgumentException(
              "Usage of '|' as a list separator for JaCoCo agent options is deprecated and will not work in future versions - use ':' instead.");
      logger.logExeption(ex);
      return src.replace('|', ':');
    }
    return src;
  }

  private static String toVMName(final String srcName) {
    return srcName.replace('.', '/');
  }
}
