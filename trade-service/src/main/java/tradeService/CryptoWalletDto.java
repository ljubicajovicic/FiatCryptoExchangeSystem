package tradeService;

import java.math.BigDecimal;

public class CryptoWalletDto {

	private long id;
	
	private BigDecimal btc_amount;
	
	private BigDecimal eth_amount;
	
	private BigDecimal usdt_amount;
	
	private BigDecimal xrp_amount;

	public CryptoWalletDto() {
		
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
	
	
	
}
