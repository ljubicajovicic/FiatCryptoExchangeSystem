package cryptoWallet;

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

import cryptoWallet.model.CryptoWallet;
import jakarta.transaction.Transactional;

@RestController
public class CryptoWalletController {
	
	@Autowired
	private CustomCryptoWalletRepository repo;
	
	@Autowired
	private UsersServiceProxy userServiceProxy;
	
	@Autowired
	private BankAccountProxy bankAccountProxy;
	
	@GetMapping("/crypto-wallet/wallets")
	public List<CryptoWallet> getAllCryptoWallets(){
		return repo.findAll();
	}
	
	@GetMapping("/crypto-wallet/wallets/{email}")
	public CryptoWallet getCryptoWalletByEmail(@PathVariable String email) {
		CryptoWallet CryptoWallet = repo.getByEmail(email);
	    return CryptoWallet;
	}
	
	@GetMapping("/crypto-wallet/wallets/exists/{email}")
	public Boolean getCryptoWalletExists(@PathVariable String email) {
		CryptoWallet CryptoWallet = repo.getByEmail(email);
	    if (CryptoWallet == null) {
	    	return false;
	    }
	    else
	    return true;
	}
	
	
	@PostMapping("/crypto-wallet/wallets")
	public ResponseEntity<?> createCryptoWallet (@RequestBody CryptoWallet cryptoWallet,
			@RequestHeader("Authorization") String authorizationHeader){
		String role = userServiceProxy.extractRole(authorizationHeader);
		if("ADMIN".equals(role)) {
			if(repo.existsByEmail(cryptoWallet.getEmail())) {
				String errorMessage = "Novcanik sa email-om: " + cryptoWallet.getEmail() + " vec postoji.";
				return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
			}else {
				if(userServiceProxy.getUsersRoleByEmail(cryptoWallet.getEmail()).equals("USER")) {
					if(bankAccountProxy.getBankAccountExists(cryptoWallet.getEmail())) {
					CryptoWallet createdCryptoWallet = repo.save(cryptoWallet);
					return new ResponseEntity<CryptoWallet>(createdCryptoWallet, HttpStatus.CREATED);
					} else {
						String errorMessage = "Korisniku sa email-om: " + cryptoWallet.getEmail() + " se ne moze kreirati novcanik jer nema racun.";
						return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
					}
					
				} else {
					String errorMessage = "Korisniku sa email-om: " + cryptoWallet.getEmail() + " se ne moze kreirati novcanik jer nema ulogu USER.";
					return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
				}
			}
		} else {
			String errorMessage = "Korisnik koji nije admin ne moze kreirati novcanik.";
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMessage);
		}
	}
	
	@PutMapping("/crypto-wallet/wallets/{email}")
	public ResponseEntity<?> updateCryptoWallet (@PathVariable String email, @RequestBody CryptoWallet cryptoWallet,
			@RequestHeader("Authorization") String authorizationHeader){
		String role = userServiceProxy.extractRole(authorizationHeader);
		if("ADMIN".equals(role)) {
			CryptoWallet existingAccount = repo.getByEmail(email);
			if(repo.existsByEmail(email)) {
				String newEmail = cryptoWallet.getEmail();
				if(!newEmail.equals(existingAccount.getEmail())) {
				String errorMessage = "Nije moguce izvrsiti modifikaciju e-mail-a";
	            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
				} else {
				repo.save(cryptoWallet);
				String errorMessage = "Novcanik sa email-om " + cryptoWallet.getEmail() + " je modifikovan.";
				return ResponseEntity.status(HttpStatus.OK).body(errorMessage);
				}
			} else {
				String errorMessage = "Novcanik sa email-om " + cryptoWallet.getEmail() + " ne postoji.";
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
			}
		} else {
			String errorMessage = "Korisnik koji nije admin ne moze modifikuje novcanik.";
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMessage);
		}
	}
	
	@PutMapping("/crypto-wallet/wallets/email/{email}/new/{newEmail}")
	public ResponseEntity<String> updateEmailForCryptoWallet(@PathVariable String email, @PathVariable String newEmail ){
		CryptoWallet CryptoWallet = repo.getByEmail(email);
		if(CryptoWallet != null) {
			CryptoWallet.setEmail(newEmail);
			repo.save(CryptoWallet);
			String errorMessage = "Stari email: " + email + " , novi email: " + newEmail;
			return ResponseEntity.status(HttpStatus.OK).body(errorMessage);
		} else {
			String errorMessage = "Novcanik sa email-om " + email + " ne postoji.";
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
		}
	}
	
	@GetMapping("/crypto-wallet/wallets/update-amount/user/{email}/amount/{amount}/from/{currencyOne}")
	public Boolean getposibilityOfConversion(@PathVariable String email, @PathVariable BigDecimal amount,
	                                                       @PathVariable String currencyOne) {
	    CryptoWallet CryptoWallet = repo.getByEmail(email);

	    BigDecimal current = BigDecimal.ZERO;
	    BigDecimal newAmount = BigDecimal.ZERO;

	    switch (currencyOne) {
	        case "BTC":
	            current = CryptoWallet.getBtc_amount();
	            newAmount = current.subtract(amount);
	            CryptoWallet.setBtc_amount(newAmount);
	            break;
	        case "ETH":
	            current = CryptoWallet.getEth_amount();
	            newAmount = current.subtract(amount);
	            CryptoWallet.setEth_amount(newAmount);
	            break;
	        case "USDT":
	            current = CryptoWallet.getUsdt_amount();
	            newAmount = current.subtract(amount);
	            CryptoWallet.setUsdt_amount(newAmount);
	            break;
	        case "XRP":
	            current = CryptoWallet.getXrp_amount();
	            newAmount = current.subtract(amount);
	            CryptoWallet.setXrp_amount(newAmount);
	            break;
	    }
        if (current == null || amount.compareTo(current) > 0) //current amount SMALER than amount
        {
        	return false;
        }else {
        	repo.save(CryptoWallet);
        	return true;
        	
        }
	}
	
	@PutMapping("/crypto-wallet/wallets/{email}/update/currency/{currencyTo}/for/{amount}")
    public  ResponseEntity<String> updateAmount(@PathVariable String email, @PathVariable String currencyTo,@PathVariable BigDecimal amount) {
    	CryptoWallet cryptoWallet = repo.getByEmail(email);
		
    	if (cryptoWallet!=null) {
        BigDecimal current = BigDecimal.ZERO;
		BigDecimal newAmount = BigDecimal.ZERO;

	    switch (currencyTo) {
	        case "BTC":
	            current = cryptoWallet.getBtc_amount() != null ? cryptoWallet.getBtc_amount() : BigDecimal.ZERO;
				newAmount=current.add(amount);
				cryptoWallet.setBtc_amount(newAmount);
	            break;
	        case "ETH":
	            current = cryptoWallet.getEth_amount() != null ? cryptoWallet.getEth_amount() : BigDecimal.ZERO;
				newAmount=current.add(amount);
				cryptoWallet.setEth_amount(newAmount);
	            break;
	        case "USDT":
	            current = cryptoWallet.getUsdt_amount() != null ? cryptoWallet.getUsdt_amount() : BigDecimal.ZERO;;
				newAmount=current.add(amount);
				cryptoWallet.setUsdt_amount(newAmount);
	            break;
	        case "XRP":
	            current = cryptoWallet.getXrp_amount() != null ? cryptoWallet.getXrp_amount() : BigDecimal.ZERO;;
				newAmount=current.add(amount);
				cryptoWallet.setXrp_amount(newAmount);
	            break;
	    }
           repo.save(cryptoWallet);
           	String errorMessage = "Novcanik sa email-om " + cryptoWallet.getEmail() + " azuriran.";
			return ResponseEntity.status(HttpStatus.OK).body(errorMessage);
        } else {
        	String errorMessage = "Novcanik sa email-om " + email+" ne postoji.";
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
        }
    }
	
	@DeleteMapping("/crypto-wallet/wallets/{email}")
	@Transactional
	public ResponseEntity<String> deleteCryptoWallet(@PathVariable String email) {
			if (repo.existsByEmail(email)) {
					repo.deleteByEmail(email);
					String successMessage = "Korisnik sa email-om  " + email + " obrisan.";
					return ResponseEntity.ok(successMessage);
			} else {
				String errorMessage = "Korisnik sa email-om " + email + " ne postoji.";
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
			}
		
		}

}
