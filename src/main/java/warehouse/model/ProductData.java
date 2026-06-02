package warehouse.model;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * A single product stock entry of one product at one warehouse location.
 *
 * The document is intentionally kept "flat" (warehouse meta data is duplicated
 * on every product entry). This denormalised structure is the typical NoSQL
 * approach: it allows the reporting queries in the protocol to filter and
 * aggregate over warehouses and products in a single collection without joins.
 */
@Document(collection = "productData")
public class ProductData {

	@Id
	private String ID;

	// --- warehouse meta data ---
	private String warehouseID;
	private String warehouseName;
	private String warehouseCity;

	// --- product data ---
	private String productID;
	private String productName;
	private String productCategory;
	private double productQuantity;

	// --- when this stock snapshot was stored in the central repository ---
	private LocalDateTime timestamp;

	/**
	 * Constructor
	 */
	public ProductData() {
	}

	public ProductData(String warehouseID, String warehouseName, String warehouseCity,
			String productID, String productName, String productCategory, double productQuantity) {
		this.warehouseID = warehouseID;
		this.warehouseName = warehouseName;
		this.warehouseCity = warehouseCity;
		this.productID = productID;
		this.productName = productName;
		this.productCategory = productCategory;
		this.productQuantity = productQuantity;
		this.timestamp = LocalDateTime.now();
	}

	public String getID() {
		return ID;
	}

	public void setID(String ID) {
		this.ID = ID;
	}

	public String getWarehouseID() {
		return warehouseID;
	}

	public void setWarehouseID(String warehouseID) {
		this.warehouseID = warehouseID;
	}

	public String getWarehouseName() {
		return warehouseName;
	}

	public void setWarehouseName(String warehouseName) {
		this.warehouseName = warehouseName;
	}

	public String getWarehouseCity() {
		return warehouseCity;
	}

	public void setWarehouseCity(String warehouseCity) {
		this.warehouseCity = warehouseCity;
	}

	public String getProductID() {
		return productID;
	}

	public void setProductID(String productID) {
		this.productID = productID;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductCategory() {
		return productCategory;
	}

	public void setProductCategory(String productCategory) {
		this.productCategory = productCategory;
	}

	public double getProductQuantity() {
		return productQuantity;
	}

	public void setProductQuantity(double productQuantity) {
		this.productQuantity = productQuantity;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Methods
	 */
	@Override
	public String toString() {
		return String.format(
			"Product Info: WarehouseID = %s (%s), ProductID = %s, ProductName = %s, ProductCategory = %s, ProductQuantity = %4.1f, Timestamp = %s",
			warehouseID, warehouseCity, productID, productName, productCategory, productQuantity, timestamp);
	}
}
