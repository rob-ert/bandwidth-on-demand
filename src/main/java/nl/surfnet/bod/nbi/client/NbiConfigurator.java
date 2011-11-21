package nl.surfnet.bod.nbi.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NbiConfigurator {

	@SuppressWarnings("unused")
	private final Logger log = LoggerFactory.getLogger(getClass());

	// @Value("#{nbiProperties.nbiUsername}")
	private String username;

	// @Value("#{nbiProperties.nbiPassword}")
	private String password;

	// @Value("#{nbiProperties.nbiUrl}")
	private String url;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
