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

import bisq.common.config.Config;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DevEnv {

    // The UI got set the private dev key so the developer does not need to do anything and can test those features.
    // Features: Arbitration registration (alt+R at account), Alert/Update (alt+m), private message to a
    // peer (click user icon and alt+r), filter/block offers by various data like offer ID (cmd + f).
    // The user can set a program argument to ignore all of those privileged network_messages. They are intended for
    // emergency cases only (beside update message and arbitrator registration).
    public static final String DEV_PRIVILEGE_PUB_KEY = "cd293be6cea034bd45a0352775a219ef5dc7825ce55d1f7dae9762d80ce64411";
    public static final String DEV_PRIVILEGE_PRIV_KEY = "bab72d760d4f979d30624f228a6a62837bd9be2530d5e434a0a3d996f8889d41";

    public static void setup(Config config) {
        DevEnv.setDevMode(config.useDevMode);
    }

    // If set to true we ignore several UI behavior like confirmation popups as well dummy accounts are created and
    // offers are filled with default values. Intended to make dev testing faster.
    private static boolean devMode = false;

    public static boolean isDevMode() {
        return devMode;
    }

    public static void setDevMode(boolean devMode) {
        DevEnv.devMode = devMode;
    }

    public static void logErrorAndThrowIfDevMode(String msg) {
        log.error(msg);
        if (devMode)
            throw new RuntimeException(msg);
    }

}
