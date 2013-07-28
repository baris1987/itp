RESTful WebService
===

##JAX-RS
* Um in Java WebServices entwickeln zu können wird die Standard-Bibliothek **JAX-RS** verwendet. 

* Informationen zu JAX-RS gibt es im **Galileo Buch Java 7** unter: http://openbook.galileocomputing.de/java7/1507_13_002.html

* Während der Testphase wird als Webserver **Jersey** eingesetzt. Jersey ist ein minimalistischer WebServer, der JAX-RS vollständig unterstützt.
Später kann man - falls notwendig - auf einen ausgereiften Application Server wie **GlassFish** umstellen. 

* Alle notwendigen Bibliotheken sind im Projekt enthalten. Die Bibliothken müssen manuell in den **Build-Pfad** aufgenommen werden.

##Datenbank
* Als Datenbank wird **H2** eingesetzt. Die Datenbank ist weit verbreitet und hat den Vorteil gegenüber SQLite, dass sie vollständig in Java geschrieben ist und somit
  plattformunabhängig läuft. Weitere Informationen dazu sind unter folgendem Link zu finden: http://www.h2database.com

* Im WebService Eclipse-Projekt ist im Verzeichnis **utils** die Datei **h2.bat**. Mit dem Starten dieses Programms wird im
  Internet-Browser eine Webseite zur Verwaltung der Datenbank geöffnet.

* In der Datei **database/structure.sql** sind alle SQL Statements der Datenbank enthalten. 

* Die **Datenbank-Tabellen** sind in der Datei "Database" wie gewohnt in unserem Google Drive Verzeichnis zu finden: https://drive.google.com/folderview?id=0B7ABUEVnTXzXNUhQZXI2anZQUjA&usp=sharing

##Web Service
* Eine Dokumentation zum Web Service ist zu finden unter https://docs.google.com/document/d/12nlCI590x0oPD-HcsK96C4woCqhKLvPaGi8sBnw4bIg/edit
