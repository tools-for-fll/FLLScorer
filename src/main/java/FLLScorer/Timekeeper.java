// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

package FLLScorer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.ee10.websocket.server.JettyServerUpgradeRequest;
import org.eclipse.jetty.ee10.websocket.server.JettyServerUpgradeResponse;
import org.eclipse.jetty.ee10.websocket.server.JettyWebSocketCreator;
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketOpen;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

public class Timekeeper
{
  /**
   * The object for the Timekeeper singleton.
   */
  private static Timekeeper m_instance = null;

  /**
   * Gets the Timekeeper singleton object, creating it if necessary.
   *
   * @return Returns the Timekeeper singleton.
   */
  public static Timekeeper
  getInstance()
  {
    // Create the Timekeeper object if required.
    if(m_instance == null)
    {
      m_instance = new Timekeeper();
    }

    // Return the Timekeeper object.
    return(m_instance);
  }

  /**
   * The constructor.  This is private so that the object can only be created
   * via the getIntance() method.
   */
  private
  Timekeeper()
  {
  }

  @WebSocket
  public static class TimeSocket implements Runnable
  {
    private TimeZone m_timezone;
    private Session m_session;
    private int m_delay = 1000;

    @OnWebSocketOpen
    public void
    onOpen(Session session)
    {
      m_session = session;
      m_timezone = TimeZone.getTimeZone("UTC");
      new Thread(this).start();
    }

    @OnWebSocketMessage
    public void
    onMessage(Session session, String text)
    {
      int delay = Integer.parseInt(text);
      if(delay >= 100)
      {
        m_delay = delay;
      }

      System.out.println(text);
    }

    @OnWebSocketClose
    public void
    onClose(int closeCode, String closeReasonPhrase)
    {
      m_session = null;
    }

    @Override
    public void
    run()
    {
      while (m_session != null)
      {
        try
        {
          SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
          dateFormat.setTimeZone(m_timezone);

          String timestamp = dateFormat.format(new Date());
          m_session.sendText(timestamp, Callback.NOOP);
          //TimeUnit.SECONDS.sleep(1);
          TimeUnit.MILLISECONDS.sleep(m_delay);
        }
        catch(InterruptedException e)
        {
          System.out.println("Send of TEXT message interrupted: " + e);
        }
      }
    }
  }

  public static class TimeSocketCreator implements JettyWebSocketCreator
  {
    @Override
    public Object createWebSocket(JettyServerUpgradeRequest jettyServerUpgradeRequest, JettyServerUpgradeResponse jettyServerUpgradeResponse)
    {
      return new TimeSocket();
    }
  }

  public void
  setup()
  {
    WebServer.getInstance().addWebSocket("/timekeeper/timekeeper.ws", new TimeSocketCreator(), 5000);
  }
}
