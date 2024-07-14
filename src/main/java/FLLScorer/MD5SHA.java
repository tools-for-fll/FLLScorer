// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

package FLLScorer;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import org.eclipse.jetty.util.security.Credential;
import org.eclipse.jetty.util.thread.AutoLock;

/**
 * A mechanism for storing credentials using a MD5 hash of the user name as the
 * salt for a SHA-256 hash of the password.
 */
public class MD5SHA extends Credential
{
  /**
   * The prefix that identifies this credential type.
   */
  private static final String TYPE = "MD5SHA:";

  /**
   * A lock for protecting access to the cryptographic functions.
   */
  private static final AutoLock m_cryptoLock = new AutoLock();

  /**
   * The MD5 cryptographic method.
   */
  private static MessageDigest m_md5 = null;

  /**
   * The SHA-256 cryptographic method.
   */
  private static MessageDigest m_sha = null;

  /**
   * The encrypted credentials for this user.
   */
  private final String m_cooked;

  /**
   * The constructor.
   *
   * @param cooked The encrypted credentials for this user.
   */
  public
  MD5SHA(String cooked)
  {
    // Save the credentials, stripping the type prefix if necessary.
    m_cooked = (cooked.startsWith(TYPE) ? cooked.substring(TYPE.length()) :
                cooked);
  }

  /**
   * Gets the prefix associated with this credential type.
   *
   * @return The string prefix.
   */
  public static String
  prefix()
  {
    // Return the prefix.
    return(TYPE);
  }

  // The over-ridden check method.
  @Override
  public boolean
  check(Object credentials)
  {
    String hash;

    // If the provided credentials are in a character array, convert it to a
    // String.
    if(credentials instanceof char[])
    {
      credentials = new String((char[])credentials);
    }

    // A try/catch to wrap the cryptographic methods with the lock, preventing
    // multiple threads from accesing them simultaneously.
    try(AutoLock l = m_cryptoLock.lock())
    {
      // See if the SHA-256 cryptographic method has been obtained yet.
      if(m_sha == null)
      {
        // Get an instance of the SHA-256 cryptographic method.
        try
        {
          m_sha = MessageDigest.getInstance("SHA256");
        }
        catch(Exception e)
        {
          System.err.println("Digest error: " + e);
          return(false);
        }
      }

      // Compute the SHA-256 of the password, salted with the MD5 of the user
      // name.
      m_sha.reset();
      m_sha.update(HexFormat.of().parseHex(m_cooked.substring(0, 32)));
      m_sha.update(credentials.toString().getBytes(StandardCharsets.UTF_8));

      // Generate the full hash of the credentials from the MD5 salt and the
      // SHA-256 of the credentials.
      hash = m_cooked.substring(0, 32) +
             HexFormat.of().formatHex(m_sha.digest());
    }
    catch(Exception e)
    {
      System.out.println("Digest error:" + e);
      return(false);
    }

    // Compare the stored credentials to the provided credentials. The
    // stringEquals method is used to prevent timing attacks (as it is contant
    // time on the length of the provided credentials).
    return(stringEquals(m_cooked, hash));
  }

  // The over-ridden equals method.
  @Override
  public boolean
  equals(Object obj)
  {
    // The objects are not equal if the provided object is not an instance of
    // this class.
    if(!(obj instanceof MD5SHA))
    {
      return(false);
    }

    // It is now safe to typecast the provided object to an instance of this
    // class.
    MD5SHA c = (MD5SHA)obj;

    // The objects are equal if the stored credentials are the same.  The
    // stringEquals method is used to prevent timing attacks (as it is constant
    // time on the length of the credentials in the provided object).
    return(stringEquals(m_cooked, c.m_cooked));
  }

  /**
   * Generates the credential string for the given user name and password.
   *
   * @param user The name of the user.
   *
   * @param password The password for the user.
   *
   * @return A string containing the MD5SHA encrypted credentials for this
   *         user.
   */
  public static String
  hash(String user, String password)
  {
    byte[] md5sum;
    String hash;

    // A try/catch to wrap the cryptographic methods with the lock, preventing
    // multiple threads from accesing them simultaneously.
    try(AutoLock l = m_cryptoLock.lock())
    {
      // See if the MD5 cryptographic method has been obtained yet.
      if(m_md5 == null)
      {
        // Get an instance of the MD5 cryptographic method.
        try
        {
          m_md5 = MessageDigest.getInstance("MD5");
        }
        catch(Exception e)
        {
          System.err.println("Digest error: " + e);
          return(null);
        }
      }

      // Compute the MD5 of the user name.
      m_md5.reset();
      m_md5.update(user.getBytes(StandardCharsets.ISO_8859_1));
      md5sum = m_md5.digest();

      // See if the SHA-256 cryptographic method has been obtained yet.
      if(m_sha == null)
      {
        // Get an instance of the SHA-256 cryptographic method.
        try
        {
          m_sha = MessageDigest.getInstance("SHA256");
        }
        catch(Exception e)
        {
          System.err.println("Digest error: " + e);
          return(null);
        }
      }

      // Compute the SHA-256 of the password, salted with the MD5 of the user
      // name.
      m_sha.reset();
      m_sha.update(md5sum);
      m_sha.update(password.getBytes(StandardCharsets.UTF_8));

      // Generate the full hash of the credentials from the MD5 salt and the
      // SHA-256 of the credentials.
      hash = HexFormat.of().formatHex(md5sum) +
             HexFormat.of().formatHex(m_sha.digest());
    }
    catch(Exception e)
    {
      System.out.println("Digest error:" + e);
      return(null);
    }

    // Return the full credential string.
    return(TYPE + hash);
  }
}