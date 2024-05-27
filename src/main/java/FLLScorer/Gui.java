// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

package FLLScorer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Gui
{
  private static Gui m_instance = null;

  public static Gui
  getInstance()
  {
    if(m_instance == null)
    {
        m_instance = new Gui();
    }

    return(m_instance);
  }

  private
  Gui()
  {
  }

  public void
  setup()
  {
    JFrame frame = new JFrame(WebServer.getInstance().getSSI("str_title"));
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(300,300);
    JPanel panel = new JPanel();
    JButton button1 = new JButton("Button 1");
    JButton button2 = new JButton("Button 2");
    panel.add(button1);
    panel.add(button2);
    frame.getContentPane().add(panel);
    frame.setVisible(true);
  }
}