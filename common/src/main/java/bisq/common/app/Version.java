/*
 * This file is part of Penumbra.
 *
 * Penumbra is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Penumbra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Penumbra. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.common.app;

import java.util.Arrays;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import static com.google.common.base.Preconditions.checkArgument;

@Slf4j
public class Version {
    // The application versions
    // VERSION = 0.5.0 introduces proto buffer for the P2P network and local DB and is a not backward compatible update
    // Therefore all sub versions start again with 1
    // We use semantic versioning with major, minor and patch
    public static final String VERSION = "0.2";
    public static final String FLAVOR = "Penumbra";

    /**
     * Holds a list of the tagged resource files for optimizing the getData requests.
     * This must not contain each version but only those where we add new version-tagged resource files for
     * historical data stores.
     */
    public static final List<String> HISTORICAL_RESOURCE_FILE_VERSION_TAGS = Arrays.asList("0.1");

    public static String getVersion() { return VERSION; }


    public static boolean isNewVersion(String newVersion) {
        return isNewVersion(newVersion, VERSION);
    }

    public static boolean isNewVersion(String newVersion, String currentVersion) {
        if (newVersion.equals(currentVersion))
             return false;
        /*
        else if (getVersion() > currentVersion)
            return true;
        else if (getVersion() < currentVersion)
            return false;
         */
        else
            return false;
    }

    private static int getSubVersion(String version, int index) {
        final String[] split = version.split("\\.");
        checkArgument(split.length == 3, "Version number must be in semantic version format (contain 2 '.'). version=" + version);
        return Integer.parseInt(split[index]);
    }

    // The version no. for the objects sent over the network. A change will break the serialization of old objects.
    // If objects are used for both network and database the network version is applied.
    // VERSION = 0.5.0 -> P2P_NETWORK_VERSION = 1
    // With version 1.2.2 we change to version 2 (new trade protocol)
    public static final String P2P_NETWORK_VERSION = "A";

    // The version no. of the serialized data stored to disc. A change will break the serialization of old objects.
    // VERSION = 0.5.0 -> LOCAL_DB_VERSION = 1
    public static final int LOCAL_DB_VERSION = 1;

    // The version no. of the current protocol. The offer holds that version.
    // A taker will check the version of the offers to see if his version is compatible.
    // For the switch to version 2, offers created with the old version will become invalid and have to be canceled.
    // For the switch to version 3, offers created with the old version can be migrated to version 3 just by opening
    // the Bisq app.
    // VERSION = 0.5.0 -> TRADE_PROTOCOL_VERSION = 1
    // Version 1.2.2 -> TRADE_PROTOCOL_VERSION = 2
    // Version 1.5.0 -> TRADE_PROTOCOL_VERSION = 3
    public static final int TRADE_PROTOCOL_VERSION = 3;
    private static String p2pMessageVersion;

    public static String getP2PMessageVersion() {
        return p2pMessageVersion;
    }

    // The version for the crypto network (XMR_Mainnet = 0, XMR_LOCAL = 1, XMR_Regtest = 2, ...)
    private static int BASE_CURRENCY_NETWORK;

    public static void setBaseCryptoNetworkId(int baseCryptoNetworkId) {
        BASE_CURRENCY_NETWORK = baseCryptoNetworkId;

        // CRYPTO_NETWORK_ID is ordinal of enum. We use for changes at NETWORK_PROTOCOL_VERSION a multiplication with 10
        // to not mix up networks:
        if (BASE_CURRENCY_NETWORK == 0)
            p2pMessageVersion =  "0" + P2P_NETWORK_VERSION;
        if (BASE_CURRENCY_NETWORK == 1)
            p2pMessageVersion =  "1" + P2P_NETWORK_VERSION;
        if (BASE_CURRENCY_NETWORK == 2)
            p2pMessageVersion =  "2" + P2P_NETWORK_VERSION;
    }

    public static int getBaseCurrencyNetwork() {
        return BASE_CURRENCY_NETWORK;
    }

    public static void printVersion() {
        log.info("Version{" +
                "VERSION=" + VERSION +
                ", FLAVOR=" + FLAVOR +
                ", P2P_NETWORK_VERSION=" + P2P_NETWORK_VERSION +
                ", LOCAL_DB_VERSION=" + LOCAL_DB_VERSION +
                ", TRADE_PROTOCOL_VERSION=" + TRADE_PROTOCOL_VERSION +
                ", BASE_CURRENCY_NETWORK=" + BASE_CURRENCY_NETWORK +
                ", getP2PNetworkId()=" + getP2PMessageVersion() +
                '}');
    }

    public static String getVersionFlavor() {
        return FLAVOR;
    }
}
