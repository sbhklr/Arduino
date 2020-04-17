/*
 * This file is part of Arduino.
 *
 * Code inspired by this tutorial http://wiki.netbeans.org/Splash_Screen_Beginner_Tutorial. License says "You may modify and use it as you wish."
 *
 * Copyright 2015 Arduino LLC (http://www.arduino.cc/)
 *
 * Arduino is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * As a special exception, you may use this file as part of a free software
 * library without restriction.  Specifically, if other files instantiate
 * templates or use macros or inline functions from this file, or you compile
 * this file and link it with other files to produce an executable, this
 * file does not by itself cause the resulting executable to be covered by
 * the GNU General Public License.  This exception does not however
 * invalidate any other reasons why the executable file might be covered by
 * the GNU General Public License.
 */

package cc.arduino.view;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Map;

import processing.app.BaseNoGui;
import processing.app.Theme;

public class SplashScreenHelper {

  private static final int X_OFFSET = 50;
  private static final int STATUS_MESSAGE_Y_OFFSET = 260;
  private static final int RELEASE_VERSION_Y_OFFSET = 55;
  private static final int TEXTAREA_HEIGHT = 20;
  private static final int TEXTAREA_WIDTH = 256;
  private static final int RELEASE_VERSION_FONT_SIZE = 10;
  private static final Color TEXT_COLOR = Color.WHITE;

  private final Map desktopHints;
  private final SplashScreen splash;
  private Rectangle2D.Double splashTextArea;
  private Graphics2D splashGraphics;

  public SplashScreenHelper(SplashScreen splash) {
    this.splash = splash;
    if (splash != null) {
      Toolkit tk = Toolkit.getDefaultToolkit();
      desktopHints = (Map) tk.getDesktopProperty("awt.font.desktophints");
    } else {
      desktopHints = null;
    }
  }

  public void splashText(String text) {
    if (splash == null) {
      printText(text);
      return;
    }

    if (!splash.isVisible()) {
      return;
    }

    if (splashTextArea == null) {
      prepareTextAreaAndGraphics();
      drawReleaseVersion(splashGraphics);
      setStatusMessageFont();
    }

    eraseLastStatusText();

    drawText(text);

    ensureTextIsDiplayed();
  }

  private void ensureTextIsDiplayed() {
    synchronized (SplashScreen.class) {
      if (splash.isVisible()) {
        splash.update();
      }
    }
  }

  private void drawReleaseVersion(Graphics2D graphics) {       
    Font font = new Font("Monospaced", Font.PLAIN, RELEASE_VERSION_FONT_SIZE);
    graphics.setFont(font);
    graphics.setColor(TEXT_COLOR);    
    graphics.drawString("RELEASE " + BaseNoGui.VERSION_NAME_LONG, X_OFFSET, RELEASE_VERSION_Y_OFFSET);
  }
  
  private void drawText(String str) {    
    splashGraphics.setColor(TEXT_COLOR);    
    FontMetrics metrics = splashGraphics.getFontMetrics();    
    splashGraphics.drawString(str, (int) splashTextArea.getX(), (int) splashTextArea.getY() + (TEXTAREA_HEIGHT - metrics.getHeight()) + 5);
  }

  private void setStatusMessageFont() {
    Font defaultFont = Theme.getDefaultFont();
    splashGraphics.setFont(defaultFont);        
  }

  private void eraseLastStatusText() {
    splashGraphics.setComposite(AlphaComposite.Clear);
    splashGraphics.setPaint(TEXT_COLOR);
    splashGraphics.fillRect(X_OFFSET, STATUS_MESSAGE_Y_OFFSET, TEXTAREA_WIDTH, TEXTAREA_HEIGHT);
    splashGraphics.setPaintMode();
  }

  private void prepareTextAreaAndGraphics() {
    splashTextArea = new Rectangle2D.Double(X_OFFSET, STATUS_MESSAGE_Y_OFFSET, TEXTAREA_WIDTH, TEXTAREA_HEIGHT);

    splashGraphics = Theme.setupGraphics2D(splash.createGraphics());

    if (desktopHints != null) {
      splashGraphics.addRenderingHints(desktopHints);
    }
  }

  public void close() {
    if (splash == null) {
      return;
    }
    splash.close();
  }

  private void printText(String str) {
    System.err.println(str);
  }

}
