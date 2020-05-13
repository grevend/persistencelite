module grevend.persistencelite {

    requires java.sql;
    requires jdk.httpserver;
    requires java.compiler;
    requires reflections;
    requires com.google.gson;
    requires org.jetbrains.annotations;

    exports grevend.common.jacoco;
    exports grevend.common;
    exports grevend.persistencelite;
    exports grevend.persistencelite.dao;
    exports grevend.persistencelite.entity;
    exports grevend.persistencelite.service;
    exports grevend.persistencelite.service.sql;
    exports grevend.persistencelite.util;
    exports grevend.sequence.function;
    exports grevend.sequence.iterators;
    exports grevend.sequence;

    opens grevend.common.jacoco;
    opens grevend.common;
    opens grevend.persistencelite;
    opens grevend.persistencelite.dao;
    opens grevend.persistencelite.entity;
    opens grevend.persistencelite.service;
    opens grevend.persistencelite.service.sql;
    opens grevend.persistencelite.util;
    opens grevend.sequence.function;
    opens grevend.sequence.iterators;
    opens grevend.sequence;

    //opens grevend.persistencelite.builder;

    //provides System.LoggerFinder with PersistenceLiteLoggerFinder;
}