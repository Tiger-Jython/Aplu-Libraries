package ch.aplu.mbm;

import java.io.*;
import java.nio.file.Path;

public class USBDeviceIdentifier {

    /**
     * Tries to identify the device connected at the given path.  The path should be the root-directory of either
     * a micro:bit or a Calliope mini.
     *
     * @param path  The root path of the device (micro:bit or Calliope mini).
     * @return      The device and version or `Unknown` if there is not sufficient evidence or information.
     */
    public static USBDevice identify(String path) {
        return identify(new File(path));
    }

    /**
     * Tries to identify the device connected at the given path.  The path should be the root-directory of either
     * a micro:bit or a Calliope mini.
     *
     * @param path  The root path of the device (micro:bit or Calliope mini).
     * @return      The device and version or `Unknown` if there is not sufficient evidence or information.
     */
    public static USBDevice identify(Path path) {
        return identify(path.toFile());
    }

    /**
     * Tries to identify the device connected at the given path.  The path should be the root-directory of either
     * a micro:bit or a Calliope mini.
     *
     * @param path  The root path of the device (micro:bit or Calliope mini).
     * @return      The device and version or `Unknown` if there is not sufficient evidence or information.
     */
    public static USBDevice identify(File path) {
        Integer interfaceVersion = null;
        // Integer bootloaderVersion = null;
        String detailsName = "DETAILS.TXT";
        Boolean deviceIsCalliope = null;

        String[] fileList = path.list();
        if (fileList == null)
            return USBDevice.Unknown;

        for (String s : fileList) {
            if (s.toUpperCase().equals("DETAILS.TXT"))
                detailsName = s;
            s = s.toUpperCase();
            if (s.endsWith("MINI.HTM") || s.endsWith("MINI.HTML"))
                deviceIsCalliope = true;
            else if (s.endsWith("MICROBIT.HTM") || s.endsWith("MICROBIT.HTML"))
                deviceIsCalliope = false;
        }

        if (deviceIsCalliope == null)
            for (String s : fileList)
                if (s.toUpperCase().endsWith(".HTM") || s.toUpperCase().endsWith(".HTML")) {
                    deviceIsCalliope = extractIsDeviceCalliopeFromLink(new File(path, s));
                    if (deviceIsCalliope != null)
                        break;
                }
        if (deviceIsCalliope == null)
            deviceIsCalliope = false;

        File details = new File(path, detailsName);
        if (details.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(details))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.toLowerCase();
                    if (line.startsWith("interface version: ")) {
                        interfaceVersion = extractVersion(line);
                    }
                    // The Bootloader version is also >= 255 for MicroBit V2.
                    // We could use this to confirm/refine the detection of the version if the interface alone
                    // should prove insufficient.
                    /*else if (line.startsWith("bootloader version: ")) {
                        bootloaderVersion = extractVersion(line);
                    }*/
                }
            } catch (IOException e) {
                return USBDevice.Unknown;
            }
        }

        if (deviceIsCalliope) {
            // TODO: make sure we can differentiate between calliope V1 and V2
            // This here is just an educated guess since the Calliope Mini seems to mirror the Micro:bit
            if (interfaceVersion == null || interfaceVersion < 255)
                return USBDevice.CalliopeV1;
            else
                return USBDevice.CalliopeV2;
        } else {
            if (interfaceVersion == null || interfaceVersion < 255)
                return USBDevice.MicroBitV1;
            else
                return USBDevice.MicroBitV2;
        }
    }

    private static Integer extractVersion(String line) {
        int i = line.indexOf(':') + 1;
        while (i < line.length() && line.charAt(i) == ' ')
            i++;
        while (i < line.length() && line.charAt(i) == '0')
            i++;
        int j = i;
        while (j < line.length() && Character.isDigit(line.charAt(j)))
            j++;
        try {
            return Integer.parseInt(line.substring(i, j));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Boolean extractIsDeviceCalliopeFromLink(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.toLowerCase();
                if (line.contains("https://calliope.cc/"))
                    return true;
                if (line.contains("https://microbit.org/"))
                    return false;
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }
    
    public static void main(String[] args)
  {
    System.out.println("Device type detected: " + Tools.deviceType);
    System.out.println("Is MicroBit V2?       " + Tools.isMicrobitV2);
  }
}
