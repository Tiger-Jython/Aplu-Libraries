// DoubleEntry.java

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

/**
 * Class derived from EntryItem to create an input text field in a EntryDialog.
 * IntegerEntry, DoubleEntry and StringEntry can be mixed up in the same EntryPane.
 */
public class DoubleEntry extends TextEntry
{
  /**
   * Creates a text field with given prompt text.
   * @param prompt the information text
   */
  public DoubleEntry(String prompt)
  {
    super(prompt, null);
  }

  /**
   * Creates a text field with given prompt text and given initializing value.
   * @param prompt the information text
   * @param init the value already present when the field is shown
   */
  public DoubleEntry(String prompt, double init)
  {
    super(prompt, Double.toString(init));
  }

  /**
   * Returns the current value in the text field. If the text cannot be
   * converted to a double number, null is returned.
   * @return the current value
   */
  public Double getValue()
  {
    Double v = null;
    try
    {
      v = Double.parseDouble(getTextValue());
    }
    catch (NumberFormatException ex)
    {
    }

    return v;
  }

  /**
   * Enters a double number into the text field programmatically.
   * @param value the number to be entered
   */
  public void setValue(Double value)
  {
    if (value == null)
      setTextValue("");
    else
      setTextValue(Double.toString(value));
  }

  /**
   * Selects if the text field is editable.
   * @param b if true, the field is editable
   */
  public void setEditable(boolean b)
  {
    super.setEditable(b);
  }
}
