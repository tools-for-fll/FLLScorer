// Copyright (c) 2025 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

package FLLScorer;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Handles resource streams in the JAR file that can be overridden by files in
 * the file system.
 */
public class ResourceStream
{
  /**
   * Gets an InputStream for a given file.  If the file exists in the file
   * system, it is used.  Otherwise, the file from the JAR file is used.
   *
   * @param path The path of the file to read.
   *
   * @return The {@InputStream} for the given path, or <b>null<b> if the path
   *         does not exist.
   */
  @SuppressWarnings("resource")
  public static InputStream
  getResourceStream(String path)
  {
    InputStream in;

    // A try/catch to handle exceptions.
    try
    {
      // Open the given path as a file.
      in = new FileInputStream(path);
    }
    catch(Exception e)
    {
      // The path does not exist, so get the class loader so that resources can
      // be read.
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

      // Create an input stream for the given resource.
      in = classLoader.getResourceAsStream(path);
    }

    // Return the created input stream.
    return(in);
  }
}