package com.gestankbratwurst.mandelexplorer;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class RenderPanel extends JPanel {

  private static final int[] BIT_MASKS = new int[]{0xFF0000, 0xFF00, 0xFF, 0xFF000000};

  public RenderPanel(int width, int height) {
    Dimension size = new Dimension(width, height);
    this.setPreferredSize(size);
    this.setSize(size);
    this.width = width;
    this.height = height;
    this.mandelbrot = new Mandelbrot(width, height);
    mandelbrot.setBounds(-2.2, 1, -1.2, 1.2);
    mandelbrot.setIterationDepth(50);
    mandelbrot.setColorOperator(this::translateColorBasic);
    AtomicInteger x = new AtomicInteger();
    Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
      repaint();
    }, 0, 1, TimeUnit.SECONDS);
  }

  private final int width;
  private final int height;
  private final Mandelbrot mandelbrot;

  private int translateColorAlpha(int iterDepth, int value) {
    int result = (int) (255.0 / iterDepth * value);
    return (((255 - result) << 24) & 0xFF000000) | ((result << 16) & 0x00FF0000) | ((result << 8) & 0x0000FF00) | (result & 0x000000FF);
  }

  private int translateColorBasic(int iterDepth, int value) {
    int result = (int) (255.0 / iterDepth * value);
    return 0xFF000000 | ((result << 16) & 0x00FF0000) | ((result << 8) & 0x0000FF00) | (result & 0x000000FF);
  }

  @Override
  public void paintComponent(Graphics graphics) {
    long start = System.nanoTime();
    int[] buffer = mandelbrot.calculateSingleBufferedMultiThreaded();
    long mandelTime = System.nanoTime() - start;
    start = System.nanoTime();
    graphics.drawImage(toImage(buffer), 0, 0, null);
    long drawTime = System.nanoTime() - start;
    graphics.setColor(Color.GREEN);
    graphics.drawString("Mandelbrot: " + toMillis(mandelTime) + "ms    Draw: " + toMillis(drawTime) + "ms", 10, 20);
  }

  private double toMillis(long nanos) {
    return ((int) (nanos / 1E4)) / 100.0;
  }

  private BufferedImage toImage(int[][] matrix) {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics g = image.getGraphics();
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        g.setColor(matrix[x][y] == 0 ? Color.WHITE : Color.BLACK);
        g.fillRect(x, y, 1, 1);
      }
    }
    return image;
  }

  private BufferedImage toImage(int[] buffer) {
    SinglePixelPackedSampleModel sm = new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT, width, height, BIT_MASKS);
    DataBufferInt db = new DataBufferInt(buffer, buffer.length);
    WritableRaster wr = Raster.createWritableRaster(sm, db, new Point());
    return new BufferedImage(ColorModel.getRGBdefault(), wr, false, null);
  }

}
