package cryptoWallet;

import org.springframework.data.jpa.repository.JpaRepository;
import cryptoWallet.model.CryptoWallet;

public interface CustomCryptoWalletRepository extends JpaRepository<CryptoWallet, Long> {
	
	boolean existsByEmail(String email);
	
	CryptoWallet getByEmail(String email);
	
	void deleteByEmail(String email);

}
