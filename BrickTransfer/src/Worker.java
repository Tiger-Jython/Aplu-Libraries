// Worker.java

import ch.aplu.util.FilePath;
import static ch.aplu.util.FilePath.isDirectoryEmpty;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.jar.JarOutputStream;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import lejos.remote.ev3.RemoteRequestMenu;

public class Worker
{
  private String tmpDir = System.getProperty("java.io.tmpdir");
  private String tempFolder = "___ev3_jar_maker";
  private final String fs = System.getProperty("file.separator");
  private String classFolder;
  private String appName;
  private String mainClass;
  private String ipAddress;
  private BrickTransfer app;
  private ArrayList<File> fileList = new ArrayList<File>();
  private boolean doRun;

  public Worker(BrickTransfer app, String[] args, boolean doRun)
  {
    fileList.clear();
    this.app = app;
    classFolder = args[0];
    mainClass = args[1];
    appName = args[2];
    ipAddress = args[3];
    this.doRun = doRun;
    System.out.println("Worker starting with:");
    System.out.println("class folder: " + classFolder);
    System.out.println("main class: " + mainClass);
    System.out.println("app name: " + appName);
    System.out.println("IP address: " + ipAddress);
    System.out.println("doRun: " + doRun);

    // Remove trailing / or \
    String tmp = tmpDir.replace('\\', '/');
    System.out.println(tmp);
    if (tmp.charAt(tmp.length() - 1) == '/')
      tmpDir = tmpDir.substring(0, tmpDir.length() - 1);

    boolean rc;
    String msg;
    System.out.println("Checking for temp directory subdir '" + tempFolder + "'");
    String absTempDir = tmpDir + fs + tempFolder;
    File fabsTempDir = new File(fs + absTempDir + fs);  // Escaped for spaces in name
    if (!fabsTempDir.exists())
    {
      System.out.println("Must create directory");
      rc = fabsTempDir.mkdir();
      if (!rc)
      {
        msg = "Fail to create " + fabsTempDir;
        System.out.println(msg);
        app.showStatus(msg);
        return;
      }
    }
    else
    {
      // Remove all files (but not directories)
      FilePath.removeFiles(fabsTempDir, true);
      msg = "Cleaning directory...Done";
      System.out.println(msg);
    }

    msg = "Collecting classes...";
    app.showStatus(msg);
    BrickTransfer.delay(1000);  // Time to show processing message
    System.out.println(msg);

    // Copy only .class files
    rc = FilePath.copyTree(new File(classFolder), fabsTempDir, "class");
    if (!rc)
    {  
       msg = msg + "failed";
       app.showStatus(msg);
       System.out.println(msg);
       return;
    } 

    msg = "Creating JAR...";
    app.showStatus(msg);
    BrickTransfer.delay(1000);

    System.out.println("Creating MANIFEST");
    // Create Manifest
    String absManifestDir = absTempDir + fs + "META-INF";
    File fabsManifestDir = new File(absManifestDir);
    fabsManifestDir.mkdir();
    String text
      = "Manifest-Version: 1.0\n"
      + "Created-By: 1.7.0_21-b11 (Oracle Corporation)\n"
      + "Main-Class: " + mainClass + "\n\n";
    FilePath.writeTextFile(new File(absManifestDir + fs + "MANIFEST.MF"),
      text, false);

    String jarArchive = appName + ".jar";
    System.out.println("Creating JAR archive '" + jarArchive + "'");
    String absJarArchive = absTempDir + fs + jarArchive;
    rc = FilePath.pack(new File(absJarArchive), fabsTempDir);
    if (!rc)
    {
      msg = "JAR packing failed";
      app.showStatus(msg);
      System.out.println(msg);
      return;
    }

    msg = "Downloading to EV3...";
    app.showStatus(msg);
    BrickTransfer.delay(1000);
    System.out.println("Downloading to EV3");
    EV3Copy ev3Copy
      = new EV3Copy(ipAddress, absJarArchive, "/home/lejos/programs/" + jarArchive);
    if (ev3Copy.copy())
    {
      if (doRun)
      {
        msg = "Running program now";
        System.out.println(msg);
        app.showStatus(msg);
        try
        {
          RemoteRequestMenu menu = new RemoteRequestMenu(ipAddress);
          menu.runProgram(appName);
        }
        catch (IOException ex)
        {
          msg = "Execution failed";
          System.out.println(msg);
          app.showStatus(msg);
        }
      } 
      else
      {
        msg = "Success. Start app now!";
        System.out.println(msg);
        app.showStatus(msg);
      }
    }
    else
    {
      msg = "Download failed";
      System.out.println(msg);
      app.showStatus(msg);
    }
  }

  public boolean pack(File jarFile, File dirPath)
  {
    if (isDirectoryEmpty(dirPath))
      return false;

    createFileList(dirPath);
    File[] files = new File[fileList.size()];
    int i = 0;
    for (File file : fileList)
      files[i++] = file;
    return doPack(jarFile, files, dirPath);
  }

  private void createFileList(File dir)
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

  private boolean doPack(File archiveFile, File[] tobeJared, File workingDir)
  {
    long systime = new Date().getTime();
    System.out.println("doPack " + archiveFile.exists());

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
        System.out.println("ok10");
        fis.read(fileData);
        fis.close();
        System.out.println("ok11");
        entryName = entryName.replaceAll("\\\\+", "/");  // Must insert forward slashes!!!
        ZipEntry zipentry = new ZipEntry(entryName);
        System.out.println("ok12");
        zipentry.setSize(fileData.length);
        zipentry.setTime(systime);
        zipentry.setMethod(ZipEntry.DEFLATED);
        CRC32 crc = new CRC32();	// ZIP formatted data
        crc.update(fileData);
        zipentry.setCrc(crc.getValue());
        System.out.println("ok13");

        out.putNextEntry(zipentry);
        System.out.println("ok14");
        out.write(fileData);
        out.flush();
        out.closeEntry();
      }

      System.out.println("ok1");
      out.finish();
      System.out.println("ok2");
      out.close();
      System.out.println("ok3");
      stream.close();
      return true;
    }
    catch (IOException ex)
    {
      System.out.println("ex" + ex);
      return false;
    }
  }

}
