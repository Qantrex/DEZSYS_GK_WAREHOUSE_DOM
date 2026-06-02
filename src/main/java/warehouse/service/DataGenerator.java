package warehouse.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Component;

import warehouse.model.ProductData;

/**
 * Generates the test data for the central repository.
 *
 * Advanced requirement: at least
 * 300 products in 6 product categories
 */
@Component
public class DataGenerator {

	/** A warehouse location. */
	public record Warehouse(String id, String name, String city) {
	}

	/** A product in the master catalog (same productID across all warehouses). */
	public record CatalogItem(String productID, String name, String category) {
	}

	/** Blueprint for one product category: id prefix + product types + variants. */
	private record Category(String prefix, String name, String[] types, String[] variants) {
	}

	/** 5 warehouse locations (advanced requirement: "5 Warenhaeuser"). */
	public static final List<Warehouse> WAREHOUSES = List.of(
		new Warehouse("1", "Linz Bahnhof", "Linz"),
		new Warehouse("2", "Wien Mitte", "Wien"),
		new Warehouse("3", "Graz Hauptplatz", "Graz"),
		new Warehouse("4", "Salzburg Altstadt", "Salzburg"),
		new Warehouse("5", "Innsbruck Zentrum", "Innsbruck")
	);

	/**
	 * 6 product categories, each with 10 product types and 5 variants
	 * = 50 distinct products per category = 300 products in total.
	 */
	private static final List<Category> CATEGORIES = List.of(
		new Category("00", "Getraenk",
			new String[] { "Orangensaft", "Apfelsaft", "Mineralwasser", "Cola", "Eistee",
				"Energy Drink", "Traubensaft", "Limonade", "Tomatensaft", "Eiskaffee" },
			new String[] { "Classic", "Zero", "Bio", "Light", "Premium" }),
		new Category("01", "Waschmittel",
			new String[] { "Color Waschmittel", "Universal Waschmittel", "Weichspueler",
				"Fleckenentferner", "Spuelmaschinen Tabs", "Entkalker", "Vollwaschmittel",
				"Feinwaschmittel", "Waschpulver", "Hygienespueler" },
			new String[] { "Frisch", "Sensitiv", "Ultra", "Plus", "Konzentrat" }),
		new Category("02", "Tierfutter",
			new String[] { "Katzenfutter", "Hundefutter", "Trockenfutter", "Nassfutter",
				"Nagerfutter", "Vogelfutter", "Fischfutter", "Pferdefutter",
				"Kaninchenfutter", "Welpenfutter" },
			new String[] { "Rind", "Huhn", "Fisch", "Lamm", "Gefluegel" }),
		new Category("03", "Reinigung",
			new String[] { "Allzweckreiniger", "Glasreiniger", "WC Reiniger", "Scheuermilch",
				"Bodenreiniger", "Badreiniger", "Kuechenreiniger", "Edelstahlreiniger",
				"Polsterreiniger", "Kalkreiniger" },
			new String[] { "Zitrone", "Frisch", "Aktiv", "Sensitiv", "Power" }),
		new Category("04", "Lebensmittel",
			new String[] { "Spaghetti", "Basmati Reis", "Olivenoel", "Tomatensauce", "Mehl",
				"Zucker", "Schokocreme", "Cornflakes", "Honig", "Nudeln" },
			new String[] { "Classic", "Bio", "Fein", "Gold", "Natur" }),
		new Category("05", "Drogerie",
			new String[] { "Shampoo", "Duschgel", "Zahnpasta", "Handseife", "Bodylotion",
				"Deospray", "Haarspray", "Rasierschaum", "Sonnencreme", "Feuchttuecher" },
			new String[] { "Sensitiv", "Fresh", "Repair", "Care", "Soft" })
	);

	/** The master catalog: 300 distinct products across 6 categories. */
	public static final List<CatalogItem> CATALOG = buildCatalog();

	/**
	 * Builds the 300-product master catalog by combining, per category, every
	 * product type with every variant. ProductIDs follow the existing scheme
	 * {@code "<prefix>-<6 digits>"}, e.g. {@code 00-000001}.
	 */
	private static List<CatalogItem> buildCatalog() {
		List<CatalogItem> catalog = new ArrayList<>();
		for (Category cat : CATEGORIES) {
			int counter = 1;
			for (String type : cat.types()) {
				for (String variant : cat.variants()) {
					String productID = String.format("%s-%06d", cat.prefix(), counter++);
					String name = type + " " + variant;
					catalog.add(new CatalogItem(productID, name, cat.name()));
				}
			}
		}
		return catalog;
	}

	/**
	 * Builds the full set of stock entries: every catalog product at every
	 * warehouse with a (deterministic) random quantity. About 5% of the entries
	 * are intentionally below 10 units so the reporting query for critically low
	 * stock returns meaningful results.
	 */
	public List<ProductData> generateAll() {
		// fixed seed -> reproducible test data for the protocol
		Random random = new Random(42);
		List<ProductData> result = new ArrayList<>();

		for (Warehouse wh : WAREHOUSES) {
			for (CatalogItem item : CATALOG) {
				// every ~20th entry is intentionally low (< 10) for low-stock reporting,
				// the rest is spread between 0 and ~5000 units
				int quantity = (random.nextInt(20) == 0) ? random.nextInt(10) : random.nextInt(5000);
				result.add(new ProductData(
					wh.id(), wh.name(), wh.city(),
					item.productID(), item.name(), item.category(), quantity));
			}
		}
		return result;
	}
}
