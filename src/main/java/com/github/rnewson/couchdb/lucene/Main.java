/*
 * Copyright Robert Newson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.rnewson.couchdb.lucene;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.NetworkTrafficServerConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.GzipFilter;

import javax.servlet.DispatcherType;
import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;

public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class);

    /**
     * Run couchdb-lucene.
     */
    public static void main(String[] args) throws Exception {
        final Config config = new Config();
        final File dir = config.getDir();

        final Server server = new Server();
        final NetworkTrafficServerConnector connector = new NetworkTrafficServerConnector(server);
        connector.setHost(config.getConfiguration().getString("lucene.host", "localhost"));
        connector.setPort(config.getConfiguration().getInt("lucene.port", 5985));

        LOG.info("Accepting connections with " + connector);

        final int blacklistSize = config.getConfiguration().getList(
                "couchdb.blacklist", new ArrayList<String>()).size();
        if (blacklistSize > 0) {
            LOG.info("Blacklisted index documents are " +
                            config.getConfiguration().getList("couchdb.blacklist", new ArrayList<String>())
            );
        }


        server.addConnector(connector);
        server.setStopAtShutdown(true);

        final LuceneServlet servlet = new LuceneServlet(config.getClient(), dir, config.getConfiguration());

        final ServletContextHandler context = new ServletContextHandler(server, "/",
                ServletContextHandler.NO_SESSIONS | ServletContextHandler.NO_SECURITY);
        context.addServlet(new ServletHolder(servlet), "/*");
        context.addFilter(new FilterHolder(new GzipFilter()), "/*",
                EnumSet.of(DispatcherType.REQUEST));
        context.setErrorHandler(new JSONErrorHandler());
        server.setHandler(context);

        server.start();
        server.join();
    }

}
