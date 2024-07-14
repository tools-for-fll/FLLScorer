// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

package FLLScorer;

import org.eclipse.jetty.util.security.Credential;
import org.eclipse.jetty.util.security.CredentialProvider;

/**
 * A CredentialProvider for the MD5SHA credential class.
 */
public class MD5SHAProvider implements CredentialProvider
{
  // The over-ridden getCredential method.
  @Override
  public Credential
  getCredential(String arg0)
  {
    // Create and return a new MD5SHA Credential.
    return(new MD5SHA(arg0));
  }

  // The over-ridden getPrefix method.
  @Override
  public String
  getPrefix()
  {
    // Get and return the MD5SHA Credential string prefix.
    return(MD5SHA.prefix());
  }
}