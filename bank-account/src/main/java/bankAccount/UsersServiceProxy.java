package bankAccount;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "users-service")
public interface UsersServiceProxy {

	@GetMapping("/users-service/user/{email}")
	public Boolean getUserByEmail(@PathVariable String email);
	

	@GetMapping("/users-service/user/role/{email}")
	public String getUsersRoleByEmail(@PathVariable String email);
	
	@GetMapping("/users-service/logged-user")
	public String extractRole(@RequestHeader("Authorization") String authorizationHeader);
	
}
