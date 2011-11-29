package nl.surfnet.bod.support;

public class Poller {

  private long[] waitTimes = new long[] { 0, 50, 100, 100, 100, 500, 1000, 1000 };

  public void check(Probe probe) throws InterruptedException {
    int tries = 0;
    while (!probe.isSatisfied() && tries < waitTimes.length) {
      probe.sample();
      Thread.sleep(waitTimes[tries]);
      tries++;
    }

    if (!probe.isSatisfied()) {
      throw new AssertionError(probe.message());
    }
  }

  public static void assertEventually(Probe probe) throws InterruptedException {
    new Poller().check(probe);
  }
}
