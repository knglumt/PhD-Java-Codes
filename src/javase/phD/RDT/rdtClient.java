package client;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import model.FileDataResponseType;
import model.FileListResponseType;
import model.FileSizeResponseType;
import model.RequestType;
import model.ResponseType;
import model.ResponseType.RESPONSE_TYPES;

// main class
public class rdtClient {

	public xClient rdtClient1;
	public xClient rdtClient2;
	private static int fileID;
	private static String fileName;
	private static long fileSize;
	private static long writtenData;
	private static Boolean parallel;
	long startByte = 1;
	long maxBuffer = ResponseType.MAX_RESPONSE_SIZE;
	static long actBuffer = 0;
	long endByte = maxBuffer;
	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	FileOutputStream fileWriter = null;
	long writtenData0 = 0;
	FileDataResponseType responseRDT;
	FileTransfer fileTransfer1;
	FileTransfer fileTransfer2;
	long currentRTT1 = 1;
	long avgRTT1 = 0;
	double speed1 = 0.0;
	double avgSpeed1 = 0.0;
	long lossClient1 = 0;
	long packetClient1 = 0;
	long currentRTT2 = 1;
	long avgRTT2 = 0;
	double speed2 = 0.0;
	double avgSpeed2 = 0.0;
	long lossClient2 = 0;
	long packetClient2 = 0;

	/** Thread for each client */
	private class FileTransfer extends Thread {

		private xClient aClient;

		/** constructs a new client. */
		public FileTransfer(xClient client) {

			aClient = client;
		}

		@Override
		public void run() {

			InetAddress IPAddress;
			DatagramPacket sendPacket = null;
			int timeout = 30; // 30 ms
			// long lossByte = 0;
			boolean packetLoss = false;
			boolean timeOut = false;
			boolean corrupted = false;
			long startTime;
			long endTime;

			startTime = System.currentTimeMillis();

			// UDP connection
			try {
				IPAddress = InetAddress.getByName(aClient.getIp());
				RequestType req = new RequestType(RequestType.REQUEST_TYPES.GET_FILE_DATA, fileID, startByte, endByte,
						null);
				byte[] sendData = req.toByteArray();
				sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, aClient.getPort());
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}

			DatagramSocket dsocket = null;
			try {
				dsocket = new DatagramSocket();
				dsocket.send(sendPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}

			byte[] receiveData = new byte[ResponseType.MAX_RESPONSE_SIZE];
			long maxReceivedByte = 0;
			byte[] packetData = null;

			// get data for each packet (MAX_RESPONSE_SIZE)
			try {
				dsocket.setSoTimeout(timeout);
			} catch (SocketException e1) {
				e1.printStackTrace();
			}

			while (maxReceivedByte < endByte) {

				corrupted = false;
				packetLoss = false;
				timeOut = false;

				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				try {
					dsocket.receive(receivePacket);
				} catch (SocketTimeoutException e) {
					timeOut = true;
					// update loss count
					updateLoss(this.getName(), true);
					break;
				} catch (IOException e) {
					e.printStackTrace();
				}

				endTime = System.currentTimeMillis();

				packetData = receivePacket.getData();
				FileDataResponseType response = new FileDataResponseType(packetData);

				if ((response.getData() != null) && (actBuffer == 0)) {
					actBuffer = response.getData().length;
					endByte = actBuffer;
				}
				if ((startByte) != response.getStart_byte()) {

					packetLoss = true;
					// update loss count
					updateLoss(this.getName(), true);
					break;
				}

				// checksum, corrupted?
				if (((endByte - startByte) + 1) != response.getData().length) {
					corrupted = true;
					// update loss count
					updateLoss(this.getName(), true);
					break;
				}

				// update stats and packet count
				updateStats(this.getName(), startTime, endTime);
				updateLoss(this.getName(), false);

				if (!packetLoss && !corrupted) {
					responseRDT = response;
				}

				if (response.getResponseType() != RESPONSE_TYPES.GET_FILE_DATA_SUCCESS) {
					break;
				}

				if ((response.getEnd_byte() > maxReceivedByte) && (!corrupted)) {
					maxReceivedByte = response.getEnd_byte();
				}
			}

		}

	}

	// Get file list from server
	public static void getFileList(String ip, int port) throws IOException {

		InetAddress IPAddress = InetAddress.getByName(ip);
		RequestType req = new RequestType(RequestType.REQUEST_TYPES.GET_FILE_LIST, 0, 0, 0, null);
		byte[] sendData = req.toByteArray();

		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
		DatagramSocket dsocket = new DatagramSocket();
		dsocket.send(sendPacket);

		byte[] receiveData = new byte[ResponseType.MAX_RESPONSE_SIZE];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		dsocket.receive(receivePacket);

		FileListResponseType response = new FileListResponseType(receivePacket.getData());
		// print file list
		System.out.println(response.toString());

		dsocket.close();
	}

	private static long getFileSize(int file_id, String ip, int port) throws IOException {

		InetAddress IPAddress = InetAddress.getByName(ip);
		RequestType req = new RequestType(RequestType.REQUEST_TYPES.GET_FILE_SIZE, file_id, 0, 0, null);
		byte[] sendData = req.toByteArray();

		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
		DatagramSocket dsocket = new DatagramSocket();
		dsocket.send(sendPacket);

		byte[] receiveData = new byte[ResponseType.MAX_RESPONSE_SIZE];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		dsocket.receive(receivePacket);

		FileSizeResponseType response = new FileSizeResponseType(receivePacket.getData());
		System.out.println(response.toString());
		dsocket.close();
		// get file size to download
		return response.getFileSize();
	}

	private void getFileData(int file_id, long start, long end) throws IOException, InterruptedException {

		double speed = 0.0;
		double percentage;
		double remainingTime;
		long downloadStart;
		long elapsedTime;

		if (fileSize <= maxBuffer) {
			endByte = fileSize;
		}

		try {
			fileWriter = new FileOutputStream(fileName, true);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		downloadStart = System.currentTimeMillis();

		// loop for each packet
		while (endByte <= fileSize) {

			responseRDT = null;

			// create Filetransfer1 for client1
			fileTransfer1 = new FileTransfer(rdtClient1);
			fileTransfer1.setName("Client1");

			// create Filetransfer2 for client2
			fileTransfer2 = null;
			if (parallel) {
				fileTransfer2 = new FileTransfer(rdtClient2);
				fileTransfer2.setName("Client2");
			}

			if (parallel) {
				fileTransfer2.start();
				TimeUnit.MILLISECONDS.sleep(5);
			}

			fileTransfer1.start();

			// start threads
			try {
				if (parallel) {
					fileTransfer2.join();
				}
				fileTransfer1.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			fileTransfer1.interrupt();
			fileTransfer1.join();
			fileTransfer2.interrupt();
			fileTransfer2.join();

			writtenData0 = writtenData;
			if ((responseRDT != null) && (writtenData < responseRDT.getEnd_byte())) {
				try {

					writtenData = responseRDT.getEnd_byte();

					outputStream.write(responseRDT.getData());
					outputStream.flush();

					fileWriter.write(responseRDT.getData());
					fileWriter.flush();

				} catch (IOException e) {
					writtenData = writtenData0;
					e.printStackTrace();
				}
			}

			// calculate speed, percentage, elapsed time, remaining time
			elapsedTime = (System.currentTimeMillis() - downloadStart);
			speed = (((double) endByte * 8.0) / ((double) elapsedTime / 1000.0)) / 1024.0;

			percentage = ((double) endByte / (double) fileSize) * 100.0;
			remainingTime = (((double) (elapsedTime / 1000) / (percentage)) * (100.0)) - (elapsedTime / 1000);

			// print download stats
			System.out.println("Elapsed Time  : " + (elapsedTime / 1000) + " seconds");
			System.out.println("Speed         : " + speed + " Kbps");
			System.out.println("Percentage    : " + percentage);
			System.out.println("Remaining Time: " + remainingTime + " seconds");
			System.out.println("\r");

			// check EOF for loop
			if (endByte == fileSize) {
				break;
			}

			if (endByte < fileSize) {
				startByte = endByte + 1;
				endByte = endByte + actBuffer;

				if (endByte > fileSize) {
					endByte = fileSize;
				}
			}

		}

	}

	// update stats for each client
	private void updateStats(String client, long startTime, long endTime) {
		long data;

		data = endByte - startByte;

		if (client == "Client1") {

			currentRTT1 = endTime - startTime;

			if (avgRTT1 == 0)
				avgRTT1 = currentRTT1;
			else
				avgRTT1 = (avgRTT1 + currentRTT1) / 2;

			speed1 = (((double) data * 8.0) / ((double) currentRTT1 / 1000.0)) / 1024.0;

			if (avgSpeed1 == 0.0)
				avgSpeed1 = speed1;
			else
				avgSpeed1 = (avgSpeed1 + speed1) / 2;

		}

		if (client == "Client2") {

			currentRTT2 = endTime - startTime;

			if (avgRTT2 == 0)
				avgRTT2 = currentRTT2;
			else
				avgRTT2 = (avgRTT2 + currentRTT2) / 2;

			speed2 = (((double) data * 8.0) / ((double) currentRTT2 / 1000.0)) / 1024.0;

			if (avgSpeed2 == 0.0)
				avgSpeed2 = speed2;
			else
				avgSpeed2 = (avgSpeed2 + speed2) / 2;
		}
	}

	private void updateLoss(String client, Boolean isLoss) {

		if (client == "Client1") {
			if (isLoss)
				lossClient1++;
			else
				packetClient1++;
		}

		if (client == "Client2") {

			if (isLoss)
				lossClient2++;
			else
				packetClient2++;

		}

	}

	private void printStats() {

		// calculate MD5 and print
		MessageDigest MD5;
		try {
			MD5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException(e);
		}
		byte[] fileMD5 = MD5.digest(outputStream.toByteArray());
		
		if (fileSize == outputStream.toByteArray().length)
			System.out.println("DOWNLOAD SUCCESSFUL...");
			else 
				System.out.println("DOWNLOAD FAILED!...");
		
		System.out.println("File Name : " + fileName);
		System.out.println("File Size : " + fileSize + " bytes");
		System.out.println("Downloaded: " + outputStream.toByteArray().length + " bytes");
		System.out.println("MD5 HASH  : " + (bytesToHex(fileMD5).toUpperCase()));
	
		System.out.println(" ");
		System.out.println("CLIENT 1 STATS...");
		System.out.println("Average RTT  : " + avgRTT1 + " ms ");
		System.out.println("Average Speed: " + avgSpeed1 + " Kbps ");
		System.out.println("Loss         : " + lossClient1 + " packets");
		System.out.println("Loss Rate    : " + ((double) lossClient1 / ((double) packetClient1 + (double) lossClient1)) * 100);

		System.out.println(" ");
		System.out.println("CLIENT 2 STATS...");
		System.out.println("Average RTT  : " + avgRTT2 + " ms ");
		System.out.println("Average Speed: " + avgSpeed2 + " Kbps ");
		System.out.println("Loss         : " + lossClient2 + " packets");
		System.out.println("Loss Rate    : " + ((double) lossClient2 / ((double) packetClient2 + (double) lossClient2)) * 100);
		System.out.println(" ");
	}

	private String bytesToHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}
	
	//reset values for new download
	private void resetValues() {
		startByte = 1;
		actBuffer = 0;
		endByte = maxBuffer;
		outputStream = new ByteArrayOutputStream();
		fileWriter = null;
		writtenData0 = 0;
		currentRTT1 = 1;
		avgRTT1 = 0;
		speed1 = 0.0;
		avgSpeed1 = 0.0;
		lossClient1 = 0;
		packetClient1 = 0;
		currentRTT2 = 1;
		avgRTT2 = 0;
		speed2 = 0.0;
		avgSpeed2 = 0.0;
		lossClient2 = 0;
		packetClient2 = 0;

	}

	public static void main(String[] args) throws Exception {

		// TODO
		// Checksum OK
		// timer, timeout OK
		// transfer more data on the faster connection OK
		// if the md5 check fails OK
		// the maximum data size in a single packet is 1000 bytes, check the response’s
		// OK
		// start_byte and end_byte OK

		// Print Client stats OK
		// speed over each connection, OK
		// percentage completed, OK
		// elapsed time, OK
		// packet loss rate experienced so far, OK
		// current/average round-trip times, etc. OK

		if (args.length < 1) {
			throw new IllegalArgumentException("ip:port is mandatory, ip:port ip:port for parallel download!");
		}

		parallel = args.length == 2;

		rdtClient rdtClientMain = new rdtClient();

		// server adr1
		String[] adr1 = args[0].split(":");
		String ip1 = adr1[0];
		int port1 = Integer.valueOf(adr1[1]);
		rdtClientMain.rdtClient1 = new xClient(ip1, port1);

		// server adr2
		String ip2 = "";
		int port2 = 0;

		if (parallel) {
			String[] adr2 = args[1].split(":");
			ip2 = adr2[0];
			port2 = Integer.valueOf(adr2[1]);
			rdtClientMain.rdtClient2 = new xClient(ip2, port2);
		}

		Scanner sc = new Scanner(System.in);
		Scanner sc2 = new Scanner(System.in);
		// Select a file to download with loop
		fileID = -1;
		while (fileID != 0) {
			writtenData = 0;
			// Get file list
			rdtClient.getFileList(ip1, port1);
			System.out.println("---------------------");
			System.out.println("#Enter '0' to exit!");
			System.out.println("Enter a file number: ");

			fileID = sc.nextInt(); // reads file id

			if (fileID == 0) {
				System.out.println("Exit!");
				break;
			}
			
			fileSize = rdtClient.getFileSize(fileID, ip1, port1);
			System.out.println("---------------------");
			System.out.println("File size: " + (fileSize / 1024) + " KB");

			System.out.println("Enter a file name: ");
			fileName = sc2.nextLine(); // reads string

			// start download
			rdtClientMain.getFileData(fileID, 1, fileSize);

			// print stats
			rdtClientMain.printStats();

			// reset for new download
			rdtClientMain.resetValues();

		}

	}
}
