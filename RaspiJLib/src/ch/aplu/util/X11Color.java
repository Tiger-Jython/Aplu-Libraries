// X11Color.java

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

import java.awt.Color;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Random;

/** 
 * This class provides the mapping of X11 colors names (in string notation)
 * to RGB colors represented the java.awt.Color class. 
 * See<br><br>http://en.wikipedia.org/wiki/X11_color_names<br><br>  
 * for the names and a view of the colors.
 */
public class X11Color extends java.awt.Color
{
  private static int[] RAINBOW_COLORS_INT =
  {
    0x170027, 0x25003D, 0x36005F, 0x450081,
    0x5000A4, 0x5300C1, 0x4900D4, 0x3800E0,
    0x2803EB, 0x1917F2, 0x0D3FF8, 0x0669F5,
    0x0191E5, 0x00B4C6, 0x00D09D, 0x00D972,
    0x00D148, 0x00C624, 0x00BC0C, 0x00B802,
    0x01B700, 0x07B900, 0x18BE00, 0x3DC900,
    0x68D300, 0x92D800, 0xBCD600, 0xE0CA00,
    0xFAB500, 0xFF9900, 0xFF7800, 0xFF5B00,
    0xFF4300, 0xFF3000, 0xFF2100, 0xF01500,
    0xCC0D00, 0xA10600, 0x770200, 0x4B0000
  };
  
  public static Color[] RAINBOW_COLORS = new Color[RAINBOW_COLORS_INT.length];
  
  private static final Hashtable<String, Color> table = new Hashtable<String, Color>();

  static
  {
    for (int i = 0; i < RAINBOW_COLORS_INT.length; i++)
      RAINBOW_COLORS[i] = new Color(RAINBOW_COLORS_INT[i]);
    
    table.put("aqua", new Color(0, 255, 255));
    table.put("cornflower", new Color(100, 149, 237));
    table.put("crimson", new Color(220, 20, 60));
    table.put("fuchsia", new Color(255, 0, 255));
    table.put("indigo", new Color(75, 0, 130));
    table.put("lime", new Color(50, 205, 50));
    table.put("silver", new Color(192, 192, 192));
    table.put("ghost white", new Color(248, 248, 255));
    table.put("snow", new Color(255, 250, 250));
    table.put("ghostwhite", new Color(248, 248, 255));
    table.put("white smoke", new Color(245, 245, 245));
    table.put("whitesmoke", new Color(245, 245, 245));
    table.put("gainsboro", new Color(220, 220, 220));
    table.put("floral white", new Color(255, 250, 240));
    table.put("floralwhite", new Color(255, 250, 240));
    table.put("old lace", new Color(253, 245, 230));
    table.put("oldlace", new Color(253, 245, 230));
    table.put("linen", new Color(250, 240, 230));
    table.put("antique white", new Color(250, 235, 215));
    table.put("antiquewhite", new Color(250, 235, 215));
    table.put("papaya whip", new Color(255, 239, 213));
    table.put("papayawhip", new Color(255, 239, 213));
    table.put("blanched almond", new Color(255, 235, 205));
    table.put("blanchedalmond", new Color(255, 235, 205));
    table.put("bisque", new Color(255, 228, 196));
    table.put("peach puff", new Color(255, 218, 185));
    table.put("peachpuff", new Color(255, 218, 185));
    table.put("navajo white", new Color(255, 222, 173));
    table.put("navajowhite", new Color(255, 222, 173));
    table.put("moccasin", new Color(255, 228, 181));
    table.put("cornsilk", new Color(255, 248, 220));
    table.put("ivory", new Color(255, 255, 240));
    table.put("lemon chiffon", new Color(255, 250, 205));
    table.put("lemonchiffon", new Color(255, 250, 205));
    table.put("seashell", new Color(255, 245, 238));
    table.put("honeydew", new Color(240, 255, 240));
    table.put("mint cream", new Color(245, 255, 250));
    table.put("mintcream", new Color(245, 255, 250));
    table.put("azure", new Color(240, 255, 255));
    table.put("alice blue", new Color(240, 248, 255));
    table.put("aliceblue", new Color(240, 248, 255));
    table.put("lavender", new Color(230, 230, 250));
    table.put("lavender blush", new Color(255, 240, 245));
    table.put("lavenderblush", new Color(255, 240, 245));
    table.put("misty rose", new Color(255, 228, 225));
    table.put("mistyrose", new Color(255, 228, 225));
    table.put("white", new Color(255, 255, 255));
    table.put("black", new Color(0, 0, 0));
    table.put("dark slate gray", new Color(47, 79, 79));
    table.put("darkslategray", new Color(47, 79, 79));
    table.put("dark slate grey", new Color(47, 79, 79));
    table.put("darkslategrey", new Color(47, 79, 79));
    table.put("dim gray", new Color(105, 105, 105));
    table.put("dimgray", new Color(105, 105, 105));
    table.put("dim grey", new Color(105, 105, 105));
    table.put("dimgrey", new Color(105, 105, 105));
    table.put("slate gray", new Color(112, 128, 144));
    table.put("slategray", new Color(112, 128, 144));
    table.put("slate grey", new Color(112, 128, 144));
    table.put("slategrey", new Color(112, 128, 144));
    table.put("light slate gray", new Color(119, 136, 153));
    table.put("lightslategray", new Color(119, 136, 153));
    table.put("light slate grey", new Color(119, 136, 153));
    table.put("lightslategrey", new Color(119, 136, 153));
    table.put("gray", new Color(190, 190, 190));
    table.put("grey", new Color(190, 190, 190));
    table.put("light grey", new Color(211, 211, 211));
    table.put("lightgrey", new Color(211, 211, 211));
    table.put("light gray", new Color(211, 211, 211));
    table.put("lightgray", new Color(211, 211, 211));
    table.put("midnight blue", new Color(25, 25, 112));
    table.put("midnightblue", new Color(25, 25, 112));
    table.put("navy", new Color(0, 0, 128));
    table.put("navy blue", new Color(0, 0, 128));
    table.put("navyblue", new Color(0, 0, 128));
    table.put("cornflower blue", new Color(100, 149, 237));
    table.put("cornflowerblue", new Color(100, 149, 237));
    table.put("dark slate blue", new Color(72, 61, 139));
    table.put("darkslateblue", new Color(72, 61, 139));
    table.put("slate blue", new Color(106, 90, 205));
    table.put("slateblue", new Color(106, 90, 205));
    table.put("medium slate blue", new Color(123, 104, 238));
    table.put("mediumslateblue", new Color(123, 104, 238));
    table.put("light slate blue", new Color(132, 112, 255));
    table.put("lightslateblue", new Color(132, 112, 255));
    table.put("medium blue", new Color(0, 0, 205));
    table.put("mediumblue", new Color(0, 0, 205));
    table.put("royal blue", new Color(65, 105, 225));
    table.put("royalblue", new Color(65, 105, 225));
    table.put("blue", new Color(0, 0, 255));
    table.put("dodger blue", new Color(30, 144, 255));
    table.put("dodgerblue", new Color(30, 144, 255));
    table.put("deep sky blue", new Color(0, 191, 255));
    table.put("deepskyblue", new Color(0, 191, 255));
    table.put("sky blue", new Color(135, 206, 235));
    table.put("skyblue", new Color(135, 206, 235));
    table.put("light sky blue", new Color(135, 206, 250));
    table.put("lightskyblue", new Color(135, 206, 250));
    table.put("steel blue", new Color(70, 130, 180));
    table.put("steelblue", new Color(70, 130, 180));
    table.put("light steel blue", new Color(176, 196, 222));
    table.put("lightsteelblue", new Color(176, 196, 222));
    table.put("light blue", new Color(173, 216, 230));
    table.put("lightblue", new Color(173, 216, 230));
    table.put("powder blue", new Color(176, 224, 230));
    table.put("powderblue", new Color(176, 224, 230));
    table.put("pale turquoise", new Color(175, 238, 238));
    table.put("paleturquoise", new Color(175, 238, 238));
    table.put("dark turquoise", new Color(0, 206, 209));
    table.put("darkturquoise", new Color(0, 206, 209));
    table.put("medium turquoise", new Color(72, 209, 204));
    table.put("mediumturquoise", new Color(72, 209, 204));
    table.put("turquoise", new Color(64, 224, 208));
    table.put("cyan", new Color(0, 255, 255));
    table.put("light cyan", new Color(224, 255, 255));
    table.put("lightcyan", new Color(224, 255, 255));
    table.put("cadet blue", new Color(95, 158, 160));
    table.put("cadetblue", new Color(95, 158, 160));
    table.put("medium aquamarine", new Color(102, 205, 170));
    table.put("mediumaquamarine", new Color(102, 205, 170));
    table.put("aquamarine", new Color(127, 255, 212));
    table.put("dark green", new Color(0, 100, 0));
    table.put("darkgreen", new Color(0, 100, 0));
    table.put("dark olive green", new Color(85, 107, 47));
    table.put("darkolivegreen", new Color(85, 107, 47));
    table.put("dark sea green", new Color(143, 188, 143));
    table.put("darkseagreen", new Color(143, 188, 143));
    table.put("sea green", new Color(46, 139, 87));
    table.put("seagreen", new Color(46, 139, 87));
    table.put("medium sea green", new Color(60, 179, 113));
    table.put("mediumseagreen", new Color(60, 179, 113));
    table.put("light sea green", new Color(32, 178, 170));
    table.put("lightseagreen", new Color(32, 178, 170));
    table.put("pale green", new Color(152, 251, 152));
    table.put("palegreen", new Color(152, 251, 152));
    table.put("spring green", new Color(0, 255, 127));
    table.put("springgreen", new Color(0, 255, 127));
    table.put("lawn green", new Color(124, 252, 0));
    table.put("lawngreen", new Color(124, 252, 0));
    table.put("green", new Color(0, 255, 0));
    table.put("chartreuse", new Color(127, 255, 0));
    table.put("medium spring green", new Color(0, 250, 154));
    table.put("mediumspringgreen", new Color(0, 250, 154));
    table.put("green yellow", new Color(173, 255, 47));
    table.put("greenyellow", new Color(173, 255, 47));
    table.put("lime green", new Color(50, 205, 50));
    table.put("limegreen", new Color(50, 205, 50));
    table.put("yellow green", new Color(154, 205, 50));
    table.put("yellowgreen", new Color(154, 205, 50));
    table.put("forest green", new Color(34, 139, 34));
    table.put("forestgreen", new Color(34, 139, 34));
    table.put("olive drab", new Color(107, 142, 35));
    table.put("olivedrab", new Color(107, 142, 35));
    table.put("dark khaki", new Color(189, 183, 107));
    table.put("darkkhaki", new Color(189, 183, 107));
    table.put("khaki", new Color(240, 230, 140));
    table.put("pale goldenrod", new Color(238, 232, 170));
    table.put("palegoldenrod", new Color(238, 232, 170));
    table.put("light goldenrod yellow", new Color(250, 250, 210));
    table.put("lightgoldenrodyellow", new Color(250, 250, 210));
    table.put("light yellow", new Color(255, 255, 224));
    table.put("lightyellow", new Color(255, 255, 224));
    table.put("yellow", new Color(255, 255, 0));
    table.put("gold", new Color(255, 215, 0));
    table.put("light goldenrod", new Color(238, 221, 130));
    table.put("lightgoldenrod", new Color(238, 221, 130));
    table.put("goldenrod", new Color(218, 165, 32));
    table.put("dark goldenrod", new Color(184, 134, 11));
    table.put("darkgoldenrod", new Color(184, 134, 11));
    table.put("rosy brown", new Color(188, 143, 143));
    table.put("rosybrown", new Color(188, 143, 143));
    table.put("indian red", new Color(205, 92, 92));
    table.put("indianred", new Color(205, 92, 92));
    table.put("saddle brown", new Color(139, 69, 19));
    table.put("saddlebrown", new Color(139, 69, 19));
    table.put("sienna", new Color(160, 82, 45));
    table.put("peru", new Color(205, 133, 63));
    table.put("burlywood", new Color(222, 184, 135));
    table.put("beige", new Color(245, 245, 220));
    table.put("wheat", new Color(245, 222, 179));
    table.put("sandy brown", new Color(244, 164, 96));
    table.put("sandybrown", new Color(244, 164, 96));
    table.put("tan", new Color(210, 180, 140));
    table.put("chocolate", new Color(210, 105, 30));
    table.put("firebrick", new Color(178, 34, 34));
    table.put("brown", new Color(165, 42, 42));
    table.put("dark salmon", new Color(233, 150, 122));
    table.put("darksalmon", new Color(233, 150, 122));
    table.put("salmon", new Color(250, 128, 114));
    table.put("light salmon", new Color(255, 160, 122));
    table.put("lightsalmon", new Color(255, 160, 122));
    table.put("orange", new Color(255, 165, 0));
    table.put("dark orange", new Color(255, 140, 0));
    table.put("darkorange", new Color(255, 140, 0));
    table.put("coral", new Color(255, 127, 80));
    table.put("light coral", new Color(240, 128, 128));
    table.put("lightcoral", new Color(240, 128, 128));
    table.put("tomato", new Color(255, 99, 71));
    table.put("orange red", new Color(255, 69, 0));
    table.put("orangered", new Color(255, 69, 0));
    table.put("red", new Color(255, 0, 0));
    table.put("hot pink", new Color(255, 105, 180));
    table.put("hotpink", new Color(255, 105, 180));
    table.put("deep pink", new Color(255, 20, 147));
    table.put("deeppink", new Color(255, 20, 147));
    table.put("pink", new Color(255, 192, 203));
    table.put("light pink", new Color(255, 182, 193));
    table.put("lightpink", new Color(255, 182, 193));
    table.put("pale violet red", new Color(219, 112, 147));
    table.put("palevioletred", new Color(219, 112, 147));
    table.put("maroon", new Color(176, 48, 96));
    table.put("medium violet red", new Color(199, 21, 133));
    table.put("mediumvioletred", new Color(199, 21, 133));
    table.put("violet red", new Color(208, 32, 144));
    table.put("violetred", new Color(208, 32, 144));
    table.put("magenta", new Color(255, 0, 255));
    table.put("violet", new Color(238, 130, 238));
    table.put("plum", new Color(221, 160, 221));
    table.put("orchid", new Color(218, 112, 214));
    table.put("medium orchid", new Color(186, 85, 211));
    table.put("mediumorchid", new Color(186, 85, 211));
    table.put("dark orchid", new Color(153, 50, 204));
    table.put("darkorchid", new Color(153, 50, 204));
    table.put("dark violet", new Color(148, 0, 211));
    table.put("darkviolet", new Color(148, 0, 211));
    table.put("blue violet", new Color(138, 43, 226));
    table.put("blueviolet", new Color(138, 43, 226));
    table.put("purple", new Color(160, 32, 240));
    table.put("medium purple", new Color(147, 112, 219));
    table.put("mediumpurple", new Color(147, 112, 219));
    table.put("thistle", new Color(216, 191, 216));
    table.put("snow1", new Color(255, 250, 250));
    table.put("snow2", new Color(238, 233, 233));
    table.put("snow3", new Color(205, 201, 201));
    table.put("snow4", new Color(139, 137, 137));
    table.put("seashell1", new Color(255, 245, 238));
    table.put("seashell2", new Color(238, 229, 222));
    table.put("seashell3", new Color(205, 197, 191));
    table.put("seashell4", new Color(139, 134, 130));
    table.put("antiquewhite1", new Color(255, 239, 219));
    table.put("antiquewhite2", new Color(238, 223, 204));
    table.put("antiquewhite3", new Color(205, 192, 176));
    table.put("antiquewhite4", new Color(139, 131, 120));
    table.put("bisque1", new Color(255, 228, 196));
    table.put("bisque2", new Color(238, 213, 183));
    table.put("bisque3", new Color(205, 183, 158));
    table.put("bisque4", new Color(139, 125, 107));
    table.put("peachpuff1", new Color(255, 218, 185));
    table.put("peachpuff2", new Color(238, 203, 173));
    table.put("peachpuff3", new Color(205, 175, 149));
    table.put("peachpuff4", new Color(139, 119, 101));
    table.put("navajowhite1", new Color(255, 222, 173));
    table.put("navajowhite2", new Color(238, 207, 161));
    table.put("navajowhite3", new Color(205, 179, 139));
    table.put("navajowhite4", new Color(139, 121, 94));
    table.put("lemonchiffon1", new Color(255, 250, 205));
    table.put("lemonchiffon2", new Color(238, 233, 191));
    table.put("lemonchiffon3", new Color(205, 201, 165));
    table.put("lemonchiffon4", new Color(139, 137, 112));
    table.put("cornsilk1", new Color(255, 248, 220));
    table.put("cornsilk2", new Color(238, 232, 205));
    table.put("cornsilk3", new Color(205, 200, 177));
    table.put("cornsilk4", new Color(139, 136, 120));
    table.put("ivory1", new Color(255, 255, 240));
    table.put("ivory2", new Color(238, 238, 224));
    table.put("ivory3", new Color(205, 205, 193));
    table.put("ivory4", new Color(139, 139, 131));
    table.put("honeydew1", new Color(240, 255, 240));
    table.put("honeydew2", new Color(224, 238, 224));
    table.put("honeydew3", new Color(193, 205, 193));
    table.put("honeydew4", new Color(131, 139, 131));
    table.put("lavenderblush1", new Color(255, 240, 245));
    table.put("lavenderblush2", new Color(238, 224, 229));
    table.put("lavenderblush3", new Color(205, 193, 197));
    table.put("lavenderblush4", new Color(139, 131, 134));
    table.put("mistyrose1", new Color(255, 228, 225));
    table.put("mistyrose2", new Color(238, 213, 210));
    table.put("mistyrose3", new Color(205, 183, 181));
    table.put("mistyrose4", new Color(139, 125, 123));
    table.put("azure1", new Color(240, 255, 255));
    table.put("azure2", new Color(224, 238, 238));
    table.put("azure3", new Color(193, 205, 205));
    table.put("azure4", new Color(131, 139, 139));
    table.put("slateblue1", new Color(131, 111, 255));
    table.put("slateblue2", new Color(122, 103, 238));
    table.put("slateblue3", new Color(105, 89, 205));
    table.put("slateblue4", new Color(71, 60, 139));
    table.put("royalblue1", new Color(72, 118, 255));
    table.put("royalblue2", new Color(67, 110, 238));
    table.put("royalblue3", new Color(58, 95, 205));
    table.put("royalblue4", new Color(39, 64, 139));
    table.put("blue1", new Color(0, 0, 255));
    table.put("blue2", new Color(0, 0, 238));
    table.put("blue3", new Color(0, 0, 205));
    table.put("blue4", new Color(0, 0, 139));
    table.put("dodgerblue1", new Color(30, 144, 255));
    table.put("dodgerblue2", new Color(28, 134, 238));
    table.put("dodgerblue3", new Color(24, 116, 205));
    table.put("dodgerblue4", new Color(16, 78, 139));
    table.put("steelblue1", new Color(99, 184, 255));
    table.put("steelblue2", new Color(92, 172, 238));
    table.put("steelblue3", new Color(79, 148, 205));
    table.put("steelblue4", new Color(54, 100, 139));
    table.put("deepskyblue1", new Color(0, 191, 255));
    table.put("deepskyblue2", new Color(0, 178, 238));
    table.put("deepskyblue3", new Color(0, 154, 205));
    table.put("deepskyblue4", new Color(0, 104, 139));
    table.put("skyblue1", new Color(135, 206, 255));
    table.put("skyblue2", new Color(126, 192, 238));
    table.put("skyblue3", new Color(108, 166, 205));
    table.put("skyblue4", new Color(74, 112, 139));
    table.put("lightskyblue1", new Color(176, 226, 255));
    table.put("lightskyblue2", new Color(164, 211, 238));
    table.put("lightskyblue3", new Color(141, 182, 205));
    table.put("lightskyblue4", new Color(96, 123, 139));
    table.put("slategray1", new Color(198, 226, 255));
    table.put("slategray2", new Color(185, 211, 238));
    table.put("slategray3", new Color(159, 182, 205));
    table.put("slategray4", new Color(108, 123, 139));
    table.put("lightsteelblue1", new Color(202, 225, 255));
    table.put("lightsteelblue2", new Color(188, 210, 238));
    table.put("lightsteelblue3", new Color(162, 181, 205));
    table.put("lightsteelblue4", new Color(110, 123, 139));
    table.put("lightblue1", new Color(191, 239, 255));
    table.put("lightblue2", new Color(178, 223, 238));
    table.put("lightblue3", new Color(154, 192, 205));
    table.put("lightblue4", new Color(104, 131, 139));
    table.put("lightcyan1", new Color(224, 255, 255));
    table.put("lightcyan2", new Color(209, 238, 238));
    table.put("lightcyan3", new Color(180, 205, 205));
    table.put("lightcyan4", new Color(122, 139, 139));
    table.put("paleturquoise1", new Color(187, 255, 255));
    table.put("paleturquoise2", new Color(174, 238, 238));
    table.put("paleturquoise3", new Color(150, 205, 205));
    table.put("paleturquoise4", new Color(102, 139, 139));
    table.put("cadetblue1", new Color(152, 245, 255));
    table.put("cadetblue2", new Color(142, 229, 238));
    table.put("cadetblue3", new Color(122, 197, 205));
    table.put("cadetblue4", new Color(83, 134, 139));
    table.put("turquoise1", new Color(0, 245, 255));
    table.put("turquoise2", new Color(0, 229, 238));
    table.put("turquoise3", new Color(0, 197, 205));
    table.put("turquoise4", new Color(0, 134, 139));
    table.put("cyan1", new Color(0, 255, 255));
    table.put("cyan2", new Color(0, 238, 238));
    table.put("cyan3", new Color(0, 205, 205));
    table.put("cyan4", new Color(0, 139, 139));
    table.put("darkslategray1", new Color(151, 255, 255));
    table.put("darkslategray2", new Color(141, 238, 238));
    table.put("darkslategray3", new Color(121, 205, 205));
    table.put("darkslategray4", new Color(82, 139, 139));
    table.put("aquamarine1", new Color(127, 255, 212));
    table.put("aquamarine2", new Color(118, 238, 198));
    table.put("aquamarine3", new Color(102, 205, 170));
    table.put("aquamarine4", new Color(69, 139, 116));
    table.put("darkseagreen1", new Color(193, 255, 193));
    table.put("darkseagreen2", new Color(180, 238, 180));
    table.put("darkseagreen3", new Color(155, 205, 155));
    table.put("darkseagreen4", new Color(105, 139, 105));
    table.put("seagreen1", new Color(84, 255, 159));
    table.put("seagreen2", new Color(78, 238, 148));
    table.put("seagreen3", new Color(67, 205, 128));
    table.put("seagreen4", new Color(46, 139, 87));
    table.put("palegreen1", new Color(154, 255, 154));
    table.put("palegreen2", new Color(144, 238, 144));
    table.put("palegreen3", new Color(124, 205, 124));
    table.put("palegreen4", new Color(84, 139, 84));
    table.put("springgreen1", new Color(0, 255, 127));
    table.put("springgreen2", new Color(0, 238, 118));
    table.put("springgreen3", new Color(0, 205, 102));
    table.put("springgreen4", new Color(0, 139, 69));
    table.put("green1", new Color(0, 255, 0));
    table.put("green2", new Color(0, 238, 0));
    table.put("green3", new Color(0, 205, 0));
    table.put("green4", new Color(0, 139, 0));
    table.put("chartreuse1", new Color(127, 255, 0));
    table.put("chartreuse2", new Color(118, 238, 0));
    table.put("chartreuse3", new Color(102, 205, 0));
    table.put("chartreuse4", new Color(69, 139, 0));
    table.put("olivedrab1", new Color(192, 255, 62));
    table.put("olivedrab2", new Color(179, 238, 58));
    table.put("olivedrab3", new Color(154, 205, 50));
    table.put("olivedrab4", new Color(105, 139, 34));
    table.put("darkolivegreen1", new Color(202, 255, 112));
    table.put("darkolivegreen2", new Color(188, 238, 104));
    table.put("darkolivegreen3", new Color(162, 205, 90));
    table.put("darkolivegreen4", new Color(110, 139, 61));
    table.put("khaki1", new Color(255, 246, 143));
    table.put("khaki2", new Color(238, 230, 133));
    table.put("khaki3", new Color(205, 198, 115));
    table.put("khaki4", new Color(139, 134, 78));
    table.put("lightgoldenrod1", new Color(255, 236, 139));
    table.put("lightgoldenrod2", new Color(238, 220, 130));
    table.put("lightgoldenrod3", new Color(205, 190, 112));
    table.put("lightgoldenrod4", new Color(139, 129, 76));
    table.put("lightyellow1", new Color(255, 255, 224));
    table.put("lightyellow2", new Color(238, 238, 209));
    table.put("lightyellow3", new Color(205, 205, 180));
    table.put("lightyellow4", new Color(139, 139, 122));
    table.put("yellow1", new Color(255, 255, 0));
    table.put("yellow2", new Color(238, 238, 0));
    table.put("yellow3", new Color(205, 205, 0));
    table.put("yellow4", new Color(139, 139, 0));
    table.put("gold1", new Color(255, 215, 0));
    table.put("gold2", new Color(238, 201, 0));
    table.put("gold3", new Color(205, 173, 0));
    table.put("gold4", new Color(139, 117, 0));
    table.put("goldenrod1", new Color(255, 193, 37));
    table.put("goldenrod2", new Color(238, 180, 34));
    table.put("goldenrod3", new Color(205, 155, 29));
    table.put("goldenrod4", new Color(139, 105, 20));
    table.put("darkgoldenrod1", new Color(255, 185, 15));
    table.put("darkgoldenrod2", new Color(238, 173, 14));
    table.put("darkgoldenrod3", new Color(205, 149, 12));
    table.put("darkgoldenrod4", new Color(139, 101, 8));
    table.put("rosybrown1", new Color(255, 193, 193));
    table.put("rosybrown2", new Color(238, 180, 180));
    table.put("rosybrown3", new Color(205, 155, 155));
    table.put("rosybrown4", new Color(139, 105, 105));
    table.put("indianred1", new Color(255, 106, 106));
    table.put("indianred2", new Color(238, 99, 99));
    table.put("indianred3", new Color(205, 85, 85));
    table.put("indianred4", new Color(139, 58, 58));
    table.put("sienna1", new Color(255, 130, 71));
    table.put("sienna2", new Color(238, 121, 66));
    table.put("sienna3", new Color(205, 104, 57));
    table.put("sienna4", new Color(139, 71, 38));
    table.put("burlywood1", new Color(255, 211, 155));
    table.put("burlywood2", new Color(238, 197, 145));
    table.put("burlywood3", new Color(205, 170, 125));
    table.put("burlywood4", new Color(139, 115, 85));
    table.put("wheat1", new Color(255, 231, 186));
    table.put("wheat2", new Color(238, 216, 174));
    table.put("wheat3", new Color(205, 186, 150));
    table.put("wheat4", new Color(139, 126, 102));
    table.put("tan1", new Color(255, 165, 79));
    table.put("tan2", new Color(238, 154, 73));
    table.put("tan3", new Color(205, 133, 63));
    table.put("tan4", new Color(139, 90, 43));
    table.put("chocolate1", new Color(255, 127, 36));
    table.put("chocolate2", new Color(238, 118, 33));
    table.put("chocolate3", new Color(205, 102, 29));
    table.put("chocolate4", new Color(139, 69, 19));
    table.put("firebrick1", new Color(255, 48, 48));
    table.put("firebrick2", new Color(238, 44, 44));
    table.put("firebrick3", new Color(205, 38, 38));
    table.put("firebrick4", new Color(139, 26, 26));
    table.put("brown1", new Color(255, 64, 64));
    table.put("brown2", new Color(238, 59, 59));
    table.put("brown3", new Color(205, 51, 51));
    table.put("brown4", new Color(139, 35, 35));
    table.put("salmon1", new Color(255, 140, 105));
    table.put("salmon2", new Color(238, 130, 98));
    table.put("salmon3", new Color(205, 112, 84));
    table.put("salmon4", new Color(139, 76, 57));
    table.put("lightsalmon1", new Color(255, 160, 122));
    table.put("lightsalmon2", new Color(238, 149, 114));
    table.put("lightsalmon3", new Color(205, 129, 98));
    table.put("lightsalmon4", new Color(139, 87, 66));
    table.put("orange1", new Color(255, 165, 0));
    table.put("orange2", new Color(238, 154, 0));
    table.put("orange3", new Color(205, 133, 0));
    table.put("orange4", new Color(139, 90, 0));
    table.put("darkorange1", new Color(255, 127, 0));
    table.put("darkorange2", new Color(238, 118, 0));
    table.put("darkorange3", new Color(205, 102, 0));
    table.put("darkorange4", new Color(139, 69, 0));
    table.put("coral1", new Color(255, 114, 86));
    table.put("coral2", new Color(238, 106, 80));
    table.put("coral3", new Color(205, 91, 69));
    table.put("coral4", new Color(139, 62, 47));
    table.put("tomato1", new Color(255, 99, 71));
    table.put("tomato2", new Color(238, 92, 66));
    table.put("tomato3", new Color(205, 79, 57));
    table.put("tomato4", new Color(139, 54, 38));
    table.put("orangered1", new Color(255, 69, 0));
    table.put("orangered2", new Color(238, 64, 0));
    table.put("orangered3", new Color(205, 55, 0));
    table.put("orangered4", new Color(139, 37, 0));
    table.put("red1", new Color(255, 0, 0));
    table.put("red2", new Color(238, 0, 0));
    table.put("red3", new Color(205, 0, 0));
    table.put("red4", new Color(139, 0, 0));
    table.put("deeppink1", new Color(255, 20, 147));
    table.put("deeppink2", new Color(238, 18, 137));
    table.put("deeppink3", new Color(205, 16, 118));
    table.put("deeppink4", new Color(139, 10, 80));
    table.put("hotpink1", new Color(255, 110, 180));
    table.put("hotpink2", new Color(238, 106, 167));
    table.put("hotpink3", new Color(205, 96, 144));
    table.put("hotpink4", new Color(139, 58, 98));
    table.put("pink1", new Color(255, 181, 197));
    table.put("pink2", new Color(238, 169, 184));
    table.put("pink3", new Color(205, 145, 158));
    table.put("pink4", new Color(139, 99, 108));
    table.put("lightpink1", new Color(255, 174, 185));
    table.put("lightpink2", new Color(238, 162, 173));
    table.put("lightpink3", new Color(205, 140, 149));
    table.put("lightpink4", new Color(139, 95, 101));
    table.put("palevioletred1", new Color(255, 130, 171));
    table.put("palevioletred2", new Color(238, 121, 159));
    table.put("palevioletred3", new Color(205, 104, 137));
    table.put("palevioletred4", new Color(139, 71, 93));
    table.put("maroon1", new Color(255, 52, 179));
    table.put("maroon2", new Color(238, 48, 167));
    table.put("maroon3", new Color(205, 41, 144));
    table.put("maroon4", new Color(139, 28, 98));
    table.put("violetred1", new Color(255, 62, 150));
    table.put("violetred2", new Color(238, 58, 140));
    table.put("violetred3", new Color(205, 50, 120));
    table.put("violetred4", new Color(139, 34, 82));
    table.put("magenta1", new Color(255, 0, 255));
    table.put("magenta2", new Color(238, 0, 238));
    table.put("magenta3", new Color(205, 0, 205));
    table.put("magenta4", new Color(139, 0, 139));
    table.put("orchid1", new Color(255, 131, 250));
    table.put("orchid2", new Color(238, 122, 233));
    table.put("orchid3", new Color(205, 105, 201));
    table.put("orchid4", new Color(139, 71, 137));
    table.put("plum1", new Color(255, 187, 255));
    table.put("plum2", new Color(238, 174, 238));
    table.put("plum3", new Color(205, 150, 205));
    table.put("plum4", new Color(139, 102, 139));
    table.put("mediumorchid1", new Color(224, 102, 255));
    table.put("mediumorchid2", new Color(209, 95, 238));
    table.put("mediumorchid3", new Color(180, 82, 205));
    table.put("mediumorchid4", new Color(122, 55, 139));
    table.put("darkorchid1", new Color(191, 62, 255));
    table.put("darkorchid2", new Color(178, 58, 238));
    table.put("darkorchid3", new Color(154, 50, 205));
    table.put("darkorchid4", new Color(104, 34, 139));
    table.put("purple1", new Color(155, 48, 255));
    table.put("purple2", new Color(145, 44, 238));
    table.put("purple3", new Color(125, 38, 205));
    table.put("purple4", new Color(85, 26, 139));
    table.put("mediumpurple1", new Color(171, 130, 255));
    table.put("mediumpurple2", new Color(159, 121, 238));
    table.put("mediumpurple3", new Color(137, 104, 205));
    table.put("mediumpurple4", new Color(93, 71, 139));
    table.put("thistle1", new Color(255, 225, 255));
    table.put("thistle2", new Color(238, 210, 238));
    table.put("thistle3", new Color(205, 181, 205));
    table.put("thistle4", new Color(139, 123, 139));
    table.put("gray0", new Color(0, 0, 0));
    table.put("grey0", new Color(0, 0, 0));
    table.put("gray1", new Color(3, 3, 3));
    table.put("grey1", new Color(3, 3, 3));
    table.put("gray2", new Color(5, 5, 5));
    table.put("grey2", new Color(5, 5, 5));
    table.put("gray3", new Color(8, 8, 8));
    table.put("grey3", new Color(8, 8, 8));
    table.put("gray4", new Color(10, 10, 10));
    table.put("grey4", new Color(10, 10, 10));
    table.put("gray5", new Color(13, 13, 13));
    table.put("grey5", new Color(13, 13, 13));
    table.put("gray6", new Color(15, 15, 15));
    table.put("grey6", new Color(15, 15, 15));
    table.put("gray7", new Color(18, 18, 18));
    table.put("grey7", new Color(18, 18, 18));
    table.put("gray8", new Color(20, 20, 20));
    table.put("grey8", new Color(20, 20, 20));
    table.put("gray9", new Color(23, 23, 23));
    table.put("grey9", new Color(23, 23, 23));
    table.put("gray10", new Color(26, 26, 26));
    table.put("grey10", new Color(26, 26, 26));
    table.put("gray11", new Color(28, 28, 28));
    table.put("grey11", new Color(28, 28, 28));
    table.put("gray12", new Color(31, 31, 31));
    table.put("grey12", new Color(31, 31, 31));
    table.put("gray13", new Color(33, 33, 33));
    table.put("grey13", new Color(33, 33, 33));
    table.put("gray14", new Color(36, 36, 36));
    table.put("grey14", new Color(36, 36, 36));
    table.put("gray15", new Color(38, 38, 38));
    table.put("grey15", new Color(38, 38, 38));
    table.put("gray16", new Color(41, 41, 41));
    table.put("grey16", new Color(41, 41, 41));
    table.put("gray17", new Color(43, 43, 43));
    table.put("grey17", new Color(43, 43, 43));
    table.put("gray18", new Color(46, 46, 46));
    table.put("grey18", new Color(46, 46, 46));
    table.put("gray19", new Color(48, 48, 48));
    table.put("grey19", new Color(48, 48, 48));
    table.put("gray20", new Color(51, 51, 51));
    table.put("grey20", new Color(51, 51, 51));
    table.put("gray21", new Color(54, 54, 54));
    table.put("grey21", new Color(54, 54, 54));
    table.put("gray22", new Color(56, 56, 56));
    table.put("grey22", new Color(56, 56, 56));
    table.put("gray23", new Color(59, 59, 59));
    table.put("grey23", new Color(59, 59, 59));
    table.put("gray24", new Color(61, 61, 61));
    table.put("grey24", new Color(61, 61, 61));
    table.put("gray25", new Color(64, 64, 64));
    table.put("grey25", new Color(64, 64, 64));
    table.put("gray26", new Color(66, 66, 66));
    table.put("grey26", new Color(66, 66, 66));
    table.put("gray27", new Color(69, 69, 69));
    table.put("grey27", new Color(69, 69, 69));
    table.put("gray28", new Color(71, 71, 71));
    table.put("grey28", new Color(71, 71, 71));
    table.put("gray29", new Color(74, 74, 74));
    table.put("grey29", new Color(74, 74, 74));
    table.put("gray30", new Color(77, 77, 77));
    table.put("grey30", new Color(77, 77, 77));
    table.put("gray31", new Color(79, 79, 79));
    table.put("grey31", new Color(79, 79, 79));
    table.put("gray32", new Color(82, 82, 82));
    table.put("grey32", new Color(82, 82, 82));
    table.put("gray33", new Color(84, 84, 84));
    table.put("grey33", new Color(84, 84, 84));
    table.put("gray34", new Color(87, 87, 87));
    table.put("grey34", new Color(87, 87, 87));
    table.put("gray35", new Color(89, 89, 89));
    table.put("grey35", new Color(89, 89, 89));
    table.put("gray36", new Color(92, 92, 92));
    table.put("grey36", new Color(92, 92, 92));
    table.put("gray37", new Color(94, 94, 94));
    table.put("grey37", new Color(94, 94, 94));
    table.put("gray38", new Color(97, 97, 97));
    table.put("grey38", new Color(97, 97, 97));
    table.put("gray39", new Color(99, 99, 99));
    table.put("grey39", new Color(99, 99, 99));
    table.put("gray40", new Color(102, 102, 102));
    table.put("grey40", new Color(102, 102, 102));
    table.put("gray41", new Color(105, 105, 105));
    table.put("grey41", new Color(105, 105, 105));
    table.put("gray42", new Color(107, 107, 107));
    table.put("grey42", new Color(107, 107, 107));
    table.put("gray43", new Color(110, 110, 110));
    table.put("grey43", new Color(110, 110, 110));
    table.put("gray44", new Color(112, 112, 112));
    table.put("grey44", new Color(112, 112, 112));
    table.put("gray45", new Color(115, 115, 115));
    table.put("grey45", new Color(115, 115, 115));
    table.put("gray46", new Color(117, 117, 117));
    table.put("grey46", new Color(117, 117, 117));
    table.put("gray47", new Color(120, 120, 120));
    table.put("grey47", new Color(120, 120, 120));
    table.put("gray48", new Color(122, 122, 122));
    table.put("grey48", new Color(122, 122, 122));
    table.put("gray49", new Color(125, 125, 125));
    table.put("grey49", new Color(125, 125, 125));
    table.put("gray50", new Color(127, 127, 127));
    table.put("grey50", new Color(127, 127, 127));
    table.put("gray51", new Color(130, 130, 130));
    table.put("grey51", new Color(130, 130, 130));
    table.put("gray52", new Color(133, 133, 133));
    table.put("grey52", new Color(133, 133, 133));
    table.put("gray53", new Color(135, 135, 135));
    table.put("grey53", new Color(135, 135, 135));
    table.put("gray54", new Color(138, 138, 138));
    table.put("grey54", new Color(138, 138, 138));
    table.put("gray55", new Color(140, 140, 140));
    table.put("grey55", new Color(140, 140, 140));
    table.put("gray56", new Color(143, 143, 143));
    table.put("grey56", new Color(143, 143, 143));
    table.put("gray57", new Color(145, 145, 145));
    table.put("grey57", new Color(145, 145, 145));
    table.put("gray58", new Color(148, 148, 148));
    table.put("grey58", new Color(148, 148, 148));
    table.put("gray59", new Color(150, 150, 150));
    table.put("grey59", new Color(150, 150, 150));
    table.put("gray60", new Color(153, 153, 153));
    table.put("grey60", new Color(153, 153, 153));
    table.put("gray61", new Color(156, 156, 156));
    table.put("grey61", new Color(156, 156, 156));
    table.put("gray62", new Color(158, 158, 158));
    table.put("grey62", new Color(158, 158, 158));
    table.put("gray63", new Color(161, 161, 161));
    table.put("grey63", new Color(161, 161, 161));
    table.put("gray64", new Color(163, 163, 163));
    table.put("grey64", new Color(163, 163, 163));
    table.put("gray65", new Color(166, 166, 166));
    table.put("grey65", new Color(166, 166, 166));
    table.put("gray66", new Color(168, 168, 168));
    table.put("grey66", new Color(168, 168, 168));
    table.put("gray67", new Color(171, 171, 171));
    table.put("grey67", new Color(171, 171, 171));
    table.put("gray68", new Color(173, 173, 173));
    table.put("grey68", new Color(173, 173, 173));
    table.put("gray69", new Color(176, 176, 176));
    table.put("grey69", new Color(176, 176, 176));
    table.put("gray70", new Color(179, 179, 179));
    table.put("grey70", new Color(179, 179, 179));
    table.put("gray71", new Color(181, 181, 181));
    table.put("grey71", new Color(181, 181, 181));
    table.put("gray72", new Color(184, 184, 184));
    table.put("grey72", new Color(184, 184, 184));
    table.put("gray73", new Color(186, 186, 186));
    table.put("grey73", new Color(186, 186, 186));
    table.put("gray74", new Color(189, 189, 189));
    table.put("grey74", new Color(189, 189, 189));
    table.put("gray75", new Color(191, 191, 191));
    table.put("grey75", new Color(191, 191, 191));
    table.put("gray76", new Color(194, 194, 194));
    table.put("grey76", new Color(194, 194, 194));
    table.put("gray77", new Color(196, 196, 196));
    table.put("grey77", new Color(196, 196, 196));
    table.put("gray78", new Color(199, 199, 199));
    table.put("grey78", new Color(199, 199, 199));
    table.put("gray79", new Color(201, 201, 201));
    table.put("grey79", new Color(201, 201, 201));
    table.put("gray80", new Color(204, 204, 204));
    table.put("grey80", new Color(204, 204, 204));
    table.put("gray81", new Color(207, 207, 207));
    table.put("grey81", new Color(207, 207, 207));
    table.put("gray82", new Color(209, 209, 209));
    table.put("grey82", new Color(209, 209, 209));
    table.put("gray83", new Color(212, 212, 212));
    table.put("grey83", new Color(212, 212, 212));
    table.put("gray84", new Color(214, 214, 214));
    table.put("grey84", new Color(214, 214, 214));
    table.put("gray85", new Color(217, 217, 217));
    table.put("grey85", new Color(217, 217, 217));
    table.put("gray86", new Color(219, 219, 219));
    table.put("grey86", new Color(219, 219, 219));
    table.put("gray87", new Color(222, 222, 222));
    table.put("grey87", new Color(222, 222, 222));
    table.put("gray88", new Color(224, 224, 224));
    table.put("grey88", new Color(224, 224, 224));
    table.put("gray89", new Color(227, 227, 227));
    table.put("grey89", new Color(227, 227, 227));
    table.put("gray90", new Color(229, 229, 229));
    table.put("grey90", new Color(229, 229, 229));
    table.put("gray91", new Color(232, 232, 232));
    table.put("grey91", new Color(232, 232, 232));
    table.put("gray92", new Color(235, 235, 235));
    table.put("grey92", new Color(235, 235, 235));
    table.put("gray93", new Color(237, 237, 237));
    table.put("grey93", new Color(237, 237, 237));
    table.put("gray94", new Color(240, 240, 240));
    table.put("grey94", new Color(240, 240, 240));
    table.put("gray95", new Color(242, 242, 242));
    table.put("grey95", new Color(242, 242, 242));
    table.put("gray96", new Color(245, 245, 245));
    table.put("grey96", new Color(245, 245, 245));
    table.put("gray97", new Color(247, 247, 247));
    table.put("grey97", new Color(247, 247, 247));
    table.put("gray98", new Color(250, 250, 250));
    table.put("grey98", new Color(250, 250, 250));
    table.put("gray99", new Color(252, 252, 252));
    table.put("grey99", new Color(252, 252, 252));
    table.put("gray100", new Color(255, 255, 255));
    table.put("grey100", new Color(255, 255, 255));
    table.put("dark grey", new Color(169, 169, 169));
    table.put("darkgrey", new Color(169, 169, 169));
    table.put("dark gray", new Color(169, 169, 169));
    table.put("darkgray", new Color(169, 169, 169));
    table.put("dark blue", new Color(0, 0, 139));
    table.put("darkblue", new Color(0, 0, 139));
    table.put("dark cyan", new Color(0, 139, 139));
    table.put("darkcyan", new Color(0, 139, 139));
    table.put("dark magenta", new Color(139, 0, 139));
    table.put("darkmagenta", new Color(139, 0, 139));
    table.put("dark red", new Color(139, 0, 0));
    table.put("darkred", new Color(139, 0, 0));
    table.put("light green", new Color(144, 238, 144));
    table.put("lightgreen", new Color(144, 238, 144));
    table.put("olive", new Color(128, 128, 0));
    table.put("teal", new Color(0, 128, 128));
  }

  /**
   * Creates a color with given X11 color name.
   * If the given color name is not part of
   * the implemented color names, Color.black is created.
   * @param colorStr the X11 color name (case insensitive)
   */
  public X11Color(String colorStr)
  {
    super(toColorRgb(colorStr));
  }

  /**
   * Returns the implemented X11 color names.
   * @return an alphabetically sorted string array with all implemented color names
   */
  public static String[] getColorNames()
  {
    int size = table.keySet().size();
    String[] names = new String[size];
    int i = 0;
    for (String key : table.keySet())
    {
      names[i] = key;
      i++;
    }
    Arrays.sort(names);
    return names;
  }

  /**
   * Returns the color reference attributed to the given X11 color name.
   * @param colorStr the X11 color name (case insensitive)
   * @return the color reference or null, if the given color name is not part of
   * the implemented color names
   */
  public static Color toColor(String colorStr)
  {
    Color c = table.get(colorStr.trim().toLowerCase());
    return c;
  }

  /**
   * Returns the X11 color name attributed to the given color.
   * The name is lowercase and has no blanks. Colors containing 'grey' are
   * returned with 'gray'.
   * @param color the color to search for the X11 name
   * @return the X11 color name or null, if the given color is not part of
   * the implemented X11 colors
   */
  public static String toColorStr(Color color)
  {
    for (String key : table.keySet())
    {
      char c = key.charAt(key.length() - 1);
      if (c >= '0' && c <= '9')  // Skip names with ending number
        continue;
      if (key.contains(" ")) // Skip names with ending number
        continue;
      if (key.contains("grey")) // Skip names with ending number
        continue;
      if (table.get(key).equals(color))
        return key;
    }
    return null;
  }

  private static int toColorRgb(String colorStr)
  {
    Color c = table.get(colorStr.trim().toLowerCase());
    if (c == null)
      return 0;
    return c.getBlue() + (c.getGreen() << 8) + (c.getRed() << 16);
  }

  /**
   * Returns a random color from the X11 color set. Additional color 
   * names terminating with a number are excluded.
   * @return a random X11 color
   */
  public static Color getRandomColor()
  {
    return toColor(getRandomColorStr());
  }

  ;

  /**
   * Returns a random X11 color from the X11 color set. Additional color 
   * names terminating with a number are excluded.
   * @return a random X11 color name
   */
  public static String getRandomColorStr()
  {
    Random rand = new Random();
    Object[] values = table.keySet().toArray();
    String s;
    char c;
    do
    {
      s = (String)values[rand.nextInt(values.length)];
      c = s.charAt(s.length() - 1);
    }
    while ((c >= '0' && c <= '9') || s.contains(" "));
    return s;
  }

  /**
   * Returns the color corresponding to the spectral wavelength.
   * @param wavelength the wavelength in nm (between 380nm and 780nm).
   * @return the RGB color
   */
  public static Color wavelengthToColor(int wavelength)
  {
    double blue, green, red;

    if (wavelength >= 380 && wavelength <= 439)
    {
      red = 0.0;
      green = 0.0;
      blue = (wavelength - 380) / (440.0 - 380);
    }
    else if (wavelength >= 440 && wavelength <= 489)
    {
      red = 0.0;
      green = (wavelength - 440) / (490.0 - 440);
      blue = 1.0;
    }
    else if (wavelength >= 490 && wavelength <= 509)
    {
      red = 0.0;
      green = 1.0;
      blue = -(wavelength - 510) / (510.0 - 490);
    }
    else if (wavelength >= 510 && wavelength <= 579)
    {
      red = (wavelength - 510) / (580.0 - 510);
      green = 1.0;
      blue = 0.0;
    }
    else if (wavelength >= 580 && wavelength <= 644)
    {
      red = 1.0;
      green = -(wavelength - 645) / (645.0 - 580);
      blue = 0.0;
    }
    else if (wavelength >= 645 && wavelength <= 780)
    {
      red = -(wavelength - 780) / (780.0 - 645);
      green = 0.0;
      blue = 0.0;
    }
    else
    {
      red = 0.0;
      green = 0.0;
      blue = 0.0;
    }

    return new Color((float)red, (float)green, (float)blue);
  }
}
