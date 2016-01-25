package com.ocado.javautils

object Runnables {
  implicit def toRunnable(f: () => Any): Runnable = new Runnable() {
    def run() = f()
  }
}