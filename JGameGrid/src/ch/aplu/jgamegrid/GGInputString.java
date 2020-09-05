// GGInputInt.java

/*
This software is part of the JGameGrid package.
It is Open Source Free Software, so you may
- run the code for any purpose
- study how the code works and adapt it to your needs
- integrate all or parts of the code in your own programs
- redistribute copies of the code
- improve the code and release your improvements to the public
However the use of the code is entirely your responsibility.

Author: Aegidius Pluess, www.aplu.ch
 */
package ch.aplu.jgamegrid;

import javax.swing.JOptionPane;

/**
 * Class to provide a modal dialog with one text field and a Ok and Cancel button
 * based on JOptionPane.
 */
public class GGInputString
{
  private String title;
  private String prompt;
  private String init;
  private boolean isInit;

  /**
   * Creates a modal dialog with given title, prompt and initial value. To show
   * the dialog and get the user input, call show().
   * @param title the title string in the title bar
   * @param prompt the prompt string
   * @param init an initial text put in the text field 
   */
  public GGInputString(String title, String prompt, String init)
  {
    this.title = title;
    this.prompt = prompt;
    this.init = init;
    isInit = true;
  }

  /**
   * Creates a modal dialog with given title, prompt and no initial value. To show
   * the dialog and get the user input, call show().
   * @param title the title string in the title bar
   * @param prompt the prompt string
   */
  public GGInputString(String title, String prompt)
  {
    this.title = title;
    this.prompt = prompt;
    isInit = false;
  }

  /**
   * Returns the user input when the Ok button is clicked. Leading and trailing
   * white spaces are removed. 
   * When the Cancel button is clicked, the program terminates.
   * @return the user entry
   */
  public String show()
  {
    String entry = "";
    boolean ok = false;
    String value = "";
    while (!ok)
    {
      entry =
        (String)JOptionPane.showInputDialog(null, prompt, title,
        JOptionPane.QUESTION_MESSAGE, null, null, isInit ? "" + init : "");
      if (entry == null)
        System.exit(0);
      try
      {
        value = entry.trim();
        ok = true;
      }
      catch (NumberFormatException ex)
      {
      }
    }
    return value;
  }
}
