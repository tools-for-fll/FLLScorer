// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

package FLLScorer;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import io.nayuki.qrcodegen.QrCode;

/**
 * Handles the link to dedicated web pages.
 */
public class Links
{
  /**
   * The object for the Links singleton.
   */
  private static Links m_instance = null;

  /**
   * The Webserver object.
   */
  private WebServer m_webserver = null;

  /**
   * Gets the Links singleton object, creating it if necessary.
   *
   * @return Returns the Links singleton.
   */
  public static Links
  getInstance()
  {
    // Create the Links object if required.
    if(m_instance == null)
    {
      m_instance = new Links();
    }

    // Return the Links object.
    return(m_instance);
  }

  /**
   * Serves a PNG file containing a QR code for the given string.
   *
   * @param string The string to be encoded into a QR code.
   *
   * @return Returns the byte array containing the PNG of the QR code.
   */
  private byte[]
  serveQrCode(String string)
  {
    QrCode qr = QrCode.encodeText(string, QrCode.Ecc.HIGH);
    int scale = 8;
    int border = 10;
    int lightColor = 0x000000;
    int darkColor = 0xffffff;

    // Create a buffer images.
    BufferedImage result = new BufferedImage((qr.size + border * 2) * scale,
                                             (qr.size + border * 2) * scale,
                                             BufferedImage.TYPE_INT_RGB);

    // Loop through the rows of the image.
    for(int y = 0; y < result.getHeight(); y++)
    {
      // Loop through the columns of the image.
      for(int x = 0; x < result.getWidth(); x++)
      {
        // Determine the color for this pixel.
        boolean color = qr.getModule(x / scale - border, y / scale - border);

        // Set this pixel of the image to the appropriate color.
        result.setRGB(x, y, color ? darkColor : lightColor);
      }
    }
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try
    {
      ImageIO.write(result, "png", baos);
    }
    catch(Exception e)
    {
      return(null);
    }
    return(baos.toByteArray());
  }

  /**
   * Serve a PNG with a QR code for the web page for judges.
   *
   * @param path The path from the request.
   *
   * @param parameters The parameters from the request.
   *
   * @return An array of bytes to return to the client.
   */
  private byte[]
  serveJudge(String path, String parameters)
  {
    return(serveQrCode("http://localhost:8080/judge"));
  }

  /**
   * Serve a PNG with a QR code for the web page for the referees.
   *
   * @param path The path from the request.
   *
   * @param parameters The parameters from the request.
   *
   * @return An array of bytes to return to the client.
   */
  private byte[]
  serveReferee(String path, String parameters)
  {
    return(serveQrCode("http://localhost:8080/referee"));
  }

  /**
   * Serve a PNG with a QR code for the web page of the scoreboard.
   *
   * @param path The path from the request.
   *
   * @param parameters The parameters from the request.
   *
   * @return An array of bytes to return to the client.
   */
  private byte[]
  serveScoreboard(String path, String parameters)
  {
    return(serveQrCode("http://localhost:8080/scoreboard"));
  }

  /**
   * Serve a PNG with a QR code for the web page for the time keeper.
   *
   * @param path The path from the request.
   *
   * @param parameters The parameters from the request.
   *
   * @return An array of bytes to return to the client.
   */
  private byte[]
  serveTimekeeper(String path, String parameters)
  {
    return(serveQrCode("http://localhost:8080/timekeeper"));
  }

  /**
   * Serve a PNG with a QR code for the web page of the timer.
   *
   * @param path The path from the request.
   *
   * @param parameters The parameters from the request.
   *
   * @return An array of bytes to return to the client.
   */
  private byte[]
  serveTimer(String path, String parameters)
  {
    return(serveQrCode("http://localhost:8080/timer"));
  }

  /**
   * The constructor.  This is private so that the object can only be created
   * via the getIntance() method.
   */
  private
  Links()
  {
  }

  /**
   * Performs initial setup for the links handler.
   */
  public void
  setup()
  {
    // Get a reference to the web server.
    m_webserver = WebServer.getInstance();

    // Register the dynamic handlers for the various QR code PNGs.
    m_webserver.registerDynamicFile("/links/judge.png", this::serveJudge);
    m_webserver.registerDynamicFile("/links/referee.png", this::serveReferee);
    m_webserver.registerDynamicFile("/links/scoreboard.png",
                                    this::serveScoreboard);
    m_webserver.registerDynamicFile("/links/timekeeper.png",
                                    this::serveTimekeeper);
    m_webserver.registerDynamicFile("/links/timer.png", this::serveTimer);
  }
}