package it.uniroma2.jcloudstests;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

import org.javasimon.SimonManager;
import org.javasimon.Split;
import org.javasimon.Stopwatch;

import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;

public final class LatencyTest {


	public static final void main(String[] args) throws InterruptedException {

		ArrayList<String> hosts = Lists.newArrayList();
		hosts.add("160.80.85.75");
		hosts.add("62.101.90.75");
		hosts.add("203.28.243.20");
		
		

		for (int test = 0; test < 10; test++) {
			for (String host : hosts) {
				InetAddress address = InetAddresses.forString(host);
				try {

					Stopwatch stopwatch = SimonManager.getStopwatch(host
							+ "-LATENCY-"+new Integer(test).toString());
					for (int i = 0; i < 10; i++) {
						Thread.sleep(1000);
						
						Split split = stopwatch.start(); // start the stopwatch
						address.isReachable(0);
						split.stop(); // stop it
					}
					System.out.println("Result: " + stopwatch); // here we print
																// our
																// stopwatch
																// again
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		// try {
		// echoTCP(host);
		// } catch (IOException e) {
		// e.printStackTrace();
		// System.exit(1);
		// }
		// try {
		//
		// echoUDP(host);
		// } catch (IOException e) {
		// e.printStackTrace();
		// System.exit(1);
		// }
	}
}
