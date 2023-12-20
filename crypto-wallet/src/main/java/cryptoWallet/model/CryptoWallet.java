package cryptoWallet.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class CryptoWallet {

	@Id
	private long id;
	@Column
	private BigDecimal btc_amount;
	@Column
	private BigDecimal eth_amount;
	@Column
	private BigDecimal usdt_amount;
	@Column
	private BigDecimal xrp_amount;
	
	public CryptoWallet() {
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public BigDecimal getBtc_amount() {
		return btc_amount;
	}
	public void setBtc_amount(BigDecimal btc_amount) {
		this.btc_amount = btc_amount;
	}
	public BigDecimal getEth_amount() {
		return eth_amount;
	}
	public void setEth_amount(BigDecimal eth_amount) {
		this.eth_amount = eth_amount;
	}
	public BigDecimal getUsdt_amount() {
		return usdt_amount;
	}
	public void setUsdt_amount(BigDecimal usdt_amount) {
		this.usdt_amount = usdt_amount;
	}
	public BigDecimal getXrp_amount() {
		return xrp_amount;
	}
	public void setXrp_amount(BigDecimal xrp_amount) {
		this.xrp_amount = xrp_amount;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	@Column(nullable = false)
	private String email;
	
}
