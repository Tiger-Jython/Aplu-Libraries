// LinkListener.java

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

package ch.aplu.packagedoc;

/**
 * The listener interface for receiving clicks on a hyperlink in a HtmlPane.
 */
public interface LinkListener extends java.util.EventListener
{
  /**
   * Invoked when a link is clicked in a HtmlPane.
   */
  void linkClicked(String url);
}
