package currencyConversion;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name="users-service")
public interface UserServiceProxy {

	@GetMapping("/users-service/logged-user")
	String extractRole(@RequestHeader("Authorization") String authorizationHeaders);
	
	@GetMapping("/users-service/email-logged-user")
	String getEmailOfCurrentUser(@RequestHeader("Authorization") String authorizationHeaders);
	
	@GetMapping("/users-service/id-logged-user/{email}")
	public Long extractId(@PathVariable String email);
}
