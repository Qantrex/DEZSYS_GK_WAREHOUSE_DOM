package warehouse.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import warehouse.model.ProductData;
import warehouse.repository.WarehouseRepository;

/**
 * Business logic on top of the MongoDB repository. Keeps the REST controller
 * thin and is also used to seed/refresh the data.
 */
@Service
public class WarehouseService {

	@Autowired
	private WarehouseRepository repository;

	@Autowired
	private DataGenerator generator;

	/** Drops the collection and re-creates the test data set. */
	public long resetWithTestData() {
		repository.deleteAll();
		List<ProductData> data = generator.generateAll();
		repository.saveAll(data);
		return data.size();
	}

	// --- product oriented operations ---

	public List<ProductData> getAllProducts() {
		return repository.findAll();
	}

	public List<ProductData> getProductById(String productID) {
		return repository.findByProductID(productID);
	}

	public ProductData addProduct(ProductData product) {
		// guarantee every stored entry carries a timestamp (task requirement)
		if (product.getTimestamp() == null) {
			product.setTimestamp(java.time.LocalDateTime.now());
		}
		return repository.save(product);
	}

	public void deleteProduct(String productID) {
		List<ProductData> entries = repository.findByProductID(productID);
		repository.deleteAll(entries);
	}

	public List<ProductData> getProductsByCategory(String category) {
		return repository.findByProductCategory(category);
	}

	public List<ProductData> getLowStock(double threshold) {
		return repository.findByProductQuantityLessThanEqual(threshold);
	}

	// --- warehouse oriented operations ---

	/** All distinct warehouse locations with their product count and total stock. */
	public List<Map<String, Object>> getAllWarehouses() {
		Map<String, Map<String, Object>> byWarehouse = new LinkedHashMap<>();
		for (ProductData p : repository.findAll()) {
			Map<String, Object> wh = byWarehouse.computeIfAbsent(p.getWarehouseID(), id -> {
				Map<String, Object> m = new LinkedHashMap<>();
				m.put("warehouseID", p.getWarehouseID());
				m.put("warehouseName", p.getWarehouseName());
				m.put("warehouseCity", p.getWarehouseCity());
				m.put("productCount", 0);
				m.put("totalQuantity", 0.0);
				return m;
			});
			wh.put("productCount", (int) wh.get("productCount") + 1);
			wh.put("totalQuantity", (double) wh.get("totalQuantity") + p.getProductQuantity());
		}
		return new ArrayList<>(byWarehouse.values());
	}

	public List<ProductData> getWarehouseById(String warehouseID) {
		return repository.findByWarehouseID(warehouseID);
	}

	public void deleteWarehouse(String warehouseID) {
		List<ProductData> entries = repository.findByWarehouseID(warehouseID);
		repository.deleteAll(entries);
	}
}
