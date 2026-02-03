package beacon.bof;

import aggressor.AggressorClient;
import beacon.PostExInlineObject;
import common.CommonUtils;

public class arp extends PostExInlineObject {
	public arp(AggressorClient var1) {
		super(var1);
	}

	public byte[] getObjectFile(String var1) {
		return CommonUtils.readResource("bullbof/arp." + var1 + ".o");
	}
}
