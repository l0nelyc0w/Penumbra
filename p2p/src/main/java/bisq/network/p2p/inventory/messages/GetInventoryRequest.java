/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.network.p2p.inventory.messages;


import bisq.common.app.Version;
import bisq.common.proto.network.NetworkEnvelope;

import lombok.Value;

@Value
public class GetInventoryRequest extends NetworkEnvelope {
    private final String version;

    public GetInventoryRequest(String version) {
        this(version, Version.getP2PMessageVersion());
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // PROTO BUFFER
    ///////////////////////////////////////////////////////////////////////////////////////////

    private GetInventoryRequest(String version, int messageVersion) {
        super(messageVersion);

        this.version = version;
    }

    @Override
    public protobuf.NetworkEnvelope toProtoNetworkEnvelope() {
        return getNetworkEnvelopeBuilder()
                .setGetInventoryRequest(protobuf.GetInventoryRequest.newBuilder()
                        .setVersion(version))
                .build();
    }

    public static GetInventoryRequest fromProto(protobuf.GetInventoryRequest proto, int messageVersion) {
        return new GetInventoryRequest(proto.getVersion(), messageVersion);
    }
}