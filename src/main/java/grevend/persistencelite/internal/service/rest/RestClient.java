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

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class RestClient {

    public static void main(String[] args) throws IOException {
        var url = new URL("http://localhost:8000/applications/myapp?");
        //var url = new URL("https://reqres.in/api/users");
        //HttpURLConnection con = (HttpURLConnection) url.openConnection();
        URLConnection con = url.openConnection();
        con.setRequestProperty("Accept-Charset", "utf-8");
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        //con.setRequestMethod("POST");
        //con.setRequestMethod("GET");
        //con.setConnectTimeout(50000);
        //System.out.println(con.getResponseCode());
        //con.setRequestProperty("Content-Type", "application/json; utf-8");
        //con.setRequestProperty("Accept", "application/json");
        //con.setDoOutput(true);

        /*String jsonInputString = "{\"name\": \"Upendra\", \"job\": \"Programmer\"}";
        try(OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }*/

        //int responseCode = con.getResponseCode();

        //System.out.println("Response Code: " + responseCode);

        /*try(BufferedReader br = new BufferedReader(
            new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            var gson = new Gson();
            var map = gson.fromJson(response.toString(), Map.class);
            System.out.println(response.toString());
            System.out.println(map);
        }*/

        var gson = new Gson();
        var map = gson.fromJson(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8), Map.class);
        System.out.println(map);
    }

}
