package com.ocado.javautils

object Runnables {
  implicit def toRunnable(f: () => Unit): Runnable = new Runnable() {
    def run() = f()
  }
}