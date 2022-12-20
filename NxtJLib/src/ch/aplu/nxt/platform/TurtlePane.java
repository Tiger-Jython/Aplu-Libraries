// RunArea.java

package ch.aplu.nxt.platform;

import javax.swing.event.*;
import javax.swing.*;
import java.awt.*;
import java.awt.Color;
import javax.swing.text.*;
import ch.aplu.nxt.*;

public class TurtlePane extends JFrame
  implements CaretListener
{
  // --------------- Inner class LineHighligher -----------------------
  class LineHighlighter
  {
    private Highlighter.HighlightPainter painter;
    private Object highlight;

    public LineHighlighter(Color highlightColor)
    {
      painter = new DefaultHighlighter.DefaultHighlightPainter(highlightColor);
    }

    public void highlight(final int start, final int end)
    {
      try
      {
        highlight = jTextArea.getHighlighter().addHighlight(start, end, painter);
      }
      catch (BadLocationException ex)
      {
        ex.printStackTrace();
      }
    }

    void removeHighlight()
    {
      if (highlight != null)
      {
        jTextArea.getHighlighter().removeHighlight(highlight);
        highlight = null;
      }
    }

  }
  // --------------- End of inner class -------------------------------

  private JPanel contentPane;
  private JTextArea jTextArea;
  private JScrollPane jScrollPane;
  private LineHighlighter lh;
  private TurtlePane myFrame;
  private LegoRobot robot;

  public TurtlePane(LegoRobot robot, int posX, int posY)
  {
    super("Turtle Commands");
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    init();
    setLocation(posX, posX);
    pack();
    setVisible(true);
    this.robot = robot;
  }

  public void println(final String line)
  {
    if (EventQueue.isDispatchThread())
    {
      jTextArea.append(line);
      highLight();
      jTextArea.append("\n");
    }
    else
      try
      {
        EventQueue.invokeAndWait(new Runnable()
        {
          public void run()
          {
            jTextArea.append(line);
            highLight();
            jTextArea.append("\n");
          }

        });
      }
      catch (Exception ex)
      {
      }
  }

  public void clear()
  {
    if (EventQueue.isDispatchThread())
      jTextArea.setText("");
    else
      try
      {
        EventQueue.invokeAndWait(new Runnable()
        {
          public void run()
          {
            jTextArea.setText("");
          }

        });
      }
      catch (Exception ex)
      {
      }
  }

  void init()
  {
    jTextArea = new JTextArea(30, 25);
    jScrollPane = new JScrollPane(jTextArea,
      JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    contentPane = (JPanel)getContentPane();
    contentPane.add(jScrollPane);
    jTextArea.setEditable(false);
    jTextArea.setBackground(new Color(230, 230, 230));
    lh = new LineHighlighter(new Color(100, 255, 100));
    myFrame = this;
  }
  
  public void highLight()
  {
    lh.removeHighlight();
    try
    {
      int lineIndex = getLineIndexAtCaret(jTextArea);
      int start = jTextArea.getLineStartOffset(lineIndex);
      int end = jTextArea.getLineEndOffset(lineIndex);
      lh.highlight(start, end);
    }
    catch (BadLocationException ex)
    {
      ex.printStackTrace();
    }
  }  

  public void caretUpdate(CaretEvent event)
  {
    lh.removeHighlight();
    try
    {
      int lineIndex = getLineIndexAtCaret(jTextArea);
      int start = jTextArea.getLineStartOffset(lineIndex);
      int end = jTextArea.getLineEndOffset(lineIndex);
      String line = jTextArea.getText(start, end - start - 1);
 //     if (!myApp.parseLine(line))
 //       return;
      lh.highlight(start, end);
      for (int i = 0; i < 4; i++)
      {
 //       myApp.mSpeeds[i].setText(Integer.toString(myApp.speeds[i]));
 //       myApp.mPos[i].setText(Integer.toString(myApp.targetPos[i]));
      }
    }
    catch (BadLocationException ex)
    {
      ex.printStackTrace();
    }
  }

  // Return the current line number at the caret position.
  public int getLineIndexAtCaret(JTextComponent component)
  {
    int caretPosition = component.getCaretPosition();
    Element root = component.getDocument().getDefaultRootElement();
    return root.getElementIndex(caretPosition);
  }

  public void addCaretListener()
  {
    if (EventQueue.isDispatchThread())
      jTextArea.addCaretListener(myFrame);
    else
      try
      {
        EventQueue.invokeAndWait(new Runnable()
        {
          public void run()
          {
            jTextArea.addCaretListener(myFrame);
          }

        });
      }
      catch (Exception ex)
      {
      }
  }

}
