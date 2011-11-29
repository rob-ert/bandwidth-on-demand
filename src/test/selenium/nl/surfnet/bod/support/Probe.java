package nl.surfnet.bod.support;

public interface Probe {

  void sample();

  boolean isSatisfied();

  String message();
}
