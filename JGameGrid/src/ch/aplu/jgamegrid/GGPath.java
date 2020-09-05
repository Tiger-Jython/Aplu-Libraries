// GGPath.java

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

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;

/**
 * GGPath is a helper class that simplifies file operations.  
 */
public class GGPath
{
  private final static boolean debug = false;
  private final static String fs = System.getProperty("file.separator");
  private static ArrayList<File> fileList = new ArrayList<File>();
  private static File treeSrc;
  private static File treeDest;
  private static boolean success;

  // private Ctor
  private GGPath()
  {
  }

  /**
   * Writes the given string to the given destination file. An existing file
   * is overwritten. If createTree is true and filePath contains subdirectories
   * that are nonexistant, the subdirectory directory tree is created.
   * @param filePath a relative or absolute path to the file
   * @param text the text to write
   * @param createTree if true, a nonexistent subdirectory structure is created
   * @return true, if the file is successfully written; otherwise false
   */
  public static boolean writeTextFile(File filePath, String text, boolean createTree)
  {
    debug("writeTextFile() with filePath = " + filePath);
    // Creates directory structure if not yet available
    if (createTree)
    {
      debug("reclaim directory creation");
      String path = filePath.getAbsolutePath().replace('\\', '/');
      debug("path = " + path);
      int index = path.lastIndexOf("/");
      if (index != -1)
      {
        String dir = path.substring(0, index);
        debug("directory = " + dir);
        if (!createDirectoryTree(new File(dir)))
          return false;
      }
    }

    BufferedWriter out = null;
    try
    {
      debug("Writing file " + filePath.getAbsolutePath());
      out = new BufferedWriter(new FileWriter(filePath));
      out.write(text, 0, text.length());
    }
    catch (Exception ex)
    {
      debug("failed");
      return false;
    }
    finally
    {
      try
      {
        out.close();
      }
      catch (Exception ex)
      {
      };
    }
    debug("ok");
    return true;
  }

  /**
   * Reads a text file into a string.
   * @param filePath a relative or absolute path to the file
   * @return the string containing the file data; null, if the the process fails
   */
  public static String readTextFile(File filePath)
  {
    debug("readTextFile() with filePath = " + filePath);
    if (!filePath.exists() || filePath.isDirectory())
      return null;

    int ch;
    BufferedReader in = null;
    StringBuffer sb = new StringBuffer();
    try
    {
      in =
        new BufferedReader(new FileReader(filePath));
      while ((ch = in.read()) != -1)
      {
        sb.append((char)ch);
      }
      in.close();
    }
    catch (IOException ex)
    {
      return null;
    }
    finally
    {
      try
      {
        in.close();
      }
      catch (Exception ex)
      {
      };
    }
    return sb.toString();
  }

  /**
   * Reads a text resource from the application JAR file.
   * @param relPath a relative path to the resource file starting from the 
   * root of the JAR archive
   * @return the String containing the file data; null, if the process fails
   */
  public static String readTextResource(String relPath)
  {
    debug("readTextResource() with relPath = " + relPath);
    URL url = Thread.currentThread().getContextClassLoader().
      getResource(relPath);
    InputStream is = null;
    try
    {
      is = url.openStream();  // url may be null
    }
    catch (Exception ex)
    {
    }
    if (is == null)
    {
      debug("Resource not loaded");
      return null;
    }
    int ch;
    StringBuffer sb = new StringBuffer();
    try
    {
      BufferedReader in = new BufferedReader(new InputStreamReader(is));
      while ((ch = in.read()) != -1)
      {
        sb.append((char)ch);
      }
      in.close();
    }
    catch (IOException ex)
    {
      return null;
    }
    finally
    {
      try
      {
        is.close();
      }
      catch (Exception ex)
      {
      };
    }
    return sb.toString();
  }

  /**
   * Loads a binary resource from the application JAR file,<br>e.g. get
   * a BufferedImage reference from a image resource with bi = ImageIO.read(openResource(path)).
   * @param relPath a relative path to the resource file starting 
   * from the root of the JAR archive
   * @return the InputStream reference to the file data; null, if the process fails
   */
  public static InputStream openResource(String relPath)
  {
    debug("openResource() with relPath = " + relPath);
    URL url = Thread.currentThread().getContextClassLoader().
      getResource(relPath);
    InputStream is = null;
    try
    {
      is = url.openStream();  // url may be null
    }
    catch (Exception ex)
    {
    }
    if (is == null)
      debug("Resource not loaded");
    return is;
  }

  /**
   * Closes the given input stream.
   * @param is the stream to close
   */
  public static void closeInputStream(InputStream is)
  {
    try
    {
      is.close();
    }
    catch (Exception ex)
    {
    }
  }

  /**
   * Closes the given output stream.
   * @param os the stream to close
   */
  public static void closeOutputStream(OutputStream os)
  {
    try
    {
      os.close();
    }
    catch (Exception ex)
    {
    }
  }

  /**
   * <b>(Be very careful)</b> Deletes all files in directory 
   * (but not the directory itself).
   * @param dirPath path to a directory. A trailing file separator is ignored
   * @param clean if true, all subdirectories are removed also; otherwise only
   * files in dirPath are deleted and the subdirectories with their files are
   * maintained
   * @return true if all deletions were successful or dirPath does not exist;
   * false, if a deletion fails or dirPath does not denote a directory
   */
  public static boolean removeFiles(File dirPath, boolean clean)
  {
    debug("removeFiles() with dirPath = " + dirPath + " clean = " + clean);
    if (!dirPath.exists())
      return true;
    if (!dirPath.isDirectory())
      return false;
    if (clean)
      return _cleanDir(dirPath);
    return _removeFiles(dirPath);
  }

  private static boolean _cleanDir(File dirPath)
  {
    String[] children = dirPath.list();
    boolean ok = true;
    for (int i = 0; i < children.length; i++)
    {
      File child = new File(dirPath, children[i]);
      if (child.isDirectory())
      {
        if (!removeDir(child))
          ok = false;
      }
      else
      {
        debug("Deleting " + child.getAbsolutePath() + "...");
        if (!child.delete())
        {
          debug("failed");
          ok = false;
        }
        debug("ok");
      }
    }
    return ok;
  }

  private static boolean _removeFiles(File dirPath)
  {
    boolean ok = true;
    if (dirPath.isDirectory())
    {
      String[] children = dirPath.list();
      debug("Directory list:");
      for (String s : children)
        debug(s);

      boolean rc;
      for (int i = 0; i < children.length; i++)
      {
        File file = new File(dirPath, children[i]);
        if (!file.isDirectory())
        {
          debug("Deleting " + file.getAbsolutePath() + "...");
          rc = file.delete();
          if (!rc)
          {
            ok = false;
            debug("failed");
          }
          else
            debug("ok");
        }
      }
    }
    return ok;
  }

  /**
   * <b>(Be very careful)</b> Deletes all files and subdirectories and 
   * the directory itself.
   * @param dirPath path to a directory. A trailing file separator is ignored
   * @return true if all deletions were successful or dirPath does not exist;
   * false, if a deletion fails or dirPath does not denote a directory
   **/
  public static boolean removeDir(File dirPath)
  {
    debug("removeDir() with dirPath = " + dirPath);
    if (!dirPath.exists())
      return true;
    if (!dirPath.isDirectory())
      return false;
    success = true;
    _removeDir(dirPath);
    return success;
  }

  // Recursively called
  private static void _removeDir(File dirPath)
  {
    if (dirPath.isDirectory())
    {
      String[] children = dirPath.list();
      debug("directory list:");
      for (String s : children)
        debug(s);

      for (int i = 0; i < children.length; i++)
        _removeDir(new File(dirPath, children[i])); // Append children to dirPath
    }

    // dirPath is a file or an empty directory
    debug("Deleting " + dirPath.getAbsolutePath() + "...");
    boolean rc = dirPath.delete();
    if (rc)
      debug("ok");
    else
    {
      debug("failed");
      success = false;
    }
  }

  /**
   * Creates a directory structure.
   * @param dirPath a relative or absolute directory path that may contain
   * subdirectories, e.g. /dir/subdir1/subdir2. A trailing file separator is ignored.
   * Under Windows the absolute path may be proceeded by a drive designator (e.g. 'c:').
   * @return true, if the directory creation was successful or the directory
   * already exists; otherwise false
   */
  public static boolean createDirectoryTree(File dirPath)
  {
    debug("createDirectoryTree with dirPath = " + dirPath);
    if (dirPath.exists())
      return dirPath.isDirectory();

    String absolutePath = dirPath.getAbsolutePath().replace('\\', '/');
    String[] subdirs = split(absolutePath, "/");
    String[] subpaths = new String[subdirs.length];
    subpaths[0] = subdirs[0];
    for (int i = 1; i < subdirs.length; i++)
    {
      subpaths[i] = subpaths[i - 1] + "/" + subdirs[i];
    }
    for (int i = 1; i < subpaths.length; i++)
    {
      File fdir = new File(subpaths[i]);
      if (!fdir.exists())
      {
        debug("mkdir() for " + fdir);
        boolean rc = fdir.mkdir();
        if (!rc)
          return false;
      }
    }
    return true;
  }

  /**
   * Copies binary data from the source URL to the destination file. 
   * Creates the destination file, if it does not yet exist. 
   * An existing destination file is overwritten. 
   * The directory path of the destination file must exist.
   * @param url the source URL, e.g. "http://myserver.com/image.gif"
   * @param dest a relative or absolute file path of the destination file
   * @return true, if the copy process was successful; otherwise false
   */
  public static boolean copyFile(URL url, File dest)
  {
    debug("copyFile() with URL = " + url + "\n    and dest = " + dest);
    InputStream in = null;
    FileOutputStream fos = null;
    try
    {
      in = url.openStream();
      dest.createNewFile();
      fos = new FileOutputStream(dest);
      int chr = in.read();
      while (chr != -1)
      {
        fos.write(chr);
        chr = in.read();
      }
    }
    catch (Exception ex)
    {
      debug("Copy failed");
      return false;
    }
    finally
    {
      try
      {
        in.close();
      }
      catch (Exception ex)
      {
      };
      try
      {
        fos.close();
      }
      catch (Exception ex)
      {
      };
    }
    debug("Copy ok");
    return true;
  }

  /**
   * Copies binary data from the source file to the destination file. 
   * Creates the destination file, if it does not yet exist. 
   * An existing destination file is overwritten. 
   * The directory path of the destination file must exist.
   * @param src a relative or absolute file path of the source file
   * @param dest a relative or absolute file path of the destination file
   * @return true, if the copy process was successful; otherwise false
   */
  public static boolean copyFile(File src, File dest)
  {
    debug("copyFile() with src = " + src + "\n    and dest = " + dest);

    if (src.isDirectory() || dest.isDirectory())
    {
      debug("Copy failed-> src or dest is a directory");
      return false;
    }

    InputStream in = null;
    OutputStream out = null;
    try
    {
      in = new FileInputStream(src.getPath());
      out = new FileOutputStream(dest.getPath());

      // Transfer bytes from in to out
      byte[] buf = new byte[1024];
      int len;
      while ((len = in.read(buf)) > 0)
      {
        out.write(buf, 0, len);
      }
    }
    catch (IOException ex)
    {
      debug("Copy failed");
      return false;
    }
    finally
    {
      try
      {
        in.close();
      }
      catch (Exception ex)
      {
      }
      try
      {
        out.close();
      }
      catch (Exception ex)
      {
      }
    }
    debug("Copy ok");
    return true;
  }

  /**
   * Copies all files from the source directory into the destination directory. If the
   * destination directory does not yet exist, it is created (with subdirectory
   * structure). Existing files are overwritten.
   * @param srcDir the source directory (absolute or relative)
   * @param destDir the destination directory (absolute or relative)
   * @param collect if true, all files in subdirectories of the source 
   * directory are copied to the <b>root</b> of the destination directory 
   * (use copyTree() to maintain the subdirectory structure under the destination
   * directory)
   * @return true, if the all files were successfully copied; otherwise false
   */
  public static boolean copyDirectory(File srcDir, File destDir, boolean collect)
  {
    debug("copyDirectory() with src = " + srcDir + "\n    and dest = " + destDir
      + " collect = " + collect);
    if (!srcDir.exists())
      return false;
    if (!destDir.exists())
    {
      debug("Creating destination dir...");
      boolean ok = createDirectoryTree(destDir);
      if (!ok)
      {
        debug("failed");
        return false;
      }
      debug("ok");
    }
    String[] children = srcDir.list();
    success = true;
    for (int i = 0; i < children.length; i++)
    {
      File file = new File(srcDir, children[i]);
      if (collect && file.isDirectory())
        doCollect(file, destDir);
      else
      {
        if (!copyFile(file, new File(destDir, children[i])))
          success = false;
      }
    }
    return success;
  }

  // Recursively called
  private static void doCollect(File srcDir, File destDir)
  {
    debug("doCollect() with src = " + srcDir + "\n    and dest = " + destDir);
    String[] children = srcDir.list();
    for (int i = 0; i < children.length; i++)
    {
      File file = new File(srcDir, children[i]);
      if (file.isDirectory())
        doCollect(file, destDir);
      else
      {
        if (!copyFile(file, new File(destDir, children[i])))
          success = false;
      }
    }
  }

  /**
   * Copies all files with subdirectory structure from the source directory to 
   * the destination directory. If the destination directory does not exist, 
   * it is created. Existing files are overwritten.
   * @param srcDir the source directory (absolute or relative)
   * @param destDir the destination directory (absolute or relative)
   * @return true, if the all files were successfully copied; otherwise false
   */
  public static boolean copyTree(File srcDir, File destDir)
  {
    debug("copyTree() with src = " + srcDir + "\n    and dest = " + destDir);
    if (!srcDir.exists())
      return false;
    if (!destDir.exists())
    {
      debug("Creating destination dir...");
      boolean ok = createDirectoryTree(destDir);
      if (!ok)
      {
        debug("failed");
        return false;
      }
      debug("ok");
    }
    treeSrc = srcDir;
    treeDest = destDir;
    success = true;
    doCopy(new File(""));
    return success;
  }

  // Recursively called
  private static void doCopy(File partialSrc)
  {
    debug("doCopy() with partialSrc = " + partialSrc);
    File currentSrcPath = new File(treeSrc.getAbsolutePath(), partialSrc.getPath());
    File currentDestPath = new File(treeDest.getAbsolutePath(), partialSrc.getPath());
    debug("currentSrcPath = " + currentSrcPath.getAbsolutePath());
    debug("currentDestPath = " + currentDestPath.getAbsolutePath());
    String[] children = currentSrcPath.list();
    for (int i = 0; i < children.length; i++)
    {
      File srcFile = new File(currentSrcPath, children[i]);
      File destFile = new File(currentDestPath, children[i]);
      if (srcFile.isDirectory())
      {
        boolean rc = destFile.mkdir();
        if (!rc)
          success = false;
        doCopy(new File(partialSrc, children[i]));
      }
      else
      {
        if (!copyFile(srcFile, destFile))
          success = false;
      }
    }
  }

  /**
   * Returns number of files (subdirectories are not counted) in given directory.
   * @param dirPath path to a directory. A trailing file separator is ignored
   * @return the total number of files, 0 if
   * dirPath is not a directory, does not exist or contains no files
   */
  public static int getNbFiles(File dirPath)
  {
    if (!dirPath.isDirectory() || !dirPath.exists())
      return 0;
    int nb = 0;
    String[] files = dirPath.list();
    for (int i = 0; i < files.length; i++)
    {
      if (!new File(dirPath, files[i]).isDirectory())
        nb++;
    }
    return nb;
  }

  /**
   * Returns true, if the directory is empty (no files and no subdirectories).
   * @param dirPath path to a directory. A trailing file separator is ignored
   * @return true, if dirPath is not a directory, does not exist or is empty; otherwise false 
   */
  public static boolean isDirectoryEmpty(File dirPath)
  {
    if (!dirPath.isDirectory() || !dirPath.exists())
      return true;
    String[] files = dirPath.list();
    if (files.length == 0)
      return true;
    return false;
  }

  /**
   * Unpacks the given JAR archive in the given directory. If the directory does not
   * exist, the directory with its parent directories is created.   
   * (An empty JAR archive is illegal.)
   * @param jarFile the JAR to unpack
   * @param dirPath path to a directory. A trailing file separator is ignored
   * @return true, if the JAR is successfully unpacked; otherwise false
   */
  public static boolean unpack(File jarFile, File dirPath)
  {
    debug("unpack() with jarFile = " + jarFile + "\n   dirPath = " + dirPath);
    if (!jarFile.exists() || jarFile.isDirectory())
      return false;

    if (dirPath.isFile())
      return false;
    if (!dirPath.exists())
    {
      boolean rc = createDirectoryTree(dirPath);
      if (!rc)
        return false;
    }

    final int BUFFER = 2048;
    //  Must create Manifest directory manually (why?)
    File manifestDir = new File(dirPath, "META-INF");
    manifestDir.mkdir();
    try
    {
      BufferedOutputStream bos = null;
      BufferedInputStream is = null;
      JarEntry entry;
      JarFile jarfile = new JarFile(jarFile);
      Enumeration e = jarfile.entries();
      while (e.hasMoreElements())
      {
        entry = (JarEntry)e.nextElement();
        if (entry.isDirectory())
        {
          File f = new File(dirPath, entry.getName());
          f.mkdir();
          continue;
        }
        is = new BufferedInputStream(jarfile.getInputStream(entry));
        int count;
        byte data[] = new byte[BUFFER];
        FileOutputStream fos =
          new FileOutputStream(dirPath.getAbsolutePath() + fs + entry.getName());
        bos = new BufferedOutputStream(fos, BUFFER);
        while ((count = is.read(data, 0, BUFFER)) != -1)
        {
          bos.write(data, 0, count);
        }
        bos.flush();
        bos.close();
        is.close();
      }
      jarfile.close();
    }
    catch (IOException ex)
    {
      return false;
    }
    return true;
  }

  /**
   * Creates a compressed JAR archive from a all files and subdirectories
   * in directory with given dirPath.
   * @param jarFile the JAR archive to create
   * @param dirPath path to the directory that holds the 
   * files/subdirectories to add to the archive. An emtpy directory is illegal. 
   * A trailing file separator is ignored
   * @return true, if successful; otherwise false
   */
  public static boolean pack(File jarFile, File dirPath)
  {
    debug("pack() with jarFile = " + jarFile + "\n   dirPath = " + dirPath);
    fileList.clear();  // Do not forget, it is static!
 
    if (isDirectoryEmpty(dirPath))
      return false;

    createFileList(dirPath);
    File[] files = new File[fileList.size()];
    int i = 0;
    for (File file : fileList)
      files[i++] = file;
    return doPack(jarFile, files, dirPath);
  }

  private static void createFileList(File dir)
  {
    if (dir.isDirectory())
    {
      String[] children = dir.list();
      for (int i = 0; i < children.length; i++)
        createFileList(new File(dir, children[i]));
    }
    else
      fileList.add(dir);
  }

  private static boolean doPack(File archiveFile, File[] tobeJared, File workingDir)
  {
    long systime = new Date().getTime();

    try
    {
      FileOutputStream stream = new FileOutputStream(archiveFile);
      JarOutputStream out = new JarOutputStream(stream);

      for (int i = 0; i < tobeJared.length; i++)
      {
        // Add archive entry
        String entryName = tobeJared[i].getPath();
        int workingDirLength = workingDir.getPath().length();
        entryName = entryName.substring(workingDirLength + 1);
        FileInputStream fis = new FileInputStream(tobeJared[i]);
        byte[] fileData = new byte[new Long(tobeJared[i].length()).intValue()];
        fis.read(fileData);
        fis.close();
        entryName = entryName.replaceAll("\\\\+", "/");  // Must insert forward slashes!!!
        ZipEntry zipentry = new ZipEntry(entryName);
        zipentry.setSize(fileData.length);
        zipentry.setTime(systime);
        zipentry.setMethod(ZipEntry.DEFLATED);
        CRC32 crc = new CRC32();	// ZIP formatted data
        crc.update(fileData);
        zipentry.setCrc(crc.getValue());

        out.putNextEntry(zipentry);
        out.write(fileData);
        out.flush();
        out.closeEntry();
      }

      out.finish();
      out.close();
      stream.close();
      return true;
    }
    catch (IOException ex)
    {
      return false;
    }
  }

  /**
   * Unpacks the given ZIP archive in the given directory. If the directory does not
   * exist, the directory with its parent directories is created. 
   * (An empty ZIP archive is illegal.)
   * @param zipFile the ZIP to unpack
   * @param dirPath path to a directory. A trailing file separator is ignored
   * @return true, if the ZIP is successfully unpacked; otherwise false
   */
  public static boolean unzip(File zipFile, File dirPath)
  {
    debug("unzip() with zipFile = " + zipFile + "\n   dirPath = " + dirPath);
    if (!zipFile.exists() || zipFile.isDirectory())
      return false;

    if (dirPath.isFile())
      return false;

    if (!dirPath.exists())
    {
      boolean rc = createDirectoryTree(dirPath);
      if (!rc)
        return false;
    }

    ZipFile zip = null;
    InputStream inputStream = null;
    try
    {
      zip = new ZipFile(zipFile);
      Enumeration<? extends ZipEntry> myEnum = zip.entries();
      while (myEnum.hasMoreElements())
      {
        ZipEntry zipEntry = myEnum.nextElement();
        if (zipEntry.isDirectory())
        {
          File destDirFile = new File(dirPath, zipEntry.getName());
          destDirFile.mkdirs();
        }
        else
        {
          File destFile = new File(dirPath, zipEntry.getName());
          inputStream = zip.getInputStream(zipEntry);
          writeStream(inputStream, destFile);
        }
      }
    }
    catch (IOException ex)
    {
      return false;
    }
    finally
    {
      try
      {
        zip.close();
      }
      catch (Exception ex)
      {
      }
      try
      {
        inputStream.close();
      }
      catch (Exception ex)
      {
      }
    }
    return true;
  }

  /**
   * Creates a ZIP archive from a all files and subdirectories
   * in directory with given dirPath. 
   * @param zipFile the ZIP archive to create
   * @param dirPath path to the directory that holds the 
   * files/subdirectories to add to the archive. An empty directory is illegal.
   *  A trailing file separator is ignored
   * @return true, if successful; otherwise false
   */
  public static boolean zip(File zipFile, File dirPath)
  {
    debug("zip() with dirName = " + zipFile + "\n   dirPath = " + dirPath);
    return pack(zipFile, dirPath);
  }

  private static void writeStream(InputStream is, File dest)
  {
    BufferedInputStream bis = null;
    BufferedOutputStream bos = null;
    try
    {
      bis = new BufferedInputStream(is);
      bos = new BufferedOutputStream(new FileOutputStream(dest));

      int byteData;
      while ((byteData = bis.read()) != -1)
        bos.write((byte)byteData);
    }
    catch (IOException ex)
    {
    }
    finally
    {
      try
      {
        bos.close();
      }
      catch (Exception ex)
      {
      }
      try
      {
        bis.close();
      }
      catch (Exception ex)
      {
      }
    }
  }

  private static String[] split(String s, String separator)
  // Splits given string with given separator string into multiple strings.
  // Returns splitted string array (without separator).<br>
  // Returned array has length 0, if input string or separator is null or empty.<br>
  // A trailing separator is stripped.
  {
    if (s == null || s.length() == 0
      || separator == null || separator.length() == 0)
      return new String[0];

    // Strip trailing separator
    int last = s.lastIndexOf(separator);
    if (s.length() >= separator.length()
      && last == s.length() - separator.length())
      s = s.substring(0, last);

    if (s.length() == 0)
      return new String[0];

    Vector<String> nodes = new Vector<String>();

    // Parse nodes into vector
    int index = s.indexOf(separator);
    while (index >= 0)
    {
      nodes.addElement(s.substring(0, index));
      s = s.substring(index + separator.length());
      index = s.indexOf(separator);
    }

    // Get the last node
    nodes.addElement(s);

    // Create splitted string array
    String[] result = new String[nodes.size()];
    if (nodes.size() > 0)
    {
      for (int i = 0; i < nodes.size(); i++)
        result[i] = (String)nodes.elementAt(i);
    }
    return result;
  }

  private static void debug(String msg)
  {
    if (debug)
      System.out.println("GGFile debug: " + msg);
  }

}
