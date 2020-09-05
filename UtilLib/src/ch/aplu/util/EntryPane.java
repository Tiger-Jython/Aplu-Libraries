// EntryPane.java

/*
 This software is part of the JEX (Java Exemplarisch) Utility Library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.
 */
package ch.aplu.util;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Container for EntryItems to be used for EntryDialog areas. 
 * The size is adapted to the items it contains.
 */
public class EntryPane
{
  private JPanel pane;
  private EntryItem[] items;
  private String title;

  /** 
   * Creates an entry pane with no surrounding border lines. The EntryPane contains any number of 
   * EntryItems, but they must be of the same type.
   * (IntegerEntry, DoubleEntry and StringEntry are considered to have the same type.)
   * @param items any number of EntryItems (of the same class)
   */
  public EntryPane(EntryItem... items)
  {
    this(null, items);
  }

  /** 
   * Creates an entry pane with thin border lines and given title 
   * text displayed at the top border line. The EntryPane contains any number of 
   * EntryItems, but they must be of the same type.
   * (IntegerEntry, DoubleEntry and StringEntry are considered to have the same type.)
   * @param borderTitle the text displayed on the top border line (may be empty)
   * @param items any number of EntryItems (of the same class)
   */
  public EntryPane(String borderTitle, EntryItem... items)
  {
    this.title = borderTitle;
    this.items = items;
  }

  protected int getNbItems()
  {
    return items.length;
  }

  protected EntryItem[] getItems()
  {
    return items;
  }

  protected String getTitle()
  {
    return title;
  }

  protected void setPane(JPanel pane)
  {
    this.pane = pane;
  }

  /** 
   * Modifies the title text displayed on the upper border line.
   * @param borderTitle the new border title. If the pane has no border,
   * nothing happens
   */
  public void setTitle(String borderTitle)
  {
    if (title == null)
      return;
    title = borderTitle;

    if (SwingUtilities.isEventDispatchThread())
    {
      pane.setBorder(
        BorderFactory.createCompoundBorder(
          BorderFactory.createTitledBorder(title),
          BorderFactory.createEmptyBorder(3, 3, 3, 3)));
    }
    else
    {
      try
      {
        SwingUtilities.invokeAndWait(
          new Runnable()
          {
            public void run()
            {
              pane.setBorder(
                BorderFactory.createCompoundBorder(
                  BorderFactory.createTitledBorder(title),
                  BorderFactory.createEmptyBorder(3, 3, 3, 3)));
            }
          });
      }
      catch (Exception ex)
      {
      }
    }
  }

  /**
   * Returns the underlaying JPanel.
   * @return the JPanel reference
   */
  public JPanel getPane()
  {
    return pane;
  }

}
