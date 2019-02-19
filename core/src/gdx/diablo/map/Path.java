package gdx.diablo.map;

import com.badlogic.gdx.utils.Array;

import java.util.Iterator;

public class Path implements Iterable<Point2> {
  public final Array<Point2> nodes = new Array<>(32);

  public void clear() {
    // TODO: maybe nodes.size = 0 is enough
    nodes.clear();
  }

  public int size() {
    return nodes.size;
  }

  public void append(Point2 dst) {
    nodes.add(dst);
  }

  public void reverse() {
    nodes.reverse();
  }

  @Override
  public Iterator<Point2> iterator() {
    return new Array.ArrayIterator<>(nodes);
  }

  @Override
  public String toString() {
    return nodes.toString();
  }
}
