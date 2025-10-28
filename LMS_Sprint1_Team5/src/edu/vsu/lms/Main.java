package edu.vsu.lms;

import javax.swing.SwingUtilities;
import edu.vsu.lms.view.MainFrame;
import edu.vsu.lms.persistence.AppState;

public class Main {
  public static void main(String[] args) {
    System.out.println("[Main] start");

    AppState.getInstance().seedDefaults();

    Thread.setDefaultUncaughtExceptionHandler((t,e)->{
      System.err.println("[Uncaught@" + t.getName() + "] " + e);
      e.printStackTrace();
    });

    SwingUtilities.invokeLater(() -> {
      System.out.println("[EDT] creating MainFrame...");
      try {
        MainFrame f = new MainFrame();
        f.setAlwaysOnTop(true);  // bring to front
        f.setVisible(true);
        f.setAlwaysOnTop(false);
        System.out.println("[EDT] MainFrame visible");
      } catch (Throwable ex) {
        System.err.println("[EDT] MainFrame ctor failed");
        ex.printStackTrace();
      }
    });
  }
}

