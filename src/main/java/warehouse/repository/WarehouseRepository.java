package warehouse.repository;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

import warehouse.model.ProductData;

public interface WarehouseRepository extends MongoRepository<ProductData, String> {

	// All stock entries of one product across every warehouse location
	List<ProductData> findByProductID(String productID);

	// All products of a specific product name across every warehouse location
	List<ProductData> findByProductName(String productName);

	// All products stored at a specific warehouse location
	List<ProductData> findByWarehouseID(String warehouseID);

	// All products of a specific category
	List<ProductData> findByProductCategory(String productCategory);

	// One specific product at one specific warehouse location
	List<ProductData> findByWarehouseIDAndProductID(String warehouseID, String productID);

	// Products whose stock is below (or equal to) a threshold
	List<ProductData> findByProductQuantityLessThanEqual(double threshold);
}
