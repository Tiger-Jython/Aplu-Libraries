// MyHTMLEditorKit.java
// Code from GitHub with same file name with thanks to the author.

package ch.aplu.packagedoc;

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
