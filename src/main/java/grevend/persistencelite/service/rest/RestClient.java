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

package grevend.persistencelite.service.rest;

import static grevend.persistencelite.service.rest.RestMode.REQUESTER;

import entities.Country;
import grevend.persistencelite.PersistenceLite;
import java.util.Objects;

public class RestClient {

    public static void main(String[] args) throws Throwable {
        // REST (REQUESTER)
        var rest = PersistenceLite.configure(RestService.class)
            .mode(REQUESTER).version(2).service();

        rest.registerTypeMarshaller(Integer.TYPE, Integer::valueOf);
        rest.registerTypeMarshaller(Integer.class, Integer::valueOf);
        rest.registerTypeMarshaller(String.class, s -> Objects.equals(s, "null") ? null : s);

        var countryDao = rest.createDao(Country.class);
        //System.out.println(countryDao.retrieveAll());
        System.out.println(countryDao.retrieveById("id", 63).orThrow());
        var res = countryDao.update(new Country(0, "greven", "LOL"),
            "code", 12).orThrow();
        countryDao.delete(res);
    }

}
