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

package bisq.desktop.main.portfolio.fund;

import bisq.desktop.common.model.ActivatableDataModel;

import bisq.core.offer.Offer;
import bisq.core.offer.OfferPayload;
import bisq.core.offer.OpenOfferManager;
import bisq.core.provider.price.PriceFeedService;
import bisq.core.trade.Trade;

import bisq.common.handlers.ErrorMessageHandler;
import bisq.common.handlers.FaultHandler;
import bisq.common.handlers.ResultHandler;

import com.google.inject.Inject;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import org.bouncycastle.crypto.params.KeyParameter;

import java.math.BigInteger;

import java.util.stream.Collectors;


import javax.annotation.Nullable;



import monero.wallet.MoneroWallet;
import monero.wallet.model.MoneroOutputWallet;
import monero.wallet.model.MoneroTxConfig;
import monero.wallet.model.MoneroTxWallet;

class FundDataModel extends ActivatableDataModel {
    private final OpenOfferManager openOfferManager;


    @Inject
    public FundDataModel(OpenOfferManager openOfferManager, PriceFeedService priceFeedService) {
        this.openOfferManager = openOfferManager;

    }

    public void onWithdrawRequest(String toAddress) {
        openOfferManager.doWithdrawal(toAddress);
    }

    public String getPrimaryAddress(){
        return openOfferManager.getPrimaryAddress();
    }
}
