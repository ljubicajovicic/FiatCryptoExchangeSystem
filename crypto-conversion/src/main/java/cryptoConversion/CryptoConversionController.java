package cryptoConversion;

import java.math.BigDecimal;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import cryptoConversion.model.CryptoConversion;
import feign.FeignException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

@RestController
public class CryptoConversionController {

	@Autowired
	private CryptoExchangeProxy cryptoExchangeProxy;
	@Autowired
	private CryptoWalletProxy cryptoWalletProxy;
	@Autowired
	private UserServiceProxy userServiceProxy;
	

	//localhost:8100/currency-conversion/from/EUR/to/RSD/quantity/100
	@GetMapping("/crypto-conversion/from/{from}/to/{to}/quantity/{quantity}")
	@RateLimiter(name="default")
	public CryptoConversion getConversion
		(@PathVariable String from, @PathVariable String to, @PathVariable double quantity) {
		
		HashMap<String,String> uriVariables = new HashMap<String,String>();
		uriVariables.put("from", from);
		uriVariables.put("to", to);
		
		ResponseEntity<CryptoConversion> response = 
				new RestTemplate().
				getForEntity("http://localhost:8400/crypto-exchange/from/{from}/to/{to}",
						CryptoConversion.class, uriVariables);
		
		CryptoConversion cc = response.getBody();
		
		return new CryptoConversion(from,to,cc.getConversionMultiple(), cc.getEnvironment(), quantity,
				cc.getConversionMultiple().multiply(BigDecimal.valueOf(quantity)));
	}
	
	@GetMapping("/crypto-conversion-feign")
	@RateLimiter(name="default")
	public ResponseEntity<?> getConversionFeign(@RequestParam String from, @RequestParam String to, @RequestParam double quantity,@RequestHeader("Authorization") String authorizationHeader){
		try {
			String role = userServiceProxy.extractRole(authorizationHeader);
			String email=userServiceProxy.getEmailOfCurrentUser(authorizationHeader);
			BigDecimal bigDecimalValue = new BigDecimal(quantity);
			if ("USER".equals(role)) {
			boolean posible = cryptoWalletProxy.getConversionPosibility(email, bigDecimalValue, from);
			if(posible) {
				ResponseEntity<CryptoConversion> response = cryptoExchangeProxy.getExchange(from, to);
				CryptoConversion responseBody = response.getBody();
				BigDecimal result = responseBody.getConversionMultiple().multiply(BigDecimal.valueOf(quantity));
				cryptoWalletProxy.updateCurrencyAmount(email, to, result);
				CryptoWalletDto walletDto = cryptoWalletProxy.getCryptoWalletByEmail(email);
				HashMap<String, Object> responseMap = new HashMap<>();
				responseMap.put("walletDto", walletDto);
				responseMap.put("message", "Uspešno je izvršena razmena " + quantity+ " "+from + " za " + to);

				return new ResponseEntity<>(responseMap, HttpStatus.OK);
				
				/*return ResponseEntity.ok(new CurrencyConversion(from,to,responseBody.getConversionMultiple(),responseBody.getEnvironment()+" feign",
					quantity, responseBody.getConversionMultiple().multiply(BigDecimal.valueOf(quantity))));*/
				
			}else {
				return ResponseEntity.status(HttpStatus.CONFLICT).body("Korisnik nema dovoljno sredstava za razmenu.");
			}
			
		}else {
            String errorMessage = "Korisnik koji nije User ne moze da izvrsi razmenu.";
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMessage);
        }

		} catch(FeignException e) {
			return ResponseEntity.status(e.status()).body(e.getMessage());
		}
	}
	
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<String> handleMissingParams(MissingServletRequestParameterException ex) {
	    String parameter = ex.getParameterName();
	    //return ResponseEntity.status(ex.getStatusCode()).body(ex.getMessage());
	    return ResponseEntity.status(ex.getStatusCode()).body("Value [" + ex.getParameterType() + "] of parameter [" + parameter + "] has been ommited");
	}
	
	@ExceptionHandler(RequestNotPermitted.class)
	public ResponseEntity<String> rateLimiterExceptionHandler(RequestNotPermitted ex){
		return ResponseEntity.status(503).body("Crypto conversion service can only serve up to 2 requests every 45 seconds");
	}
	
}
