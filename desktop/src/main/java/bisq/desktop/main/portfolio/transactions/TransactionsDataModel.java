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

package bisq.desktop.main.portfolio.transactions;

import bisq.desktop.common.model.ActivatableDataModel;

import bisq.core.btc.model.XmrAddressEntry;
import bisq.core.offer.Offer;
import bisq.core.offer.OfferDirection;
import bisq.core.offer.OfferPayload;
import bisq.core.offer.OpenOfferManager;
import bisq.core.provider.price.PriceFeedService;

import bisq.common.handlers.ErrorMessageHandler;
import bisq.common.handlers.ResultHandler;

import com.google.inject.Inject;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;


import java.util.stream.Collectors;



import monero.wallet.model.MoneroOutputQuery;
import monero.wallet.model.MoneroOutputWallet;
import monero.wallet.model.MoneroTxWallet;

class TransactionsDataModel extends ActivatableDataModel {
    private final OpenOfferManager openOfferManager;
    private final PriceFeedService priceFeedService;

    private final ObservableList<MoneroTxWallet> list = FXCollections.observableArrayList();
    private final ListChangeListener<MoneroTxWallet> transactionsListChangeListener;
    //private final ChangeListener<Number> currenciesUpdateFlagPropertyListener;

    @Inject
    public TransactionsDataModel(OpenOfferManager openOfferManager, PriceFeedService priceFeedService) {
        this.openOfferManager = openOfferManager;
        this.priceFeedService = priceFeedService;


        transactionsListChangeListener = change -> applyList();
        openOfferManager.getTransactions().addListener(transactionsListChangeListener);
        applyList();
        //currenciesUpdateFlagPropertyListener = (observable, oldValue, newValue) -> applyList();
    }

    @Override
    protected void activate() {
        //list.addListener(pendingOfferListChangeListener);
        applyList();
    }

    void onRemovePendingOffer(Offer pendingOffer, ResultHandler resultHandler, ErrorMessageHandler errorMessageHandler) {
        //openOfferManager.removePendingOffer(pendingOffer, resultHandler, errorMessageHandler);
    }

    public ObservableList<MoneroOutputWallet> getFrozen() {
        return FXCollections.observableArrayList(openOfferManager.getFrozen());
    }

    public ObservableList<MoneroTxWallet> getList() {
        return list;
        //return openOfferManager.getXmrAddressEntryList();
    }

    public OfferDirection getDirection(Offer offer) {
        return openOfferManager.isMyOffer(offer) ? offer.getDirection() : offer.getMirroredDirection();
    }

    private void applyList() {
        list.clear();

        list.addAll(openOfferManager.getTransactions().stream().map(MoneroTxWallet::new).collect(Collectors.toList()));
        //list.addAll(openOfferManager.getTransactions());

        // we sort by date, earliest first
        //list.sort((o1, o2) -> o2.getOfferId().compareTo(o1.getOfferId()));
    }

//    boolean wasTriggered(Offer pendingOffer) {
//        return TriggerPriceService.wasTriggered(priceFeedService.getMarketPrice(pendingOffer.getCurrencyCode()), );
//    }
}
