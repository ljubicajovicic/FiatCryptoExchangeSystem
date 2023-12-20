package bankAccount;

import org.springframework.data.jpa.repository.JpaRepository;

import bankAccount.model.BankAccount;

public interface CustomBankAccountRepository extends JpaRepository<BankAccount, Long> {
	
	boolean existsByEmail(String email);
	
	BankAccount getByEmail(String email);
	
	void deleteByEmail(String email);
	
}
