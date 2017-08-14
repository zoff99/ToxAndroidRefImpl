/*
    Privacy Friendly Net Monitor (Net Monitor)
    - Copyright (2015 - 2017) Felix Tsala Schiller

    ###################################################################

    This file is part of Net Monitor.

    Net Monitor is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Net Monitor is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Net Monitor.  If not, see <http://www.gnu.org/licenses/>.

    Diese Datei ist Teil von Net Monitor.

    Net Monitor ist Freie Software: Sie können es unter den Bedingungen
    der GNU General Public License, wie von der Free Software Foundation,
    Version 3 der Lizenz oder (nach Ihrer Wahl) jeder späteren
    veröffentlichten Version, weiterverbreiten und/oder modifizieren.

    Net Monitor wird in der Hoffnung, dass es nützlich sein wird, aber
    OHNE JEDE GEWÄHRLEISTUNG, bereitgestellt; sogar ohne die implizite
    Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK.
    Siehe die GNU General Public License für weitere Details.

    Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
    Programm erhalten haben. Wenn nicht, siehe <http://www.gnu.org/licenses/>.

    ###################################################################

    This app has been created in affiliation with SecUSo-Department of Technische Universität
    Darmstadt.

    Privacy Friendly Net Monitor is based on TLSMetric by Felix Tsala Schiller
    https://bitbucket.org/schillef/tlsmetric/overview.
 */

package org.secuso.privacyfriendlynetmonitor.Assistant;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Handles the execution of shell commands.
 */
public class ExecCom extends Thread
{

    //Execute user commands on shell
    static void user(String string)
    {
        if (Const.IS_DEBUG)
        {
            Log.d(Const.LOG_TAG, "Executing as user: " + string);
        }
        try
        {
            Process user = Runtime.getRuntime().exec(string);
            try
            {
                user.waitFor();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    //Execute user commands and get the result.
    public static String userForResult(String string)
    {
        if (Const.IS_DEBUG)
        {
            Log.d(Const.LOG_TAG, "Executing for result as user: " + string);
        }
        String res = "";
        DataOutputStream outputStream = null;
        InputStream response = null;
        try
        {
            Process user = Runtime.getRuntime().exec(string);

            outputStream = new DataOutputStream(user.getOutputStream());
            response = user.getInputStream();

            outputStream.writeBytes("exit\n");
            outputStream.flush();
            try
            {
                user.waitFor();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            res = readFully(response);
        }
        catch (IOException e)
        {
            if (Const.IS_DEBUG)
            {
                Log.i(Const.LOG_TAG, "IO operation unsuccessful. Pipe Broken?" + string);
            }
        }
        finally
        {
            closeSilently(outputStream, response);
        }
        return res;
    }

    //Read the command output and return an utf8 string.
    public static String readFully(InputStream is) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = is.read(buffer)) != -1)
        {
            baos.write(buffer, 0, length);
        }
        return baos.toString("UTF-8");
    }

    //Closes a variety of closable objects.
    public static void closeSilently(Object... xs)
    {
        // Note: on Android API levels prior to 19 Socket does not implement Closeable
        for (Object x : xs)
        {
            if (x != null)
            {
                try
                {
                    if (x instanceof Closeable)
                    {
                        ((Closeable) x).close();
                    }
                    else
                    {
                        Log.d(Const.LOG_TAG, "cannot close: " + x);
                        throw new RuntimeException("cannot close " + x);
                    }
                }
                catch (Throwable e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
