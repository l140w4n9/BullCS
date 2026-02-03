package beacon.bof;

import aggressor.AggressorClient;
import beacon.PostExInlineObject;
import common.CommonUtils;

public class screenshotp extends PostExInlineObject {
	public screenshotp(AggressorClient var1) {
		super(var1);
	}

	public byte[] getObjectFile(String var1) {
		return CommonUtils.readResource("bullbof/screenshotp." + var1 + ".o");
	}
}
