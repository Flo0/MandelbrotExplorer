package com.gestankbratwurst.mandelexplorer;

import javax.swing.*;

public class MainFrame extends JFrame {

  protected static void start() {
    new MainFrame();
  }

  private MainFrame() {
    super("Mandelbrot Explorer");
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    JPanel panel = new RenderPanel(1280, 720);
    this.add(panel);
    this.pack();
    this.setResizable(false);
    this.setVisible(true);
  }

}
