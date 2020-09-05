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
 * based on JOptionPane with type verification.
 */
public class GGInputInt
{
  private String title;
  private String prompt;
  private int init;
  private boolean isInit;

  /**
   * Creates a modal dialog with given title, prompt and initial value. To show
   * the dialog and get the user input, call show().
   * @param title the title string in the title bar
   * @param prompt the prompt string
   * @param init an initial value put in the text field 
   */
  public GGInputInt(String title, String prompt, int init)
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
  public GGInputInt(String title, String prompt)
  {
    this.title = title;
    this.prompt = prompt;
    isInit = false;
  }

  /**
   * Returns the user input.  When the Ok button is clicked
   * the entry is verified to have the correct data type. If rejected,
   * the dialog is shown again. When the Cancel button is clicked, 
   * the program terminates.
   * @return the verified user entry
   */
  public int show()
  {
    String entry = "";
    boolean ok = false;
    int value = 0;
    while (!ok)
    {
      entry =
        (String)JOptionPane.showInputDialog(null, prompt, title,
        JOptionPane.QUESTION_MESSAGE, null, null, isInit ? "" + init : "");
      if (entry == null)
        System.exit(0);
      try
      {
        value = Integer.valueOf(entry.trim());
        ok = true;
      }
      catch (NumberFormatException ex)
      {
      }
    }
    return value;
  }
}
