package usersService;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name= "bank-account")
public interface BankAccountServiceProxy {

	@DeleteMapping("/bank-account/accounts/{email}")
	public ResponseEntity<String> deleteBankAccount(@PathVariable String email);
	
	@PutMapping("/bank-account/accounts/email/{email}/new/{newEmail}")
	public ResponseEntity<String> updateEmailForBankAccount(@PathVariable String email, @PathVariable String newEmail );
		
	@GetMapping("/bank-account/accounts/exists/{email}")
	public Boolean bankAccountExists(@PathVariable String email);
}
