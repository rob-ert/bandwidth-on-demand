package nl.surfnet.bod.util;

public class TestView {
  final long id;

  public TestView(TestEntity test) {
    id = test.getId();
  }

  public long getId() {
    return id;
  }
}
