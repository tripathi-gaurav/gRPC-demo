package org.tripathi.grpc.hellodroidtls;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

public class HelloDroidTLSServer {
	private static final Logger logger = Logger.getLogger(HelloDroidTLSServer.class.getName());
	private Server server;
	private static final String certChainFile = "/path/to/server.crt";
	private static final String privateKeyFile = "/path/to/server.pem";
	

	/**
	 * Main launches the server from the command line.
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		final HelloDroidTLSServer server = new HelloDroidTLSServer();
		server.start();
		server.blockUntilShutdown();
	}
	
	/**
	 * == Code to drive the server ==
	 */
	private void start() throws IOException {
		/* The port on which the server should run */
		int port = 8443;
		//server = ServerBuilder.forPort(port).addService(new GreeterImpl()).build().start();
		server = ServerBuilder.forPort(port)
			    // Enable TLS
			    .useTransportSecurity(new File(certChainFile), new File(privateKeyFile))
			    .addService(new GreeterImpl())
			    .build();
		server.start();
			
		logger.info("Server started, listening on " + port);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				// Use stderr here since the logger may have been reset by its JVM shutdown
				// hook.
				System.err.println("*** shutting down gRPC server since JVM is shutting down");
				HelloDroidTLSServer.this.stop();
				System.err.println("*** server shut down");
			}
		});
	}

	private void stop() {
		if (server != null) {
			server.shutdown();
		}
	}

	/**
	 * Await termination on the main thread since the grpc library uses daemon
	 * threads.
	 */
	private void blockUntilShutdown() throws InterruptedException {
		if (server != null) {
			server.awaitTermination();
		}
	}	
	/** == End of Code to drive the server == */
	
	
	/**
	 * == Code to the actual RPC ==
	 */
	static class GreeterImpl extends GreeterGrpc.GreeterImplBase {

		@Override
		public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
			HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + req.getName()).build();
			responseObserver.onNext(reply);
			responseObserver.onCompleted();
		}

		@Override
		public void sayHelloAgain(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
			HelloReply reply = HelloReply.newBuilder().setMessage("Hello again bro, " + req.getName()).build();
			responseObserver.onNext(reply);
			responseObserver.onCompleted();
		}
	}
	/** == End of Code to the actual RPC == */
}
