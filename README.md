# PersistenceLite

The PersistenceLite library provides an easy to use abstraction over database access.

## Usage

```java
var postgres = PersistenceLite.configure(PostgresService.class)
    .credentials("credentials.properties").service();
```

```java
@Entity(name = "pet")
public interface Pet {

    @Id
    int id();

    @Property(name = "status")
    Status status();

    String name();

    @Property(name = "owner_id")
    int ownerId();

}

@Entity(name = "dog")
public record Dog(

    @Id
    int id,
        
    String name,
    
    Status status,
    
    @Property(name = "owner_id")
    int ownerId,
    
    @Property(name = "trained")
    boolean trained
    
) implements Pet {}

var dogDao = postgres.createDao(Dog.class);
Collection<Dog> dogs = dogDao.retrieveAll();
```

## License

MIT License

Copyright (c) 2020 David Greven

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
