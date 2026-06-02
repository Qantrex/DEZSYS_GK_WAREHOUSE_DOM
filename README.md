# Document Oriented Middleware – Warehouse (MongoDB)

Zentrale dokumentenorientierte Middleware mit **Spring Boot + Spring Data MongoDB**.
Die Daten aller Lagerstandorte werden über ein REST-Interface entgegengenommen und im
JSON-Format in einem zentralen MongoDB-Repository persistiert. Von dort können sie für
verschiedene Fragestellungen des Betriebes (Management, Einkauf, Vertrieb) abgefragt werden.

> Die Original-Angabe befindet sich in [`ANGABE.md`](ANGABE.md) bzw. [`TASK.md`](TASK.md).

## Setup

### Start

```bash
# MongoDB (Docker)
docker pull mongo
docker run -d -p 27017:27017 --name mongo mongo   # oder: docker start mongo

# Application
./gradlew clean bootRun
```

Beim Start befüllt die mitgelieferte Client-Applikation das Repository automatisch über
die REST-Schnittstelle (siehe Code-Snippet 4). Für das Berichtswesen ("Vertiefung")
werden **300 Produkte in 6 Kategorien** an **5 Lagerstandorten** generiert =
**1500 Lagerbestands-Einträge** (siehe Abschnitt [Erweiterte Anforderungen – Berichtswesen
("Vertiefung")](#erweiterte-anforderungen--berichtswesen-vertiefung)).

Web-UI zur Anzeige der Daten: <http://localhost:8080/>

### Stop

```bash
# Application: Ctrl+C im bootRun-Terminal
# MongoDB
docker stop mongo
```

### Mongo Shell

```bash
docker exec -it mongo mongosh
use test
db.productData.find()
```

---

## Basistest (Start → Test → Stop)

Manuelle Schritt-für-Schritt-Anleitung für den Basistest dieses Projekts (identisch
mit dem Block in der zentralen [`instructions.md`](../instructions.md); automatisiert
über `../run_all.sh`). DOM und ORM nutzen beide Port **8080** – nicht gleichzeitig
starten.

```bash
cd DEZSYS_GK_WAREHOUSE_DOM

# --- MongoDB starten (Docker) ---
docker run -d -p 27017:27017 --name dezsys_mongo mongo   # beim ersten Mal
# docker start dezsys_mongo                               # falls schon vorhanden
sleep 4

# --- App bauen + starten (Port 8080) ---
./gradlew clean bootJar
java -jar build/libs/*.jar &
# warten, bis im Log steht: "Tomcat started on port 8080" / "Started Application"
sleep 25

# --- Smoke-Check (App seedet beim Start 1500 Einträge / 5 Lager) ---
curl http://localhost:8080/warehouse

# --- App stoppen, dann MongoDB ---
pkill -f 'build/libs'         # oder Ctrl+C im Vordergrund
docker stop dezsys_mongo
```

> Alternative zu `bootJar` + `java -jar`: einfach `./gradlew bootRun` (Vordergrund,
> stoppen mit Ctrl+C). Der Jar-Ansatz wird im Skript verwendet, weil er sich leichter
> aus einem Skript heraus beenden lässt.

---

## How to Use

### Datenstruktur (ein Dokument der Collection `productData`)

```json
{
  "_id": "6a1eb768e5404267d41c7700",
  "warehouseID": "1",
  "warehouseName": "Linz Bahnhof",
  "warehouseCity": "Linz",
  "productID": "00-000003",
  "productName": "Orangensaft Bio",
  "productCategory": "Getraenk",
  "productQuantity": 2525.0,
  "timestamp": "2026-06-02T12:58:48.288"
}
```

Die Struktur ist bewusst **denormalisiert** (Lager-Metadaten liegen an jedem Produkt-Eintrag).
Das ist der typische NoSQL-Ansatz und erlaubt Filtern/Aggregieren über Lager **und** Produkte
in einer einzigen Collection ohne Joins. Mehrere Lagerstandorte werden über `warehouseID`
abgebildet; der `timestamp` stellt sicher, dass keine Daten verloren gehen.

### Endpoints

| Method | URL | Beschreibung |
|--------|-----|--------------|
| `POST` | `http://localhost:8080/product` | Produkt/Lagerbestand zu einem Lager hinzufügen |
| `GET` | `http://localhost:8080/product` | Alle Produkte inkl. Lagerstandort |
| `GET` | `http://localhost:8080/product/{id}` | Ein Produkt (productID) über alle Lagerstandorte |
| `DELETE` | `http://localhost:8080/product/{id}` | Produkt auf allen Lagerstandorten löschen |
| `POST` | `http://localhost:8080/warehouse` | Lagerbestands-Eintrag hinzufügen |
| `GET` | `http://localhost:8080/warehouse` | Alle Lagerstandorte + Bestands-Übersicht |
| `GET` | `http://localhost:8080/warehouse/{id}` | Ein Lagerstandort + dessen Lagerbestand |
| `DELETE` | `http://localhost:8080/warehouse/{id}` | Lagerstandort löschen |
| `GET` | `http://localhost:8080/report/category?name=Getraenk` | Produkte einer Kategorie |
| `GET` | `http://localhost:8080/report/lowstock?max=100` | Produkte mit Bestand ≤ max |
| `POST` | `http://localhost:8080/admin/reset` | Repository mit Testdaten neu befüllen |

### CLI Testing

```bash
# Alle Lagerstandorte (Übersicht mit Produktanzahl + Gesamtbestand)
curl http://localhost:8080/warehouse

# Lagerstandort 1 mit komplettem Lagerbestand
curl http://localhost:8080/warehouse/1

# Ein Produkt über alle Lagerstandorte
curl http://localhost:8080/product/00-000003

# Neues Produkt zu einem Lager hinzufügen (POST)
curl -X POST http://localhost:8080/product \
  -H "Content-Type: application/json" \
  -d '{"warehouseID":"1","warehouseName":"Linz Bahnhof","warehouseCity":"Linz",
       "productID":"99-999999","productName":"Test Produkt",
       "productCategory":"Getraenk","productQuantity":42}'

# Produkt löschen (DELETE)
curl -X DELETE http://localhost:8080/product/99-999999

# Reporting: Produkte unter 100 Stück
curl "http://localhost:8080/report/lowstock?max=100"

# Repository neu befüllen
curl -X POST http://localhost:8080/admin/reset
```

---

## Code Snippets

### 1 – MongoDB-Dokument (`@Document`)

```java
// ProductData.java
@Document(collection = "productData")
public class ProductData {
    @Id
    private String ID;
    private String warehouseID;
    private String productID;
    private double productQuantity;
    private LocalDateTime timestamp;
    // ...
}
```

`@Document` legt die Ziel-Collection fest, `@Id` markiert den von MongoDB generierten
Primärschlüssel (`_id`). Im Gegensatz zu JPA gibt es keine fixen Tabellen-Schemata –
jedes Dokument trägt seine Felder selbst.

---

### 2 – Abgeleitete Query-Methoden im Repository

```java
// WarehouseRepository.java
public interface WarehouseRepository extends MongoRepository<ProductData, String> {
    List<ProductData> findByProductID(String productID);
    List<ProductData> findByWarehouseID(String warehouseID);
    List<ProductData> findByProductCategory(String productCategory);
    List<ProductData> findByProductQuantityLessThanEqual(double threshold);
}
```

Spring Data MongoDB erzeugt die MongoDB-Query automatisch aus dem Methodennamen –
`findByProductQuantityLessThanEqual` wird z.B. zu `{ productQuantity: { $lte: ... } }`.

---

### 3 – REST-Controller (vollständige Schnittstelle)

```java
@RestController
@CrossOrigin(origins = "*")
public class WarehouseController {
    @PostMapping("/product")            // C
    public ProductData addProduct(@RequestBody ProductData p) { return service.addProduct(p); }

    @GetMapping("/product/{id}")        // R – ein Produkt über alle Lager
    public List<ProductData> getProduct(@PathVariable String id) { return service.getProductById(id); }

    @DeleteMapping("/warehouse/{id}")   // D – ganzen Lagerstandort entfernen
    public ResponseEntity<String> deleteWarehouse(@PathVariable String id) { ... }
}
```

`@RestController` serialisiert Rückgabewerte automatisch zu JSON; `@RequestBody`
deserialisiert eingehendes JSON zu einem `ProductData`-Objekt.

---

### 4 – Client-Applikation: Daten generieren & über REST speichern

```java
// WarehouseDataClient.java  ("Erweiterte Grundlagen")
@EventListener(ApplicationReadyEvent.class)
public void seedViaRest() {
    clearAllWarehouses();                       // DELETE /warehouse/{id}
    for (ProductData entry : generator.generateAll()) {
        restTemplate.postForObject(baseUrl() + "/product", entry, ProductData.class); // POST
    }
}
```

Eine kleine In-Process-Applikation generiert den kompletten Datensatz und legt ihn –
bewusst **über die REST-Schnittstelle** (nicht direkt im Repository) – ab. Das
demonstriert den vollständigen Round-Trip *Generator → REST → MongoDB*.

---

### 5 – application.properties (MongoDB-Verbindung)

```properties
spring.data.mongodb.uri=${MONGO_URI:mongodb://localhost:27017/test}
spring.data.mongodb.database=${MONGO_DB:test}
server.port=8080
warehouse.client.seed-on-startup=true
warehouse.scheduler.enabled=false
```

Die Syntax `${VAR:default}` liest den Wert aus einer Umgebungsvariable und fällt auf den
Standardwert zurück. Spring Boot Auto-Configuration baut daraus automatisch den
`MongoClient` und das `WarehouseRepository`.

---

### 6 – Kontinuierliche Speicherung (Scheduler)

```java
// WarehouseScheduler.java
@ConditionalOnProperty(name = "warehouse.scheduler.enabled", havingValue = "true")
public class WarehouseScheduler {
    @Scheduled(fixedRateString = "${warehouse.scheduler.rate:30000}")
    public void refreshStock() {
        // jede Bestandszeile mit neuer Menge + neuem timestamp speichern
        repository.saveAll(updatedEntries);
    }
}
```

`@EnableScheduling` (in `Application`) + `@Scheduled` realisieren die kontinuierliche
Speicherung: in einem festen Intervall werden die Lagerstände aktualisiert und mit einem
neuen Zeitstempel persistiert. Per `@ConditionalOnProperty` standardmäßig deaktiviert.

---

# Protokoll – Document Oriented Middleware (MongoDB)

## Fragen

### Nennen Sie 4 Vorteile eines NoSQL Repository gegenüber einem relationalen DBMS

1. **Flexibles Schema** – Dokumente derselben Collection können unterschiedliche Felder
   haben; Strukturänderungen erfordern keine Migration.
2. **Horizontale Skalierbarkeit** – einfaches Sharding über mehrere Knoten, dadurch sehr
   große Datenmengen und hoher Durchsatz.
3. **Performance bei denormalisierten Daten** – zusammengehörige Daten liegen in einem
   Dokument, kein teures JOIN über mehrere Tabellen notwendig.
4. **Natürliche Abbildung von Objekten/JSON** – die Datenstruktur entspricht direkt den
   Anwendungsobjekten (Object-Document-Mapping ohne Impedance Mismatch).

### Nennen Sie 4 Nachteile eines NoSQL Repository gegenüber einem relationalen DBMS

1. **Keine (bzw. eingeschränkte) ACID-Transaktionen** über mehrere Dokumente – meist nur
   *eventual consistency*.
2. **Datenredundanz** durch Denormalisierung – dieselbe Information (z.B. Lager-Name) wird
   mehrfach gespeichert und muss bei Änderungen überall aktualisiert werden.
3. **Keine standardisierte Abfragesprache** – jede Datenbank hat eine eigene API/Syntax
   (kein universelles SQL), schwächere Ad-hoc-Joins.
4. **Schema-Disziplin liegt bei der Applikation** – ohne erzwungenes Schema drohen
   Inkonsistenzen, die das DBMS nicht abfängt.

### Welche Schwierigkeiten ergeben sich bei der Zusammenführung der Daten?

Die Daten kommen von mehreren, unabhängigen Lagerstandorten zusammen. Probleme:
- **Uneinheitliche Bezeichner/Formate** (productID, Einheiten, Kategorienamen) müssen
  vereinheitlicht werden.
- **Doppelte/widersprüchliche Einträge** desselben Produkts an verschiedenen Standorten.
- **Zeitliche Konsistenz** – Daten treffen asynchron ein; ein `timestamp` ist nötig, um
  Stände zeitlich zuordnen zu können und keine Daten zu verlieren.
- **Aggregation** – für betriebsweite Auswertungen muss über alle Standorte summiert werden.

### Welche Arten von NoSQL Datenbanken gibt es? Nennen Sie einen Vertreter für jede Art

| Art | Beschreibung | Vertreter |
|---|---|---|
| **Document Store** | JSON/BSON-Dokumente | **MongoDB**, CouchDB |
| **Key-Value Store** | einfache Schlüssel-Wert-Paare | **Redis**, Amazon DynamoDB |
| **Column-Family / Wide-Column** | spaltenorientiert | **Apache Cassandra**, HBase |
| **Graph Database** | Knoten + Kanten (Beziehungen) | **Neo4j**, JanusGraph |

### Beschreiben Sie CA, CP und AP im Bezug auf das CAP-Theorem

Das CAP-Theorem besagt, dass ein verteiltes System nur **zwei** von drei Eigenschaften
gleichzeitig garantieren kann: **C**onsistency, **A**vailability, **P**artition tolerance.

- **CA** – Konsistenz + Verfügbarkeit, aber **keine** Partitionstoleranz. Funktioniert nur
  ohne Netzwerk-Partition, z.B. ein klassisches Einzelknoten-RDBMS.
- **CP** – Konsistenz + Partitionstoleranz, gibt bei einer Partition **Verfügbarkeit** auf
  (Anfragen werden abgelehnt, bis Konsistenz sicher ist), z.B. MongoDB (default),
  HBase.
- **AP** – Verfügbarkeit + Partitionstoleranz, gibt **strikte Konsistenz** auf
  (*eventual consistency*), z.B. Cassandra, DynamoDB.

### Mit welchem Befehl können Sie den Lagerstand eines Produktes aller Lagerstandorte anzeigen?

```js
db.productData.find( { productName: "Apfelsaft Bio" },
                     { _id:0, warehouseID:1, warehouseCity:1, productQuantity:1 } )
```

Aggregiert (Gesamtbestand über alle Standorte):

```js
db.productData.aggregate([
  { $match: { productName: "Apfelsaft Bio" } },
  { $group: { _id: "$productName", total: { $sum: "$productQuantity" } } }
])
// -> { _id: 'Apfelsaft Bio', total: 11275 }
```

### Mit welchem Befehl können Sie den Lagerstand eines Produktes eines bestimmten Lagerstandortes anzeigen?

```js
db.productData.find( { warehouseID: "1", productName: "Color Waschmittel Frisch" },
                     { _id:0, warehouseCity:1, productQuantity:1 } )
// -> { warehouseCity: 'Linz', productQuantity: 1140 }
```

---

## 5 CRUD-Operationen über die Mongo Shell

Jeweils Befehl **und** dokumentiertes Ergebnis (Collection `test.productData`).

### 1) CREATE – Produkt hinzufügen

```js
db.productData.insertOne({ warehouseID:"1", warehouseName:"Linz Bahnhof", warehouseCity:"Linz",
  productID:"05-500001", productName:"Demo Kaffee", productCategory:"Getraenk", productQuantity:120 })
```
```js
{ acknowledged: true, insertedId: ObjectId('6a1eb7dd8739c589249df8a3') }
```

### 2) READ – Produkt eines Lagerstandortes lesen

```js
db.productData.find({ warehouseID:"1", productName:"Demo Kaffee" }, { _id:0 })
```
```js
{ warehouseID: '1', warehouseName: 'Linz Bahnhof', warehouseCity: 'Linz',
  productID: '05-500001', productName: 'Demo Kaffee',
  productCategory: 'Getraenk', productQuantity: 120 }
```

### 3) UPDATE – Lagerbestand ändern

```js
db.productData.updateOne({ productID:"05-500001" }, { $set: { productQuantity: 999 } })
```
```js
{ acknowledged: true, matchedCount: 1, modifiedCount: 1, upsertedCount: 0 }
```

### 4) DELETE – Produkt löschen

```js
db.productData.deleteOne({ productID:"05-500001" })
```
```js
{ acknowledged: true, deletedCount: 1 }
```

### 5) COUNT – Anzahl Dokumente

```js
db.productData.countDocuments()
```
```js
1500
```

---

# Erweiterte Anforderungen – Berichtswesen ("Vertiefung")

Für ein aussagekräftiges Berichtswesen in der Zentrale wird ein deutlich größerer
Testdatensatz benötigt. Die `DataGenerator`-Klasse (Code-Snippet 4) erzeugt dafür
programmatisch:

| Kennzahl | Wert |
|---|---|
| **Produkte (Katalog)** | **300** distinct Produkte |
| **Produktkategorien** | **6** (Getraenk, Waschmittel, Tierfutter, Reinigung, Lebensmittel, Drogerie) |
| **Lagerstandorte** | **5** (Linz, Wien, Graz, Salzburg, Innsbruck) |
| **Lagerbestands-Einträge** | **300 × 5 = 1500** Dokumente in `productData` |

Jede der 6 Kategorien steuert 50 Produkte bei (10 Produkttypen × 5 Varianten); jedes
Produkt wird an allen 5 Lagerstandorten geführt. Etwa 5 % der Einträge werden bewusst
mit einem Bestand unter 10 Stück generiert, damit die Nachbestell-Auswertung sinnvolle
Treffer liefert.

```js
// Kontrolle des Datensatzes in der Mongo Shell
db.productData.countDocuments()                  // -> 1500
db.productData.distinct("productCategory").length // -> 6
db.productData.distinct("warehouseID").length     // -> 5
db.productData.distinct("productID").length       // -> 300
```

## 3 Fragestellungen für das Berichtswesen in der Zentrale

### F1 (Vertrieb/Einkauf) – Wie hoch ist der Gesamtbestand eines Produktes X über *alle* Lagerstandorte?

Aggregiert den Bestand eines Produktes über die 5 Standorte zu einer Gesamtsumme – die
zentrale Frage für Disposition und Verfügbarkeitsauskunft.

```js
db.productData.aggregate([
  { $match: { productName: "Apfelsaft Bio" } },
  { $group: { _id: "$productName",
              totalStock: { $sum: "$productQuantity" },
              locations:  { $sum: 1 } } }
])
// -> { _id: 'Apfelsaft Bio', totalStock: 11275, locations: 5 }
```

### F2 (Einkauf) – Welche Produkte müssen nachbestellt werden (kritischer *Gesamtbestand* über alle Lager)?

Nicht der einzelne Standort, sondern der unternehmensweite Gesamtbestand entscheidet über
eine Nachbestellung. Hier: alle Produkte, deren Summe über alle 5 Lager unter 5000 Stück
liegt, aufsteigend sortiert (dringendste zuerst).

```js
db.productData.aggregate([
  { $group: { _id: { productID: "$productID", productName: "$productName" },
              totalStock: { $sum: "$productQuantity" } } },
  { $match: { totalStock: { $lt: 5000 } } },
  { $sort:  { totalStock: 1 } }
])
// -> { _id: { productID: '03-000050', productName: 'Kalkreiniger Power' }, totalStock: 4121 }
//    { _id: { productID: '00-000010', productName: 'Apfelsaft Premium' }, totalStock: 4705 }
//    { _id: { productID: '04-000027', productName: 'Zucker Bio'        }, totalStock: 4932 }
```

> Variante exakt nach Beispiel-Angabe ("Lagerbestand unter 10 Stück"): einzelne
> Lagerbestände unter 10 Stück liefern
> `db.productData.countDocuments({ productQuantity: { $lt: 10 } })` → **78** Einträge.

### F3 (Management) – Wie verteilt sich der Gesamtbestand auf die 6 Produktkategorien?

Sortiment-Überblick für die Geschäftsführung: Bestand und Produktanzahl je Kategorie.

```js
db.productData.aggregate([
  { $group: { _id: "$productCategory",
              totalStock: { $sum: "$productQuantity" },
              products:   { $sum: 1 } } },
  { $sort: { totalStock: -1 } }
])
// -> Getraenk     619099 (250) | Drogerie  618044 (250) | Lebensmittel 614653 (250)
//    Waschmittel  585052 (250) | Reinigung 577446 (250) | Tierfutter   577302 (250)
```

### Zusatz – Gesamtbestand je Lagerstandort (Logistik)

```js
db.productData.aggregate([
  { $group: { _id: { id: "$warehouseID", city: "$warehouseCity" },
              totalStock: { $sum: "$productQuantity" },
              products:   { $sum: 1 } } },
  { $sort: { totalStock: -1 } }
])
// -> Innsbruck 751588 | Graz 736230 | Linz 716176 | Salzburg 701852 | Wien 685750
```

---

## Weitere Mongo-Shell-Abfragen (aus der Angabe)

```js
// Filtern nach Lagerstandort 1
db.productData.find( { warehouseID: "1" } )

// Lagerstandort 1 + bestimmtes Produkt
db.productData.find( { warehouseID: "1", productName: "Apfelsaft Bio" } )

// Alle Produkte mit Bestand unter 500 Stück
db.productData.find( { productQuantity: { $lte: 500 } } )

// Lagerstandort 1 + Bestand unter 500 Stück
db.productData.find( { warehouseID: "1", productQuantity: { $lte: 500 } } )

// Produkte bestimmter Kategorien
db.productData.find( { productCategory: { $in: [ "Waschmittel", "Getraenk" ] } } )
```
