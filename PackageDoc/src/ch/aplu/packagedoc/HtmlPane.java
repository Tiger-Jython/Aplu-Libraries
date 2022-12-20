// HtmlPane.java

package ch.aplu.packagedoc;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import javax.swing.*;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import javax.swing.text.html.StyleSheet;

/**
 * Simple graphics window similar to GPanel to be used for displaying HTML formatted
 * text. Only a subset of HTML is supported.
 * Image loading from image tag <img src="gifs/pict.gif" width="www" height="hhh">
 * is supported as follows:<br>
 * insertUrl(): subdir/pict.gif: loading from internet, subdir of html<br>
 *              http://...: loading from internet, absolute URL<br>
 *
 * insertFile(): subdir/pict.gif: loading from subdir of html on disk<br>
 *               file://path: fully qualified path to disk image file<br>
 *               http://.,. . loading from internet, absolute URL<br>
 *
 * insertResource: subdir/pict.gif: loading from subdir of html from JAR archive<br>
 *                 /absdir/pict.gif: loading from absolute dir from root of JAR archive<br>
 *                 http://.,. : loading from internet, absolute URL<br>
 *             
 * insertText():   http://...: loading from internet, absolute URL<br>
 *                 file://path: fully qualified path to disk image file<br>
 */
public class HtmlPane implements LinkListener
{
  protected static boolean debug = false;

  private enum ClosingMode
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
      _pane.setCaretPosition(0);
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
  private JFrame _frame;
  private String _title = null;
  private JEditorPane _pane;
  private Point _position = null;
  private Dimension _size = null;
  private ClosingMode _closingMode = null;
  private boolean _isDisposed = false;
  private ExitListener _exitListener = null;
  private String homeUrl;
  private MyHTMLEditorKit kit;
  private LinkListener linkListener = null;
  private String homeDir = null;

  /**
   * Construct a HtmlPane  with default size and position.
   */
  public HtmlPane(String homeDir)
  {
    this(null, null);
    this.homeDir = homeDir;
  }

  /**
   * Construct a HtmlPane  with default size and position.
   */
  public HtmlPane()
  {
    this(null, null);
  }

  /**
   * Construct a HtmlPane with given position and size. 
   * The default position and size is selected from packagedoc.properties.
   * @param position   a reference to a Position object
   * @param size a reference to a Size object
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
    setTitle("APLU Bibliotheks-Dokumentation");
    addLinkListener(this);
  }

  /**
   * Insert HTML formatted text. The old text is erased.
   * @param text the html text to show
   */
  public void insertText(String text)
  {
    if (SwingUtilities.isEventDispatchThread())
    {
      _pane.setText(text);
      _pane.setCaretPosition(0);
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

  /**
   * Insert HTML formatted text from the given file in a JAR resource.
   * The old text is erased.
   * @param filename the path of the JAR resource text file relative to the
   * application root
   */
  public void insertResource(String filename)
  {
    URL url = Thread.currentThread().getContextClassLoader().
      getResource(filename);
    if (url == null)
      return;
    kit.setHome("res://" + filename.substring(0, filename.lastIndexOf('/')));
    try
    {
      InputStream is = url.openStream();
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      insertFromStream(br);
    }
    catch (IOException ex)
    {
    }
  }

  /**
   * Insert HTML formatted text from the given file. 
   * The old text is erased.
   * @param filename the qualified or relative path to the html text file
   */
  public void insertFile(String filename)
  {
    if (HtmlPane.debug)
      System.out.println("Calling insertFile() with filename: " + filename);
    if (!new File(filename).exists())
      return;
    kit.setHome("file://" + filename.substring(0, filename.lastIndexOf('/')));
    try
    {
      BufferedReader br = new BufferedReader(new FileReader(filename));
      insertFromStream(br);
    }
    catch (IOException ex)
    {
    }
  }

  /**
   * Insert HTML from given URL. 
   * The old text is erased.
   * @param htmlUrl an URL string like http://www.aplu.ch/index.html" to 
   * the html text file
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

  private void insertFromStream(BufferedReader br)
  {
    String text = "";
    try
    {
      String line;
      while ((line = br.readLine()) != null)
        text = text + line + "\n";
    }
    catch (IOException ex)
    {
      return;
    }
    finally
    {
      try
      {
        br.close();
      }
      catch (IOException ex)
      {
      }
    }
    insertText(text);
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
    StyleSheet styleSheet = kit.getStyleSheet();
    styleSheet.addRule("body {font-size:16px; }");
    styleSheet.addRule("td {font-size:16px; }");
    styleSheet.addRule("h1 {font-size:20px; font-weight:bold;}");
    styleSheet.addRule("h2 {font-size:20px; font-weight:bold;}");
    styleSheet.addRule("h3 {font-size:16px; font-weight:bold;}");
    styleSheet.addRule("h4 {font-size:16px; font-weight:bold;}");
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
//    _frame.setResizable(false);
  }

  /**
   * Show the given title in the title bar.
   * The default title is selected from packagedoc.properties.
   * @param title the new title to show
   */
  public void setTitle(final String title)
  {
    if (SwingUtilities.isEventDispatchThread())
      _frame.setTitle(title);
    else
    {
      try
      {
        SwingUtilities.invokeAndWait(
          new Runnable()
          {
            public void run()
            {
              _frame.setTitle(title);
            }
          });
      }
      catch (Exception ex)
      {
      }
    }
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
   * @param exitListener the ExitListener to register
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

  public void browse(String url)
  {
    url = url.trim().toLowerCase();
    if (!url.substring(0, 7).equals("http://"))
      url = "http://" + url;
   
    String os = System.getProperty("os.name").toLowerCase();
    Runtime rt = Runtime.getRuntime();

    try
    {
      if (os.indexOf("win") >= 0)
      {
        // this doesn't support showing urls in the form of "page.html#nameLink" 
        rt.exec("rundll32 url.dll,FileProtocolHandler " + url);

      }
      else if (os.indexOf("mac") >= 0)
      {
        rt.exec("open " + url);

      }
      else if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0)
      {
        // Do a best guess on unix until we get a platform independent way
        // Build a list of browsers to try, in this order.
        String[] browsers =
        {
          "epiphany", "firefox", "mozilla", "konqueror",
          "netscape", "opera", "links", "lynx"
        };

        // Build a command string which looks like "browser1 "url" || browser2 "url" ||..."
        StringBuffer cmd = new StringBuffer();
        for (int i = 0; i < browsers.length; i++)
          cmd.append((i == 0 ? "" : " || ") + browsers[i] + " \"" + url + "\" ");

        rt.exec(new String[]
        {
          "sh", "-c", cmd.toString()
        });

      }
      else
      {
        return;
      }
    }
    catch (Exception e)
    {
      return;
    }
    return;
  }

  /**
   * Calls the standard system browser
   * @param url the URL of the WEB site, prefixed with http:// or not
   */
  /* This old version did not work on Ubuntu 14.04
   You just need to have the Gnome libraries installed so that Java 
   recognizes it (as ccheneson said).
   If you are running a new version of Ubuntu, it doesn't come 
   with the gnome libraries because it uses Unity. 
   Try installing libgnome2-0. When I installed it a 
   few other packages came with it 
   (libbonobo2-0, libbonobo2-common, libgnomevfs2-0, libgnomevfs2-common)
   so I don't know if libgnome2-0 is sufficient or if any of the 
   others are necessary as well. But then my 12.04 Ubuntu system 
   was recognized by the Java API.
   */
  /*
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

   */
  /**
   * Registers a LinkListener to get events when clicked
   * on a hyperlink.
   * @param listener the LinkListener to register
   */
  public void addLinkListener(LinkListener listener)
  {
    linkListener = listener;
  }

  public void linkClicked(String url)
  {
    if (url.startsWith("http://ch"))
    {
      // strip http://
      String resource = url.substring(url.indexOf('/') + 2);
      if (homeDir != null)
        insertFile(homeDir + "/" + resource);
      else
        insertResource(resource);
    }
    else
      browse(url);
  }
}
