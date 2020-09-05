// MyHTMLEditorKit.java

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

// Code from GitHub with same file name with thanks to the author.

import javax.swing.text.*;
import javax.swing.text.html.*;

class MyHTMLEditorKit extends HTMLEditorKit
{
  private static String homeUrl = null;  
  
  @Override
  public ViewFactory getViewFactory()
  {
    return new HTMLFactoryX();
  }
  
  public void setHome(String homeUrl)
  {
    this.homeUrl = homeUrl;
  }

  public static class HTMLFactoryX extends HTMLFactory implements ViewFactory
  {
    @Override
    public View create(Element elem)
    {
      Object o = elem.getAttributes().getAttribute(
        StyleConstants.NameAttribute);
      if (o instanceof HTML.Tag)
      {
        HTML.Tag kind = (HTML.Tag)o;
        if (kind == HTML.Tag.IMG)
          return new MyImageView(homeUrl, elem);
      }
      return super.create(elem);
    }
  }
}
