// EntryDialog.java

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

import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;
import javax.swing.*;
import javax.swing.event.*;

/**
 * Versatile modeless message dialog for entering user data and selecting 
 * option.
 * 
 * When the close button of the EntryDialog window title bar is hit, System.exit(0)
 * is executed that terminates the JVM, but you can modifiy this behavior by
 * registering your own implementation of the ExitListener interface or 
 * by using the key EntryDialogClosingMode in aplu_util.properties. 
 * For more details consult aplu_util.properties file found in the distribution.<br><br>
 * 
 * To prevent threading problems, the EntryDialog implementation
 * does not use the event model, but is based on the polling model, where any
 * changes of the controls are detected by repeatedly calling their getValue() methods.<br><br>
 *
 * All Swing methods are invoked in the EDT.
 */
public class EntryDialog
{
  private enum ClosingMode
  {
    TerminateOnClose,
    AskOnClose,
    DisposeOnClose,
    NothingOnClose
  }

// -------- Inner class MyActionListener ------------
  private class MyMouseListener extends MouseAdapter
  {
    public void mouseReleased(MouseEvent evt)
    {
      Object obj = evt.getSource();
      if (obj instanceof JSlider)
      {
        JSlider slider = (JSlider)obj;
        if (!slider.isEnabled())
          return;
        for (EntryPane ep : entryPanes)
        {
          for (EntryItem ei : ep.getItems())
          {
            if (ei instanceof SliderEntry)
            {
              SliderEntry se = (SliderEntry)ei;
              if (se.getSlider() == slider)
              {
                se.setTouched(true);
                if (entryListener != null)
                  entryListener.touched(se);
              }
            }
          }
        }
      }

      obj = evt.getSource();
      if (obj instanceof JTextField)
      {
        JTextField textField = (JTextField)obj;
        if (!textField.isEnabled())
          return;
        for (EntryPane ep : entryPanes)
        {
          for (EntryItem ei : ep.getItems())
          {
            if (ei instanceof TextEntry)
            {
              TextEntry te = (TextEntry)ei;
              if (te.getTextField() == textField)
              {
                te.setTouched(true);
                if (entryListener != null)
                  entryListener.touched(te);
              }
            }
          }
        }
      }

      obj = evt.getSource();
      if (obj instanceof JCheckBox)
      {
        JCheckBox checkBox = (JCheckBox)obj;
        if (!checkBox.isEnabled())
          return;
        for (EntryPane ep : entryPanes)
        {
          for (EntryItem ei : ep.getItems())
          {
            if (ei instanceof CheckEntry)
            {
              CheckEntry ce = (CheckEntry)ei;
              if (ce.getCheckBox() == checkBox)
              {
                ce.setTouched(true);
                if (entryListener != null)
                  entryListener.touched(ce);
              }
            }
          }
        }
      }

      obj = evt.getSource();
      if (obj instanceof JRadioButton)
      {
        JRadioButton radioButton = (JRadioButton)obj;
        if (!radioButton.isEnabled())
          return;
        for (EntryPane ep : entryPanes)
        {
          for (EntryItem ei : ep.getItems())
          {
            if (ei instanceof RadioEntry)
            {
              RadioEntry re = (RadioEntry)ei;
              if (re.getRadioButton() == radioButton)
              {
                re.setTouched(true);
                if (entryListener != null)
                  entryListener.touched(re);
              }
            }
          }
        }
      }

      ButtonEntry be = null;
      obj = evt.getSource();
      if (obj instanceof JButton)
      {
        JButton button = (JButton)obj;
        if (!button.isEnabled())
          return;
        for (EntryPane ep : entryPanes)
        {
          for (EntryItem ei : ep.getItems())
          {
            if (ei instanceof ButtonEntry)
            {
              be = (ButtonEntry)ei;
              if (be.getButton() == button)
              {
                be.setTouched(true);
                if (entryListener != null)
                  entryListener.touched(ei);
              }
            }
          }
        }
      }

    }
  }

// -------- Inner class MyWindowListener ------------
  private class MyWindowListener extends WindowAdapter
  {
    public void windowClosing(WindowEvent e)
    {
      if (exitListener != null)
        exitListener.notifyExit();
      else
      {
        switch (closingMode)
        {
          case TerminateOnClose:
            System.exit(0);
            break;
          case AskOnClose:
            if (JOptionPane.showConfirmDialog(frame,
              "Terminating program. Are you sure?",
              "Please confirm",
              JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
              System.exit(0);
            break;
          case DisposeOnClose:
            dispose();
            isDisposed = true;
            Monitor.wakeUp();
            break;
          case NothingOnClose:
            break;
        }
      }
    }
  }

  // -------- Inner class MyActionListener ------------
  private class MyActionListener implements ActionListener
  {
    public void actionPerformed(ActionEvent evt)
    {
      isStateChanged = true;
      if (entryListener != null)
      {
        Object obj = evt.getSource();
        if (obj instanceof JCheckBox)
        {
          JCheckBox checkBox = (JCheckBox)obj;
          for (EntryPane ep : entryPanes)
          {
            for (EntryItem ei : ep.getItems())
            {
              if (ei instanceof CheckEntry)
              {
                CheckEntry ce = (CheckEntry)ei;
                if (ce.getCheckBox() == checkBox)
                  entryListener.modified(ce);
              }
            }
          }
        }
        if (obj instanceof JRadioButton)
        {
          JRadioButton radioButton = (JRadioButton)obj;
          for (EntryPane ep : entryPanes)
          {
            for (EntryItem ei : ep.getItems())
            {
              if (ei instanceof RadioEntry)
              {
                RadioEntry re = (RadioEntry)ei;
                if (re.getRadioButton() == radioButton)
                  entryListener.modified(re);
              }
            }
          }
        }
      }
    }
  }

  // -------- Inner class MyChangeListener ------------
  private class MyChangeListener implements ChangeListener
  {
    public void stateChanged(ChangeEvent evt)
    {
      JSlider source = (JSlider)evt.getSource();
      if (!source.getValueIsAdjusting())
      {
        isStateChanged = true;
        if (entryListener != null)
        {
          Object obj = evt.getSource();
          if (obj instanceof JSlider)
          {
            JSlider slider = (JSlider)obj;
            for (EntryPane ep : entryPanes)
            {
              for (EntryItem ei : ep.getItems())
              {
                if (ei instanceof SliderEntry)
                {
                  SliderEntry se = (SliderEntry)ei;
                  if (se.getSlider() == slider)
                    entryListener.modified(se);
                }
              }
            }
          }
        }
      }
    }
  }
  // -------- End of inner classes --------------------
  //
  private static final long serialVersionUID = 1332883994005116227L;
  private JFrame frame = null;
  private ActionListener myActionListener = new MyActionListener();
  private ChangeListener myChangeListener = new MyChangeListener();
  private MouseListener myMouseListener = new MyMouseListener();
  private final boolean propertyVerbose = false;
  private JPanel contentPane;
  private int nbPanes;
  private JPanel[] panes;
  private final int DLG_WIDTH = 460;
  private EntryPane[] entryPanes;
  private ClosingMode closingMode;
  private boolean isDisposed;
  private EntryListener entryListener = null;
  private ExitListener exitListener = null;
  private String defaultTitle = "Setup";
  private boolean isStateChanged = false;

  /**
   * Same as general constructor, but the dialog is positioned at 
   * the screen center.
   */
  public EntryDialog(EntryPane... entryPanes)
  {
    this(0, 0, entryPanes);
  }

  /**
   * General constructor that shows a modeless message dialog at given
   * position (upper left corner) containing any number of EntryPanes containers
   * any number of EntryItems <b>of the same type</b>.  
   * (IntegerEntry, DoubleEntry and StringEntry can be mixed up in the same EntryPane.)<br>
   * The data in the entry items are fetched by calling the corresponding getValue()
   * methods. <br><br>
   *
   * All methods run in the Event Dispatch Thread (EDT).
   *
   * @param ulx the x-coordinate of the upper left corner of the dialog window
   * @param uly the y-coordinate of the upper left corner of the dialog window
   * @param entryPanes one or more references to EntryPane objects. They appear 
   * in the order they are passed
   */
  public EntryDialog(final int ulx, final int uly, EntryPane... entryPanes)
  {
    this.entryPanes = entryPanes;
    if (EventQueue.isDispatchThread())
      init(ulx, uly);
    else
    {
      try
      {
        EventQueue.invokeAndWait(new Runnable()
        {
          public void run()
          {
            init(ulx, uly);
          }
        });
      }
      catch (InterruptedException e)
      {
      }
      catch (InvocationTargetException e)
      {
      }
    }
  }

  private void init(int ulx, int uly)
  {
    frame = new JFrame();
    String title = defaultTitle;
    Point dialogPos = null;
    if (ulx > 0 && uly > 0)
      dialogPos = new Point(ulx, uly);

    MyProperties props = new MyProperties(propertyVerbose);
    if (props.search())
    {
      closingMode = getClosingMode(props);

      if (title == null)  // Title not given by ctor
      {
        String value = props.getStringValue("EntryDialogTitle");
        if (value != null)
          title = value.trim();
      }

      if (ulx <= 0 || uly <= 0)
      {
        int[] values = props.getIntArray("EntryDialogPosition", 2);
        if (values != null && values[0] > 0 && values[1] > 0)
          dialogPos = new Point(values[0], values[1]);
      }
    }

    boolean rc = initContent(entryPanes);
    if (!rc)
      System.out.println("Error: Entries must be of same base type.");
    frame.pack();
    frame.setResizable(false);
    Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
    if (dialogPos == null)
      dialogPos = new Point(
        (int)((dimension.getWidth() - frame.getWidth()) / 2),
        (int)((dimension.getHeight() - frame.getHeight()) / 2));
    frame.setLocation(dialogPos.x, dialogPos.y);
    frame.addWindowListener(new MyWindowListener());
    frame.setVisible(true);
  }

  private boolean initContent(EntryPane[] entryPanes)
  {
    nbPanes = entryPanes.length;
    panes = new JPanel[nbPanes];
    contentPane = (JPanel)frame.getContentPane();
    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
    GridBagLayout gridBag = new GridBagLayout();

    // ----------------- Panes ----------------------------
    for (int i = 0; i < nbPanes; i++)
    {
      int nbItems = entryPanes[i].getNbItems();
      JPanel outerPane = new JPanel();
      outerPane.setBackground(Color.white);
      panes[i] = new JPanel();
      entryPanes[i].setPane(panes[i]);
      if (entryPanes[i].getTitle() != null)
      {
        panes[i].setBorder(
          BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(entryPanes[i].getTitle()),
            BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        outerPane.add(panes[i]);
        contentPane.add(outerPane);
      }
      else
        contentPane.add(panes[i]);

      panes[i].setLayout(gridBag);
      panes[i].setBackground(Color.white);
      EntryItem[] items = entryPanes[i].getItems();

      // ------------- TextEntries ------------------
      if (items[0] instanceof TextEntry)
      {
        int paneHeight = nbItems * 20 + 20;
        panes[i].setPreferredSize(new Dimension(DLG_WIDTH, paneHeight));
        JLabel[] labels = new JLabel[nbItems];
        JTextField[] textFields = new JTextField[nbItems];
        for (int k = 0; k < nbItems; k++)
        {
          if (!(items[k] instanceof TextEntry))
            return false;
          TextEntry te = (TextEntry)items[k];
          labels[k] = te.getLabel();
          textFields[k] = te.getTextField();
          textFields[k].addMouseListener(myMouseListener);
        }
        addLabelTextRows(labels, textFields, gridBag, panes[i]);
      }

      // ------------- CheckEntries ------------------
      else if (items[0] instanceof CheckEntry)
      {
        int paneHeight = nbItems * 30 + 20;
        panes[i].setPreferredSize(new Dimension(DLG_WIDTH, paneHeight));
        JCheckBox[] checkBoxes = new JCheckBox[nbItems];
        for (int k = 0; k < nbItems; k++)
        {
          if (!(items[k] instanceof CheckEntry))
            return false;
          CheckEntry ce = (CheckEntry)items[k];
          checkBoxes[k] = ce.getCheckBox();
          checkBoxes[k].setBackground(Color.white);
          checkBoxes[k].addActionListener(myActionListener);
          checkBoxes[k].addMouseListener(myMouseListener);
        }
        addCheckBoxes(checkBoxes, gridBag, panes[i]);
      }

      // ------------- RadioEntries ------------------
      else if (items[0] instanceof RadioEntry)
      {
        int paneHeight = nbItems * 30 + 10;
        panes[i].setPreferredSize(new Dimension(DLG_WIDTH, paneHeight));
        JRadioButton[] radioButtons = new JRadioButton[nbItems];
        for (int k = 0; k < nbItems; k++)
        {
          if (!(items[k] instanceof RadioEntry))
            return false;
          RadioEntry re = (RadioEntry)items[k];
          radioButtons[k] = re.getRadioButton();
          radioButtons[k].setBackground(Color.white);
          radioButtons[k].addActionListener(myActionListener);
          radioButtons[k].addMouseListener(myMouseListener);
        }
        addRadioButtons(radioButtons, gridBag, panes[i]);
      }

      // ------------- SliderEntries ------------------
      else if (items[0] instanceof SliderEntry)
      {
        int paneHeight = nbItems * 50 + 20;
        panes[i].setPreferredSize(new Dimension(DLG_WIDTH, paneHeight));
        JSlider[] sliders = new JSlider[nbItems];
        for (int k = 0; k < nbItems; k++)
        {
          if (!(items[k] instanceof SliderEntry))
            return false;
          SliderEntry se = (SliderEntry)items[k];
          sliders[k] = se.getSlider();
          sliders[k].setBackground(Color.white);
          sliders[k].addChangeListener(myChangeListener);
          sliders[k].addMouseListener(myMouseListener);
        }
        addSliders(sliders, gridBag, panes[i]);
      }

      // ------------- ButtonEntries ------------------
      else if (items[0] instanceof ButtonEntry)
      {
        int paneHeight;
        if (entryPanes[i].getTitle() == null || entryPanes[i].getTitle().trim().equals(""))
          paneHeight = 30;
        else
          paneHeight = 40;
        panes[i].setPreferredSize(new Dimension(DLG_WIDTH, paneHeight));
        JButton[] buttons = new JButton[nbItems];
        for (int k = 0; k < nbItems; k++)
        {
          if (!(items[k] instanceof ButtonEntry))
            return false;
          ButtonEntry be = (ButtonEntry)items[k];
          buttons[k] = be.getButton();
          buttons[k].addMouseListener(myMouseListener);
        }
        addPushButtons(buttons, gridBag, panes[i]);
      }
    }
    return true;
  }

  /**
   * Checks if the state of the entry pane has changed due to user action.
   * After the call, the state is set to unmodified.
   * Puts calling thread to sleep for 1 millisecond, so it can be used 
   * in a tight polling loop.
   * @return true, if the user modified one of the dialog controls
   */
  public boolean isChanged()
  {
    delay(1);
    boolean b = isStateChanged;
    isStateChanged = false;
    return b;
  }

  private void addLabelTextRows(JLabel[] labels,
    JTextField[] textFields, GridBagLayout gridBag, Container container)
  {
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.EAST;
    int nbItems = labels.length;

    for (int i = 0; i < nbItems; i++)
    {
      c.gridwidth = GridBagConstraints.RELATIVE; // next-to-last
      c.fill = GridBagConstraints.NONE; // reset to default
      c.weightx = 0.0; // reset to default
      gridBag.setConstraints(labels[i], c);
      container.add(labels[i]);

      c.gridwidth = GridBagConstraints.REMAINDER; // end row
      c.fill = GridBagConstraints.HORIZONTAL;
      c.weightx = 1.0;
      gridBag.setConstraints(textFields[i], c);
      container.add(textFields[i]);
    }
  }

  private void addCheckBoxes(JCheckBox[] checkBoxes,
    GridBagLayout gridBag, Container container)
  {
    GridBagConstraints c = new GridBagConstraints();
    int nbItems = checkBoxes.length;

    for (int i = 0; i < nbItems; i++)
    {
      c.gridwidth = GridBagConstraints.REMAINDER;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.weightx = 1.0;
      gridBag.setConstraints(checkBoxes[i], c);
      container.add(checkBoxes[i]);
    }
  }

  private void addRadioButtons(JRadioButton[] radioButtons,
    GridBagLayout gridBag, Container container)
  {
    ButtonGroup buttonGroup = new ButtonGroup();
    GridBagConstraints c = new GridBagConstraints();
    int nbItems = radioButtons.length;

    for (int i = 0; i < nbItems; i++)
    {
      c.gridwidth = GridBagConstraints.REMAINDER;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.weightx = 1.0;
      gridBag.setConstraints(radioButtons[i], c);
      container.add(radioButtons[i]);
      buttonGroup.add(radioButtons[i]);
    }
  }

  private void addPushButtons(JButton[] buttons,
    GridBagLayout gridBag, Container container)
  {
    GridBagConstraints c = new GridBagConstraints();
    int nbItems = buttons.length;

    for (int i = 0; i < nbItems; i++)
    {
      gridBag.setConstraints(buttons[i], c);
      container.add(buttons[i]);
    }
  }

  private void addSliders(JSlider[] sliders,
    GridBagLayout gridBag,
    Container container)
  {
    GridBagConstraints c = new GridBagConstraints();
    int nbItems = sliders.length;

    for (int i = 0; i < nbItems; i++)
    {
      c.gridwidth = GridBagConstraints.REMAINDER;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.weightx = 1.0;
      gridBag.setConstraints(sliders[i], c);
      container.add(sliders[i]);
    }
  }

  public void dispose()
  {
    isDisposed = true;
    frame.dispose();
  }

  /**
   * Returns true, if the window is disposed.
   * @return if dialog is disposed; otherwise false
   */
  public boolean isDisposed()
  {
    return isDisposed;
  }

  /**
   * Registers a class that implements ExitListener.notifyExit() that will be called when
   * the title bar's close button is hit.
   */
  public void addExitListener(ExitListener listener)
  {
    exitListener = listener;
  }

  /**
   * Registers an EntryListener.
   */
  public void addEntryListener(EntryListener listener)
  {
    entryListener = listener;
  }

  private ClosingMode getClosingMode(MyProperties props)
  {
    String value = props.getStringValue("EntryDialogClosingMode");
    if (value != null)  // Entry found
    {
      value = value.trim();
      if (value.equals("TerminateOnClose"))
        return ClosingMode.TerminateOnClose;
      if (value.equals("AskOnClose"))
        return ClosingMode.AskOnClose;
      if (value.equals("DisposeOnClose"))
        return ClosingMode.DisposeOnClose;
      if (value.equals("NothingOnClose"))
        return ClosingMode.NothingOnClose;
      return ClosingMode.TerminateOnClose;  // Entry not valid
    }
    return ClosingMode.TerminateOnClose;  // Entry not valid
  }

  private static void delay(int time)
  {
    try
    {
      Thread.currentThread().sleep(time);
    }
    catch (Exception e)
    {
    }
  }

  /**
   * Displays the given title in the title bar.
   * @param text the text used as title
   */
  public void setTitle(final String text)
  {
    if (isDisposed)
      return;

    if (SwingUtilities.isEventDispatchThread())
      frame.setTitle(text);
    else
    {
      try
      {
        SwingUtilities.invokeAndWait(
          new Runnable()
          {
            public void run()
            {
              frame.setTitle(text);
            }
          });
      }
      catch (Exception ex)
      {
      }
    }
  }

  /**
   * Hides the dialog window. 
   */
  public void hide()
  {
    if (isDisposed)
      return;

    if (SwingUtilities.isEventDispatchThread())
      frame.setVisible(false);
    else
    {
      try
      {
        SwingUtilities.invokeAndWait(
          new Runnable()
          {
            public void run()
            {
              frame.setVisible(false);
            }
          });
      }
      catch (Exception ex)
      {
      }
    }
  }

  /**
   * Shows a hidden dialog window. 
   */
  public void show()
  {
    if (isDisposed)
      return;

    if (SwingUtilities.isEventDispatchThread())
      frame.setVisible(true);
    else
    {
      try
      {
        SwingUtilities.invokeAndWait(
          new Runnable()
          {
            public void run()
            {
              frame.setVisible(true);
            }
          });
      }
      catch (Exception ex)
      {
      }
    }
  }

  /**
   * Enables/disalbes whether this window should always be above other windows. 
   * @param enable if true, the dialog window remains on top (but may lost the focus)
   */
  public void setAlwaysOnTop(final boolean enable)
  {
    if (isDisposed)
      return;

    if (SwingUtilities.isEventDispatchThread())
      frame.setAlwaysOnTop(enable);
    else
    {
      try
      {
        SwingUtilities.invokeAndWait(
          new Runnable()
          {
            public void run()
            {
              frame.setAlwaysOnTop(enable);
            }
          });
      }
      catch (Exception ex)
      {
      }
    }
  }

  /**
   * Brings the visible window to the front and make it focused or brings it to back. 
   * @param enable if true, the dialog windows is put to the front; otherwise it is put to the back
   */
  public void toFront(final boolean enable)
  {
    if (isDisposed)
      return;

    if (SwingUtilities.isEventDispatchThread())
    {
      if (enable)
        frame.toFront();
      else
        frame.toBack();
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
              if (enable)
                frame.toFront();
              else
                frame.toBack();
            }
          });
      }
      catch (Exception ex)
      {
      }
    }
  }

  /**
   * Returns the underlaying JFrame
   * @return the JFrame reference
   */
  public JFrame getFrame()
  {
    return frame;
  }

}
