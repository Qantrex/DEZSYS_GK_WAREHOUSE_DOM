# Middleware Engineering "Document Oriented Middleware using MongoDB" - Taskdescription
GIT repository: [https://github.com/ThomasMicheler/DEZSYS_GK_WAREHOUSE_DOM.git](https://github.com/ThomasMicheler/DEZSYS_GK_WAREHOUSE_DOM.git)

## Einführung

Diese Übung soll helfen die Funktionsweise und Einsatzmöglichkeiten eines dokumentenorientierten dezentralen Systems mit Hilfe des Frameworks Spring Data MongoDB oder einem Framework Ihrer Wahl zu demonstrieren. Die Daten werden in dieser Übung in einem NoSQL Repository gespeichert und verarbeitet.

Es handelt sich um ein Lagerstandort Beispiel, wie in Aufgabe "GK8.1 Spring Data and ORM". Die Daten aller Lagerstandorte sollen in der Zentrale persistiert und in einer NoSQL Datenbank gespeichert werden. Von hier aus koennen die Daten fuer verschiedene Fragestellungen des Betriebes (Management, Einkauf, Vertrieb,...) abgefragt werden.

## 1.1 Ziele

Das Ziel dieser Übung ist die Implementierung einer dokumentenorientierten Middleware, die die Daten aller Warenlager zentral in einem entsprechenden Format ablegt.

## 1.2 Voraussetzungen

* Grundlagen zu JSON & REST
* Grundlagen Architektur von verteilten Systemen
* Grundlagen Spring Framework, Spring Boot oae.
* Grundlagen NoSQL
* Installation MongoDB
* Datenstruktur basierend auf der Aufgabenstellung "GK8.1 Spring Data and ORM"
* Umsetzung eines einfachen Web-Userinterfaces zur Anzeige von Daten


## 1.3 Aufgabenstellung

Implementieren Sie eine dokumentenorientierte Middleware mit Hilfe von MongoDB, dass Daten über eine REST Schnittstellen empfängt und die Daten des Lagerstandortes in einer MongoDB Datenbank im JSON Format abspeichert. Entwerfen Sie eine geeignet Datenstruktur, um eine kontinuierliche Speicherung der Daten zu gewährleisten.

Es sollen dabei folgende REST-Funktionen implementiert werden:  

* POST /warehouse: fügt einen neuen Lagerstandort hinzu. 
* GET /warehouse: abrufen aller Lagerstandorte und deren Lagerbestand  
* GET /warehouse/{id}: abrufen eines Lagerstandortes id und dessen Lagerbestand  
* DELETE /warehouse/{id}: löschen eines Lagerstandortes id   

* POST /product: fügt ein neues Produkt und dessen Lagerbestand zu einem Lagerstandort hinzu
* GET /product: abrufen aller Produkte/Lagerbestand und deren Lagerstandort
* GET /product/{id}: abrufen eines Produktes id und dessen Lagerstandorte
* DELETE /product/{id}: löschen eines Produktes id auf einem Lagerstandort

Das Format und in welchen Zeitabständen die Daten eintreffen wird von Ihnen, als System Architekt, spezifiziert und implementiert.

Die Daten werden in der Zentrale in einem MongoDB Repository gespeichert und können hier zu Kontrollzwecken abgerufen werden (mongo Shell).

## 1.4 Demo Applikation

* Download Docker for MongoDB  
  `docker pull mongo`  

* Run Docker for MongoDB (using port 27017, name mongo)  
  `docker run -d -p 27017:27017 --name mongo mongo`  

* Run MongoShell on Docker Instance  
  `docker exec -it mongo bash`  
  `mongosh`  

* Execute MongoShell Commands    
  `show dbs`  
  `use local`   
  `db.startup_log.countDocuments();`    

* Accessing Data with MongoDB and Spring  
  - Build and Run Example  
	  `gradle clean bootRun`  

  - Check Data in MongoDB.  
    `docker exec -it mongo bash`
    `mongosh`
    `use test`
    `db.warehouseData.find()`  

## 1.5 Bewertung  

*   Gruppengrösse: 1 Person
*   Abgabemodus: per Protokoll und Abgabespraech
*   Grundlagen Anforderungen **"Grundlagen"**
    * Installation und Konfiguration einer dokumentenorientierten Middleware mit einem Framework Ihrer Wahl und MongoDB
    * Entwurf und Umsetzung einer entsprechenden JSON Datenstruktur
    * Speicherung der Daten von nur einem Lagerstandort
    * Speicherung der Daten in einer MongoDB Datenbank in der Zentrale
        - mindestens 10 Produkte in 3 Produktkategorien
    * REST API:
        - POST /product, GET /product, GET /warehouse
    * Beantwortung der Fragestellungen   
    * 5 CRUD Operationen über Mongo Shell
      Dokumentieren Sie den Mongo Shell Befehl und dessen Ergebnis.
      Beispiel: ein Produkt hinzufügen, ein Produkt löschen, ein Produkt ändern, ...
*   Erweiterte Anforderungen **"Erweiterte Grundlagen"**
    * Erweiterung der Datenstruktur, sodass ein Speicherung der Daten von mehreren Lagerstandorten möglich ist.
    * REST API: Implementierung der gesamten Schnittstelle, wie in der Angabe beschrieben
    * Implementieren Sie eine kleine Applikation, dass die Daten generiert und über das REST-Interfaces dieser Übung abspeichert.
      Dabei werden sowohl Produkte, als auch Lagerstandorte abgelegt.
*   Erweiterte Anforderungen **"Vertiefung"**
    * Generieren Testdaten für das Berichtswesen: mind. 300 Produkte in 6 Produktkategorien, 5 Warenhaeuser   
    * Formulierung 3 sinnvoller Fragestellungen für das Berichtswesen in der Zentrale und deren Abfragen in einer Mongo Shell.
      Beispiel:
      Wie ist der Lagerbestand von einem Produkt X über alle Lagerstandorte?
      Welche Produkte haben einen Lagerbestand von unter 10 Stück über alle Lagerstandorte?
    * Implementieren Sie eine Schnittstelle zu einer AI Instanz (lokal Ollama, cloud-basiert Gemini), um die Daten zu übertragen und lassen Sie sich zu den 3 Fragestellungen einen Bericht / Grafik von der AI entwerfen. Dokumentieren Sie hier die Anfragen, die Ihre Applikation an die AI Instanz sendet.

## 1.6 Fragestellung für Protokoll

+ Nennen Sie 4 Vorteile eines NoSQL Repository im Gegensatz zu einem relationalen DBMS
+ Nennen Sie 4 Nachteile eines NoSQL Repository im Gegensatz zu einem relationalen DBMS
+ Welche Schwierigkeiten ergeben sich bei der Zusammenführung der Daten?
+ Welche Arten von NoSQL Datenbanken gibt es?
+ Nennen Sie einen Vertreter für jede Art?
+ Beschreiben Sie die Abkürzungen CA, CP und AP in Bezug auf das CAP Theorem
+ Mit welchem Befehl koennen Sie den Lagerstand eines Produktes aller Lagerstandorte anzeigen.
+ Mit welchem Befehl koennen Sie den Lagerstand eines Produktes eines bestimmten Lagerstandortes anzeigen.

## 1.7 Links und Dokumente
* [Was bedeutet NoSQL](https://www.oracle.com/at/database/nosql/what-is-nosql)
* [Accessing Data with MongoDB](https://spring.io/guides/gs/accessing-data-mongodb/)
* [MongoDB Installation](https://docs.mongodb.com/manual/administration/install-community/)
* [mongo Shell Quick Reference](https://docs.mongodb.com/manual/reference/mongo-shell/)
* [mongo Shell Query Reference](https://www.mongodb.com/docs/manual/tutorial/query-embedded-documents/)
* [Grundlagen Spring Framework](https://spring.io/)
* [Spring Boot](https://spring.io/guides/gs/spring-boot/)
* [Spring Data MongoDB](https://spring.io/projects/spring-data-mongodb)
* [Spring RESTful Web Service](https://spring.io/guides/gs/rest-service/#use-maven)
* NoSQL Introduction
  - [NoSQL on w3resource](https://www.w3resource.com/mongodb/nosql.php)  
  - [Introduction to NoSQL Database](https://www.edureka.co/blog/introduction-to-nosql-database/)  
  - [NoSQL im Überblick](https://www.heise.de/ct/artikel/NoSQL-im-Ueberblick-1012483.html)  
  - [Introduction to NoSQL Databases on YouTube ](https://www.youtube.com/watch?v=2yQ9TGFpDuM)  


## 1.8 Mongo Shell Abfragen  
  
Link to [Mongo Shell Query and Projection Operators](https://docs.mongodb.com/manual/reference/operator/query/)

Den Demo-Abfragen liegt folgende Datenstruktur zu Grunde:   
   `{  `  
   `    warehouseID: '1',   `   
   `    warehouseName: 'Linz Bahnhof',   `   
   `   timestamp: '2022-01-02 01:00:00',   `   
   `    warehousePostalCode: 4010,`    
   `   warehouseCity: 'Linz',`   
   `   warehouseCountrz: 'Austria',`   
   `   productData: [`  
   `      { productID: '00-443175', productName: 'Bio Orangensaft Sonne', productQuantity: 2500 },`    
   `      { productID: '00-871895', productName: 'Bio Apfelsaft Gold', productQuantity: 3420 },`    
   `      { productID: '01-926885', productName: 'Ariel Waschmittel Color', productQuantity: 478 },`     
   `   ]`   
    `}`
  
* Filtern nach dem Lagerstandort 1    
`db.productData.find( { 
	"warehouseID": "1"
} )`


* Filtern nach Lagerstandort 1 und dem Produkt mit dem Namen "Bio Apfelsaft Gold"  
`db.productData.find( { 
	"warehouseID": "1",
        "productName": "Bio Apfelsaft Gold"
} )`

* Filtern nach allen Produkten, die einen Lagerbestand unter 500 Stueck haben.  
`db.productData.find( { 
	"productQuantity": { $lte: 500 }
} )`

* Filtern nach Lagerstandort 1 und einem Lagerbestand unter 500 Stueck haben.  
`db.productData.find( { 
    "warehouseID": "1",
    "productQuantity": { $lte: 500 }
} )`

* Filtern nach allen Produkten der Produktkategorien.  
`db.productData.find( { 
     productCategory: { $in: [ "Waschmittel", "Getraenk" ] } 
} )`
