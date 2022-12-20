// FileInfo.java

package ch.aplu.nxt;

import ch.aplu.nxt.platform.*;

/**
 * Structure that gives information about a leJOS NXJ file.
 * Taken from the leJOS library (lejos.sourceforge.net),
 * with thanks to the author.
 *
 */
public class FileInfo
{
  /**
   * The name of the file - up to 20 characters.
   */
  public String fileName;

  /**
   * The handle for accessing the file.
   */
  public byte fileHandle;

  /**
   * The size of the file in bytes.
   */
  public int fileSize;

  /**
   * The status of the file - not used.
   */
  public byte status;

  /**
   * The start page of the file in flash memory.
   */
  public int startPage;

  /**
   * Constructs a FileInfo instance with given file name.
   * @param fileName the file name stored in the instance variable fileName
   */
  public FileInfo(String fileName)
  {
    this.fileName = fileName;
  }
  
  /**
   * Show all file information in the debug console.
   */
  public void show()
  {
    DebugConsole.show("name: " + fileName);
    DebugConsole.show("handle: " + fileHandle);
    DebugConsole.show("size: " + fileSize);
    DebugConsole.show("status: " + status);
    DebugConsole.show("startPage: " + startPage);
  }
}