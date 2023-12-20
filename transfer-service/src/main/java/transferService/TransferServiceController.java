package transferService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import feign.FeignException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

@RestController
public class TransferServiceController {

	@Autowired
	private BankAccountProxy bankAccountProxy;
	@Autowired
	private UsersServiceProxy usersServiceProxy;
	@Autowired
	private CryptoWalletProxy cryptoWalletProxy;

	@GetMapping("/transfer-service-feign")
	@RateLimiter(name="default")
	public ResponseEntity<?> getTransferFeign(@RequestParam String currency, @RequestParam String to,
			@RequestParam double quantity, @RequestHeader("Authorization") String authorizationHeader) {
		try {
			String role = usersServiceProxy.extractRole(authorizationHeader);
			String email = usersServiceProxy.getEmailOfCurrentUser(authorizationHeader);
			double fee = quantity * 0.01;
			BigDecimal amount = new BigDecimal(quantity);
			BigDecimal fees = new BigDecimal(fee);
			BigDecimal transferAmount = amount.add(fees);

			List<String> fiat = Arrays.asList("EUR", "USD", "RSD", "RUB", "GBP", "CHF");
			List<String> crypto = Arrays.asList("BTC", "ETH", "XRP", "USDT");

			if ("USER".equals(role)) {
				if (fiat.contains(currency)) {
					if(bankAccountProxy.getBankAccountExists(to)) {
					boolean posible = bankAccountProxy.getConversionPosibility(email, transferAmount, currency);
					if (posible) {
						bankAccountProxy.updateCurrencyAmount(to, currency, amount);
						String responseMessage = "Prenos se izvrsava sa naloga sa email-om " + email + " u iznosu "
								+ quantity + " " + currency + " na nalog sa email-om" + to;
						return ResponseEntity.status(HttpStatus.OK).body(responseMessage);

					} else {
						return ResponseEntity.status(HttpStatus.CONFLICT)
								.body("Korisnik nema dovoljno sredstava za razmenu.");
					} }else {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body("Korisnik sa emailom:" + to + " nema racun .");
                    }
				} else if (crypto.contains(currency)) {
					if (cryptoWalletProxy.getCryptoWalletExists(to)) {
					boolean posible = cryptoWalletProxy.getConversionPosibility(email, transferAmount, currency);
					if (posible) {
						cryptoWalletProxy.updateCurrencyAmount(to, currency, amount);
						String responseMessage = "Prenos se izvrsava sa naloga sa email-om " + email + " u iznosu "
								+ quantity + " " + currency + " na nalog sa email-om" + to;
						return ResponseEntity.status(HttpStatus.OK).body(responseMessage);

					} else {
						return ResponseEntity.status(HttpStatus.CONFLICT)
								.body("Korisnik nema dovoljno sredstava za razmenu.");
					}} else {
						return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body("Korisnik sa emailom:" + to + " nema novcanik .");
					}

				} else {
					return ResponseEntity.status(HttpStatus.CONFLICT)
							.body("Nepostojeca valuta!");
				}

			} else {
				String errorMessage = "Korisnik koji nije User ne moze da izvrsi razmenu.";
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMessage);
			}

		} catch (FeignException e) {
			return ResponseEntity.status(e.status()).body(e.getMessage());
		}
	}
	
	@ExceptionHandler(RequestNotPermitted.class)
	public ResponseEntity<String> rateLimiterExceptionHandler(RequestNotPermitted ex){
		return ResponseEntity.status(503).body("Transfer service can only serve up to 2 requests every 45 seconds");
	}

}
