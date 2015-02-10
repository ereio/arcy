package com.dingohub.arcy.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import org.apache.http.conn.util.InetAddressUtils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.dingohub.arcy.ChatClientActivity;
import com.dingohub.arcy.ServerSetupActivity;

/**
 * Utility class for facilitating socket-based communication between a 
 * server and client
 */
public class SocketUtil {
	static final String TAG = "NetworkUtil";
	
	/**
	 * Obtain the IPV4 address of the network interface of the device
	 * 
	 * @return The IPv4 Address of the network interface
	 */
	public static String getIPv4Address() {
		String address = null;
	
		try {
			// Obtain a collection of Network Interfaces available on the 
			// device
			Enumeration<NetworkInterface> interfaces = 
					NetworkInterface.getNetworkInterfaces();
			
			// Iterate through the interfaces
			while (interfaces.hasMoreElements()) {
				NetworkInterface current = interfaces.nextElement();
		
				// Get a collection of addresses associated with the 
				// current network interface
				Enumeration<InetAddress> currentAddresses = 
						current.getInetAddresses();
				
				// Iterate through the addresses
				while (currentAddresses.hasMoreElements()) {
					InetAddress inetAddress = 
							currentAddresses.nextElement();
					
					// The current address is the IPv4 address for the network interface
					if (!inetAddress.isLoopbackAddress() && 
							InetAddressUtils.isIPv4Address(
									inetAddress.getHostAddress())) {
						address = inetAddress.getHostAddress();
						break;
					}
				}
			}
		}catch (SocketException sockex) {
			Log.e(TAG, sockex.getLocalizedMessage(), sockex);
		}
		
		return address;
	}
	
	/**
	 * Obtain the IPV6 address of the network interface of the device
	 * 
	 * @return The IPv6 address of the network interface
	 */
	public static String getIPv6Address() {
		String address = "";
		
		try {
			// Obtain a collection of Network Interfaces available on the 
			// device
			Enumeration<NetworkInterface> interfaces = 
					NetworkInterface.getNetworkInterfaces();
			
			// Iterate through the interfaces
			while (interfaces.hasMoreElements()) {
				NetworkInterface current = interfaces.nextElement();
		
				// Get a collection of addresses associated with the 
				// current network interface
				Enumeration<InetAddress> currentAddresses = 
						current.getInetAddresses();
				
				// Iterate through the addresses
				while (currentAddresses.hasMoreElements()) {
					InetAddress inetAddress = 
							currentAddresses.nextElement();
					
					// The current address is the IPv6 address for the network interface
					if (!inetAddress.isLoopbackAddress() && 
							InetAddressUtils.isIPv6Address(
									inetAddress.getHostAddress())) {
						address = inetAddress.getHostAddress();
						break;
					}
				}
			}
		}catch (SocketException sockex) {
			Log.e(TAG, sockex.getLocalizedMessage(), sockex);
		}
		
		return address;
	}
	
}
