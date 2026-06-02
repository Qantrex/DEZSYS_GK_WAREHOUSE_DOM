package warehouse.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Component;

import warehouse.model.ProductData;

/**
 * Generates the test data for the central repository.
 *
 * Requirement (TASK.md 1.5): at least 30 products in 5 product categories,
 * stored for several warehouse locations. The catalog below contains 36
 * distinct products across 5 categories; every product is stocked at every
 * warehouse, which results in 36 * {#warehouses} stock entries.
 */
@Component
public class DataGenerator {

	/** A warehouse location. */
	public record Warehouse(String id, String name, String city) {
	}

	/** A product in the master catalog (same productID across all warehouses). */
	public record CatalogItem(String productID, String name, String category) {
	}

	public static final List<Warehouse> WAREHOUSES = List.of(
		new Warehouse("1", "Linz Bahnhof", "Linz"),
		new Warehouse("2", "Wien Mitte", "Wien"),
		new Warehouse("3", "Graz Hauptplatz", "Graz")
	);

	public static final List<CatalogItem> CATALOG = List.of(
		// --- Getraenk (8) ---
		new CatalogItem("00-443175", "Bio Orangensaft Sonne", "Getraenk"),
		new CatalogItem("00-871895", "Bio Apfelsaft Gold", "Getraenk"),
		new CatalogItem("00-112233", "Mineralwasser Still", "Getraenk"),
		new CatalogItem("00-112244", "Mineralwasser Classic", "Getraenk"),
		new CatalogItem("00-556677", "Cola Fresh", "Getraenk"),
		new CatalogItem("00-667788", "Eistee Pfirsich", "Getraenk"),
		new CatalogItem("00-778899", "Energy Drink Boost", "Getraenk"),
		new CatalogItem("00-889900", "Bio Traubensaft Rot", "Getraenk"),
		// --- Waschmittel (7) ---
		new CatalogItem("01-926885", "Ariel Waschmittel Color", "Waschmittel"),
		new CatalogItem("01-100001", "Persil Universal", "Waschmittel"),
		new CatalogItem("01-100002", "Lenor Weichspueler", "Waschmittel"),
		new CatalogItem("01-100003", "Vanish Oxi Action", "Waschmittel"),
		new CatalogItem("01-100004", "Spuelmaschinen Tabs All-in-1", "Waschmittel"),
		new CatalogItem("01-100005", "Calgon Entkalker", "Waschmittel"),
		new CatalogItem("01-100006", "Weisser Riese Pulver", "Waschmittel"),
		// --- Tierfutter (7) ---
		new CatalogItem("02-234811", "Mampfi Katzenfutter Rind", "Tierfutter"),
		new CatalogItem("02-200001", "Mampfi Hundefutter Huhn", "Tierfutter"),
		new CatalogItem("02-200002", "Wau Trockenfutter", "Tierfutter"),
		new CatalogItem("02-200003", "Miez Nassfutter Fisch", "Tierfutter"),
		new CatalogItem("02-200004", "Nager Knabberstangen", "Tierfutter"),
		new CatalogItem("02-200005", "Vogelfutter Mix", "Tierfutter"),
		new CatalogItem("02-200006", "Aqua Fischfutter Flocken", "Tierfutter"),
		// --- Reinigung (7) ---
		new CatalogItem("03-893173", "Saugstauberbeutel Ingres", "Reinigung"),
		new CatalogItem("03-300001", "Allzweckreiniger Zitrone", "Reinigung"),
		new CatalogItem("03-300002", "Glasreiniger Klar", "Reinigung"),
		new CatalogItem("03-300003", "WC Frisch Aktiv", "Reinigung"),
		new CatalogItem("03-300004", "Scheuermilch Sanft", "Reinigung"),
		new CatalogItem("03-300005", "Microfasertuch Set", "Reinigung"),
		new CatalogItem("03-300006", "Bodenwischer Profi", "Reinigung"),
		// --- Lebensmittel (7) ---
		new CatalogItem("04-400001", "Spaghetti No.5", "Lebensmittel"),
		new CatalogItem("04-400002", "Basmati Reis", "Lebensmittel"),
		new CatalogItem("04-400003", "Bio Olivenoel Extra", "Lebensmittel"),
		new CatalogItem("04-400004", "Tomatensauce Classico", "Lebensmittel"),
		new CatalogItem("04-400005", "Mehl Type 405", "Lebensmittel"),
		new CatalogItem("04-400006", "Zucker Fein", "Lebensmittel"),
		new CatalogItem("04-400007", "Haselnuss Schokocreme", "Lebensmittel")
	);

	/**
	 * Builds the full set of stock entries: every catalog product at every
	 * warehouse with a (deterministic) random quantity.
	 */
	public List<ProductData> generateAll() {
		// fixed seed -> reproducible test data for the protocol
		Random random = new Random(42);
		List<ProductData> result = new ArrayList<>();

		for (Warehouse wh : WAREHOUSES) {
			for (CatalogItem item : CATALOG) {
				// quantities between 0 and ~5000, a few intentionally low for reporting
				int quantity = random.nextInt(5000);
				result.add(new ProductData(
					wh.id(), wh.name(), wh.city(),
					item.productID(), item.name(), item.category(), quantity));
			}
		}
		return result;
	}
}
