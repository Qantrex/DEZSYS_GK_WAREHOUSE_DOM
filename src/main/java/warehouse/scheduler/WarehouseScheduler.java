package warehouse.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import warehouse.model.ProductData;
import warehouse.repository.WarehouseRepository;

/**
 * Continuous storage of the warehouse data.
 *
 * Simulates that fresh stock data arrives from the warehouse locations at a
 * fixed interval: every stock entry is updated with a new quantity and a new
 * timestamp. This realises the "kontinuierliche Speicherung der Daten"
 * (scheduler) requirement of the task.
 *
 * Disabled by default so the generated demo data stays stable; enable with
 * {@code warehouse.scheduler.enabled=true} in application.properties.
 */
@Component
@ConditionalOnProperty(name = "warehouse.scheduler.enabled", havingValue = "true")
public class WarehouseScheduler {

	@Autowired
	private WarehouseRepository repository;

	private final Random random = new Random();

	@Scheduled(fixedRateString = "${warehouse.scheduler.rate:30000}")
	public void refreshStock() {
		List<ProductData> all = repository.findAll();
		if (all.isEmpty()) {
			return;
		}
		LocalDateTime now = LocalDateTime.now();
		for (ProductData p : all) {
			// simulate a stock change of +/- up to 50 units, never below 0
			double delta = random.nextInt(101) - 50;
			double updated = Math.max(0, p.getProductQuantity() + delta);
			p.setProductQuantity(updated);
			p.setTimestamp(now);
		}
		repository.saveAll(all);
		System.out.println("[Scheduler] refreshed " + all.size() + " stock entries at " + now);
	}
}
