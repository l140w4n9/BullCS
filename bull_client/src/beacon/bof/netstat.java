package beacon.bof;

import aggressor.AggressorClient;
import beacon.PostExInlineObject;
import common.CommonUtils;

public class netstat extends PostExInlineObject {
	public netstat(AggressorClient var1) {
		super(var1);
	}

	public byte[] getObjectFile(String var1) {
		return CommonUtils.readResource("bullbof/netstat." + var1 + ".o");
	}
}
