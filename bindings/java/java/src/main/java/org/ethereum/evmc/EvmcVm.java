// EVMC: Ethereum Client-VM Connector API.
// Copyright 2019-2020 The EVMC Authors.
// Licensed under the Apache License, Version 2.0.
package org.ethereum.evmc;

import org.ethereum.evmc.EvmcLoaderException;

import java.nio.ByteBuffer;

/**
 * The Java interface to the evm instance.
 *
 * <p>Defines the Java methods capable of accessing the evm implementation.
 */
public final class EvmcVm implements AutoCloseable {
  private ByteBuffer nativeVm;

  /**
   * This method loads the specified evm shared library and loads/initializes the jni bindings.
   *
   * @param evmcPath the path to the evmc shared library
   * @param filename /path/filename of the evm shared object
   * @throws EvmcLoaderException
   */
  public static EvmcVm create(String evmcPath, String filename) {
    System.load(evmcPath);
    return new EvmcVm(filename);
  }

  private EvmcVm(String filename) throws EvmcLoaderException {
    nativeVm = load_and_create(filename);
  }

  /**
   * This method loads the specified EVM implementation and returns its pointer.
   *
   * @param Path to the dynamic object representing the EVM implementation.
   * @return Internal object pointer.
   * @throws EvmcLoaderException
   */
  private static native ByteBuffer load_and_create(String filename) throws EvmcLoaderException;

  /**
   * EVMC ABI version implemented by the VM instance.
   *
   * <p>Can be used to detect ABI incompatibilities. The EVMC ABI version represented by this file
   * is in ::EVMC_ABI_VERSION.
   */
  public static native int abi_version();

  /**
   * The name of the EVMC VM implementation.
   *
   * <p>It MUST be a NULL-terminated not empty string. The content MUST be UTF-8 encoded (this
   * implies ASCII encoding is also allowed).
   */
  private static native String name(ByteBuffer nativeVm);

  /** Function is a wrapper around native name(). */
  public String name() {
    return name(nativeVm);
  }

  /**
   * The version of the EVMC VM implementation, e.g. "1.2.3b4".
   *
   * <p>It MUST be a NULL-terminated not empty string. The content MUST be UTF-8 encoded (this
   * implies ASCII encoding is also allowed).
   */
  private static native String version(ByteBuffer nativeVm);

  /** Function is a wrapper around native version(). */
  public String version() {
    return version(nativeVm);
  }

  /**
   * Function to destroy the VM instance.
   *
   * <p>This is a mandatory method and MUST NOT be set to NULL.
   */
  private static native void destroy(ByteBuffer nativeVm);

  /**
   * Function to execute a code by the VM instance.
   *
   * <p>This is a mandatory method and MUST NOT be set to NULL.
   */
  private static native void execute(
      ByteBuffer nativeVm,
      HostContext context,
      int rev,
      ByteBuffer msg,
      ByteBuffer code,
      ByteBuffer result);

  /**
   * Function is a wrapper around native execute.
   *
   * <p>This allows the context to managed in one method
   */
  public synchronized ByteBuffer execute(
      HostContext context, int rev, ByteBuffer msg, ByteBuffer code) {
    int resultSize = get_result_size();
    ByteBuffer result = ByteBuffer.allocateDirect(resultSize);
    execute(nativeVm, context, rev, msg, code, result);
    return result;
  }

  /**
   * A method returning capabilities supported by the VM instance.
   *
   * <p>The value returned MAY change when different options are set via the set_option() method.
   *
   * <p>A Client SHOULD only rely on the value returned if it has queried it after it has called the
   * set_option().
   *
   * <p>This is a mandatory method and MUST NOT be set to NULL.
   */
  private static native int get_capabilities(ByteBuffer nativeVm);

  /** Function is a wrapper around native get_capabilities(). */
  public int get_capabilities() {
    return get_capabilities(nativeVm);
  }

  /**
   * Function that modifies VM's options.
   *
   * <p>If the VM does not support this feature the pointer can be NULL.
   */
  private static native int set_option(ByteBuffer nativeVm, String name, String value);

  /** Function is a wrapper around native set_option(). */
  public int set_option(String name, String value) {
    return set_option(nativeVm, name, value);
  }

  /** get size of result struct */
  private static native int get_result_size();

  /**
   * Utility method to get a bytebuffer address
   * @param buffer the byte buffer to consider
   * @return the byte buffer address
   */
  public native long address(ByteBuffer buffer);

  /** This method cleans up resources */
  @Override
  public void close() {
    destroy(nativeVm);
  }
}
