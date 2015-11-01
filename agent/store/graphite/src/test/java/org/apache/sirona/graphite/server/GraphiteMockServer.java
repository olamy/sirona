/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sirona.graphite.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

// little mok to replace Graphite as a server
public class GraphiteMockServer {
    private final int port;

    private ServerSocket server;
    private GraphiteThread thread;

    private Collection<String> messages = new CopyOnWriteArrayList<String>();

    public GraphiteMockServer(final int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public GraphiteMockServer start() throws IOException {
        server = new ServerSocket(port);
        thread = new GraphiteThread(server, messages);
        thread.start();
        return this;
    }

    public void stop() throws IOException {
        thread.shutdown();
        server.close();
    }

    public Collection<String> getMessages() {
        return messages;
    }

    private static class GraphiteThread extends Thread {
        private final Collection<String> messages;

        private final AtomicBoolean done = new AtomicBoolean(false);
        private final ServerSocket server;

        public GraphiteThread(final ServerSocket server, final Collection<String> messages) {
            this.messages = messages;
            this.server = server;
            setName("graphite-server");
        }

        @Override
        public void run() {
            while (!done.get()) {
                try {
                    final Socket s = server.accept();
                    synchronized (this) {
                        final InputStream is = s.getInputStream();
                        final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                        String line;

                        try {
                            while ((line = reader.readLine()) != null) {
                                messages.add(line);
                            }
                        } finally {
                            s.close();
                        }
                    }
                } catch (final IOException e) {
                    if (!done.get()) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        public void shutdown() {
            done.set(true);
        }
    }
}
