package tradeService;

import org.springframework.data.jpa.repository.JpaRepository;

import tradeService.model.TradeService;

public interface CustomTradeServiceRepository extends JpaRepository<TradeService, Long>{
	
	TradeService findByFromAndToIgnoreCase(String from, String to);
	
}
