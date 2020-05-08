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

import grevend.persistencelite.service.Configurator;
import grevend.persistencelite.service.Service;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * @author David Greven
 * @see RestService
 * @since 0.3.0
 */
public final class RestConfigurator implements Configurator<RestService> {

    private final RestService restService;

    private RestMode mode = RestMode.REQUESTER;
    private Service<?> service;
    private int version;
    private String scope;

    /**
     * @param restService
     *
     * @since 0.3.0
     */
    @Contract(pure = true)
    RestConfigurator(@NotNull RestService restService) {
        this.restService = restService;
    }

    /**
     * @param mode
     *
     * @return
     *
     * @since 0.3.0
     */
    @NotNull
    @Contract("_ -> this")
    public RestConfigurator mode(@NotNull RestMode mode) {
        this.mode = mode;
        return this;
    }

    /**
     * @param service
     *
     * @return
     *
     * @since 0.3.0
     */
    @NotNull
    @Contract("_ -> this")
    public RestConfigurator uses(@NotNull Service<?> service) {
        if (this.mode != RestMode.SERVER) {
            throw new IllegalArgumentException();
        } else {
            this.service = service;
        }
        return this;
    }

    /**
     * @param version
     *
     * @return
     *
     * @since 0.3.3
     */
    @NotNull
    @Contract("_ -> this")
    public RestConfigurator version(int version) {
        this.version = version;
        return this;
    }

    /**
     * @param packageScope
     *
     * @return
     *
     * @since 0.3.3
     */
    @NotNull
    @Contract("_ -> this")
    public RestConfigurator scope(@NotNull String packageScope) {
        if (this.mode != RestMode.SERVER) {
            throw new IllegalArgumentException();
        } else {
            this.scope = packageScope;
        }
        return this;
    }

    /**
     * @return
     *
     * @since 0.3.0
     */
    @NotNull
    @Override
    @Contract(" -> new")
    public RestService service() {
        if (this.mode == RestMode.SERVER && this.service == null) {
            throw new IllegalStateException("No service defined.");
        }
        this.restService.setMode(this.mode);
        this.restService.setService(this.service);
        return this.restService;
    }

}
