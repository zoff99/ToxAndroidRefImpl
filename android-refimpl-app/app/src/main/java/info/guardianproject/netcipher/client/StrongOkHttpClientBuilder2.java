/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2017 Zoff <zoff@zoff.cc>
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 */

/*
 * Copyright 2012-2016 Nathan Freitas
 * Copyright 2015 str4d
 * Portions Copyright (c) 2016 CommonsWare, LLC
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

package info.guardianproject.netcipher.client;

import android.content.Context;
import android.content.Intent;

import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLSocketFactory;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Creates an OkHttpClient using NetCipher configuration. Use
 * build() if you have no other OkHttpClient configuration
 * that you need to perform. Or, use applyTo() to augment an
 * existing OkHttpClient.Builder with NetCipher.
 */
public class StrongOkHttpClientBuilder2 extends StrongBuilderBase<StrongOkHttpClientBuilder2, OkHttpClient>
{
    /**
     * Creates a StrongOkHttpClientBuilder using the strongest set
     * of options for security. Use this if the strongest set of
     * options is what you want; otherwise, create a
     * builder via the constructor and configure it as you see fit.
     *
     * @param ctxt any Context will do
     * @return a configured StrongOkHttpClientBuilder
     * @throws Exception
     */
    static public StrongOkHttpClientBuilder2 forMaxSecurity(Context ctxt) throws Exception
    {
        return (new StrongOkHttpClientBuilder2(ctxt).withBestProxy());
    }

    /**
     * Creates a builder instance.
     *
     * @param ctxt any Context will do; builder will hold onto
     *             Application context
     */
    public StrongOkHttpClientBuilder2(Context ctxt)
    {
        super(ctxt);
    }

    /**
     * Copy constructor.
     *
     * @param original builder to clone
     */
    public StrongOkHttpClientBuilder2(StrongOkHttpClientBuilder2 original)
    {
        super(original);
    }

    /**
     * OkHttp3 does not support SOCKS proxies:
     * https://github.com/square/okhttp/issues/2315
     *
     * @return false
     */
    @Override
    public boolean supportsSocksProxy()
    {
        return (false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OkHttpClient build(Intent status)
    {
        return (applyTo(new OkHttpClient.Builder(), status).
                connectTimeout(60, TimeUnit.SECONDS).
                writeTimeout(60, TimeUnit.SECONDS).
                connectTimeout(60, TimeUnit.SECONDS).
                build());
    }

    /**
     * Adds NetCipher configuration to an existing OkHttpClient.Builder,
     * in case you have additional configuration that you wish to
     * perform.
     *
     * @param builder a new or partially-configured OkHttpClient.Builder
     * @return the same builder
     */
    public OkHttpClient.Builder applyTo(OkHttpClient.Builder builder, Intent status)
    {
        SSLSocketFactory factory = buildSocketFactory();

        if (factory != null)
        {
            builder.sslSocketFactory(factory);
        }

        return (builder.proxy(buildProxy(status)));
    }

    @Override
    protected String get(Intent status, OkHttpClient connection, String url) throws Exception
    {
        Request request = new Request.Builder().url(TOR_CHECK_URL).build();

        return (connection.newCall(request).execute().body().string());
    }
}
