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

package com.zoffcc.applications.trifa;

public class MainActivity
{
    private static final String TAG = "trifa.MainActivity";
    // --------- global config ---------
    // --------- global config ---------
    // --------- global config ---------
    final static boolean CTOXCORE_NATIVE_LOGGING = true; // set "false" for release builds
    final static boolean ORMA_TRACE = false; // set "false" for release builds
    final static boolean DB_ENCRYPT = true; // set "true" always!
    final static boolean VFS_ENCRYPT = true; // set "true" always!
    // --------- global config ---------
    // --------- global config ---------
    // --------- global config ---------

	public static void main(String[] args)
	{
		// Prints "Hello, World" in the terminal window.
      		System.out.println("Hello, World");
	}

	static
	{
		try
		{
    			System.loadLibrary("jni-c-toxcore");
		}
		catch (UnsatisfiedLinkError e)
		{
      			System.err.println("Native code library failed to load.\n" + e);
			System.exit(1);
		}
	}
}

