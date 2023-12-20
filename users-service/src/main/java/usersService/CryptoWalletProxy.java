package usersService;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name="crypto-wallet")
public interface CryptoWalletProxy {

	@DeleteMapping("/crypto-wallet/wallets/{email}")
	public ResponseEntity<String> deleteCryptoWallet(@PathVariable String email);

	@PutMapping("/crypto-wallet/wallets/email/{email}/new/{newEmail}")
	public ResponseEntity<String> updateEmailForCryptoWallet(@PathVariable String email, @PathVariable String newEmail );

	@GetMapping("/crypto-wallet/wallets/exists/{email}")
	public Boolean getCryptoWalletExists(@PathVariable String email);
}
