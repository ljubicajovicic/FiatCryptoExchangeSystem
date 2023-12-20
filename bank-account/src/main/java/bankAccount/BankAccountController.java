package bankAccount;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import bankAccount.model.BankAccount;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;

@RestController
public class BankAccountController {
	
	@Autowired
	private CustomBankAccountRepository repo;
	
	@Autowired
	private UsersServiceProxy proxy;
	
	@GetMapping("/bank-account/accounts")
	public List<BankAccount> getAllBankAccounts(){
		return repo.findAll();
	}
	
	@GetMapping("/bank-account/accounts/{email}")
	public BankAccount getBankAccountByEmail(@PathVariable String email) {
		BankAccount bankAccount = repo.getByEmail(email);
	    return bankAccount;
	}
	
	@GetMapping("/bank-account/accounts/exists/{email}")
	public Boolean getBankAccountExists(@PathVariable String email) {
		BankAccount bankAccount = repo.getByEmail(email);
	    if (bankAccount == null) {
	    	return false;
	    }
	    else
	    return true;
	}
	

	@PostMapping("/bank-account/accounts")
	@Retry(name = "default", fallbackMethod = "fallbackCreateAccount")
	public ResponseEntity<?> createBankAccount (@RequestBody BankAccount bankAccount,
			@RequestHeader("Authorization") String authorizationHeader){
		String role = proxy.extractRole(authorizationHeader);
		if("ADMIN".equals(role)) {
			if(repo.existsByEmail(bankAccount.getEmail())) {
				String errorMessage = "Racun sa email-om: " + bankAccount.getEmail() + " vec postoji.";
				return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
			}else {
				if(!proxy.getUserByEmail(bankAccount.getEmail())) {
					throw new RuntimeException("Korisnik nema email, retry test");
				}
				else if(proxy.getUsersRoleByEmail(bankAccount.getEmail()).equals("USER")) {
					BankAccount createdBankAccount = repo.save(bankAccount);
					return new ResponseEntity<BankAccount>(createdBankAccount, HttpStatus.CREATED);
				} else {
					String errorMessage = "Korisniku sa email-om: " + bankAccount.getEmail() + " se ne moze kreirati racun jer nema ulogu USER.";
					return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
				}
			}
		} else {
			String errorMessage = "Korisnik koji nije admin ne moze kreirati racun.";
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMessage);
		}
	}
	
	@PutMapping("/bank-account/accounts/{email}")
	public ResponseEntity<?> updateBankAccount (@PathVariable String email, @RequestBody BankAccount bankAccount,
			@RequestHeader("Authorization") String authorizationHeader){
		String role = proxy.extractRole(authorizationHeader);
		if("ADMIN".equals(role)) {
			BankAccount existingAccount = repo.getByEmail(email);
			if(repo.existsByEmail(email)) {
				String newEmail = bankAccount.getEmail();
				if(!newEmail.equals(existingAccount.getEmail())) {
				String errorMessage = "Nije moguce izvrsiti modifikaciju e-mail-a";
	            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
				} else {
				repo.save(bankAccount);
				String errorMessage = "Racun sa email-om " + bankAccount.getEmail() + " je modifikovan.";
				return ResponseEntity.status(HttpStatus.OK).body(errorMessage);
				}
			} else {
				String errorMessage = "Racun sa email-om " + bankAccount.getEmail() + " ne postoji.";
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
			}
		} else {
			String errorMessage = "Korisnik koji nije admin ne moze modifikuje racun.";
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMessage);
		}
	}
	
	@PutMapping("/bank-account/accounts/email/{email}/new/{newEmail}")
	public ResponseEntity<String> updateEmailForBankAccount(@PathVariable String email, @PathVariable String newEmail ){
		BankAccount bankAccount = repo.getByEmail(email);
		if(bankAccount != null) {
			bankAccount.setEmail(newEmail);
			repo.save(bankAccount);
			String errorMessage = "Stari email: " + email + " , novi email: " + newEmail;
			return ResponseEntity.status(HttpStatus.OK).body(errorMessage);
		} else {
			String errorMessage = "Racun sa email-om " + email + " ne postoji.";
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
		}
	}
	
	@GetMapping("/bank-account/accounts/update-amount/user/{email}/amount/{amount}/from/{currencyOne}")
	public Boolean getposibilityOfConversion(@PathVariable String email, @PathVariable BigDecimal amount,
	                                                       @PathVariable String currencyOne) {
	    BankAccount bankAccount = repo.getByEmail(email);

	    BigDecimal current = BigDecimal.ZERO;
	    BigDecimal newAmount = BigDecimal.ZERO;

	    switch (currencyOne) {
	        case "EUR":
	            current = bankAccount.getEur_amount();
	            newAmount = current.subtract(amount);
	            bankAccount.setEur_amount(newAmount);
	            break;
	        case "RSD":
	            current = bankAccount.getRsd_amount();
	            newAmount = current.subtract(amount);
	            bankAccount.setRsd_amount(newAmount);
	            break;
	        case "USD":
	            current = bankAccount.getUsd_amount();
	            newAmount = current.subtract(amount);
	            bankAccount.setUsd_amount(newAmount);
	            break;
	        case "CHF":
	            current = bankAccount.getChf_amount();
	            newAmount = current.subtract(amount);
	            bankAccount.setChf_amount(newAmount);
	            break;
	        case "GBP":
	            current = bankAccount.getGbp_amount();
	            newAmount = current.subtract(amount);
	            bankAccount.setGbp_amount(newAmount);
	        case "RUB":
	        	current = bankAccount.getRub_amount();
	            newAmount = current.subtract(amount);
	            bankAccount.setRub_amount(newAmount);
	            break;
	    }
        if (current == null || amount.compareTo(current) > 0) //current amount SMALER than amount
        {
        	return false;
        }else {
        	repo.save(bankAccount);
        	return true;
        	
        }
	}
	
	@PutMapping("/bank-account/accounts/{email}/update/currency/{currencyTo}/for/{amount}")
    public  ResponseEntity<String> updateAmount(@PathVariable String email, @PathVariable String currencyTo,@PathVariable BigDecimal amount) {
    	BankAccount bankAccount = repo.getByEmail(email);
		
    	if (bankAccount!=null) {
        BigDecimal current = BigDecimal.ZERO;
		BigDecimal newAmount = BigDecimal.ZERO;

	    switch (currencyTo) {
	        case "EUR":
	            current = bankAccount.getEur_amount() != null ? bankAccount.getEur_amount() : BigDecimal.ZERO;
				newAmount=current.add(amount);
				bankAccount.setEur_amount(newAmount);
	            break;
	        case "RSD":
	            current = bankAccount.getRsd_amount() != null ? bankAccount.getRsd_amount() : BigDecimal.ZERO;
				newAmount=current.add(amount);
				bankAccount.setRsd_amount(newAmount);
	            break;
	        case "USD":
	            current = bankAccount.getUsd_amount() != null ? bankAccount.getUsd_amount() : BigDecimal.ZERO;;
				newAmount=current.add(amount);
				bankAccount.setUsd_amount(newAmount);
	            break;
	        case "CHF":
	            current = bankAccount.getChf_amount() != null ? bankAccount.getEur_amount() : BigDecimal.ZERO;;
				newAmount=current.add(amount);
				bankAccount.setChf_amount(newAmount);
	            break;
	        case "GBP":
	            current = bankAccount.getGbp_amount() != null ? bankAccount.getGbp_amount() : BigDecimal.ZERO;;
				newAmount=current.add(amount);
				bankAccount.setGbp_amount(newAmount);
	            break;
	        case "RUB":
	            current = bankAccount.getRub_amount() != null ? bankAccount.getRub_amount() : BigDecimal.ZERO;;
				newAmount=current.add(amount);
				bankAccount.setRub_amount(newAmount);
	            break; 
	    }
           repo.save(bankAccount);
           	String errorMessage = "Racun sa email-om " + bankAccount.getEmail() + " azuriran.";
			return ResponseEntity.status(HttpStatus.OK).body(errorMessage);
        } else {
        	String errorMessage = "Racun sa email-om " + email+" ne postoji.";
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
        }
    }
	
	@DeleteMapping("/bank-account/accounts/{email}")
	@Transactional
	public ResponseEntity<String> deleteBankAccount(@PathVariable String email) {
			if (repo.existsByEmail(email)) {
					repo.deleteByEmail(email);
					String successMessage = "Korisnik sa email-om  " + email + " obrisan.";
					return ResponseEntity.ok(successMessage);
			} else {
				String errorMessage = "Korisnik sa email-om " + email + " ne postoji.";
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
			}
		
		}
	
	public ResponseEntity<?> fallbackCreateAccount(Exception ex) {
	    String errorMessage = "Došlo je do greške prilikom kreiranja racuna.";
	    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
	}
}
