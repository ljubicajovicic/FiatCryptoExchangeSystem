package transferService;

import java.math.BigDecimal;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name="crypto-wallet")
public interface CryptoWalletProxy {
	@GetMapping("/crypto-wallet/wallets/update-amount/user/{email}/amount/{amount}/from/{currencyOne}")
	public Boolean getConversionPosibility(@PathVariable String email, @PathVariable BigDecimal amount,
	                                                       @PathVariable String currencyOne); 
	@PutMapping("/crypto-wallet/wallets/{email}/update/currency/{currencyTo}/for/{amount}")
    public ResponseEntity<String> updateCurrencyAmount(@PathVariable String email, @PathVariable String currencyTo,@PathVariable BigDecimal amount);
	
	@GetMapping("/crypto-wallet/wallets/{email}")
	public CryptoWalletDto getCryptoWalletByEmail(@PathVariable String email);
	
	@GetMapping("/crypto-wallet/wallets/exists/{email}")
	public Boolean getCryptoWalletExists(@PathVariable String email);
}
