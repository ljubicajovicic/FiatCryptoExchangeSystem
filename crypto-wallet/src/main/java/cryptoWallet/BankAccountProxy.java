package cryptoWallet;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="bank-account")
public interface BankAccountProxy {

	@GetMapping("/bank-account/accounts/exists/{email}")
	public Boolean getBankAccountExists(@PathVariable String email);
}
