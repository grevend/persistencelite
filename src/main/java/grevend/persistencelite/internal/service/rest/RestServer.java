/*
 * MIT License
 *
 * Copyright (c) 2020 David Greven
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package grevend.persistencelite.internal.service.rest;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class RestServer {

    public static void main(String[] args) throws IOException {
        /*class MyHandler implements HttpHandler {
            public void handle(@NotNull HttpExchange t) throws IOException {
                System.out.println("Request...");
                InputStream is = t.getRequestBody();
                //read(is); // .. read the request body
                //String response = "This is the response";
                String response = "{\"name\": \"Dave\", \"job\": \"Programmer\"}";
                t.getResponseHeaders().put("Content-Type", List.of("application/json; utf-8"));
                t.sendResponseHeaders(200, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }

            Serializer

        }*/

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        //server.createContext("/applications/myapp", new MyHandler());
        server.createContext("/applications/myapp", exchange -> {
            System.out.println("Request...");
            InputStream is = exchange.getRequestBody();
            //read(is); // .. read the request body
            //String response = "This is the response";
            String response = "{\"name\": \"Dave\", \"job\": \"Programmer\"}";
            exchange.getResponseHeaders().put("Content-Type", List.of("application/json; utf-8"));
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        });
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println(server.getAddress());
    }

}
