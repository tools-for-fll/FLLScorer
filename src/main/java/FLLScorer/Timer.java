// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

package FLLScorer;

import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.ee10.websocket.server.JettyServerUpgradeRequest;
import org.eclipse.jetty.ee10.websocket.server.JettyServerUpgradeResponse;
import org.eclipse.jetty.ee10.websocket.server.JettyWebSocketCreator;
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketOpen;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

/**
 * Handles the timing controls for the timer.
  * <p>
 * This is a singleton that is acquired via the getInstance() method.
*/
public class Timer
{
  /**
   * The object for the Timer singleton.
   */
  private static Timer m_instance = null;

  /**
   * Gets the Timer singleton object, creating it if necessary.
   *
   * @return Returns the Timer singleton.
   */
  public static Timer
  getInstance()
  {
    // Create the Timer object if required.
    if(m_instance == null)
    {
      m_instance = new Timer();
    }

    // Return the Timer object.
    return(m_instance);
  }

  /**
   * The constructor.  This is private so that the object can only be created
   * via the getIntance() method.
   */
  private
  Timer()
  {
  }

  /**
   * The WebSocket for the timer.
   */
  @WebSocket
  public static class TimerSocket implements Runnable
  {
    private TimeKeeper m_instance = TimeKeeper.getInstance();

    /**
     * The session for this WebSocket.
     */
    private Session m_session;

    /**
     * The cached state of the match timer; used to detect when the timer has
     * changed state.
     */
    private TimeKeeper.TimerState m_state = m_instance.state();

    /**
     * The time, in milliseconds, at which the update was sent to the
     * WebSocket.
     */
    private long m_lastSend = 0;

    /**
     * Called when the WebSocket is first opened.
     *
     * @param session The session for this WebSocket.
     */
    @OnWebSocketOpen
    public void
    onOpen(Session session)
    {
      // Save the session.
      m_session = session;

      // Start the thread that handles sending updates via the WebSocket.
      new Thread(this).start();
    }

    /**
     * Called when the WebSocket is closed.
     *
     * @param closeCode The code for why the WebSocket was closed.
     *
     * @param closeReasonPhrase The reason the WebSocket was closed.
     */
    @OnWebSocketClose
    public void
    onClose(int closeCode, String closeReasonPhrase)
    {
      // Clear the stored session, which causes the background thread to exit.
      m_session = null;
    }

    /**
     * The code that runs in the timer thread.
     */
    @Override
    public void
    run()
    {
      long now, delay;

      // See if the timer is currently running.
      if(m_state == TimeKeeper.TimerState.RUN)
      {
        // Send a run mode message via the WebSocket.
        m_session.sendText("m:run", Callback.NOOP);
      }

      // See if the timer is currently stopped.
      else if(m_state == TimeKeeper.TimerState.STOP)
      {
        // Send a stop mode message via the WebSocket.
        m_session.sendText("m:stop", Callback.NOOP);
      }

      // Otherwise the the timer is currently reset.
      else
      {
        // Send a reset mode message via the WebSocket.
        m_session.sendText("m:reset", Callback.NOOP);
      }

      // Send the current match time via the WebSocket.
      m_session.sendText("t:" + m_instance.matchTime(), Callback.NOOP);

      // Loop while the session is still active.
      while (m_session != null)
      {
        // Get the current time.
        now = java.lang.System.currentTimeMillis();

        // See if the state of the timer has changed.
        if(m_state != m_instance.state())
        {
          // Update the cached timer state.
          m_state = m_instance.state();

          // See if the timer is now running.
          if(m_state == TimeKeeper.TimerState.RUN)
          {
            // Send a mode run message via the WebSocket.
            m_session.sendText("m:run", Callback.NOOP);

            // Set the last time a message was sent to zero, so that a new
            // determination of of the timer state is made immediately.
            m_lastSend = 0;
          }

          // See if the timer is now stopped.
          else if(m_state == TimeKeeper.TimerState.STOP)
          {
            // Send a mode stop message via the WebSocket.
            m_session.sendText("m:stop", Callback.NOOP);
          }

          // Otherwise, the timer is now reset.
          else
          {
            // Send a mode reset message via the WebSocket.
            m_session.sendText("m:reset", Callback.NOOP);
          }

          // Send the current match time via the WebSocket.
          m_session.sendText("t:" + m_instance.matchTime(), Callback.NOOP);
        }

        // See if the timer is not running.
        if((m_state == TimeKeeper.TimerState.STOP) ||
           (m_state == TimeKeeper.TimerState.RESET))
        {
          // See if it has been more than a second since the last message was
          // sent via the WebSocket.
          if((now - m_lastSend) >= 1000)
          {
            // Send a NOP via the WebSocket (to keep it from timing out and
            // closing).
            m_session.sendText("nop", Callback.NOOP);

            // Reset the last send time to now.
            m_lastSend = now;
          }

          // The timer is not running, so delay for 10 ms (so that it will be
          // responsive to requests to start the timer).
          try
          {
            TimeUnit.MILLISECONDS.sleep(10);
          }
          catch(InterruptedException e)
          {
          }
        }

        // Otherwise, the timer is running.
        else
        {
          // See if it has been at least a second since the last time a message
          // was sent.
          if((now - m_lastSend) >= 1000)
          {
            // Get the time elapsed in the match.
            long elapsed = m_instance.timeElapsed();

            // Send the current match time via the WebSocket.
            m_session.sendText("t:" + m_instance.matchTime(), Callback.NOOP);

            // See if the timer is now stopped.
            if(m_instance.state() == TimeKeeper.TimerState.STOP)
            {
              // Set the last send time to a second ago, so that the delay
              // before the next check will be short.
              m_lastSend = now - 1000;
            }
            else
            {
              // Set the last send time to the time when the current match
              // time second transition occurred (which might be a bit in the
              // past).  This ensures that the next send check occurs
              // reasonably close to the timer second boundary, even if this
              // one ended up being a bit delayed.
              m_lastSend = m_instance.startTime() + ((elapsed / 1000) * 1000);
            }
          }

          // See if there has been less than 990 ms since the last time the
          // timer was checked.  Note that the delay might be interrupted, so
          // this slightly more complicated approach to handling the delay is
          // crucial to accurate timing.
          if((now - m_lastSend) < 990)
          {
            // Delay until the 990 ms boundary.
            delay = 990 - (now - m_lastSend);
          }
          else
          {
            // Delay 1 ms at a time until the next check time.
            delay = 1;
          }

          // Delay for the computed amount of time.
          try
          {
            TimeUnit.MILLISECONDS.sleep(delay);
          }
          catch(InterruptedException e)
          {
          }
        }
      }
    }
  }

  /**
   * A creator for timer WebSockets.
   */
  private static class TimerSocketCreator implements JettyWebSocketCreator
  {
    // Creates a WebSocket for the incoming request.
    @Override
    public Object
    createWebSocket(JettyServerUpgradeRequest jettyServerUpgradeRequest,
                    JettyServerUpgradeResponse jettyServerUpgradeResponse)
    {
      // Create a new timer WebSocket for this request.
      return(new TimerSocket());
    }
  }

  /**
   * Performs initial setup for the timer page.
   */
  public void
  setup()
  {
    // Register the WebSocket that supports the timer page.
    WebServer.getInstance().addWebSocket("/timer/timer.ws",
                                         new TimerSocketCreator(), 5000);
  }
}