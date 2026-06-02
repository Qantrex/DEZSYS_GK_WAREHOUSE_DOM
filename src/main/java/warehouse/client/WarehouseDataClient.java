package warehouse.client;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import warehouse.model.ProductData;
import warehouse.service.DataGenerator;

/**
 * "Erweiterte Grundlagen": a small client application that generates the
 * warehouse/product data and stores it through the REST interface of this
 * middleware (POST /product). It runs in-process and talks to the own
 * embedded web server once it is ready.
 *
 * It is intentionally REST based (not direct repository access) to demonstrate
 * the full round trip data-generator -> REST -> MongoDB.
 */
@Component
public class WarehouseDataClient {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private DataGenerator generator;

	@Value("${server.port:8080}")
	private int port;

	@Value("${warehouse.client.seed-on-startup:true}")
	private boolean seedOnStartup;

	private String baseUrl() {
		return "http://localhost:" + port;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void seedViaRest() {
		if (!seedOnStartup) {
			System.out.println("[DataClient] seed-on-startup disabled, skipping.");
			return;
		}

		// 1) clear existing data via the REST interface (idempotent restarts)
		clearAllWarehouses();

		// 2) generate the data set and POST every entry to /product
		List<ProductData> data = generator.generateAll();
		int stored = 0;
		for (ProductData entry : data) {
			restTemplate.postForObject(baseUrl() + "/product", entry, ProductData.class);
			stored++;
		}

		System.out.println("[DataClient] stored " + stored + " product entries via REST for "
			+ DataGenerator.WAREHOUSES.size() + " warehouses and "
			+ DataGenerator.CATALOG.size() + " catalog products.");
	}

	@SuppressWarnings("unchecked")
	private void clearAllWarehouses() {
		List<Map<String, Object>> warehouses =
			restTemplate.getForObject(baseUrl() + "/warehouse", List.class);
		if (warehouses == null || warehouses.isEmpty()) {
			return;
		}
		for (Map<String, Object> wh : warehouses) {
			String id = String.valueOf(wh.get("warehouseID"));
			restTemplate.delete(baseUrl() + "/warehouse/" + id);
		}
		System.out.println("[DataClient] cleared " + warehouses.size() + " existing warehouses via REST.");
	}
}
