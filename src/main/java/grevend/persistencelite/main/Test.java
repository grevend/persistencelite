/*
 * MIT License
 *
 * Copyright (c) 2020 David Greven
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package grevend.persistencelite.main;

import grevend.persistencelite.entity.Entity;
import grevend.persistencelite.util.PrimaryKey;
import grevend.persistencelite.util.sequence.Seq;
import java.util.Date;
import java.util.stream.Collectors;

public class Test {


  public static void main(String[] args) {
    /*try (var db = Persistence.databaseBuilder(SqlDatabaseBuilder.class, "postgres", 0)
        .setCredentials("postgres", "mypassword").build()) {
      var dao = db.getDao(HazardousLiquid.class);
      dao.create(new HazardousLiquid("Water", 997, "TODO"));
    } catch (Exception e) {
      e.printStackTrace();
    }*/

    // SQL
    /*try (var db = Persistence.databaseBuilder(SqlDatabaseBuilder.class, "postgres", 0)
        .setCredentials("...", "...").build()) {
      var dao = db.getDao(HazardousLiquid.class);
    } catch (Exception e) {
      e.printStackTrace();
    }

    // InMemory
    try (var db = Persistence.databaseBuilder(InMemoryDatabaseBuilder.class, "postgres", 0)
        .setOutputFile(new File("postgres.ser")).build()) {
      var dao = db.getDao(HazardousLiquid.class);
    } catch (Exception e) {
      e.printStackTrace();
    }*/

    // Fibonacci Sequence
    /*var res = Seq.generate(Pair.of(0, 1),
        (prev) -> Pair.of(prev.getB(), prev.getA() + prev.getB()))
        .map(Pair::getA)
        .limit(10)
        .toList();*/

    //System.out.println(res); // [0, 1, 1, 2, 3, 5, 8, 13, 21, 34]

    /*Seq.range(5, 10).toList(); // [5, 6, 7, 8, 9, 10]

    Seq.range(10, 4, 2).toList(); // [10, 8, 6, 4]

    Seq.range(10.0, 5.0, 0.5).toList();
    // [10.0, 9.5, 9.0, 8.5, 8.0, 7.5, 7.0, 6.5, 6.0, 5.5, 5.0]

    Seq.range('z', 'a', 2).toList();
    // ['z', 'x', 'v', 't', 'r', 'p', 'n', 'l', 'j', 'h', 'f', 'd', 'b', 'a']

    // Color Gradient
    var res = Seq.range(Color.RED, Color.ORANGE, (color, end, step) -> new Color(
        (float) ((color.getRed() / 255.0) * step + (end.getRed() / 255) * (1 - step)),
        (float) ((color.getGreen() / 255.0) * step + (end.getGreen() / 255) * (1 - step)),
        (float) ((color.getBlue() / 255.0) * step + (end.getBlue() / 255) * (1 - step))
    ), 0.10).limit(10).toList();

    System.out.println(res);*/

    //var numbers = List.of(1, 2, 3, 4, 5);

    // Collections
    /*numbers.iterator().

    Stream.iterate(0, i -> i + 1)
        .flatMap(i -> Stream.of(i, i, i, i))
        .map(i -> i + 1)
        .peek(i -> System.out.println("Map: " + i))
        .limit(5)
        .forEach(i -> {});

    Stream.iterate(0, i -> i + 1)
          .flatMap(i -> Stream.of(i, i, i, i))
        .map(i -> i + 1)
        .peek(i -> System.out.println("Map1: " + i))
        .limit(5)
        .forEach(i -> {});

    System.out.println();
    System.out.println();

    Stream.iterate(0, i -> i + 1)
        .flatMap(i -> Stream.of(i, i, i, i))
        .limit(5)
        .map(i -> i + 1)
        .peek(i -> System.out.println("Map2: " + i))
        .forEach(i -> {});

    System.out.println();
    System.out.println("Sequence");
    System.out.println();

    Seq.generate(0, i -> i + 1)
        .flatMap(i -> Seq.of(i, i, i, i))
        .map(i -> i + 1)
        .peek(i -> System.out.println("Seq: " + i))
        .limit(5)
        .toList();

    System.out.println();
    System.out.println();
    Seq.generate(0, i -> i + 1).flatMap(i -> Seq.of(i, i, i, i)).limit(5).map(i -> i + 1).peek(i -> System.out.println("Seq2: " + i)).toList();

    var list = new ArrayList<Integer>();
    list.add(12);
    list.add(24);

    var l = List.of(
        Optional.of(12),
        Optional.of(24),
        //Optional.of
    );*/

    var a = Seq.range(1, 30);
    // [1, 2, 3, 4, 5, ..., 25, 26, 27, 28, 29, 30]
    var b = Seq.range(2, 10, 2);
    // [2, 4, 6, 8, 10]
    var c = Seq.range(12, 1);
    // [12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1]

    var res = Seq.of(a, b, c)
        .flatMap(seq -> seq.limit(10)) // intermediate (limit - stateful)
        .filter(number -> number % 2 == 0) // intermediate
        .peek(System.out::println) // intermediate
        .groupBy(number -> number, Collectors.counting()); // terminal

    System.out.println(res);
    // {2=2, 4=3, 6=3, 8=3, 10=3, 12=1}

  }

  public enum Test2 {

  }

  @Entity(name = "liquid")
  public static class Liquid {

    @PrimaryKey
    public int liquid_id;
    public String name;
    public double density;

    protected Liquid() {

    }

    public Liquid(String name, double density) {
      this.name = name;
      this.density = density;
    }
  }

  @Entity(name = "hazardous_liquid")
  public static class HazardousLiquid extends Liquid {

    @PrimaryKey
    public int hazardous_id;
    public String safetyInstructions;

    public HazardousLiquid(String name, double density, String safetyInstructions) {
      this.name = name;
      this.density = density;
      this.safetyInstructions = safetyInstructions;
    }
  }

  @Entity(name = "fresh_liquid")
  public static class FreshLiquid extends Liquid {

    @PrimaryKey
    public int fresh_id;
    public Date t;
  }

  /*

   */

}
