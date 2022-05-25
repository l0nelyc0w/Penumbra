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

import bisq.desktop.common.model.ActivatableWithDataModel;
import bisq.desktop.common.model.ViewModel;
import bisq.desktop.main.PriceUtil;
import bisq.desktop.util.GUIUtil;

import bisq.core.offer.Offer;
import bisq.core.util.FormattingUtils;
import bisq.core.util.coin.CoinFormatter;

import bisq.network.p2p.P2PService;

import com.google.inject.Inject;

import javax.inject.Named;

import javafx.collections.ObservableList;


import java.math.BigInteger;



import monero.wallet.model.MoneroOutputWallet;
import monero.wallet.model.MoneroTxWallet;

class FundViewModel extends ActivatableWithDataModel<FundDataModel> implements ViewModel {
    private final P2PService p2PService;
    private final PriceUtil priceUtil;
    private final CoinFormatter btcFormatter;


    @Inject
    public FundViewModel(FundDataModel dataModel,
                         P2PService p2PService,
                         PriceUtil priceUtil,
                         @Named(FormattingUtils.BTC_FORMATTER_KEY) CoinFormatter btcFormatter) {
        super(dataModel);

        this.p2PService = p2PService;
        this.priceUtil = priceUtil;
        this.btcFormatter = btcFormatter;
    }

    @Override
    protected void activate() {
    }


    public void onWithdrawRequest(String toAddress){//, BigInteger amount) {
        //throw new RuntimeException("Withdraw trade funds after payout to Haveno wallet not supported");
        dataModel.onWithdrawRequest(toAddress);//, amount);
    }

    public String getPrimaryAddress(){
        return dataModel.getPrimaryAddress();
    }
}
