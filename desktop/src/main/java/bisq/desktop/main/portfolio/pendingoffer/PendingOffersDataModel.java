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

package bisq.desktop.main.portfolio.pendingoffer;

import bisq.desktop.common.model.ActivatableDataModel;
import bisq.desktop.main.portfolio.pendingoffer.PendingOfferListItem;

import bisq.core.btc.model.XmrAddressEntry;
import bisq.core.btc.model.XmrAddressEntryList;
import bisq.core.offer.Offer;
import bisq.core.offer.OfferPayload;
import bisq.core.offer.OpenOffer;
import bisq.core.offer.OpenOfferManager;
import bisq.core.offer.TriggerPriceService;
import bisq.core.provider.price.PriceFeedService;

import bisq.common.handlers.ErrorMessageHandler;
import bisq.common.handlers.ResultHandler;

import com.google.inject.Inject;

import javafx.beans.value.ChangeListener;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.stream.Collectors;

class PendingOffersDataModel extends ActivatableDataModel {
    private final OpenOfferManager openOfferManager;
    private final PriceFeedService priceFeedService;

    private final ObservableList<XmrAddressEntry> list = FXCollections.observableArrayList();
    private final ListChangeListener<XmrAddressEntry> pendingOfferListChangeListener;
    //private final ChangeListener<Number> currenciesUpdateFlagPropertyListener;

    @Inject
    public PendingOffersDataModel(OpenOfferManager openOfferManager, PriceFeedService priceFeedService) {
        this.openOfferManager = openOfferManager;
        this.priceFeedService = priceFeedService;

        pendingOfferListChangeListener = change -> applyList();
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

    public ObservableList<XmrAddressEntry> getList() {
        return list;
        //return openOfferManager.getXmrAddressEntryList();
    }

    public OfferPayload.Direction getDirection(Offer offer) {
        return openOfferManager.isMyOffer(offer) ? offer.getDirection() : offer.getMirroredDirection();
    }

    private void applyList() {
        list.clear();

        //list.addAll(openOfferManager.getObservableList().stream().map(PendingOfferListItem::new).collect(Collectors.toList()));
        list.addAll(openOfferManager.getXmrAddressEntryList());

        // we sort by date, earliest first
        //list.sort((o1, o2) -> o2.getOfferId().compareTo(o1.getOfferId()));
    }

//    boolean wasTriggered(Offer pendingOffer) {
//        return TriggerPriceService.wasTriggered(priceFeedService.getMarketPrice(pendingOffer.getCurrencyCode()), );
//    }
}
