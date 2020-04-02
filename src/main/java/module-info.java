module PersistenceLite {
  requires org.jetbrains.annotations;
  requires java.sql;

  exports grevend.jacoco;
  exports grevend.persistencelite;
  exports grevend.persistencelite.dao;
  exports grevend.persistencelite.database;
  exports grevend.persistencelite.database.inmemory;
  exports grevend.persistencelite.database.sql;
  exports grevend.persistencelite.entity;
  exports grevend.persistencelite.util;
  exports grevend.persistencelite.util.function;
  exports grevend.persistencelite.util.iterators;
  exports grevend.persistencelite.util.sequence;

  opens grevend.jacoco;
  opens grevend.persistencelite;
  opens grevend.persistencelite.dao;
  opens grevend.persistencelite.database;
  opens grevend.persistencelite.database.inmemory;
  opens grevend.persistencelite.database.sql;
  opens grevend.persistencelite.entity;
  opens grevend.persistencelite.util;
  opens grevend.persistencelite.util.function;
  opens grevend.persistencelite.util.iterators;
  opens grevend.persistencelite.util.sequence;
}