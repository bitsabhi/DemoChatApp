package com.sample.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ConduitServer extends Thread {
	private static final int PORT = 8080;
	private String mTextLine;
	private List<BufferedWriter> mWriterList;

	public static void main(String[] args){

		ConduitServer conduitServer = new ConduitServer();

		if (conduitServer != null) {
			conduitServer.startServer();
		}
	}

	private void pingAll(int connectionId) {

		for (int i = 0; i < mWriterList.size(); i++) {

			try {
				if (i != connectionId) {
					mWriterList.get(i).write(mTextLine, 0, mTextLine.length());
					mWriterList.get(i).flush();
				}

			} catch (IOException e) {
				e.printStackTrace();				
			}
		}
	}

	public void startServer () {

		mWriterList = new ArrayList<BufferedWriter>();

		try {

			final ServerSocket server = new ServerSocket(PORT, 1);

			while (true) {

				final Socket client = server.accept();
				System.out.println("Client connected");

				new Thread(new Runnable() {

					int connectionId;

					public void run() {

						try {

							BufferedReader reader = null;
							BufferedWriter writer = null;

							reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

							writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
							mWriterList.add(writer);
							connectionId = mWriterList.indexOf(writer);

							while (true) {

								mTextLine = reader.readLine() + "\n";

								if (mTextLine.equalsIgnoreCase("EXIT\n")) {
									System.out.println("Closing connection");
									break;
								}								

								pingAll(connectionId);

							}

						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}).start();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
}
