package currencyConversion;



import java.math.BigDecimal;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name="bank-account")
public interface BankAccountProxy {
	@GetMapping("/bank-account/accounts/update-amount/user/{email}/amount/{amount}/from/{currencyOne}")
	public Boolean getConversionPosibility(@PathVariable String email, @PathVariable BigDecimal amount,
	                                                       @PathVariable String currencyOne); 
	@PutMapping("/bank-account/accounts/{email}/update/currency/{currencyTo}/for/{amount}")
    public ResponseEntity<String> updateCurrencyAmount(@PathVariable String email, @PathVariable String currencyTo,@PathVariable BigDecimal amount);
	
	@GetMapping("/bank-account/accounts/{email}")
	public BankAccountDto getBankAccountByEmail(@PathVariable String email);
}
