package cryptoExchange;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import cryptoExchange.model.CryptoExchange;

@RestController
public class CryptoExchangeController {
	
	@Autowired
	private CustomCryptoExchangeRepository repo;
	
	@Autowired 
	private Environment environment;

	@GetMapping("/crypto-exchange/from/{from}/to/{to}")
	public ResponseEntity<?> getExchange(@PathVariable String from, @PathVariable String to) {
		//return new CurrencyExchange(10000, from, to, BigDecimal.valueOf(117),"");
		String port = environment.getProperty("local.server.port");
		CryptoExchange kurs = repo.findByFromAndToIgnoreCase(from, to);
		
		if(kurs!=null) {
			kurs.setEnvironment(port);
			return ResponseEntity.ok(kurs);
		}else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Requested currency exchange could not be found!");
		}
		
	}
	
	//localhost:8400/crypto-exchange/from/ETH/to/BTC
}
