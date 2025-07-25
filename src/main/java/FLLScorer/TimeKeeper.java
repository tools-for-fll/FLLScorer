// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

package FLLScorer;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import org.eclipse.jetty.ee10.websocket.server.JettyServerUpgradeRequest;
import org.eclipse.jetty.ee10.websocket.server.JettyServerUpgradeResponse;
import org.eclipse.jetty.ee10.websocket.server.JettyWebSocketCreator;
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketOpen;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

/**
 * Handles the timing controls for the time keeper.
 * <p>
 * This is a singleton that is acquired via the getInstance() method.
 */
public class TimeKeeper
{
  /**
   * The object for the Timekeeper singleton.
   */
  private static TimeKeeper m_instance = null;

  /**
   * The state of the timer.
   */
  enum TimerState
  {
    /**
     * The timer is reset, ready for a match to start.
     */
    RESET,

    /**
     * The timer is running, timing a match.
     */
    RUN,

    /**
     * The timer is stopped, either as a result of aborting a match (when
     * <i>m_matchTime</i> != 0) or the match ending (when <i>m_matchTime</i> ==
     * 0).
     */
    STOP
  }

  /**
   * The current state of the timer.
   */
  private TimerState m_state = TimerState.RESET;

  /**
   * The start time of the match, in milliseconds.
   */
  private long m_startTimeMillis;

  /**
   * The length of a match, in seconds.
   */
  private int m_matchLen = 150;

  /**
   * The current match time, in seconds.
   */
  private int m_matchTime = 150;

  /**
   * Gets the TimeKeeper singleton object, creating it if necessary.
   *
   * @return Returns the TimeKeeper singleton.
   */
  public static TimeKeeper
  getInstance()
  {
    // Create the TimeKeeper object if required.
    if(m_instance == null)
    {
      m_instance = new TimeKeeper();
    }

    // Return the TimeKeeper object.
    return(m_instance);
  }

  /**
   * The constructor.  This is private so that the object can only be created
   * via the getIntance() method.
   */
  private
  TimeKeeper()
  {
  }

  /**
   * Sets the length of a match.
   *
   * @param matchLen The length of a match, in seconds.
   */
  public void
  matchLength(int matchLen)
  {
    // If the timer is stopped and the current match time matches the current
    // match length, adjust the current match time to the new match length.
    if(((m_state == TimerState.STOP) || (m_state == TimerState.RESET)) &&
       (m_matchTime == m_matchLen))
    {
      m_matchTime = matchLen;
    }

    // Save the match length.
    m_matchLen = matchLen;
  }

  /**
   * Gets the length of a match.
   *
   * @return The lenght of a match, in seconds.
   */
  public int
  matchLength()
  {
    // Return the length of a match.
    return(m_matchLen);
  }

  /**
   * Starts the timer, if it is currently in the reset state.
   */
  public void
  start()
  {
    // Only start the timer if it is is the reset state (meaning it is ready to
    // start).
    if(m_state == TimerState.RESET)
    {
      // Save the start time of the match.
      m_startTimeMillis = java.lang.System.currentTimeMillis();

      // Change the timer state to run, since the timer is now running.
      m_state = TimerState.RUN;
    }
  }

  /**
   * Stops the timer, if it is currently in the run state.
   */
  public void
  stop()
  {
    // Only stop the timer if it is in the stop state.
    if(m_state == TimerState.RUN)
    {
      // Change the timer state to stop, since the timer is no longer running.
      m_state = TimerState.STOP;
    }
  }

  /**
   * Resets the timer, if it is currently in the stop state.
   */
  public void
  reset()
  {
    // Only reset the timer if it is in the stop state.
    if(m_state == TimerState.STOP)
    {
      // Change the timer state to reset.
      m_state = TimerState.RESET;

      // Change the match time to the match length.
      m_matchTime = m_matchLen;
    }
  }

  /**
   * Gets the current state of the timer.
   *
   * @return The state of the timer.
   */
  public TimerState
  state()
  {
    // Return the state of the timer.
    return(m_state);
  }

  /**
   * Gets the start time of the match.
   *
   * @return The start time of the match, in milliseconds.
   */
  public long
  startTime()
  {
    // Return the start time.
    return(m_startTimeMillis);
  }

  /**
   * Gets the current match time (reaming).
   *
   * @return The time remaining in the match, in seconds.
   */
  public int
  matchTime()
  {
    // Return the match time.
    return(m_matchTime);
  }

  /**
   * Computes the time elapsed in the match.
   *
   * @return The time elapsed in the match, in milliseconds.
   */
  public long
  timeElapsed()
  {
    // There is no time elapsed if the timer is not running.
    if(m_state != TimerState.RUN)
    {
      return(0);
    }

    // Compute the time elapsed in the match, in milliseconds.
    long elapsed = java.lang.System.currentTimeMillis() - m_startTimeMillis;

    // Set the match time based on the elapsed time.
    m_matchTime = m_matchLen - (int)(elapsed / 1000);
    if(m_matchTime < 0)
    {
      m_matchTime = 0;
    }

    // Stop the timer if the match length has elapsed.
    if(elapsed >= (m_matchLen * 1000))
    {
      m_state = TimerState.STOP;
    }

    // Return the time elapsed in the match.
    return(elapsed);
  }

  /**
   * Plays an audio file.
   *
   * @param file The name of the audio file to play.
   */
  private void
  play(String file)
  {
    // A try/catch to handle exceptions.
    try
    {
      // The InputStream for the requested audio file.
      InputStream resource =
        ResourceStream.getResourceStream("sounds/" + file);

      // Created a buffered input stream for the audio file.
      InputStream bufferedResource = new BufferedInputStream(resource);

      // Get an audio input stream from this file.
      AudioInputStream audioIn =
        AudioSystem.getAudioInputStream(bufferedResource);

      // Create a Clip object for playing the contents of the file.
      Clip clip = AudioSystem.getClip();

      // Attach the audio input stream to the Clip object.
      clip.open(audioIn);

      // Start playback of the audio file.
      clip.start();
    }
    catch(Exception e)
    {
      System.out.println("Sound error: " + e);
    }
  }

  /**
   * Handles timing a match, in the background.
   */
  private static class Timer implements Runnable
  {
    /**
     * The cached state of the match timer; used to detect when the timer has
     * changed state.
     */
    private TimerState m_state = m_instance.state();

    /**
     * When the timer is running, this is the time, in milliseconds, at which
     * the match time was last checked.
     *
     * When the timer is stopped, this is the time, in milliseconds, at which
     * the timer was stopped.
     */
    private long m_lastCheck = 0;

    /**
     * A boolean that is <b>true</b> when the end game sound effect has been
     * played during a match.
     */
    private boolean m_playedEndGame = false;

    /**
     * The code that runs in the timer thread.
     */
    @Override
    public void
    run()
    {
      long now, delay;

      // Loop forever.
      while(true)
      {
        // Get the current time.
        now = java.lang.System.currentTimeMillis();

        // See if the state of the timer has changed.
        if(m_state != m_instance.state())
        {
          // Update the cached timer state.
          m_state = m_instance.state();

          // See if the timer is now running.
          if(m_state == TimerState.RUN)
          {
            // Play the start of match sound.
            m_instance.play("start.wav");

            // Since a new match has started, clear the end game played flag.
            m_playedEndGame = false;

            // Set the last time the timer was checked to zero, so that a new
            // determination of the timer state is made immediately.
            m_lastCheck = 0;
          }

          // See if the timer is now stopped.
          else if(m_state == TimerState.STOP)
          {
            // If the match time is zero, the timer stopped because the end of
            // the match was reached.  In this case, play the end of match
            // sound.
            if(m_instance.matchTime() == 0)
            {
              m_instance.play("end.wav");
            }

            // Otherwise, play the canceled match sound.
            else
            {
              m_instance.play("cancel.wav");
            }

            // Save the time that the timer was stopped.
            m_lastCheck = now;
          }
        }

        // See if the timer is not running.
        if((m_state == TimerState.STOP) || (m_state == TimerState.RESET))
        {
          // Reset the timer if it has been stopped for 15 seconds.
          if((m_state == TimerState.STOP) &&
             (m_instance.m_matchTime != m_instance.m_matchLen) &&
             ((now - m_lastCheck) >= 15000))
          {
            m_instance.reset();
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
          // See if it has been at least a second since the last time the timer
          // was checked.
          if((now - m_lastCheck) >= 1000)
          {
            // Get the time elapsed in the match.
            long elapsed = m_instance.timeElapsed();

            // See if it is time to play the end game sound effect.
            if((m_instance.matchTime() <= 30) && !m_playedEndGame)
            {
              // Play the end game sound effect.
              m_instance.play("end_game.wav");

              // Indicate that the end game sound effect has been played, so
              // that it will not be played again for this match.
              m_playedEndGame = true;
            }

            // See if the timer is now stopped.
            if(m_instance.state() == TimerState.STOP)
            {
              // Set the last check time to a second ago, so that the delay
              // before the next check will be short.
              m_lastCheck = now - 1000;
            }
            else
            {
              // Set the last check time to the time when the current match
              // time second transition occurred (which might be a bit in the
              // past).  This ensures that the next timer check occurs
              // reasonably close to the timer second boundary, even if this
              // one ended up being a bit delayed.
              m_lastCheck = m_instance.startTime() + ((elapsed / 1000) * 1000);
            }
          }

          // See if there has been less than 990 ms since the last time the
          // timer was checked.  Note that the delay might be interrupted, so
          // this slightly more complicated approach to handling the delay is
          // crucial to accurate timing.
          if((now - m_lastCheck) < 990)
          {
            // Delay until the 990 ms boundary.
            delay = 990 - (now - m_lastCheck);
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
   * The WebSocket for the timer keeper.
   */
  @WebSocket
  public static class TimeKeeperSocket implements Runnable
  {
    /**
     * The session for this WebSocket.
     */
    private Session m_session;

    /**
     * The cached state of the match timer; used to detect when the timer has
     * changed state.
     */
    private TimerState m_state = m_instance.state();

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
     * Called when data is received from the WebSocket.
     *
     * @param text The text that was received.
     */
    @OnWebSocketMessage
    public void
    onMessage(String text)
    {
      // See if this is a start request.
      if(text.equals("start"))
      {
        // Attempt to start the timer.
        m_instance.start();
      }

      // See if this is a stop request.
      if(text.equals("stop"))
      {
        // Attempt to stop the timer.
        m_instance.stop();
      }

      // See if this is a reset request.
      if(text.equals("reset"))
      {
        // Attempt to reset the timer.
        m_instance.reset();
      }

      // See if this is a test request to play the start sound.
      if(text.equals("test start"))
      {
        // Play the start sound.
        m_instance.play("start.wav");
      }

      // See if this is a test request to play the end game sound.
      if(text.equals("test end game"))
      {
        // Play the end game sound.
        m_instance.play("end_game.wav");
      }

      // See if this is a test request to play the end of match sound.
      if(text.equals("test end"))
      {
        // Play the end sound.
        m_instance.play("end.wav");
      }

      // See if this is a test request to play the match cancel sound.
      if(text.equals("test cancel"))
      {
        // Play the cancel sound.
        m_instance.play("cancel.wav");
      }
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
      if(m_state == TimerState.RUN)
      {
        // Send a run mode message via the WebSocket.
        m_session.sendText("m:run", Callback.NOOP);
      }

      // See if the timer is currently stopped.
      else if(m_state == TimerState.STOP)
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
          if(m_state == TimerState.RUN)
          {
            // Send a mode run message via the WebSocket.
            m_session.sendText("m:run", Callback.NOOP);

            // Set the last time a message was sent to zero, so that a new
            // determination of of the timer state is made immediately.
            m_lastSend = 0;
          }

          // See if the timer is now stopped.
          else if(m_state == TimerState.STOP)
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
        if((m_state == TimerState.STOP) || (m_state == TimerState.RESET))
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
            if(m_instance.state() == TimerState.STOP)
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
   * A creator for time keeper WebSockets.
   */
  private static class TimeKeeperSocketCreator implements JettyWebSocketCreator
  {
    // Creates a WebSocket for the incoming request.
    @Override
    public Object
    createWebSocket(JettyServerUpgradeRequest jettyServerUpgradeRequest,
                    JettyServerUpgradeResponse jettyServerUpgradeResponse)
    {
      // Create a new time keeper WebSocket for this request.
      return(new TimeKeeperSocket());
    }
  }

  /**
   * Performs initial setup for the time keeper page.
   */
  public void
  setup()
  {
    // Register the WebSocket that supports the time keeper page.
    WebServer.getInstance().addWebSocket("/timekeeper/timekeeper.ws",
                                         new TimeKeeperSocketCreator(), 5000);

    // Start the background thread that manages the match timer.
    new Thread(new Timer()).start();
  }
}