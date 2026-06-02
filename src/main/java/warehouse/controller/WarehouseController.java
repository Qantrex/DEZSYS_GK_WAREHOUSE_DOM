package warehouse.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import warehouse.model.ProductData;
import warehouse.service.WarehouseService;

/**
 * REST interface of the central document oriented middleware.
 *
 * Implements the endpoints from the task description:
 *   POST   /product           add a product stock entry
 *   GET    /product           all products and their warehouse
 *   GET    /product/{id}      one product (by productID) and its warehouses
 *   DELETE /product/{id}      delete a product on all warehouses
 *   POST   /warehouse         add a warehouse stock entry
 *   GET    /warehouse         all warehouses and their stock summary
 *   GET    /warehouse/{id}    one warehouse and its stock
 *   DELETE /warehouse/{id}    delete a warehouse
 *
 * Plus convenience endpoints used by the web UI / reporting.
 */
@RestController
@CrossOrigin(origins = "*")
public class WarehouseController {

	@Autowired
	private WarehouseService service;

	// --------------------------------------------------------------- product
	@PostMapping("/product")
	public ProductData addProduct(@RequestBody ProductData product) {
		return service.addProduct(product);
	}

	@GetMapping("/product")
	public List<ProductData> getAllProducts() {
		return service.getAllProducts();
	}

	@GetMapping("/product/{id}")
	public List<ProductData> getProduct(@PathVariable("id") String productID) {
		return service.getProductById(productID);
	}

	@DeleteMapping("/product/{id}")
	public ResponseEntity<String> deleteProduct(@PathVariable("id") String productID) {
		service.deleteProduct(productID);
		return ResponseEntity.ok("Deleted product " + productID + " on all warehouses");
	}

	// ------------------------------------------------------------- warehouse
	@PostMapping("/warehouse")
	public ProductData addWarehouseEntry(@RequestBody ProductData product) {
		return service.addProduct(product);
	}

	@GetMapping("/warehouse")
	public List<Map<String, Object>> getAllWarehouses() {
		return service.getAllWarehouses();
	}

	@GetMapping("/warehouse/{id}")
	public List<ProductData> getWarehouse(@PathVariable("id") String warehouseID) {
		return service.getWarehouseById(warehouseID);
	}

	@DeleteMapping("/warehouse/{id}")
	public ResponseEntity<String> deleteWarehouse(@PathVariable("id") String warehouseID) {
		service.deleteWarehouse(warehouseID);
		return ResponseEntity.ok("Deleted warehouse " + warehouseID);
	}

	// ----------------------------------------------------- reporting helpers
	@GetMapping("/report/category")
	public List<ProductData> byCategory(@RequestParam("name") String category) {
		return service.getProductsByCategory(category);
	}

	@GetMapping("/report/lowstock")
	public List<ProductData> lowStock(@RequestParam(value = "max", defaultValue = "100") double max) {
		return service.getLowStock(max);
	}

	/** Reset the repository to the generated test data set. */
	@PostMapping("/admin/reset")
	public ResponseEntity<String> reset() {
		long count = service.resetWithTestData();
		return ResponseEntity.ok("Stored " + count + " product entries");
	}
}
