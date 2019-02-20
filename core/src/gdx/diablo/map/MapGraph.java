package gdx.diablo.map;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.DefaultConnection;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.pfa.Heuristic;
import com.badlogic.gdx.ai.pfa.PathFinder;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;

public class MapGraph implements IndexedGraph<MapGraph.Point2> {
  Heuristic<Point2> heuristic = new ManhattanDistanceHeuristic();

  Map map;
  IntMap<Point2> points = new IntMap<>();

  public MapGraph(Map map) {
    this.map = map;
  }

  public GraphPath<Point2> path(Vector3 src, Vector3 dst, GraphPath<Point2> path) {
    Map.Zone zone = map.getZone((int) dst.x, (int) dst.y);
    if (zone != null && zone.flags((int) dst.x, (int) dst.y) != 0) {
      return path;
    }

    int hash = Point2.hash(src);
    Point2 srcP = points.get(hash);
    if (srcP == null) {
      srcP = new Point2(src);
      points.put(hash, srcP);
    }
    hash = Point2.hash(dst);
    Point2 dstP = points.get(hash);
    if (dstP == null) {
      dstP = new Point2(dst);
      points.put(hash, dstP);
    }
    return path(srcP, dstP, path);
  }

  public GraphPath<Point2> path(Point2 src, Point2 dst, GraphPath<Point2> path) {
    path.clear();
    new IndexedAStarPathFinder<>(this).searchNodePath(src, dst, heuristic, path);
    return path;
  }

  public boolean searchNodePath(PathFinder<Point2> pathFinder, Vector3 src, Vector3 dst, GraphPath<Point2> path) {
    path.clear();
    Map.Zone zone = map.getZone((int) dst.x, (int) dst.y);
    if (zone != null && zone.flags((int) dst.x, (int) dst.y) != 0) {
      return false;
    }

    int hash = Point2.hash(src);
    Point2 srcP = points.get(hash);
    if (srcP == null) {
      srcP = new Point2(src);
      points.put(hash, srcP);
    }
    hash = Point2.hash(dst);
    Point2 dstP = points.get(hash);
    if (dstP == null) {
      dstP = new Point2(dst);
      points.put(hash, dstP);
    }
    return searchNodePath(pathFinder, srcP, dstP, path);
  }

  public boolean searchNodePath(PathFinder<Point2> pathFinder, Point2 src, Point2 dst, GraphPath<Point2> path) {
    return pathFinder.searchNodePath(src, dst, heuristic, path);
  }

  @Override
  public int getIndex(Point2 node) {
    return node.index;
  }

  @Override
  public int getNodeCount() {
    return 2 << 20;
  }

  @Override
  public Array<Connection<Point2>> getConnections(Point2 src) {
    Array<Connection<Point2>> connections = src.connections;
    if (connections == null) {
      connections = src.connections = new Array<>(8);
      tryConnect(src, src.x - 1, src.y - 1);
      tryConnect(src, src.x - 1, src.y    );
      tryConnect(src, src.x - 1, src.y + 1);
      tryConnect(src, src.x    , src.y - 1);
      tryConnect(src, src.x    , src.y + 1);
      tryConnect(src, src.x + 1, src.y - 1);
      tryConnect(src, src.x + 1, src.y    );
      tryConnect(src, src.x + 1, src.y + 1);
    }

    return connections;
  }

  private void tryConnect(Point2 src, int x, int y) {
    Map.Zone zone = map.getZone(x, y);
    if (zone != null && zone.flags(x, y) == 0) {
      final int hash = Point2.hash(x, y);
      Point2 dst = points.get(hash);
      if (dst == null) {
        dst = new Point2(x, y);
        points.put(hash, dst);
      }
      src.connections.add(new Path(src, dst));
    }
  }

  public static class Point2 {
    public int x;
    public int y;
    int index;
    Array<Connection<Point2>> connections;

    static int indexes = 0;

    Point2(int x, int y) {
      this.x = x;
      this.y = y;
      index = indexes++;
    }

    Point2(Vector3 src) {
      this((int) src.x, (int) src.y);
    }

    @Override
    public int hashCode() {
      return hash(x, y);
    }

    static int hash(Vector3 src) {
      return hash((int) src.x, (int) src.y);
    }

    static int hash(int x, int y) {
      return (x * 73856093) ^ (y * 83492791);
    }

    @Override
    public String toString() {
      return "(" + x + "," + y + ")";
    }
  }

  static class Path extends DefaultConnection<Point2> {
    static final float DIAGONAL_COST = (float)Math.sqrt(2);

    Path(Point2 src, Point2 dst) {
      super(src, dst);
    }

    @Override
    public float getCost() {
      return fromNode.x != toNode.x && fromNode.y != toNode.y ? DIAGONAL_COST : 1;
    }
  }

  static class ManhattanDistanceHeuristic implements Heuristic<Point2> {
    @Override
    public float estimate(Point2 src, Point2 dst) {
      return Math.abs(dst.x - src.x) + Math.abs(dst.y - src.y);
    }
  }
}
