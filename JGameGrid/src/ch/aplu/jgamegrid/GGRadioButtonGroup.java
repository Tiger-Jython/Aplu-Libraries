// GGRadioButtonGroup.java

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

import java.util.*;

/**
 * A radio button group contains a certain number of radio buttons.
 * The group takes care of unselecting the previously selected
 * button when the user selects another button in the group. Initially,
 * if all buttons added to the group are deselected, no button is selected.
 * When a selected button is added to the group, any previously
 * selected button is deselected. A button should not be contained in more than
 * one button group.
 */
public class GGRadioButtonGroup
{
  // -------------- Inner class MyGGRadioButtonListener ----------
  private class MyRadioButtonListener implements GGRadioButtonListener
  {
    public void buttonSelected(GGRadioButton radioButton, boolean selected)
    {
      if (radioButton != selectedButton) // Do not select a button that is already selected
      {
        if (selectedButton != null)
          selectedButton.setSelected(false); // Deselect the selected button
        selectedButton = radioButton;
        selectedButton.setSelected(true); // Select button
        if (radioButtonListener != null)
          radioButtonListener.buttonSelected(selectedButton, true);
      }
    }
  }
  // ------------ End of inner class ------------------
  //
  private GGRadioButtonListener radioButtonListener;
  private GGRadioButtonListener myRadioButtonListener;
  private ArrayList<GGRadioButton> buttons = new ArrayList<GGRadioButton>();
  private GGRadioButton selectedButton = null;

  /**
   * Creates a GGRadioButtonGroup instance that contains no button.
   */
  public GGRadioButtonGroup()
  {
    myRadioButtonListener = new MyRadioButtonListener();
  }

  /**
   * Creates a GGRadionButton instance that contains the given GGRadioButtons.
   * If more than one button is already selected, all but the last
   * are deselected.
   * @param radioButtons the GGRadioButtons to be added to the group
   */
  public GGRadioButtonGroup(GGRadioButton... radioButtons)
  {
    myRadioButtonListener = new MyRadioButtonListener();
    for (int i = 0; i < radioButtons.length; i++)
      add(radioButtons[i]);
  }

  /**
   * Adds the given GGRadioButtons to the group.
   * If more than one button is selected, all but the last are deselected.
   * @param radioButtons the GGRadioButtons to be added to the group
   */
  public void add(GGRadioButton... radioButtons)
  {
    for (int i = 0; i < radioButtons.length; i++)
      add(radioButtons[i]);
  }

  /**
   * Adds the given GGRadioButton to the group. If the button is already
   * selected, any previously selected button is deselected.
   * @param radioButton the GGRadioButton to be added to the group
   */
  public void add(GGRadioButton radioButton)
  {
    buttons.add(radioButton);
    radioButton.addRadioButtonListener(myRadioButtonListener);
    if (radioButton.isSelected())
    {
      if (selectedButton != null)
        selectedButton.setSelected(false);
      selectedButton = radioButton;
    }
    radioButton.setButtonGroup(this);
  }

  protected void setSelectedButton(GGRadioButton button)
  {
    selectedButton = button;
  }

  /**
   * Returns the currently selected button.
   * @return the currently selected button, null if no button is selected
   */
  public GGRadioButton getSelectedButton()
  {
    return selectedButton;
  }

  /**
   * Returns the id of the currently selected button. The button ids
   * corresponds to the order the buttons are added to the button group.
   * @return the id of the selected button, -1 if no button is selected
   */
  public int getSelectedButtonId()
  {
    for (int i = 0; i < buttons.size(); i++)
      if (selectedButton == buttons.get(i))
        return i;
    return -1;
  }

  /**
   * Returns a list of all buttons added to the group.
   * @return all GGRadioButtons in the group
   */
  public ArrayList<GGRadioButton> getButtons()
  {
    return buttons;
  }

  /**
   * Registers the given GGRadioButtonListener to get notifications when
   * the currently selected button changes because the user clicks another button.
   * @param listener the GGRadioButtonListener to register
   */
  public void addRadioButtonListener(GGRadioButtonListener listener)
  {
    radioButtonListener = listener;
  }
}
