// HtmlPane.java

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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import javax.swing.*;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;

public class HtmlPane
{
  protected static boolean debug = false;

  private static enum ClosingMode
  {
    TerminateOnClose,
    HideOnClose,
    ClearOnClose,
    AskOnClose,
    DisposeOnClose,
    NothingOnClose
  }
// ------------------------ Internal classes -------------------

  private class MyWindowAdapter extends WindowAdapter
  {
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
          System.exit(0);
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
            System.exit(0);
          }
          break;
        case DisposeOnClose:
          _isDisposed = true;
          hide();
          clear();
          break;
      }
    }
  }

  class InsertThread extends Thread
  {
    private String _msg;

    InsertThread(String msg)
    {
      _msg = msg;
    }

    InsertThread(String msg, int charCount)
    {
      _msg = msg;
    }

    public void run()
    {
      _pane.setText(_msg);
    }
  }

  class HideThread extends Thread
  {
    public void run()
    {
      _frame.setVisible(false);
    }
  }
// ------------------------ End of internal classes -------------------
// 
  private final static boolean propertyVerbose = false;
  private static JFrame _frame;
  private String _title = null;
  private static JEditorPane _pane;
  private static Point _position = null;
  private static Dimension _size = null;
  private static ClosingMode _closingMode = null;
  private static boolean _isDisposed = false;
  private ExitListener _exitListener = null;
  private String homeUrl;
  private MyHTMLEditorKit kit;
  private LinkListener linkListener = null;

  /**
   * Construct a HtmlPane  with default size and position.
   */
  public HtmlPane()
  {
    this(null, null);
  }

  /**
   * Construct a HtmlPane with given position and size. 
   * @param position   a reference to a Position object
   * @see ch.aplu.util.Position
   * @param size a reference to a Size object
   * @see ch.aplu.util.Size
   */
  public HtmlPane(final Position position, final Size size)
  {
    _isDisposed = false;
    if (EventQueue.isDispatchThread())
      createPane(position, size);
    else
    {
      try
      {
        EventQueue.invokeAndWait(new Runnable()
        {
          public void run()
          {
            createPane(position, size);
          }
        });
      }
      catch (Exception ex)
      {
      }
    }
  }

  /**
   * Insert HTML from given URL. 
   * @param htmlUrl must be a valid URL string like http://www.aplu.ch/index.html"
   */
  public void insertUrl(String htmlUrl)
  {
    htmlUrl = htmlUrl.trim().toLowerCase();
    if (HtmlPane.debug)
      System.out.println("HtmlPane.insertUrl() httlUrl = " + htmlUrl);
    try
    {
      new URL(htmlUrl);
    }
    catch (MalformedURLException ex)
    {
      System.out.println("Error in HttPane.insertUrl(). Illegal url = " + htmlUrl);
      return;
    }
    homeUrl = htmlUrl;
    if (!htmlUrl.substring(0, 7).equals("http://"))
    {
      System.out.println("HttPane.insertUrl() Illegal url = " + htmlUrl);
      return;
    }
    String s = htmlUrl.substring(7);
    int index = s.indexOf("/");
    if (index != -1)  // file requested
    {
      index = htmlUrl.lastIndexOf("/");
      homeUrl = htmlUrl.substring(0, index);
    }

    if (HtmlPane.debug)
      System.out.println("HtmlPane.insertUrl() homeUrl = " + homeUrl);
    kit.setHome(homeUrl);
    String result = "";
    try
    {
      URL url = new URL(htmlUrl);
      URLConnection urlConnection = url.openConnection();
      InputStream is = urlConnection.getInputStream();
      InputStreamReader isr = new InputStreamReader(is);

      int numCharsRead;
      char[] charArray = new char[1024];
      StringBuffer sb = new StringBuffer();
      while ((numCharsRead = isr.read(charArray)) > 0)
      {
        sb.append(charArray, 0, numCharsRead);
      }
      result = sb.toString();
    }
    catch (MalformedURLException e)
    {
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    insertText(result);
  }

  private void createPane(Position position, Size size)
  {
    MyProperties props = new MyProperties(HtmlPane.propertyVerbose);
    if (props.search())
    {
      _title = props.getStringValue("HtmlPaneTitle");
      if (_title != null)
        _title = _title.trim();

      _closingMode = getClosingMode(props);

      int[] values = props.getIntArray("HtmlPanePosition", 2);
      if (values != null && values[0] >= 0 && values[1] >= 0)
        _position = new Point(values[0], values[1]);

      values = props.getIntArray("HtmlPaneSize", 2);
      if (values != null && values[0] > 0 && values[1] > 0)
        _size = new Dimension(values[0], values[1]);
    }

    if (_title == null)
      _frame = new JFrame("HtmlPane");
    else
      _frame = new JFrame(_title);
    _frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    _frame.addWindowListener(new MyWindowAdapter());
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = null;
    int ulx = 0;
    int uly = 0;

    if (size == null)
    {
      if (_size == null)
      {
        frameSize = new Dimension(1000, 800);
        if (position == null)
        {
          ulx = (int)((screenSize.width - frameSize.width) / 2);
          uly = (int)((screenSize.height - frameSize.height) / 2);
        }
        else
        {
          ulx = position.getUlx();
          uly = position.getUly();
        }
      }
      else
      {
        frameSize = new Dimension(_size.width, _size.height);
        if (_position == null)
        {
          if (position == null)
          {
            ulx = (int)((screenSize.width - frameSize.width) / 2);
            uly = (int)((screenSize.height - frameSize.height) / 2);
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
            ulx = _position.x;
            uly = _position.y;
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
        if (_position == null)
        {
          ulx = (int)((screenSize.width - frameSize.width) / 2);
          uly = (int)((screenSize.height - frameSize.height) / 2);
        }
        else
        {
          ulx = _position.x;
          uly = _position.y;
        }
      }
      else
      {
        ulx = position.getUlx();
        uly = position.getUly();
      }
    }

    _frame.setBounds(ulx, uly, frameSize.width, frameSize.height);
    _pane = new JEditorPane();
    _pane.setEditable(false);
    _pane.addHyperlinkListener(new HyperlinkListener()
    {
      public void hyperlinkUpdate(HyperlinkEvent e)
      {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
        {
          final URL url = e.getURL();
          if (linkListener != null && url != null)
            new Thread()
            {
              public void run()
              {
                linkListener.linkClicked(url.toString());
              }
            }.start();
        }
      }
    });
    kit = new MyHTMLEditorKit();
    kit.setAutoFormSubmission(false);
    _pane.setEditorKit(kit);
    Document doc = kit.createDefaultDocument();
    _pane.setDocument(doc);
    _pane.setEditable(false);

    doc.putProperty("IgnoreCharsetDirective", Boolean.TRUE);

    _frame.getContentPane().setLayout(new BorderLayout());
    JScrollPane scrollPane = new JScrollPane(_pane,
      JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
      JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    _frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
    _frame.setVisible(true);
  }

  /**
   * Erase all text.
   */
  public void clear()
  {
    insertText("");
  }

  /**
   * Register an ExitListener to get a notification when the 
   * close button is clicked.
   * After registering the automatic termination of the application is disabled.
   * To terminate the application, System.exit() should be called.
   */
  public void addExitListener(ExitListener exitListener)
  {
    _exitListener = exitListener;
  }

  /**
   * Hide the console window.
   */
  public void hide()
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

  /** 
   * Return true, if the HtmlPane window was disposed.
   */
  public boolean isDisposed()
  {
    return _isDisposed;
  }

  /**
   * Insert HTML formatted text. The old text is erased.
   */
  public void insertText(String text)
  {
    if (SwingUtilities.isEventDispatchThread())
    {
      _pane.setText(text);
    }
    else
    {
      try
      {
        SwingUtilities.invokeAndWait(new InsertThread(text));
      }
      catch (Exception ex)
      {
      }
    }
  }

  private static ClosingMode getClosingMode(MyProperties props)
  {
    String value = props.getStringValue("HtmlPaneClosingMode");
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
      if (value.equals("DisposeOnClose"))
        return ClosingMode.DisposeOnClose;
      if (value.equals("NothingOnClose"))
        return ClosingMode.NothingOnClose;
      return ClosingMode.TerminateOnClose;  // Entry not valid
    }
    return ClosingMode.TerminateOnClose;  // Entry not valid
  }

  /**
   * Calls the standard system browser
   * @param url the URL of the WEB site, prefixed with http:// or not
   */
  public static void browse(String url)
  {
    url = url.trim().toLowerCase();
    if (!url.substring(0, 7).equals("http://"))
      url = "http://" + url;
    try
    {
      new URL(url);
      Desktop.getDesktop().browse(new URI(url));
    }
    catch (Exception ex)
    {
      System.out.println("Illegal URL in HtmlPane.browse(): " + url);
    }
  }

  /**
   * Registers a LinkListener to get events when clicked
   * on a hyperlink.
   * @param listener the listener to register
   */
  public void addLinkListener(LinkListener listener)
  {
    linkListener = listener;
  }
}
