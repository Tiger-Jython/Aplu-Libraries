// SimulationBar.java

package ch.aplu.simulationbar;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SimulationBar
{
  // --------------- Inner class SimulationThread ------------------
  private class SimulationThread extends Thread
  {
    public void run()
    {
      while (isSimulationThreadRunning)
      {
        long startTime;
        long offset = 1000000;  // Increases precision of period
        if (isRunning)
        {
          startTime = System.nanoTime();
          if (simulationListener != null)
          {
            try
            {
              simulationListener.loop();
            }
            catch (Exception ex)
            {
            }
          }
          if (simulationPeriodNanos > 0)
          {
            while (System.nanoTime() - startTime < (simulationPeriodNanos - offset) && !isSingleStep)
              delay(1);
          }
          Thread.yield();
          if (isSingleStep)
          {
            isRunning = false;
            isSingleStep = false;
          }
        }
        else  // Paused
        {
          isPaused = true;
          delay(10);
        }
      }
//      System.out.println("Simulation thread terminated");
    }
  }

  private class MyWindowListener extends WindowAdapter
  {
    public void windowClosing(WindowEvent e)
    {
      if (simulationListener != null)
        simulationListener.exit();
      dispose();
    }
  }

  private static final int SLIDER_MIN_VALUE = 0;
  private static final int SLIDER_MAX_VALUE = 3000;
  private JFrame myFrame = null;
  private JSlider speedSlider;
  private final JButton stepBtn = new JButton("Step");
  private final JButton runBtn = new JButton("Run");
  private final JButton resetBtn = new JButton("Reset");
  private final int w = 500;
  private final int h = 50;
  private int simulationPeriod = 100;
  private long simulationPeriodNanos = simulationPeriod * 1000000;
  private SimulationListener simulationListener = null;
  private volatile boolean isSimulationThreadRunning = true;
  private volatile boolean isRunning = false;
  private volatile boolean isPaused = false;
  private volatile boolean isSingleStep = false;

  /** 
   * Same as SimulatonBar(ulx, uly, initPeriod) where the dialog is positioned in the center of the
   * the screen and the initial simulation period is 100 ms.
   */
  public SimulationBar()
  {
    this(0, 0, 100);
  }

  /** 
   * Same as SimulatonBar(ulx, uly, initPeriod) 
   * with the initial simulation period 100 ms.
   * @param ulx the upper left screen x-coordinate of the dialog window
   * @param uly the upper left screen y-coordinate of the dialog window
   */
  public SimulationBar(int ulx, int uly)
  {
    this(ulx, uly, 100);
  }

  /** 
   * Same as SimulatonBar(ulx, uly, initPeriod) where the dialog is positioned 
   * in the center of the screen.
   * @param initPeriod the initial simulation period (0...1000 ms)
   */
  public SimulationBar(int initPeriod)
  {
    this(0, 0, initPeriod);
  }

  /** 
   * Shows a dialog with a 3 buttons (Step, Run/Pause, Reset) and a slider to select
   * the simulation period. When a SimulationListener is registered, it gets the corresponding
   * events.
   * @param ulx the upper left screen x-coordinate of the dialog window
   * @param uly the upper left screen y-coordinate of the dialog window
   * @param initPeriod the initial simulation period (0...1000 ms)
   */
  public SimulationBar(final int ulx, final int uly, final int initPeriod)
  {
    java.awt.EventQueue.invokeLater(new Runnable()
    {
      public void run()
      {
        init(ulx, uly, initPeriod);
      }
    });
    SimulationThread simulationThread = new SimulationThread();
    simulationThread.start();
  }

  private void init(int ulx, int uly, int initPeriod)
  {
    myFrame = new JFrame();
    myFrame.setUndecorated(true);  // Workaround for setResizable() bug 
    myFrame.setResizable(false);
    myFrame.setUndecorated(false);
    JPanel contentPane = (JPanel)myFrame.getContentPane();
    contentPane.setPreferredSize(new Dimension(w, h));
    simulationPeriod = initPeriod;
    myFrame.setTitle(String.format("Simulation Period: %6dms", simulationPeriod));

    speedSlider = new JSlider(SLIDER_MIN_VALUE, SLIDER_MAX_VALUE,
      (int)(434.2317 * Math.log((double)simulationPeriod) + 0.5));

    speedSlider.setPreferredSize(new Dimension(100,
      speedSlider.getPreferredSize().height));
    speedSlider.setMaximumSize(speedSlider.getPreferredSize());
    speedSlider.setInverted(true);
    speedSlider.setPaintLabels(false);

    JPanel dlgPanel = new JPanel();
    dlgPanel.setPreferredSize(new Dimension(100, 40));
    stepBtn.setPreferredSize(new Dimension(70, 25));
    runBtn.setPreferredSize(new Dimension(70, 25));
    resetBtn.setPreferredSize(new Dimension(70, 25));
    dlgPanel.add(stepBtn);
    dlgPanel.add(runBtn);
    dlgPanel.add(resetBtn);
    dlgPanel.add(new JLabel("     "));
    dlgPanel.add(new JLabel("Slow"));
    dlgPanel.add(speedSlider);
    dlgPanel.add(new JLabel("Fast"));
    contentPane.add(dlgPanel, BorderLayout.SOUTH);
//    myFrame.setUndecorated(true);
    myFrame.pack();
    if (ulx == 0) // Center dialog
    {
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension size = myFrame.getSize();
      ulx = (screenSize.width - size.width) / 2;
      uly = (screenSize.height - size.height) / 2;
    }
    myFrame.setLocation(ulx, uly);

    runBtn.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent evt)
      {
        if (simulationListener == null)
          return;
        if (runBtn.getText().equals("Pause"))
        {
          try
          {
            simulationListener.pause();
          }
          catch (Exception ex)
          {
          }
          doPause();
          runBtn.setText("Run");
        }
        else
        {
          try
          {
            simulationListener.start();
          }
          catch (Exception ex)
          {
          }
          doStart();
          runBtn.setText("Pause");
        }
      }
    });

    resetBtn.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent evt)
      {
        if (simulationListener == null)
          return;
        try
        {
          simulationListener.reset();
        }
        catch (Exception ex)
        {
        }
        doReset();
        runBtn.setText("Run");
      }
    });

    stepBtn.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent evt)
      {
        if (simulationListener == null)
          return;
        try
        {
          simulationListener.step();
        }
        catch (Exception ex)
        {
        }
        doStep();
      }
    });

    speedSlider.addChangeListener(new ChangeListener()
    {
      public void stateChanged(ChangeEvent evt)
      {
        if (simulationListener == null)
          return;
        int value = ((JSlider)evt.getSource()).getValue();
        // SLIDER_MIN_VALUE -> 0, SLIDER_MAX_VALUE -> 1000
        int time = (int)(Math.exp(value / 434.2317) - 0.5);
        if (time != simulationPeriod)
        {
          try
          {
            simulationListener.change(time);
          }
          catch (Exception ex)
          {
          }
          simulationPeriod = time;
          simulationPeriodNanos = simulationPeriod * 1000000L;
          myFrame.setTitle(String.format("Simulation Period: %6dms", simulationPeriod));
        }
      }
    });
    myFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    myFrame.addWindowListener(new MyWindowListener());
    runBtn.requestFocus();
    myFrame.setVisible(true);
  }

  /**
   * Registers a SimulationListener that gets the events from the dialog.
   * @param listener the listener to register
   */
  public void addSimulationListener(SimulationListener listener)
  {
    simulationListener = listener;
  }

  private void delay(long timeout)
  {
    try
    {
      Thread.sleep(timeout);
    }
    catch (InterruptedException ex)
    {
    }
  }

  private void doStart()
  {
    isRunning = true;
    isPaused = false;
    isSingleStep = false;
  }

  private void doPause()
  {
    isRunning = false;
    isPaused = true;
    isSingleStep = false;
  }

  private void doStep()
  {
    isRunning = true;
    isPaused = false;
    isSingleStep = true;
  }

  private void doReset()
  {
    isRunning = false;
    isPaused = false;
    isSingleStep = false;
  }

  /**
   * Hides the simulation bar and releases all resources.
   */
  public void dispose()
  {
    isSimulationThreadRunning = false;
    myFrame.dispose();
  }
}
