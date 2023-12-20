package tradeService;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name="currency-conversion")
public interface CurrencyConversionProxy {
	@GetMapping("/currency-conversion-feign")
	public ResponseEntity<?> getConversionFeign(@RequestParam String from, @RequestParam String to,
			@RequestParam double quantity,@RequestHeader("Authorization") String authorizationHeader);


}
