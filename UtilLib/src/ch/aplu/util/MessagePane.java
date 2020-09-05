// MessagePane.java

package ch.aplu.util;

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
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

/**
 * Modeless dialog containing one line of text.
 * When the close button of the MessagePane window title bar is hit, System.exit(0)
 * is executed that terminates the JVM, but you can modifiy this behavior by
 * registering your own implementation of the ExitListener interface or 
 * by using the key MessagePaneClosingMode in aplu_util.properties. 
 * For more details consult aplu_util.properties file found in the distribution.<br><br>
 * 
 * All Swing methods are invoked in the EDT.
 */
public class MessagePane
{
  private enum ClosingMode
  {
    TerminateOnClose,
    AskOnClose,
    DisposeOnClose,
    NothingOnClose
  }
  // ------------------ Inner classes ------------------------
  // Used in order to call Swing methods from EDT only

  private class SetText implements Runnable
  {
    private String text;

    public SetText(String text)
    {
      this.text = text;
    }

    public void run()
    {
      textArea.setText(text);
      textArea.setCaretPosition(0);
      if (!dlg.isVisible())  // When using timed pane
        dlg.setVisible(true);
    }

  }

  // Used in order to call Swing methods from EDT only
  private class Title implements Runnable
  {
    private String text;

    public Title(String text)
    {
      this.text = text;
    }

    public void run()
    {
      dlg.setTitle(text);
      if (!dlg.isVisible())  // When using timed pane
        dlg.setVisible(true);
    }

  }

  // Used in order to call Swing methods from EDT only
  private class Init implements Runnable
  {
    private int ulx, uly;
    private int nbChars;

    public Init(int ulx, int uly, int nbChars)
    {
      this.ulx = ulx;
      this.uly = uly;
      this.nbChars = nbChars;
    }

    public void run()
    {
      init(nbChars);
      if (ulx == -1)
        setCenter();
      else
        dlg.setLocation(ulx, uly);
      dlg.setVisible(true);
    }

  }

  // Get event when horizontal scollbar gets visible
  private class MyComponentAdapter extends ComponentAdapter
  {
    public void componentShown(ComponentEvent evt)
    {
      setTextAreaSize(2, true);
    }

    public void componentHidden(ComponentEvent evt)
    {
      setTextAreaSize(1, true);
    }

  }
  // ------------------ End of inner classes -----------------

  private final boolean propertyVerbose = false;
  private JDialog dlg;
  private Cleanable cleanable = null;
  private ExitListener exitListener = null;
  private JTextArea textArea;
  private JScrollPane scrollPane;
  private int lineHeight;
  private int charWidth;
  private int nbChars;
  private ClosingMode closingMode;
  private boolean isDisposed;

  /**
   * Show a MessagePane at given position (upper left corner) with
   * maximal number of characters to display.
   */
  public MessagePane(int ulx, int uly, int nbChars)
  {
    setup(ulx, uly, nbChars);
  }

  /**
   * Show a MessagePane at center of screen with given
   * maximal number of characters to display.
   */
  public MessagePane(int nbChars)
  {
    setup(-1, -1, nbChars);
    setText("");
  }

  /**
   * Show a MessagePane at given position (upper left corner) containing
   * given text.
   * The size of the window is adapted to the length of the text.
   */
  public MessagePane(int ulx, int uly, String text)
  {
    setup(uly, uly, text.length());
    setText(text);
  }

  /**
   * Show a MessagePane at center of screen containing the
   * given text.
   * The size of the window is adapted to the length of the text.
   */
  public MessagePane(String text)
  {
    setup(-1, -1, text.length());
    setText(text);
  }

  /**
   * Construct a MessagePane at given position (upper left corner) containing
   * given text and return after given amount of time (in milliseconds).
   * If millis < 0 the delay time is -millis, but setVisible(false) is
   * called before returning.
   * The size of the window is adapted to the length of the text.
   */
  public MessagePane(int ulx, int uly, String text, int millis)
  {
    this(ulx, uly, text);
    delay(Math.abs(millis));
    if (millis < 0)
      dlg.setVisible(false);
  }

  /**
   * Construct a MessagePane at center of screen containing
   * given text and return after given amount of time (in milliseconds).
   * If millis < 0 the delay time is -millis, but setVisible(false) is
   * called before returning.
   * The size of the window is adapted to the length of the text.
   */
  public MessagePane(String text, int millis)
  {
    this(text);
    delay(Math.abs(millis));
    if (millis < 0)
      dlg.setVisible(false);
  }

  private void init(int nbChars)
  {
    MyProperties props = new MyProperties(propertyVerbose);
    if (props.search())
      closingMode = getClosingMode(props);

    dlg = new JDialog()
    {
      protected void processWindowEvent(WindowEvent e)
      {
        if (e.getID() == WindowEvent.WINDOW_CLOSING)
        {
          if (cleanable != null)
            cleanable.clean();
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
                if (JOptionPane.showConfirmDialog(this,
                  "Terminating program. Are you sure?",
                  "Please confirm",
                  JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                  System.exit(0);
                break;
              case DisposeOnClose:
                dlg.setVisible(false);
                dlg.dispose();
                isDisposed = true;
                break;
              case NothingOnClose:
                break;
            }
          }
        }
        else
          super.processWindowEvent(e);
      }

    };
    isDisposed = false;
    textArea = new JTextArea();
    this.nbChars = nbChars;
    scrollPane = new JScrollPane(textArea,
      JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    JScrollBar horzBar = scrollPane.getHorizontalScrollBar();
    horzBar.addComponentListener(new MyComponentAdapter());
    dlg.getContentPane().add(scrollPane);
    dlg.setResizable(false);
    dlg.setTitle("MessagePane");
    Font font = new Font("Courier New", Font.PLAIN, 16);
    FontMetrics metrics = textArea.getFontMetrics(font);
    textArea.setFont(font);
    textArea.setEditable(false);
    textArea.setBackground(new Color(200, 228, 255));
    charWidth = metrics.charWidth('A');
    lineHeight = metrics.getHeight();
    setTextAreaSize(1, false);
  }

  private void setTextAreaSize(int nbLines, boolean keepWidth)
  {
    int scrollPaneWidth;
    int scrollPaneHeight = (int)((nbLines + 2.2) * lineHeight);
    if (keepWidth)
      scrollPaneWidth = scrollPane.getWidth();
    else
      scrollPaneWidth = (int)((nbChars + 0.5) * charWidth);
    scrollPane.setPreferredSize(new Dimension(scrollPaneWidth, scrollPaneHeight));
    dlg.pack();
  }

  private void setCenter()
  {
    int wWidth = dlg.getWidth();
    int wHeight = dlg.getHeight();
    Fullscreen fs = new Fullscreen();
    int ulx = (int)((fs.getWidth() - wWidth) / 2.0);
    int uly = (int)((fs.getHeight() - wHeight) / 2.0);
    dlg.setLocation(ulx, uly);
  }
 
  /**
   * Register a class that implements Cleanable.clean() that will be called when
   * the title bar's close button is hit. (Deprecated, use addExitListener())
   */
  public void addCleanable(Cleanable cl)
  {
    cleanable = cl;
  }

  /**
   * Register a class that implements ExitListener.notifyExit() that will be called when
   * the title bar's close button is hit. Replacement of addCleanable().
   */
  public void addExitListener(ExitListener listener)
  {
    exitListener = listener;
  }

  /**
   * Display the given text.
   * Swing methods are called from Event Dispatch Thread (EDT).
   */
  public void setText(String text)
  {
    if (isDisposed)
      return;

    if (SwingUtilities.isEventDispatchThread())
    {
      textArea.setText("\n" + text);  // Write on second line to get some free space
      textArea.setCaretPosition(0);
      if (!dlg.isVisible())  // When using timed pane
        dlg.setVisible(true);
    }
    else
    {
      try
      {
        SwingUtilities.invokeAndWait(new SetText("\n" + text)); // ditto
      }
      catch (Exception ex)
      {
      }
    }
  }

  /**
   * Display the given title.
   * Swing methods are called from Event Dispatch Thread (EDT).
   */
  public void title(String text)
  {
    if (isDisposed)
      return;

    if (SwingUtilities.isEventDispatchThread())
    {
      dlg.setTitle(text);
      if (!dlg.isVisible())  // When using timed pane
        dlg.setVisible(true);
    }
    else
    {
      try
      {
        SwingUtilities.invokeAndWait(new Title(text));
      }
      catch (Exception ex)
      {
      }
    }
  }

  private void setup(int ulx, int uly, int nbChars)
  {
    if (SwingUtilities.isEventDispatchThread())
    {
      init(nbChars);
      if (ulx == -1)
        setCenter();
      else
        dlg.setLocation(ulx, uly);
      dlg.setVisible(true);
    }
    else
    {
      try
      {
        SwingUtilities.invokeAndWait(new Init(ulx, uly, nbChars));
      }
      catch (Exception ex)
      {
      }
    }
  }

  /**
   * Display the given text and return after the given amount of time (in milliseconds).
   * If millis < 0 the delay time is -millis, but setVisible(false) is
   * called before returning. The next invocation of setText() redisplays the MessagePane.
   */
  public void setText(String text, int millis)
  {
    if (isDisposed)
      return;

    setText(text);
    delay(Math.abs(millis));
    if (millis < 0)
      dlg.setVisible(false);
  }

  private void delay(int millis)
  {
    try
    {
      Thread.currentThread().sleep(millis);
    }
    catch (InterruptedException ex)
    {
    }
  }

  /**
   * Return the dialog.
   */
  public JDialog getDialog()
  {
    return dlg;
  }

  /**
   * Show/hide the dialog.
   * @param visible if true, the dialog is shown, otherwise hidden.
   */
  public void setVisible(final boolean visible)
  {
    if (isDisposed)
      return;

    if (EventQueue.isDispatchThread())
      dlg.setVisible(visible);
    else
    {
      try
      {
        EventQueue.invokeAndWait(new Runnable()
        {
          public void run()
          {
            dlg.setVisible(visible);
          }

        });
      }
      catch (Exception ex)
      {
      }
    }
  }

  /**
   * If in aplu_util.properties the MessagePaneClosingMode key is set to
   * DisposeOnClose, the dialog is disposed when the close 
   * title button is clicked and this method returns true
   */
  public boolean isDisposed()
  {
    return isDisposed;
  }

  private ClosingMode getClosingMode(MyProperties props)
  {
    String value = props.getStringValue("MessagePaneClosingMode");
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

}
