package usersService;

import java.util.List;

import org.bouncycastle.util.encoders.Base64;
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

import jakarta.transaction.Transactional;
import usersService.model.CustomUser;

@RestController
public class UserController {

	@Autowired
	private BankAccountServiceProxy proxy;

	@Autowired
	private CryptoWalletProxy cryptoWalletProxy;

	@Autowired
	private CustomUserRepository repo;

	@GetMapping("/users-service/users")
	public List<CustomUser> getAllUsers() {
		return repo.findAll();
	}

	/*
	 * @PostMapping("/users-service/users") public ResponseEntity<CustomUser>
	 * createUser(@RequestBody CustomUser user) { CustomUser createdUser =
	 * repo.save(user); return ResponseEntity.status(201).body(createdUser); }
	 */

	@PostMapping("/users-service/users")
	public ResponseEntity<?> createUser(@RequestBody CustomUser user,
			@RequestHeader("Authorization") String authorizationHeader) {
		String role = extractRoleFromAuthorizationHeader(authorizationHeader);
		if ("ADMIN".equals(role)) {
			if (user.getRole().equals("USER")) {
				if (repo.existsById(user.getId())) {
					String errorMessage = "Korisnik sa ID-em " + user.getId() + " vec postoji.";
					return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
				} else {
					CustomUser createdUser = repo.save(user);
					return new ResponseEntity<CustomUser>(createdUser, HttpStatus.CREATED);
				}
			} else {
				String errorMessage = "Admin ne moze da kreira korisnike sa ulogom koja nije 'USER'.";
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMessage);
			}

		} else if ("OWNER".equals(role)) {
			if (user.getRole().equals("USER") || user.getRole().equals("ADMIN")) {
				if (repo.existsById(user.getId())) {
					String errorMessage = "Korisnik sa ID-em " + user.getId() + " vec postoji.";
					return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
				} else {
				CustomUser createdUser = repo.save(user);
				return new ResponseEntity<CustomUser>(createdUser, HttpStatus.CREATED);
				}
			} else {
				String errorMessage = "Owner ne moze da kreira korisnike sa ulogom koja nije 'USER' ili 'ADMIN'.";
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMessage);
			}
		} else {
			String errorMessage = "USER nema pravo da kreira korisnike.";
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMessage);
		}
	}

	@PutMapping("/users-service/users/{id}")
	public ResponseEntity<?> updateUser(@PathVariable long id, @RequestBody CustomUser user,
			@RequestHeader("Authorization") String authorizationHeader) {
		String role = extractRoleFromAuthorizationHeader(authorizationHeader);
		String email = extractEmailFromAuthorizationHeader(authorizationHeader);
		String oldEmail = repo.findById(id).getEmail();
		if ("ADMIN".equals(role)) {
			if (user.getRole().equals("USER")) {
				if (repo.existsById(user.getId())) {
					if (proxy.bankAccountExists(oldEmail)) {
						proxy.updateEmailForBankAccount(oldEmail, user.getEmail());
					}
					if (cryptoWalletProxy.getCryptoWalletExists(oldEmail)) {
						cryptoWalletProxy.updateEmailForCryptoWallet(oldEmail, user.getEmail());
					}
					repo.save(user);
					String errorMessage = "Korisnik sa ID-em " + user.getId() + " azuriran.";
					return ResponseEntity.status(HttpStatus.OK).body(errorMessage);
				} else {
					String errorMessage = "Korisnik sa ID-em " + user.getId() + " ne postoji.";
					return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
				}
			} else {
				String errorMessage = "Admin ne moze da azurira korisnike koji nemaju ulogu 'USER'";
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMessage);
			}

		} else if ("OWNER".equals(role)) {
			if (repo.existsById(user.getId())) {
				if (proxy.bankAccountExists(oldEmail)) {
					proxy.updateEmailForBankAccount(oldEmail, user.getEmail());
				}
				if (cryptoWalletProxy.getCryptoWalletExists(oldEmail)) {
					cryptoWalletProxy.updateEmailForCryptoWallet(oldEmail, user.getEmail());
				}
				repo.save(user);
				String errorMessage = "Korisnik sa ID-em " + user.getId() + " azuriran.";
				return ResponseEntity.status(HttpStatus.OK).body(errorMessage);
			} else {
				String errorMessage = "Korisnik sa ID-em " + user.getId() + " ne postoji.";
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
			}
		} else {
			if (oldEmail.equals(email)) {
				if (repo.existsById(user.getId())) {
					if (proxy.bankAccountExists(oldEmail)) {
						proxy.updateEmailForBankAccount(oldEmail, user.getEmail());
					}
					if (cryptoWalletProxy.getCryptoWalletExists(oldEmail)) {
						cryptoWalletProxy.updateEmailForCryptoWallet(oldEmail, user.getEmail());
					}
					repo.save(user);
					String errorMessage = "Korisnik sa ID-em " + user.getId() + " azuriran.";
					return ResponseEntity.status(HttpStatus.OK).body(errorMessage);
				} else {
					String errorMessage = "Korisnik sa ID-em " + user.getId() + " ne postoji.";
					return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
				}
			} else {
				String errorMessage = "User ne moze da azurira ni jedan nalog osim svog";
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMessage);
			}

		}
	}

	@DeleteMapping("/users-service/users/{id}")
	@Transactional
	public ResponseEntity<?> deleteUser(@PathVariable long id,
			@RequestHeader("Authorization") String authorizationHeader) {
		String role = extractRoleFromAuthorizationHeader(authorizationHeader);

		if ("OWNER".equals(role)) {
			if (repo.existsById(id)) {
				CustomUser user = repo.findById(id);
				if (user.getRole().equals("USER") && proxy.bankAccountExists(user.getEmail())) {
					proxy.deleteBankAccount(user.getEmail());
					if (cryptoWalletProxy.getCryptoWalletExists(user.getEmail())) {
						cryptoWalletProxy.deleteCryptoWallet(user.getEmail());
					}
				}
				repo.deleteById(id);
				String successMessage = "Korisnik sa ID-em  " + id + " obrisan.";
				return ResponseEntity.ok(successMessage);
			} else {
				String errorMessage = "Korisnik sa ID-em " + id + " ne postoji.";
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
			}
		} else {
			String errorMessage = "Samo korisnik sa ulogom 'OWNER' moze da vrsi brisanje.";
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMessage);
		}
	}

	@GetMapping("/users-service/user/{email}")
	public Boolean getUserByEmail(@PathVariable String email) {
		CustomUser user = repo.findByEmail(email);
		if (user == null) {
			return false;
		} else
			return true;
	}

	@GetMapping("/users-service/user/role/{email}")
	public String getUsersRoleByEmail(@PathVariable String email) {
		CustomUser user = repo.findByEmail(email);
		if (user == null) {
			return null;
		} else
			return user.getRole();
	}

	public String extractRoleFromAuthorizationHeader(String authorizationHeader) {
		String encodedCredentials = authorizationHeader.replaceFirst("Basic ", "");
		byte[] decodedBytes = Base64.decode(encodedCredentials.getBytes());
		String decodedCredentials = new String(decodedBytes);
		String[] credentials = decodedCredentials.split(":");
		String role = credentials[0]; 
		CustomUser user = repo.findByEmail(role);
		return user.getRole();
	}

	public String extractEmailFromAuthorizationHeader(String authorizationHeader) {
		String encodedCredentials = authorizationHeader.replaceFirst("Basic ", "");
		byte[] decodedBytes = Base64.decode(encodedCredentials.getBytes());
		String decodedCredentials = new String(decodedBytes);
		String[] credentials = decodedCredentials.split(":");
		String role = credentials[0]; 
		CustomUser user = repo.findByEmail(role);
		return user.getEmail();
	}

	@GetMapping("/users-service/logged-user")
	public String extractRole(@RequestHeader("Authorization") String authorizationHeader) {
		String role = extractRoleFromAuthorizationHeader(authorizationHeader);
		return role;
	}

	@GetMapping("/users-service/email-logged-user")
	public String extractEmail(@RequestHeader("Authorization") String authorizationHeader) {
		String email = extractEmailFromAuthorizationHeader(authorizationHeader);
		return email;
	}

	@GetMapping("/users-service/id-logged-user/{email}")
	public Long extractId(@PathVariable String email) {
		CustomUser user = repo.findByEmail(email);
		Long id = user.getId();
		return id;
	}
}
