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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VersionTest {

    @Test
    public void testVersionNumber() {
        assertEquals("0.2", Version.getVersion());
    }
/*
    @Test
    public void testIsNewVersion() {
        assertFalse(Version.isNewVersion("0.0.0", "0.0.0"));
        assertTrue(Version.isNewVersion("0.1.0", "0.0.0"));
        assertTrue(Version.isNewVersion("0.0.1", "0.0.0"));
        assertTrue(Version.isNewVersion("1.0.0", "0.0.0"));
        assertTrue(Version.isNewVersion("0.5.1", "0.5.0"));
        assertFalse(Version.isNewVersion("0.5.0", "0.5.1"));
        assertTrue(Version.isNewVersion("0.6.0", "0.5.0"));
        assertTrue(Version.isNewVersion("0.6.0", "0.5.1"));
        assertFalse(Version.isNewVersion("0.5.0", "1.5.0"));
        assertFalse(Version.isNewVersion("0.4.9", "0.5.0"));
    }

 */
}
