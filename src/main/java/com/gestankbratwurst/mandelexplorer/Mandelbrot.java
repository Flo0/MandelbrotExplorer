package com.gestankbratwurst.mandelexplorer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.IntBinaryOperator;

public class Mandelbrot {

  public Mandelbrot(int width, int height) {
    this.width = width;
    this.height = height;
    this.pixelMatrix = new int[width][height];
    this.pixelBuffer = new int[width * height];
  }

  private IntBinaryOperator colorOperator = (k, v) -> v;
  private int iterationDepth;
  private double realMin;
  private double realMax;
  private double imMin;
  private double imMax;
  private double realStep;
  private double imStep;
  private final int width;
  private final int height;
  private final int[][] pixelMatrix;
  private final int[] pixelBuffer;

  public void setColorOperator(IntBinaryOperator operator) {
    colorOperator = operator;
  }

  public void setIterationDepth(int value) {
    this.iterationDepth = value;
  }

  public void setBounds(double realMin, double realMax, double imMin, double imMax) {
    if (realMin >= realMax || imMin >= imMax) {
      throw new IllegalArgumentException();
    }
    this.realMin = realMin;
    this.realMax = realMax;
    this.imMin = imMin;
    this.imMax = imMax;
    this.realStep = (realMax - realMin) / width;
    this.imStep = (imMax - imMin) / height;
  }

  public void shift(double real, double im) {
    setBounds(this.realMin + real, this.realMax + real, this.imMin + im, this.imMax + im);
  }

  public void zoom(double value) {
    double zoom = value / 2D;
    setBounds(this.realMin - zoom, this.realMax + zoom, this.imMin - zoom, this.imMax + zoom);
  }

  public int[][] calculate() {
    double real;
    double im = imMin;
    for (int pixIm = 0; pixIm < height; pixIm++) {
      real = realMin;
      for (int pixReal = 0; pixReal < width; pixReal++) {
        pixelMatrix[pixReal][pixIm] = calculateMandelbrot(real, im);
        real += realStep;
      }
      im += imStep;
    }
    return pixelMatrix;
  }

  public int[] calculateSingleBuffered() {
    double real;
    double im = imMin;
    int bufferIndex = 0;
    for (int pixIm = 0; pixIm < height; pixIm++) {
      real = realMin;
      for (int pixReal = 0; pixReal < width; pixReal++) {
        pixelBuffer[bufferIndex++] = calculateMandelbrot(real, im);
        real += realStep;
      }
      im += imStep;
    }
    return pixelBuffer;
  }

  public int[] calculateSingleBufferedMultiThreaded() {
    List<CompletableFuture<?>> futureList = new ArrayList<>();
    for (int pixIm = 0; pixIm < height; pixIm++) {
      int indexBufferStart = pixIm * height;
      int imTemp = pixIm;
      futureList.add(CompletableFuture.runAsync(() -> {
        int bufferIndex = indexBufferStart;
        for (int pixReal = 0; pixReal < width; pixReal++) {
          bufferIndex++;
          pixelBuffer[bufferIndex] = calculateMandelbrot(realMin + pixReal * realStep, imTemp);
        }
      }));
    }
    for (CompletableFuture<?> future : futureList) {
      future.join();
    }
    return pixelBuffer;
  }

  private int calculateMandelbrot(double real, double im) {
    double zR = real;
    double zI = im;
    double zRsQ, zIsQ;
    double realTemp;
    double condition = 2.0 * 2.0;
    for (int i = 0; i < iterationDepth; i++) {
      zRsQ = zR * zR;
      zIsQ = zI * zI;
      if (zRsQ + zIsQ > condition) return colorOperator.applyAsInt(iterationDepth, i);
      realTemp = zRsQ - zIsQ + real;
      zI = 2 * zR * zI + im;
      zR = realTemp;
    }
    return colorOperator.applyAsInt(iterationDepth, iterationDepth);
  }

}
