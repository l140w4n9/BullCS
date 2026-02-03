package beacon.bof;

import aggressor.AggressorClient;
import beacon.PostExInlineObject;
import common.CommonUtils;

public class env extends PostExInlineObject {
	public env(AggressorClient var1) {
		super(var1);
	}

	public byte[] getObjectFile(String var1) {
		return CommonUtils.readResource("bullbof/env." + var1 + ".o");
	}
}
