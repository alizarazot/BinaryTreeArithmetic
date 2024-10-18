package com.ufpso.binarytreearithmetic;

public class InvalidCharException extends Exception {
  int pos;
  char c;

  public InvalidCharException(int pos, char c) {
    super(String.format("Invalid char: '%c', at pos: %d", c, pos));

    this.pos = pos;
    this.c = c;
  }
}
