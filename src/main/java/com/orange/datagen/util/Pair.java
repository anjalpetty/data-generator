package com.orange.datagen.util;


public class Pair {

  private Object first;
  private Class<?> second;

  public Pair(Object first, Class<?> second) {
    this.first = first;
    this.second = second;
  }

  public Object getFirst() {
    return first;
  }

  public void setFirst(Object first) {
    this.first = first;
  }

  public Class<?> getSecond() {
    return second;
  }

  public void setSecond(Class<?> second) {
    this.second = second;
  }

  @Override
  public String toString() {
    return "Pair{" +
            "first=" + first +
            ", second=" + second +
            '}';
  }
}
