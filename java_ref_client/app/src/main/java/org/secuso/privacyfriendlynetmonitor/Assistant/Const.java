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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Storing constant values for various usages within the app
 */

public interface Const
{

    //App constants
    boolean IS_DEBUG = false;
    String LOG_TAG = "NetMonitor";
    String FILE_IF_LIST = "iflist";

    //SSL LABS CONSTANTS
    String SSLLABS_URL = "https://www.ssllabs.com/ssltest/analyze.html?d=";

    //Detector constants
    long REPORT_TTL_DEFAULT = 10000;
    Integer[] TLS_PORT_VALUES = new Integer[]{993, 443, 995, 995, 614, 465, 587, 22};
    Set<Integer> TLS_PORTS = new HashSet<>(Arrays.asList(TLS_PORT_VALUES));
    Integer[] INCONCLUSIVE_PORT_VALUES = new Integer[]{25, 110, 143};
    Set<Integer> INCONCUSIVE_PORTS = new HashSet<>(Arrays.asList(INCONCLUSIVE_PORT_VALUES));
    Integer[] UNSECURE_PORT_VALUES = new Integer[]{21, 23, 80, 109, 137, 138, 139, 161, 992};
    Set<Integer> UNSECURE_PORTS = new HashSet<>(Arrays.asList(UNSECURE_PORT_VALUES));

    //String Builder Constants
    String STATUS_TLS = "Encrypted";
    String STATUS_UNSECURE = "Unencrypted";
    String STATUS_INCONCLUSIVE = "Inconclusive";
    String STATUS_UNKNOWN = "Unknown";

    //SharedPrefs identifiers
    String REPORT_TTL = "REPORT_TTL";
    String IS_DETAIL_MODE = "IS_DETAIL_MODE";
    String IS_FIRST_START = "IS_FIRST_START";
    String IS_LOG = "IS_LOG";
    String IS_CERTVAL = "IS_CERTVAL";
    String PREF_NAME = "PREF_NAME";

}
