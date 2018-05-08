package plccom;

import PLCCom.CPUModeInfoResult;
import PLCCom.ConnectResult;
import PLCCom.ConnectionStateChangeNotifier;
import PLCCom.OperationResult;
import PLCCom.TCP_ISO_Device;
import PLCCom.authentication;
import PLCCom.eConnectionState;
import PLCCom.ePLCType;
import PLCCom.iConnectionStateChangeEvent;

public class TestConnection implements iConnectionStateChangeEvent {

	public static void main(String[] args) {
		new TestConnection();

	}

	public TestConnection() {

		authentication.Serial("73471-56153-102135-2267354");
		authentication.User("magna");

		TCP_ISO_Device plc = new TCP_ISO_Device("192.168.0.20", 0, 2, ePLCType.S7_300_400_compatibel);

		plc.setAutoConnect(true, 1000);

		ConnectResult res = plc.Connect();
		if (!res.Quality().equals(OperationResult.eQuality.GOOD)) {
			System.out.println(res.Message());
		}

		// register Connection state change event
		plc.StateChangeNotifier = new ConnectionStateChangeNotifier(this);

		Thread th = new Thread(new Runnable() {

			@Override
			public void run() {

				while (true) {

					String text = null;

					CPUModeInfoResult res = plc.GetCPUMode();

					if (res.HasWorked()) {
						text = String.valueOf(res.CPUModeInfo());

					}
					System.out.println(text);

					System.out.println(plc.getConnectionState());
					System.out.println(plc.IsConnected());
					System.out.println(plc.getAutoConnectIdleTimespan());

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {

						e.printStackTrace();
						Thread.currentThread().interrupt();
					}
				}
			}
		});

		th.start();

	}

	@Override
	public void On_ConnectionStateChange(eConnectionState arg0) {
		System.out.println(arg0);

	}

}
