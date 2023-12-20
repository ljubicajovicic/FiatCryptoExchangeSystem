package apiGateway.authentication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.client.RestTemplate;

import authentication.dtos.CustomUserDto;

@Configuration
@EnableWebFluxSecurity
public class ApiGatewayAuthentication {

	
	/*@Bean
	public MapReactiveUserDetailsService userDetailsService(BCryptPasswordEncoder encoder) {
		List<UserDetails> users = new ArrayList<>();
		users.add(User.withUsername("user")
				.password(encoder.encode("password1"))
				.roles("USER")
				.build());
		
		users.add(User.withUsername("admin")
				.password(encoder.encode("password2"))
				.roles("ADMIN")
				.build());
		
		return new MapReactiveUserDetailsService(users);
	}*/
	
	@Bean
	public MapReactiveUserDetailsService userDetailsService(BCryptPasswordEncoder encoder) {
		List<UserDetails> users = new ArrayList<>();
		List<CustomUserDto> usersFromDatabase;
		
		ResponseEntity<CustomUserDto[]> response = 
		new RestTemplate().getForEntity("http://localhost:8770/users-service/users", CustomUserDto[].class);
		
		usersFromDatabase = Arrays.asList(response.getBody());
		
		for(CustomUserDto cud: usersFromDatabase) {
			users.add(User.withUsername(cud.getEmail())
					.password(encoder.encode(cud.getPassword()))
					.roles(cud.getRole())
					.build());
		}
		
		
		return new MapReactiveUserDetailsService(users);
	}
	
	@Bean
	public BCryptPasswordEncoder getEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityWebFilterChain filterChain(ServerHttpSecurity http) throws Exception{
		http.csrf().disable()
		.authorizeExchange()
		.pathMatchers(HttpMethod.DELETE).hasRole("OWNER")
		.pathMatchers("/currency-exchange/**").hasAnyRole("ADMIN","OWNER","USER")
		.pathMatchers(HttpMethod.GET,"/users-service/**").hasAnyRole("ADMIN","OWNER","USER")
		.pathMatchers(HttpMethod.PUT,"/users-service/**").hasAnyRole("ADMIN","OWNER","USER")
		.pathMatchers(HttpMethod.POST,"/users-service/**").hasAnyRole("ADMIN","OWNER")
		.pathMatchers("/currency-conversion-feign/**").hasRole("USER")
		.pathMatchers(HttpMethod.GET,"/bank-account/**").hasAnyRole("ADMIN","OWNER","USER")
		.pathMatchers(HttpMethod.PUT,"/bank-account/**").hasAnyRole("ADMIN","OWNER","USER")
		.pathMatchers(HttpMethod.POST,"/bank-account/**").hasRole("ADMIN")
		.pathMatchers(HttpMethod.GET,"/crypto-wallet/**").hasAnyRole("ADMIN","OWNER","USER")
		.pathMatchers(HttpMethod.PUT,"/crypto-wallet/**").hasAnyRole("ADMIN","OWNER","USER")
		.pathMatchers(HttpMethod.POST,"/crypto-wallet/**").hasRole("ADMIN")
		.pathMatchers("/crypto-conversion-feign/**").hasRole("USER")
		.pathMatchers("/crypto-exchange/**").hasAnyRole("ADMIN","OWNER","USER")
		.pathMatchers("/trade-service-feign/**").hasRole("USER")
		.pathMatchers("/transfer-service-feign/**").hasRole("USER")
		.and()
		.httpBasic();
		
		return http.build();
	}
}
