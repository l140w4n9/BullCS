package beacon.bof;

import aggressor.AggressorClient;
import beacon.PostExInlineObject;
import common.CommonUtils;
import common.Packer;

public class uac2 extends PostExInlineObject {
	protected String FilePath;

	public uac2(AggressorClient var1, String FilePath) {
		super(var1);
		this.FilePath = FilePath;
	}

	public byte[] getArguments(String var1) {
		Packer var2 = new Packer();
		var2.addLengthAndStringASCIIZ(this.FilePath);
		return var2.getBytes();
	}

	public byte[] getObjectFile(String var1) {
		return CommonUtils.readResource("bullbof/uac2." + var1 + ".o");
	}
}
