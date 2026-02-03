package beacon.bof;

import aggressor.AggressorClient;
import beacon.PostExInlineObject;
import common.CommonUtils;
import common.Packer;

public class openport extends PostExInlineObject {
	protected String Params;

	public openport(AggressorClient var1, String Params) {
		super(var1);
		this.Params = Params;
	}

	public byte[] getArguments(String var1) {
		Packer var2 = new Packer();
		String[] args = this.Params.split("\\s+");
		if (args.length == 2) {
			var2.addLengthAndEncodedStringASCIIZ(this.client, var1, args[0]);
			int port = Integer.parseInt(args[1]);
			if (port < 1 && port > 65535) {
				return new byte[0];
			} else {
				var2.addInt(port);
				return var2.getBytes();
			}
		} else {
			return new byte[0];
		}
	}

	public byte[] getObjectFile(String var1) {
		return CommonUtils.readResource("bullbof/openport." + var1 + ".o");
	}
}
