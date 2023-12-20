package currencyConversion;

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

import feign.FeignException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

@RestController
public class CurrencyConversionController {
	
	@Autowired
	private CurrencyExchangeProxy proxy;
	@Autowired
	private BankAccountProxy bankAccountProxy;
	@Autowired
	private UserServiceProxy userServiceProxy;
	

	//localhost:8100/currency-conversion/from/EUR/to/RSD/quantity/100
	@GetMapping("/currency-conversion/from/{from}/to/{to}/quantity/{quantity}")
	@RateLimiter(name="default")
	public CurrencyConversion getConversion
		(@PathVariable String from, @PathVariable String to, @PathVariable double quantity) {
		
		HashMap<String,String> uriVariables = new HashMap<String,String>();
		uriVariables.put("from", from);
		uriVariables.put("to", to);
		
		ResponseEntity<CurrencyConversion> response = 
				new RestTemplate().
				getForEntity("http://localhost:8000/currency-exchange/from/{from}/to/{to}",
						CurrencyConversion.class, uriVariables);
		
		CurrencyConversion cc = response.getBody();
		
		return new CurrencyConversion(from,to,cc.getConversionMultiple(), cc.getEnvironment(), quantity,
				cc.getConversionMultiple().multiply(BigDecimal.valueOf(quantity)));
	}
	
	//localhost:8100/currency-conversion?from=EUR&to=RSD&quantity=50
	/*@GetMapping("/currency-conversion")
	public ResponseEntity<?> getConversionParams(@RequestParam String from, @RequestParam String to, @RequestParam double quantity) {
		
		HashMap<String,String> uriVariable = new HashMap<String, String>();
		uriVariable.put("from", from);
		uriVariable.put("to", to);
		
		try {
		ResponseEntity<CurrencyConversion> response = new RestTemplate().
				getForEntity("http://localhost:8000/currency-exchange/from/{from}/to/{to}", CurrencyConversion.class, uriVariable);
		CurrencyConversion responseBody = response.getBody();
		return ResponseEntity.status(HttpStatus.OK).body(new CurrencyConversion(from,to,responseBody.getConversionMultiple(),responseBody.getEnvironment(),
				quantity, responseBody.getConversionMultiple().multiply(BigDecimal.valueOf(quantity))));
		}
		catch(HttpClientErrorException e) {
			return ResponseEntity.status(e.getStatusCode()).body(e.getMessage());
		}
	}*/
	
	
	@GetMapping("/currency-conversion-feign")
	@RateLimiter(name="default")
	public ResponseEntity<?> getConversionFeign(@RequestParam String from, @RequestParam String to, @RequestParam double quantity,@RequestHeader("Authorization") String authorizationHeader){
		try {
			String role = userServiceProxy.extractRole(authorizationHeader);
			String email=userServiceProxy.getEmailOfCurrentUser(authorizationHeader);
			BigDecimal bigDecimalValue = new BigDecimal(quantity);
			if ("USER".equals(role)) {
			boolean posible = bankAccountProxy.getConversionPosibility(email, bigDecimalValue, from);
			if(posible) {
				ResponseEntity<CurrencyConversion> response = proxy.getExchange(from, to);
				CurrencyConversion responseBody = response.getBody();
				BigDecimal result = responseBody.getConversionMultiple().multiply(BigDecimal.valueOf(quantity));
				bankAccountProxy.updateCurrencyAmount(email, to, result);
				BankAccountDto bankDto = bankAccountProxy.getBankAccountByEmail(email);
				HashMap<String, Object> responseMap = new HashMap<>();
				responseMap.put("bankDto", bankDto);
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
		return ResponseEntity.status(503).body("Currency conversion service can only serve up to 2 requests every 45 seconds");
	}
	
	
}
