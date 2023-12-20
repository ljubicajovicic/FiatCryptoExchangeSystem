package cryptoExchange;

import org.springframework.data.jpa.repository.JpaRepository;

import cryptoExchange.model.CryptoExchange;

public interface CustomCryptoExchangeRepository extends JpaRepository<CryptoExchange, Long>{

	CryptoExchange findByFromAndToIgnoreCase(String from, String to);
}
