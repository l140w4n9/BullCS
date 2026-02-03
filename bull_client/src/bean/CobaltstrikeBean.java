//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package bean;

import annon.Alias;
import annon.Config;
import annon.Require;
import java.util.Properties;
import util.Utils;

@Config(
		name = "BullClient"
)
public class CobaltstrikeBean {
	@Require
	@Alias(
			name = "OpenOnlineMusic"
	)
	private Boolean OpenOnlineMusic;
	@Require
	@Alias(
			name = "OnlineMusic"
	)
	private String OnlineMusic;
	@Require
	@Alias(
			name = "OpenAuth"
	)
	private Boolean OpenAuth;
	@Require
	@Alias(
			name = "Version"
	)
	private String Version;
	@Require
	@Alias(
			name = "InjectSelf"
	)
	private Boolean InjectSelf;
	@Require
	@Alias(
			name = "ini_name"
	)
	private String ini_name;
	@Require
	@Alias(
			name = "theme"
	)
	private Integer theme;
	private static CobaltstrikeBean cobaltstrike = null;

	public CobaltstrikeBean() {
	}

	public Boolean getOpenOnlineMusic() {
		return this.OpenOnlineMusic;
	}

	public void setOpenOnlineMusic(Boolean openOnlineMusic) {
		this.OpenOnlineMusic = openOnlineMusic;
	}

	public String getOnlineMusic() {
		return this.OnlineMusic;
	}

	public void setOnlineMusic(String onlineMusic) {
		this.OnlineMusic = onlineMusic;
	}

	public String getVersion() {
		return this.Version;
	}

	public void setVersion(String version) {
		this.Version = version;
	}

	public Integer getTheme() {
		return this.theme;
	}

	public void setTheme(Integer theme) {
		this.theme = theme;
	}

	public String getIni_name() {
		return this.ini_name;
	}

	public void setIni_name(String ini_name) {
		this.ini_name = ini_name;
	}

	public Boolean getInjectSelf() {
		return this.InjectSelf;
	}

	public void setInjectSelf(Boolean injectSelf) {
		this.InjectSelf = injectSelf;
	}

	public void setOpenAuth(Boolean openAuth) {
		this.OpenAuth = openAuth;
	}

	public Boolean getOpenAuth() {
		return this.OpenAuth;
	}

	public static CobaltstrikeBean getInstance() {
		if (cobaltstrike == null) {
			throw new Error("Please Init Cobaltstrike");
		} else {
			return cobaltstrike;
		}
	}

	public static void init(Properties properties) {
		cobaltstrike = (CobaltstrikeBean)Utils.objectFromProperties(properties, CobaltstrikeBean.class);
		CobaltstrikeBean instance = getInstance();
	}

	public String toString() {
		return "Ok!";
	}
}
