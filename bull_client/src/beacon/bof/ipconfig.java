package beacon.bof;

import aggressor.AggressorClient;
import beacon.PostExInlineObject;
import common.CommonUtils;

public class ipconfig extends PostExInlineObject {
	public ipconfig(AggressorClient var1) {
		super(var1);
	}

	public byte[] getObjectFile(String var1) {
		return CommonUtils.readResource("bullbof/ipconfig." + var1 + ".o");
	}
}
