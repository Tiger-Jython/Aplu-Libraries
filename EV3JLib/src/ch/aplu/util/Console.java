// Console.java
// J2SE V5 up only

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

import java.io.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.print.*;
import javax.swing.text.*;
import javax.swing.JOptionPane;

/**
 * Console window for line oriented input and output.<br><br>
 * Once the console window is instantiated all output to stdout and stderr is
 * redirected to this window. Only one console instance is allowed.<br>
 * To avoid creating an object, the static factory method init() may be used.<br>
 * Only 7-bit-ASCII characters are supported.<br><br>
 *
 * All Swing GUI methods are invoked from the Event Dispatch Thread (EDT)
 * if it's not done by the caller.<br><br>
 * 
 * Default properties of the console window can
 * be changed from the library defaults by using a 
 * Java properties file aplu_util.properties. For more details,
 * consult aplu_util.properties found in the distribution.<br><br>
 * 
 * When the close button of the console window title bar is hit, System.exit(0)
 * is executed that terminates the JVM, but you can modifiy this behavior 
 * by registering your own implementation of the ExitListener interface or 
 * by using the key ConsoleClosingMode in aplu_util.properties.<br><br>
 *
 * Part of code from  Rjhm van den Bergh (rvdb@comweb.nl)
 * with thanks for the permission to use and distribute
 */
public class Console implements Runnable, Printable
{
  public static enum ClosingMode
  {
    /** 
     * Terminating and shutting down JRE  by System.exit(0)
     */
    TerminateOnClose,
    /** 
     * ClearOnClose -> Clears the console content<br>
     */
    ClearOnClose,
    /** 
     * HideOnClose -> Hides the console, may be shown again by calling show()
     */
    HideOnClose,
    /** 
     * AskOnClose -> Shows confirmation dialog asking for termination<br>
     */
    AskOnClose,
    /** 
     * DisposeOnClose -> Closes the console window, but does not shutdown JRE<br>
     */
    DisposeOnClose,
    /** 
     * ReleaseOnClose -> Like DisposeOnClose, but throws runtime exception 
     * when graphics methods are called<br>
     */
    ReleaseOnClose,
    /** 
     * NothingOnClose -> Does nothing<br>
     */
    NothingOnClose
  }

// ------------------------ Internal classes -------------------
  private class MyWindowAdapter extends WindowAdapter
  {
    public synchronized void windowClosed(WindowEvent evt)
    {
      _quit = true;
      notifyAll();     // Take all threads out of wait state
      try
      {
        _reader1.join(1000);
        _pin1.close();
      }
      catch (Exception e)
      {
      }
      try
      {
        _reader2.join(1000);
        _pin2.close();
      }
      catch (Exception e)
      {
      }
      if (_closingMode == ClosingMode.TerminateOnClose
        || _closingMode == ClosingMode.AskOnClose)
        System.exit(0);
    }

    public synchronized void windowClosing(WindowEvent evt)
    {
      if (_exitListener != null)
      {
        _exitListener.notifyExit();
        return;
      }
      switch (_closingMode)
      {
        case TerminateOnClose:
          end();
          break;
        case HideOnClose:
          clear();
          hide();
          break;
        case ClearOnClose:
          clear();
          break;
        case AskOnClose:
          if (JOptionPane.showConfirmDialog(_frame,
            "Terminating program. Are you sure?",
            "Please confirm",
            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
          {
            _frame.setVisible(false);
            _frame.dispose();
          }
          break;
        case ReleaseOnClose:
          _isDisposed = true;
          hide();
          clear();
          Monitor.wakeUp(monitor);  // wakeup waiting key events
          break;
        case DisposeOnClose:
          _isDisposed = true;
          hide();
          clear();
          end();
          break;
      }
    }
  }

  private class MyFocusAdapter implements FocusListener
  {
    public void focusGained(FocusEvent evt)
    {
      setCaretPosition(_caretPosition);
    }

    public void focusLost(FocusEvent evt)
    {
      _caretPosition = _textArea.getCaretPosition();  // Called from EDT
    }
  }

  private class MyKeyAdapter implements KeyListener
  {
    public void keyPressed(KeyEvent evt)
    {
      _gotKey = true;
      _keyCode = evt.getKeyCode();
      _keyChar = evt.getKeyChar();
      _modifiers = evt.getModifiers();
      _modifiersText = KeyEvent.getKeyModifiersText(_modifiers);
      Monitor.wakeUp(monitor);
    }

    public void keyReleased(KeyEvent evt)
    {
    }

    public void keyTyped(KeyEvent evt)
    {
    }
  }

  private class MyActionAdapter implements ActionListener
  {
    // ActionListener for button 'clear'. Called by EDT, so Swing calls allowed
    public synchronized void actionPerformed(ActionEvent evt)
    {
      _textArea.setText("");
      setCaretPosition(0);
      _charCount = 0;
      _textArea.requestFocus();
    }
  }

  static class InsertThread extends Thread
  {
    private String _msg;
    private int _charCount;

    InsertThread(String msg)
    {
      _msg = msg;
      _charCount = -1;
    }

    InsertThread(String msg, int charCount)
    {
      _msg = msg;
      _charCount = charCount;
    }

    public void run()
    {
      if (_charCount == -1)
        _textArea.setText(_msg);
      else
        _textArea.insert(_msg, _charCount);
    }
  }

  static class GetTextThread extends Thread
  {
    public void run()
    {
      _textAreaText = _textArea.getText();
    }
  }

  static class CaretThread extends Thread
  {
    private int _pos;

    CaretThread(int pos)
    {
      _pos = pos;
    }

    public void run()
    {
      _caret.setVisible(false);
      _textArea.setCaretPosition(_pos);
      _caretPosition = _pos;
      _caret.setVisible(true);
    }
  }

  static class OptionThread extends Thread
  {
    private String _title;
    private Position _position;
    private Size _size;
    private Font _font;

    OptionThread(String title, Position position, Size size, Font font)
    {
      _title = title;
      _position = position;
      _size = size;
      _font = font;
    }

    public void run()
    {
      if (_title != null)
        _frame.setTitle(_title);
      if (_position != null)
        _frame.setLocation(_position.getUlx(), _position.getUlx());
      if (_size != null)
        _frame.setSize(_size._width, _size._height);
      if (_font != null)
        _textArea.setFont(_font);
    }
  }

  static class ShowThread extends Thread
  {
    public void run()
    {
      _frame.setVisible(true);
    }
  }

  static class HideThread extends Thread
  {
    public void run()
    {
      _frame.setVisible(false);
    }
  }

  static class ScrollBarThread extends Thread
  {
    private boolean _b;
    private boolean _horizontal;

    ScrollBarThread(boolean b, boolean horizontal)
    {
      _b = b;
      _horizontal = horizontal;
    }

    public void run()
    {
      if (_horizontal)
      {
        if (_b)
          _scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        else
          _scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        _textArea.revalidate();
      }
      else
      {
        if (_b)
          _scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        else
          _scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        _textArea.revalidate();
      }
    }
  }
// ------------------------ End of internal classes -------------------
// 
  private final static boolean propertyVerbose = false;
  private static PrintStream _ps;
  private static PrintStream _oldPs1;
  private static PrintStream _oldPs2;
  private static Console _console;    // Needed because of several static methods
  private static int _instanceCount = 0;
  private static JFrame _frame;
  private static JTextArea _textArea;
  private static String _textAreaText;
  private static JScrollPane _scrollPane;
  private static Caret _caret;
  private static int _caretPosition = 0;
  private static Thread _reader1;
  private static Thread _reader2;
  private static boolean _quit;
  private static boolean _gotKey = false;
  private static char _keyChar = KeyEvent.CHAR_UNDEFINED;
  private static int _keyCode = 0;
  private static int _modifiers = 0;
  private static String _modifiersText = "";
  private static int _charCount = 0;
  private static PipedInputStream _pin1 = new PipedInputStream();
  private static PipedInputStream _pin2 = new PipedInputStream();
  private static double _scale;
  private static boolean _isVisible = true;
  private static boolean _isRedirectToFile = false;
  private static String _filename = "";
  private static ExitListener _exitListener = null;
  private static Object monitor = new Object();
  private static String _consoleTitle = null;
  private static Point _consolePos = null;
  private static Dimension _consoleSize = null;
  private static String _consoleFont = null;
  private static ClosingMode _closingMode = null;
  private static boolean _isDisposed = false;

  /**
   * Construct a Console with attributes. Runs in Event Dispatch Thread (EDT).
   * @param position   a reference to a Position object
   * @see ch.aplu.util.Position
   * @param size a reference to a Size object
   * @see ch.aplu.util.Size
   * @param font a reference to a Font object
   * @see java.awt.Font
   */
  public Console(final Position position, final Size size, final Font font)     // Ctor
  {
    if (EventQueue.isDispatchThread())
      createConsole(position, size, font);
    else
    {
      try
      {
        EventQueue.invokeAndWait(new Runnable()
        {
          public void run()
          {
            createConsole(position, size, font);
          }
        });
      }
      catch (Exception ex)
      {
      }
    }
  }

  private void createConsole(Position position, Size size, Font font)
  {
    synchronized (this)  // If two threads try to construct at same time
    {
      if (_instanceCount == 0)
        _instanceCount = 1;
      else
      {
        JOptionPane.showMessageDialog(null,
          "Only one instance of Console allowed.");
        System.exit(1);
      }
    }

    MyProperties props = new MyProperties(Console.propertyVerbose);
    if (props.search())
    {
      _consoleTitle = props.getStringValue("ConsoleTitle");
      if (_consoleTitle != null)
        _consoleTitle = _consoleTitle.trim();

      _closingMode = getClosingMode(props);

      int[] values = props.getIntArray("ConsolePosition", 2);
      if (values != null && values[0] >= 0 && values[1] >= 0)
        _consolePos = new Point(values[0], values[1]);

      values = props.getIntArray("ConsoleSize", 2);
      if (values != null && values[0] > 0 && values[1] > 0)
        _consoleSize = new Dimension(values[0], values[1]);

      _consoleFont = props.getStringValue("ConsoleFont");
      if (_consoleFont != null)
        _consoleFont = _consoleFont.trim();
    }

    // Create all components and add them
    if (_consoleTitle == null)
      _frame = new JFrame("Java Input/Output Console");
    else
      _frame = new JFrame(_consoleTitle);
    _frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = null;
    int ulx = 0;
    int uly = 0;

    if (size == null)
    {
      if (_consoleSize == null)
      {
        frameSize = new Dimension((int)(screenSize.width / 2),
          (int)(screenSize.height / 2));
        if (position == null)
        {
          ulx = (int)(frameSize.width / 2);
          uly = (int)(frameSize.height / 2);
        }
        else
        {
          ulx = position.getUlx();
          uly = position.getUly();
        }
      }
      else
      {
        frameSize = new Dimension(_consoleSize.width, _consoleSize.height);
        if (_consolePos == null)
        {
          if (position == null)
          {
            ulx = (int)(frameSize.width / 2);
            uly = (int)(frameSize.height / 2);
          }
          else
          {
            ulx = position.getUlx();
            uly = position.getUly();
          }
        }
        else
        {
          if (position == null)
          {
            ulx = _consolePos.x;
            uly = _consolePos.y;
          }
          else
          {
            ulx = position.getUlx();
            uly = position.getUly();
          }
        }
      }
    }
    else // size provided
    {
      frameSize = new Dimension(size.getWidth(), size.getHeight());
      if (position == null)
      {
        if (_consolePos == null)
        {
          ulx = (int)(frameSize.width / 2);
          uly = (int)(frameSize.height / 2);
        }
        else
        {
          ulx = _consolePos.x;
          uly = _consolePos.y;
        }
      }
      else
      {
        ulx = position.getUlx();
        uly = position.getUly();
      }
    }

    _frame.setBounds(ulx, uly, frameSize.width, frameSize.height);

    _textArea = new JTextArea();
    _textArea.setEditable(false);
    _caret = _textArea.getCaret();
    _caret.setVisible(false);

    if (font == null)
    {
      if (_consoleFont == null)
        _textArea.setFont(new Font("Courier New", Font.PLAIN, 16));
      else
        _textArea.setFont(Font.decode(_consoleFont));
    }
    else
      _textArea.setFont(font);
    JButton button = new JButton("Clear");

    _frame.getContentPane().setLayout(new BorderLayout());
    _scrollPane = new JScrollPane(_textArea,
      JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    _frame.getContentPane().add(_scrollPane, BorderLayout.CENTER);
    _frame.getContentPane().add(button, BorderLayout.SOUTH);

    _frame.addWindowListener(new MyWindowAdapter());
    _textArea.addFocusListener(new MyFocusAdapter());
    _textArea.addKeyListener(new MyKeyAdapter());
    button.addActionListener(new MyActionAdapter());

    try
    {
      if (_isRedirectToFile)
      {
        _ps = new java.io.PrintStream(
          new java.io.FileOutputStream(
            new java.io.File(_filename), true));
      }
      else
      {
        PipedOutputStream pout1 = new PipedOutputStream(_pin1);
        _ps = new PrintStream(pout1, true);
      }
      _oldPs1 = new PrintStream(System.out);
      System.setOut(_ps);
    }
    catch (java.io.IOException io)
    {
      String msg = "Couldn't redirect STDOUT\n"
        + io.getMessage();
      JOptionPane.showMessageDialog(null, msg);
    }
    catch (SecurityException se)
    {
      String msg = "Couldn't redirect STDOUT\n"
        + se.getMessage();
      JOptionPane.showMessageDialog(null, msg);
    }

    if (_closingMode != ClosingMode.ReleaseOnClose)
    {
      PrintStream ps;
      try
      {
        if (_isRedirectToFile)
        {
          ps = new java.io.PrintStream(
            new java.io.FileOutputStream(
              new java.io.File(_filename), true));
        }
        else
        {
          PipedOutputStream pout2 = new PipedOutputStream(_pin2);
          ps = new PrintStream(pout2, true);
        }
        _oldPs2 = new PrintStream(System.err);
        System.setErr(ps);
      }
      catch (java.io.IOException io)
      {
        String msg = "Couldn't redirect STDERR to this console\n"
          + io.getMessage();
        _textArea.insert(msg, _charCount);
        _charCount += msg.length();
      }
      catch (SecurityException se)
      {
        String msg = "Couldn't redirect STDERR to this console\n"
          + se.getMessage();
        _textArea.insert(msg, _charCount);
        _charCount += msg.length();
      }
    }

    _quit = false;    // signals the Threads that they should exit

    // Starting two separate threads to read from the PipedInputStreams
    // First
    _reader1 = new Thread(this);
    _reader1.setDaemon(true);
    _reader1.start();
    // Second
    _reader2 = new Thread(this);
    _reader2.setDaemon(true);
    _reader2.start();

    _frame.setVisible(_isVisible);
  }

  /**
   * Construct a Console with default attributes ( see init() ).
   */
  public Console()
  {
    this(null, null, null);
  }

  private static void setDefaults()
  {
    if (_console == null || _frame == null)
      return;

    if (_consoleTitle == null)
      _console.setTitle("Java Input/Output Console");
    else
      _console.setTitle(_consoleTitle);

    if (_consoleSize == null || _consolePos == null)
    {
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

      Dimension frameSize = new Dimension((int)(screenSize.width / 2),
        (int)(screenSize.height / 2));
      int ulx = (int)(frameSize.width / 2);
      int uly = (int)(frameSize.height / 2);
      _frame.setBounds(ulx, uly,
        frameSize.width, frameSize.height);
    }
    else
    {
      _frame.setBounds(_consolePos.x, _consolePos.y,
        _consoleSize.width, _consoleSize.height);
    }

    if (_consoleFont == null)
      _textArea.setFont(new Font("Courier New", Font.PLAIN, 16));
    else
      _textArea.setFont(Font.decode(_consoleFont));

    _console.clear();
  }

  /**
   * If no console was created before, create a new Console instance 
   * with default attributes and returns a reference to it; otherwise
   * return the existing Console reference and show the exiting console
   * window if it was hidden (content is erased).
   * Default size: half the screen dimensions<br>
   * Default position: centered to screen<br>
   * Default font: Font( "Courier New", Font.PLAIN, 16 )
   */
  public static Console init()
  {
    _isDisposed = false;
    if (_console == null)
      _console = new Console(null, null, null);
    else
    {
      setDefaults();
      _console.show();
    }
    return _console;
  }

  /**
   * Same as init() but enable autowrapping.
   */
  public static Console initw()
  {
    Console cs = init();
    setAutowrap(true);
    return cs;
  }

  /**
   * If no console was created before, create a new Console instance 
   * with given position and size and returns a reference to it; otherwise
   * return the existing Console reference and show the exiting console window
   * with new position and size.
   * @param position a reference to a Position object
   * @see ch.aplu.util.Position
   * @param size a reference to a Size object
   * @see ch.aplu.util.Size
   */
  public static Console init(Position position, Size size)
  {
    _isDisposed = false;
    if (_console == null)
      _console = new Console(position, size, null);
    else
    {
      setDefaults();
      if (SwingUtilities.isEventDispatchThread())
        _frame.setBounds(position.getUlx(), position.getUly(), size._width, size._width);
      else
      {
        try
        {
          SwingUtilities.invokeAndWait(new OptionThread(null, position, size, null));
        }
        catch (Exception ex)
        {
        }
      }
      _console.show();
    }
    return _console;
  }

  /**
   * Same as init(Position position, Size size), but enable autowrapping.
   */
  public static Console initw(Position position, Size size)
  {
    Console cs = init(position, size);
    setAutowrap(true);
    return cs;
  }

  /**
   * If no console was created before, create a new Console instance 
   * with given font and returns a reference to it; otherwise
   * return the existing Console reference and use the given font.
   * @param font   a reference to a Font object
   * @see java.awt.Font 
   */
  public static Console init(Font font)
  {
    _isDisposed = false;
    if (_console == null)
      _console = new Console(null, null, font);
    else
    {
      setDefaults();
      if (SwingUtilities.isEventDispatchThread())
      {
        _textArea.setFont(font);
      }
      else
      {
        try
        {
          SwingUtilities.invokeAndWait(new OptionThread(null, null, null, font));
        }
        catch (Exception ex)
        {
        }
      }
      _console.show();
    }
    return _console;
  }

  /**
   * Same as init(Font font) but enable autowrapping.
   */
  public static Console initw(Font font)
  {
    Console cs = init(font);
    setAutowrap(true);
    return cs;
  }

  /**
   * If no console was created before, create a new Console instance 
   * with given position, size, font and returns a reference to it; otherwise
   * return the existing Console reference and use the position, size and font.
   * @param position   a reference to a Position object, null to keep default
   * @see ch.aplu.util.Position
   * @param size a reference to a Size object
   * @see ch.aplu.util.Size
   * @param font a reference to a Font object
   * @see java.awt.Font
   */
  public static Console init(Position position, Size size, Font font)
  {
    _isDisposed = false;
    if (_console == null)
      _console = new Console(position, size, font);
    else
    {
      setDefaults();
      if (SwingUtilities.isEventDispatchThread())
      {
        _frame.setBounds(position.getUlx(), position.getUly(), size._width, size._width);
        _textArea.setFont(font);
      }
      else
      {
        try
        {
          SwingUtilities.invokeAndWait(new OptionThread(null, position, size, font));
        }
        catch (Exception ex)
        {
        }
      }
      _console.show();
    }
    return _console;
  }

  /**
   * Same as init(Position position, Size size, Font font) but enable autowrapping.
   */
  public static Console initw(Position position, Size size, Font font)
  {
    Console cs = init(position, size, font);
    setAutowrap(true);
    return cs;
  }

  /**
   * Redirect all output to stdout and stderr to a text file
   * with the given filename.
   * If the file exists the text is appended, otherwise the
   * file is created.
   * (No console window is shown, so no input is possible.)
   * If null is given, all output to stdout and stderr is
   * ignored. This may be used to hide output from library functions.
   */
  public static Console init(String filename)
  {
    _filename = filename;
    if (filename != null)
      _isRedirectToFile = true;
    _isVisible = false;
    _console = new Console();
    return _console;
  }

  /**
   * Hide the console window and releases resources. Redirect System.out
   * to stdout.
   * (The Console reference should not be used any more.)
   */
  public static void end()
  {
    if (_instanceCount == 0)
      return;

    _quit = true;
    System.setOut(_oldPs1);
    try
    {
      _console._reader1.join(1000);
      _console._pin1.close();
    }
    catch (Exception e)
    {
    }

    if (_closingMode != ClosingMode.ReleaseOnClose)
    {
      System.setErr(_oldPs2);
      try
      {
        _console._reader2.join(1000);
        _console._pin2.close();
      }
      catch (Exception e)
      {
      }
    }

    if (EventQueue.isDispatchThread())
      _frame.dispose();
    else
    {
      try
      {
        EventQueue.invokeAndWait(new Runnable()
        {
          public void run()
          {
            _frame.dispose();
          }
        });
      }
      catch (Exception ex)
      {
      }
    }
  }

  /**
   * Erase all text in console window
   */
  public static void clear()
  {
    if (_instanceCount == 0)
      return;

    if (SwingUtilities.isEventDispatchThread())
    {
      _textArea.setText("");
    }
    else
    {
      try
      {
        SwingUtilities.invokeAndWait(new InsertThread("", -1));
      }
      catch (Exception ex)
      {
      }
    }
    setCaretPosition(0);
    _charCount = 0;
    _gotKey = false;
  }

  /**
   * Return a Position reference with specified upperleft x and y coordinates.
   * May be used in init() to avoid the keyword <code>new</code>
   */
  public static Position position(int ulx, int uly)
  {
    Position p = new Position(ulx, uly);
    return p;
  }

  /**
   * Return a Size reference with specified width and height.
   * May be used in init() to avoid the keyword new
   */
  public static Size size(int width, int height)
  {
    Size s = new Size(width, height);
    return s;
  }

  /**
   * Register an ExitListener to get a notification when the close button is clicked.
   * After registering the automatic termination of the application is disabled.
   * To close the Console window without termination, Console.end() should be called.
   * To terminate the application, System.exit() should be called.
   */
  public static void addExitListener(ExitListener exitListener)
  {
    _exitListener = exitListener;
  }

  /**
   * For internal use only.
   */
  public synchronized void run()
  {
    try
    {
      while (Thread.currentThread() == _reader1)
      {
        try
        {
          wait(100);
        }
        catch (InterruptedException ie)
        {
        }

        if (_pin1.available() != 0)
        {
          String input = pushLine(_pin1);
//          int offsetStartOfLine = input.indexOf('\n');

          try
          {
            SwingUtilities.invokeAndWait(new InsertThread(input, _charCount));
          }
          catch (Exception ex)
          {
          }
//          _textArea.insert(input, _charCount);
          _charCount += input.length();
//          System.err.println(offsetStartOfLine);
          setCaretPosition(_charCount);  // Not from EDT
        }
        if (_quit)
          return;
      }

      while (Thread.currentThread() == _reader2)
      {
        try
        {
          wait(100);
        }
        catch (InterruptedException ie)
        {
        }

        if (_pin2.available() != 0)
        {
          String input = pushLine(_pin2);
          try
          {
            SwingUtilities.invokeAndWait(new InsertThread(input, _charCount));
          }
          catch (Exception ex)
          {
          }
//          _textArea.insert(input, _charCount);
          _charCount += input.length();
          setCaretPosition(_charCount);
        }
        if (_quit)
          return;
      }
    }
    catch (Exception e)
    {
      String msg = "\nConsole reports an internal error: " + e.getMessage();
      try
      {
        SwingUtilities.invokeAndWait(new InsertThread(msg, _charCount));
      }
      catch (Exception ex)
      {
      }
//      _textArea.insert(msg, _charCount);
      _charCount += msg.length();
    }
  }

  private static void setCaretPosition(int pos)
  {
    if (SwingUtilities.isEventDispatchThread())
    {
      _caret.setVisible(false);
      _textArea.setCaretPosition(pos);
      _caretPosition = pos;
      _caret.setVisible(true);
    }
    else
    {
      try
      {
        SwingUtilities.invokeAndWait(new CaretThread(pos));
      }
      catch (Exception ex)
      {
      }
    }
  }

  /**
   * Show the console window.
   */
  public static void show()
  {
    if (SwingUtilities.isEventDispatchThread())
    {
      _frame.setVisible(true);
    }
    else
    {
      try
      {
        SwingUtilities.invokeAndWait(new ShowThread());
      }
      catch (Exception ex)
      {
      }
    }
  }

  /**
   * Hide the console window.
   */
  public static void hide()
  {
    if (SwingUtilities.isEventDispatchThread())
    {
      _frame.setVisible(false);
    }
    else
    {
      try
      {
        SwingUtilities.invokeAndWait(new HideThread());
      }
      catch (Exception ex)
      {
      }
    }
  }

  private synchronized String pushLine(PipedInputStream pin) throws IOException
  {
    String input = "";
    do
    {
      int available = pin.available();
      if (available == 0)
        break;
      byte b[] = new byte[available];
      pin.read(b);
      for (int i = 0; i < b.length; i++)
      {
        if ((b[i] >= 32 && b[i] < 127) || b[i] == '\n' || b[i] == '\t')
          input += (char)b[i];
      }
    }
    while (!input.endsWith("\n") && !input.endsWith("\r\n") && !_quit);
    return input;
  }

  /**
   * Return true if a key was hit since the last time the one-character buffer
   * was read with getKey() oder getKeyWait(). The one-character buffer
   * is not modified.
   * (Put the current thread to sleep for 1 ms to reduce CPU time when
   * used in an iteration loop.)
   */
  public static boolean kbhit()
  {
    checkInstance("kbhit()");
    delay(1);
    return _gotKey;
  }

  /**
   * Return the unicode character associated with last key pressed and flush the
   * one-charactor buffer.
   * Return KeyEvent.CHAR_UNDEFINED if the one-character buffer is empty.
   * (No echo in the console window.)
   */
  public static char getKey()
  {
    checkInstance("getKey()");

    if (_gotKey)
    {
      _gotKey = false;
      return _keyChar;
    }
    else
      return KeyEvent.CHAR_UNDEFINED;
  }

  /**
   * Return the keycode associated with last key pressed and
   * flush the one-character buffer.
   * Return KeyEvent.CHAR_UNDEFINED if the one-character buffer is empty.
   * (No echo in the console window.)
   */
  public static int getKeyCode()
  {
    checkInstance("getKeyCode()");

    if (_gotKey)
    {
      _gotKey = false;
      return _keyCode;
    }
    else
      return KeyEvent.CHAR_UNDEFINED;
  }

  /**
   * Wait until a key is typed and
   * return the unicode character associated with last key pressed. Flush the
   * one-character buffer.
   * (No echo in the console window.)
   */
  public static char getKeyWait()
  {
    checkDisposed();
    checkInstance("getKeyWait()");
    Monitor.putSleep(monitor);
    checkDisposed();
    return getKey();
  }

  /**
   * Wait until a key is typed and
   * return the keycode associated with last key pressed. Flush the
   * one-character buffer.
   * (No echo in the console window.)
   */
  public static int getKeyCodeWait()
  {
    checkDisposed();
    checkInstance("getKeyCodeWait()");
    Monitor.putSleep(monitor);
    checkDisposed();
    return getKeyCode();
  }

  /**
   * Wait until a key is typed and
   * return the unicode character associated it.
   * (Echo the character in the console window.)
   */
  public static char readChar()
  {
    checkInstance("readChar()");

    Monitor.putSleep(monitor);
    char ch = getKey();
    if (SwingUtilities.isEventDispatchThread())
    {
      _textArea.insert(Character.toString(ch), _charCount);
    }
    else
    {
      try
      {
        SwingUtilities.invokeAndWait(
          new InsertThread(Character.toString(ch), _charCount));
      }
      catch (Exception ex)
      {
      }
    }
    _charCount++;
    return ch;
  }

  /**
   * Wait until a sequence of characters with trailing newline is typed.
   * Return the string (without the trailing newline)
   * (Echo the characters in the console wiindow.)
   */
  public static String readLine()
  {
    checkInstance("readLine()");

    char ch = ' ';
    String s = "";

    while (ch != '\n')
    {
      ch = getKeyWait();

      if (_keyCode == '\b')    // Delete Key
      {
        if (s.length() > 0)
        {
          if (SwingUtilities.isEventDispatchThread())
          {
            _textAreaText = _textArea.getText();
            _textArea.setText(_textAreaText.substring(0, _textAreaText.length() - 1));
          }
          else
          {
            try
            {
              SwingUtilities.invokeAndWait(new GetTextThread());  // Return text in _textAreaText
              SwingUtilities.invokeAndWait(new InsertThread(
                _textAreaText.substring(0, _textAreaText.length() - 1), -1));
            }
            catch (Exception ex)
            {
            }
          }
          _charCount--;
          setCaretPosition(_charCount);
          s = s.substring(0, s.length() - 1);
        }
      }
      else
      {
        if (Character.isDefined(ch)
          && getLastModifiers() < 2
          && !(getLastModifiers() == 1 && getLastKeyCode() == 16)) // Shift key alone
        {
          if (SwingUtilities.isEventDispatchThread())
          {
            _textArea.insert(Character.toString(ch), _charCount);
          }
          else
          {
            try
            {
              SwingUtilities.invokeAndWait(
                new InsertThread(Character.toString(ch), _charCount));
            }
            catch (Exception ex)
            {
            }
          }
          _charCount++;
          setCaretPosition(_charCount);
          if (ch != '\n')
            s = s + Character.toString(ch);
        }
      }
    }
    return s;
  }

  /**
   *  Return the key character associated with last key pressed. Do not flush
   *  the one-charater buffer. Return KeyEvent.CHAR_UNDEFINED, if no key was pressed.
   */
  public static char getLastKey()
  {
    return _keyChar;
  }

  /**
   *  Return the key code associated with last key pressed. Do not flush
   *  the one-character buffer. Return 0, if no key was pressed.
   */
  public static int getLastKeyCode()
  {
    return _keyCode;
  }

  /**
   *  Return the modifiers associated with last key pressed. Do not flush
   *  the one-character buffer. Return 0, if no key was pressed.
   */
  public static int getLastModifiers()
  {
    return _modifiers;
  }

  /**
   *  Return the modifiers text associated with last key pressed. Do not flush
   *  the one-character buffer. Return 0, if no key was pressed.  Return empty
   *  string, if no key was pressed.
   */
  public static String getLastModifiersText()
  {
    return _modifiersText;
  }

  /**
   * Wait until a sequence of numbers with trailing newline is typed.
   * Return the converted integer (if possible)
   * (Echo the character in the console window.)
   */
  public static int readInt()
  {
    checkInstance("readInt()");
    return Integer.parseInt(readLine());
  }

  /**
   * Same as readInt() but returns an Integer object.
   * Return null, if entered character sequence cannot converted to an integer.
   */
  public static Integer getInt()
  {
    checkInstance("getInt()");
    Integer value;
    try
    {
      value = new Integer(readLine());
    }
    catch (NumberFormatException e)
    {
      return null;
    }
    return value;
  }

  /**
   * Wait until a sequence of numbers with trailing newline is typed.
   * Return the converted double (if possible)
   * (Echo the character in the console window.)
   */
  public static double readDouble()
  {
    checkInstance("getDouble()");
    return Double.parseDouble(readLine());
  }

  /**
   * Same as readDouble() but returns a Double object.
   * Return null, if entered character sequence cannot converted to a double.
   */
  static public Double getDouble()
  {
    checkInstance("getDouble()");
    Double value;
    try
    {
      value = new Double(readLine());
    }
    catch (NumberFormatException e)
    {
      return null;
    }
    return value;
  }

  /**
   * Terminate application.
   */
  public static void terminate()
  {
    checkInstance("terminate()");
    System.exit(0);
  }

  private static void checkInstance(String methodName)
  {
    if (_instanceCount == 0)
    {
      JOptionPane.showMessageDialog(null,
        "Error when calling " + methodName
        + "\nNo instance of Console found");
      System.exit(1);
    }
  }

  /**
   * Show all available fonts.
   */
  public static void showFonts()
  {
    System.out.println("All fonts available to Graphic2D:\n");
    GraphicsEnvironment ge
      = GraphicsEnvironment.getLocalGraphicsEnvironment();
    String[] fontNames = ge.getAvailableFontFamilyNames();
    for (int n = 0; n < fontNames.length; n++)
      System.out.println(fontNames[n]);
  }

  /**
   * Delay execution for the given amount of time ( in ms ).
   */
  public static void delay(int time)
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
   * Right justify the given number in a field with the given field width
   * (pad the field with leading spaces).
   * Return the padded string (if possible)
   */
  public static String pad(String num, int fieldWidth)
  {
    if (fieldWidth <= 0) // Error
      return num;

    int leadingSpaces;
    leadingSpaces = fieldWidth - num.length();

    if (leadingSpaces < 0) // Error
      return num;

    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < leadingSpaces; i++)
      buf.append(' ');
    buf.append(num);
    return new String(buf);
  }

  /**
   * Pad given number with trailing spaces to optain decimal width and
   * right justify in a field with the given width
   * (pad the the field with leading spaces).
   * Return the padded string (if possible)
   */
  public static String pad(String num, int fieldWidth, int decimalWidth)
  {
    if (fieldWidth <= 0) // Error
      return num;

    int leadingDecimals = num.indexOf('.');
    int trailingDecimals;
    int trailingSpaces;
    if (leadingDecimals == -1)  // No decimal point
    {
      trailingDecimals = 0;
      trailingSpaces = decimalWidth + 1;
    }
    else
    {
      trailingDecimals = num.length() - leadingDecimals - 1;
      trailingSpaces = decimalWidth - trailingDecimals;
      if (trailingSpaces < 0)  // Error
        return num;
    }

    StringBuffer buf = new StringBuffer(num);
    for (int i = 0; i < trailingSpaces; i++)
      buf.append(' ');

    return pad(buf.toString(), fieldWidth);
  }

  /**
   * Return a reference to the JTextArea of the console window
   */
  public static JTextArea getTextArea()
  {
    checkInstance("getTextArea()");
    return _textArea;
  }

  /**
   * Print a boolean value.
   * Return a reference to Console to concatenate print methods.
   */
  public static Console print(boolean b)
  {
    if (_instanceCount == 0)
      init();
    _ps.print(b);
    return _console;
  }

  /**
   * Print a character.
   * Return a reference to Console to concatenate print methods.
   */
  public static Console print(char c)
  {
    if (_instanceCount == 0)
      init();
    _ps.print(c);
    return _console;
  }

  /**
   * Print an array of characters.
   * Return a reference to Console to concatenate print methods.
   */
  public static Console print(char[] s)
  {
    if (_instanceCount == 0)
      init();
    _ps.print(s);
    return _console;
  }

  /**
   * Print a double-precision floating-point number.
   * Return a reference to Console to concatenate print methods.
   */
  public static Console print(double d)
  {
    if (_instanceCount == 0)
      init();
    _ps.print(d);
    return _console;
  }

  /**
   * Print a floating-point number.
   * Return a reference to Console to concatenate print methods.
   */
  public static Console print(float f)
  {
    if (_instanceCount == 0)
      init();
    _ps.print(f);
    return _console;
  }

  /**
   * Print an integer.
   * Return a reference to Console to concatenate print methods.
   */
  public static Console print(int i)
  {
    if (_instanceCount == 0)
      init();
    _ps.print(i);
    return _console;
  }

  /**
   * Print a long integer.
   * Return a reference to Console to concatenate print methods.
   */
  public static Console print(long l)
  {
    if (_instanceCount == 0)
      init();
    _ps.print(l);
    return _console;
  }

  /**
   * Print an object.
   * Return a reference to Console to concatenate print methods.
   */
  public static Console print(Object obj)
  {
    if (_instanceCount == 0)
      init();
    _ps.print(obj);
    return _console;
  }

  /**
   * Print a string.
   * Return a reference to Console to concatenate print methods.
   */
  public static Console print(String s)
  {
    if (_instanceCount == 0)
      init();
    _ps.print(s);
    return _console;
  }

  /**
   *  Terminate the current line by writing the line separator string.
   * Return a reference to Console to concatenate print methods.
   */
  public static Console println()
  {
    if (_instanceCount == 0)
      init();
    _ps.println();
    return _console;
  }

  /**
   * Print a boolean and then terminate the line.
   * Return a reference to Console to concatenate print methods.
   */
  public static Console println(boolean b)
  {
    if (_instanceCount == 0)
      init();
    _ps.println(b);
    return _console;
  }

  /**
   * Print a character and then terminate the line.
   * Return a reference to Console to concatenate print methods.
   */
  public static Console println(char c)
  {
    if (_instanceCount == 0)
      init();
    _ps.println(c);
    return _console;
  }

  /**
   * Print an array of characters and then terminate the line.
   * Return a reference to Console to concatenate print methods.
   */
  public static Console println(char[] s)
  {
    if (_instanceCount == 0)
      init();
    _ps.println(s);
    return _console;
  }

  /**
   * Print a double and then terminate the line.
   * Return a reference to Console to concatenate print methods.
   */
  public static Console println(double d)
  {
    if (_instanceCount == 0)
      init();
    _ps.println(d);
    return _console;
  }

  /**
   * Print a float and then terminate the line.
   * Return a reference to Console to concatenate print methods.
   */
  public static Console println(float f)
  {
    if (_instanceCount == 0)
      init();
    _ps.println(f);
    return _console;
  }

  /**
   * Print an integer and then terminate the line.
   * Return a reference to Console to concatenate print methods.
   */
  public static Console println(int i)
  {
    if (_instanceCount == 0)
      init();
    _ps.println(i);
    return _console;
  }

  /**
   * Print a long and then terminate the line.
   * Return a reference to Console to concatenate print methods.
   */
  public static Console println(long l)
  {
    if (_instanceCount == 0)
      init();
    _ps.println(l);
    return _console;
  }

  /**
   * Print an Object and then terminate the line.
   * Return a reference to Console to concatenate print methods.
   */
  public static Console println(Object obj)
  {
    if (_instanceCount == 0)
      init();
    _ps.println(obj);
    return _console;
  }

  /**
   * Print a String and then terminate the line.
   * Return a reference to Console to concatenate print methods.
   */
  public static Console println(String s)
  {
    if (_instanceCount == 0)
      init();
    _ps.println(s);
    return _console;
  }

  /**
   * Print a formatted string using the specified format string and varargs.
   * (See PrintStream.printf() for more information)
   * Return a reference to Console to concatenate print methods.
   */
  public static Console printf(String format, Object... args)
  {
    PrintStream ps = new PrintStream(System.out);
    ps.printf(format, args);
    return _console;
  }

  /**
   * Print a formatted string using the specified format string and varargs
   * and applying given locale during formatting.
   * (See PrintStream.printf() for more information)
   * Return a reference to Console to concatenate print methods.
   */
  public static Console printf(Locale l, String format, Object... args)
  {
    PrintStream ps = new PrintStream(System.out);
    ps.printf(l, format, args);
    return _console;
  }

  /**
   * For internal use only. Implements Printable.print().
   */
  public int print(Graphics g, PageFormat pf, int pageIndex)
  {
    if (pageIndex != 0)
      return NO_SUCH_PAGE;
    Graphics2D g2D = (Graphics2D)g;
    double printerWidth = pf.getImageableWidth();
    double printerHeight = pf.getImageableHeight();
    double printerSize
      = printerWidth > printerHeight ? printerWidth : printerHeight;
    // The 600 depends on the JPanel default size
    double scalex = 600 / printerSize * _scale;
    double scaley = scalex;

    double xZero = pf.getImageableX();
    double yZero = pf.getImageableY();

    g2D.translate(xZero, yZero);
    g2D.scale(scalex, scaley);

    _textArea.print(g);
    return PAGE_EXISTS;
  }

  /**
   * Print the current text area to an attached printer
   * with the given magnification scale factor.
   * A standard printer dialog is shown before printing is
   * started.<br>
   *
   * Return false, if printing is canceled in this dialog,
   * return true, when print data is sent to the printer spooler.<br>
   *
   * Only the text thats fits on one page is printed.
   *
   */
  public boolean printScreen(double scale)
  {
    _scale = scale;
    MessageDialog msg = new MessageDialog(_frame, "Printing in progress. Please wait...");

    PrinterJob pj = PrinterJob.getPrinterJob();
    pj.setPrintable(this);
    if (pj.printDialog())
    {
      try
      {
        msg.show();
        pj.print();
        msg.close();
      }
      catch (PrinterException ex)
      {
        System.out.println("Exception in Console.printScreen()\n" + ex);
      }
      return true;
    }
    else
      return false;
  }

  /**
   * Same as printScreen(scale) with scale = 1
   */
  public boolean printScreen()
  {
    return printScreen(1);
  }

  /**
   * Return a reference to the JFrame instance used by
   * the console
   */
  public static JFrame getFrame()
  {
    if (_instanceCount == 0)
      init();
    return _frame;
  }

  /**
   * Insert/remove a vertical scroll bar.
   * Default scroll bar policy: scroll bar added as needed
   */
  public static void showVerticalScrollBar(boolean b)
  {
    if (_instanceCount == 0)
      init();

    if (SwingUtilities.isEventDispatchThread())
    {
      if (b)
        _scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      else
        _scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
      _textArea.revalidate();
    }
    else
    {
      try
      {
        SwingUtilities.invokeAndWait(new ScrollBarThread(b, false));
      }
      catch (Exception ex)
      {
      }
    }
  }

  /**
   * Insert/remove a horizonal scroll bar.
   * Default scroll bar policy: scroll bar added as needed
   */
  public static void showHorizontalScrollBar(boolean b)
  {
    if (_instanceCount == 0)
      init();
    if (SwingUtilities.isEventDispatchThread())
    {
      if (b)
        _scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
      else
        _scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      _textArea.revalidate();
    }
    else
    {
      try
      {
        SwingUtilities.invokeAndWait(new ScrollBarThread(b, true));
      }
      catch (Exception ex)
      {
      }
    }
  }

  /**
   * Set another title in the console's title bar.
   */
  public static void setTitle(String title)
  {
    if (_instanceCount == 0)
      init();
    if (SwingUtilities.isEventDispatchThread())
    {
      _frame.setTitle(title);
    }
    else
    {
      try
      {
        SwingUtilities.invokeAndWait(new OptionThread(title, null, null, null));
      }
      catch (Exception ex)
      {
      }
    }
  }

  /**
   * Enable/disable autowrapping.
   * If enabled the lines will be wrapped at word boundaries (whitespace)
   * if they are too long to fit within the current line width.
   */
  public static void setAutowrap(boolean enable)
  {
    _textArea.setLineWrap(enable);
    _textArea.setWrapStyleWord(enable);
  }

  private static ClosingMode getClosingMode(MyProperties props)
  {
    String value = props.getStringValue("ConsoleClosingMode");
    if (value != null)  // Entry found
    {
      value = value.trim();
      if (value.equals("TerminateOnClose"))
        return ClosingMode.TerminateOnClose;
      if (value.equals("HideOnClose"))
        return ClosingMode.HideOnClose;
      if (value.equals("ClearOnClose"))
        return ClosingMode.ClearOnClose;
      if (value.equals("AskOnClose"))
        return ClosingMode.AskOnClose;
      if (value.equals("ReleaseOnClose"))
        return ClosingMode.ReleaseOnClose;
      if (value.equals("DisposeOnClose"))
        return ClosingMode.DisposeOnClose;
      if (value.equals("NothingOnClose"))
        return ClosingMode.NothingOnClose;
      return ClosingMode.TerminateOnClose;  // Entry not valid
    }
    return ClosingMode.TerminateOnClose;  // Entry not valid
  }

  private static void checkDisposed()
  {
    if (_isDisposed)
      throw new RuntimeException("Java frame disposed");
  }

  /** 
   * Return true, if the Console windows was disposed or released.
   */
  public boolean isDisposed()
  {
    return _isDisposed;
  }
  
    /** 
   * Determines what happens when the title bar close button is hit.
   * Values: On of the enums in ClosingMode:<br>
   * TerminateOnClose -> Terminating and shutting down JRE 
   * by System.exit(0)<br>
   * HideOnClose -> Hides the console window, use show() to make it visible<br>
   * ClearOnClose -> Clears the graphics content<br>
   * AskOnClose -> Shows confirmation dialog asking for termination<br>
   * DisposeOnClose -> Closes the consle window, but does not shutdown JRE<br>
   * ReleaseOnClose -> Like DisposeOnClose, but throws runtime exception 
   * when graphics methods are called<br>
   * NothingOnClose -> Does nothing<br>
   * Default value: TerminateOnClose or value read from aplu_utils.properties
   */
  public void setClosingMode(ClosingMode mode)
  {
    _closingMode = mode;
  }


}
