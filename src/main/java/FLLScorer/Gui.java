// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

package FLLScorer;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.plaf.DimensionUIResource;

/**
 * Provides a simple GUI on the host computer, providing the minimal "get out
 * of jail free" controls.
 * <p>
 * This is a singleton that is acquired via the getInstance() method.
 */
public class Gui
{  /**
   * The instance of the GUI singleton.
   */
  private static Gui m_instance = null;

  /**
   * The Webserver object.
   */
  private static WebServer m_webserver = null;

  /**
   * The Database object.
   */
  private static Database m_database = null;

  /**
   * The Config object.
   */
  private static Config m_config = null;

  /**
   * The Swing frame for the application.
   */
  private JFrame m_frame = null;

  /**
   * The Swing check box control for enabling/disabling logins (only present
   * when it is believed that the application is being run by a developer).
   */
  private JCheckBox m_checkboxLogin = null;

  /**
   * The Swing check box control for enabling/disabling the logging of database
   * queries (only present when it is believed that the application is being
   * run by a developer).
   */
  private JCheckBox m_checkboxDB = null;

  /**
   * The Swing check box control for enabling/disabling the logging of HTTP
   * requests (only present when it is belived that the application is being
   * run by a developer).
   */
  private JCheckBox m_checkboxHTTP = null;

  /**
   * Gets the Gui singleton object, creating it if necessary.
   *
   * @return Returns the Gui singleton.
   */
  public static Gui
  getInstance()
  {
    // Create the Gui object if required.
    if(m_instance == null)
    {
        m_instance = new Gui();
    }

    // Return the Gui object.
    return(m_instance);
  }

  /**
   * The constructor.  This is private so that the object can only be created
   * via the getIntance() method.
   */
  private
  Gui()
  {
  }

  /**
   * A simple wrapper for a confirmation dialog box.
   *
   * @param title The title for the confirmation dialog box.
   *
   * @param message The message to display in the confirmation dialog box.
   *
   * @return A boolean that is <b>true</b> if the user confirmed (selected
   *         "yes") the request.
   */
  private boolean
  confirm(String title, String message)
  {
    // Display a Swing confirmation dialog and return the result.
    return(JOptionPane.showConfirmDialog(m_frame, message, title,
                                         JOptionPane.YES_NO_OPTION,
                                         JOptionPane.PLAIN_MESSAGE) ==
           JOptionPane.YES_OPTION);
  }

  /**
   * A simple wrapper for a message dialog box.
   *
   * @param title The title for the message dialog box.
   *
   * @param message THe message to display in the messsage dialog box.
   */
  private void
  message(String title, String message)
  {
    // Display a Swing message dialog.
    JOptionPane.showMessageDialog(m_frame, message, title,
                                  JOptionPane.PLAIN_MESSAGE);
  }

  /**
   * An action listener to call when the user clicks on the launch web site
   * button.
   */
  private ActionListener m_launch = new ActionListener()
  {
    // The method called when the button is clicked.
    @Override
    public void
    actionPerformed(ActionEvent event)
    {
      // Attempt to launch the default web browser.
      try
      {
        // Create a URI to the web site served by this application.
        URI uri = new URI("https://localhost:8443");

        // Get the Desktop object for this computer, if one is supported.
        Desktop desktop =
          Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;

        // If there is a Desktop object, and it supports launching a browser,
        // use it to launch the web site served by this application.
        if((desktop != null) && desktop.isSupported(Desktop.Action.BROWSE))
        {
          desktop.browse(uri);
        }
      }
      catch (Exception e)
      {
        System.out.println("Launch error: " + e);
      }
    }
  };

  /**
   * An action listener to call when the user clicks on the reset admin
   * password button.
   */
  private ActionListener m_resetPassword = new ActionListener()
  {
    // The method called when the button is clicked.
    @Override
    public void
    actionPerformed(ActionEvent event)
    {
      // Confirm that the user really wants to reset the admin password.
      if(confirm(m_webserver.getSSI("str_gui_reset_admin"),
                 m_webserver.getSSI("str_gui_reset_admin_confirm")))
      {
        // Get the admin user's hashed default password.
        String password = MD5SHA.hash("admin",
                                      Database.m_adminDefaultPassword);

        // Change the admin user's password.
        m_database.userPasswordSet("admin", password);

        // Display a confirmation message.
        message(m_webserver.getSSI("str_gui_reset_admin"),
                m_webserver.getSSI("str_gui_reset_admin_msg"));
      }
    }
  };

  /**
   * An action listener to call when the user clicks on the disable logins
   * check box.
   */
  private ActionListener m_loginToggle = new ActionListener()
  {
    // The method called when the check box is clicked.
    @Override
    public void
    actionPerformed(ActionEvent event)
    {
      // Set the state of the security bypass configuration value based on the
      // new state of the check box.
      m_config.securityBypassSet(m_checkboxLogin.isSelected());

      // Inform the user that the change will not be effective until the
      // application is restarted.
      message(m_webserver.getSSI("str_gui_disable_login"),
              m_webserver.getSSI("str_gui_restart_msg"));
    }
  };

  /**
   * An action listener to call when the user clicks on the log database
   * requests check box.
   */
  private ActionListener m_dbLogToggle = new ActionListener()
  {
    // The method called when the check box is clicked.
    @Override
    public void
    actionPerformed(ActionEvent event)
    {
      // Set the state of the database debug configuration value based on the
      // new state of the check box.
      m_database.dbDebugSet(m_checkboxDB.isSelected());

      // Inform the user that the change will not be effective until the
      // application is restarted.
      message(m_webserver.getSSI("str_gui_log_db"),
              m_webserver.getSSI("str_gui_restart_msg"));
    }
  };

  /**
   * An action listener to call when the user clicks on the log HTTP requests
   * check box.
   */
  private ActionListener m_httpLogToggle = new ActionListener()
  {
    // The method called when the check box is clicked.
    @Override
    public void
    actionPerformed(ActionEvent event)
    {
      // Set the state of the HTTP debug configuration value based on the new
      // state of the check box.
      m_config.httpDebugSet(m_checkboxHTTP.isSelected());

      // Inform the user that the change will not be effective until the
      // application is restarted.
      message(m_webserver.getSSI("str_gui_log_http"),
              m_webserver.getSSI("str_gui_restart_msg"));
    }
  };

  /**
   * Performs initial setup for the GUI.
   */
  public void
  setup()
  {
    // Get references to the web server, database, and the configuration
    // manager.
    m_webserver = WebServer.getInstance();
    m_database = Database.getInstance();
    m_config = Config.getInstance();

    // Create a frame, which is the main window for the application.
    m_frame = new JFrame(m_webserver.getSSI("str_title"));
    m_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    m_frame.setMinimumSize(new DimensionUIResource(320, 180));
    m_frame.setSize(320, 180);

    // Create a panel for arranging the contents of the application.
    JPanel panel = new JPanel();

    // Create a button for starting the main page in the default web browser.
    JButton button1 = new JButton(m_webserver.getSSI("str_gui_launch"));
    button1.addActionListener(m_launch);
    panel.add(button1);

    // Create a button for resetting the administrator password (a fail-safe in
    // case it is changed and the new password has been forgotten).
    JButton button2 = new JButton(m_webserver.getSSI("str_gui_reset_admin"));
    button2.addActionListener(m_resetPassword);
    panel.add(button2);

    // See if the application is running from a JAR file.  If not, it is likely
    // being run by a developer, so add some additional controls for toggling
    // some developer-only settings in the database (each of which provides
    // assistance to the developer).
    String className = this.getClass().getSimpleName() + ".class";
    String protocol = this.getClass().getResource(className).getProtocol();
    if(Objects.equals(protocol, "file"))
    {
      // Add a check box for enabling/disabling the use of accounts and logins.
      m_checkboxLogin =
        new JCheckBox(m_webserver.getSSI("str_gui_disable_login"));
      m_checkboxLogin.addActionListener(m_loginToggle);
      m_checkboxLogin.setSelected(m_config.securityBypassGet());
      panel.add(m_checkboxLogin);

      // Add a check box for enabling/disabling the printing of all database
      // access SQL statements to standard output.
      m_checkboxDB = new JCheckBox(m_webserver.getSSI("str_gui_log_db"));
      m_checkboxDB.addActionListener(m_dbLogToggle);
      m_checkboxDB.setSelected(m_database.dbDebugGet());
      panel.add(m_checkboxDB);

      // Add a check box for enabling/disabling the printing of all HTTP
      // requests to standard output.
      m_checkboxHTTP = new JCheckBox(m_webserver.getSSI("str_gui_log_http"));
      m_checkboxHTTP.addActionListener(m_httpLogToggle);
      m_checkboxHTTP.setSelected(m_config.httpDebugGet());
      panel.add(m_checkboxHTTP);
    }

    // Add the panel to the frame and make the frame visible.
    m_frame.getContentPane().add(panel);
    m_frame.setVisible(true);
  }
}