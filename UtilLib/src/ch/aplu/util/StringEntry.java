// StringEntry.java

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
 * IntEntry, LongEntry, DoubleEntry and StringEntry can be mixed up in the same EntryPane.
 */
public class StringEntry extends TextEntry
{
  /**
   * Creates a text field with given prompt text.
   * @param prompt the information text
   */
  public StringEntry(String prompt)
  {
    super(prompt, null);
  }

  /**
   * Creates a text field with given prompt text and given initializing value.
   * @param prompt the information text
   * @param init the value already present when the field is shown
   */
  public StringEntry(String prompt, String init)
  {
    super(prompt, init);
  }

  /**
   * Returns the current value in the text field.
   * @return the current value
   */
  public String getValue()
  {
    return getTextValue();
  }

  /**
   * Enters a integer number into the text field programmatically.
   * @param value the string to be entered ("" or null for empty string)
   */
  public void setValue(String value)
  {
    if (value == null)
      value = "";
    setTextValue(value);
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
