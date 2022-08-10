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

import bisq.desktop.common.model.ActivatableWithDataModel;
import bisq.desktop.common.model.ViewModel;
import bisq.desktop.main.portfolio.pendingoffer.PendingOfferListItem;
import bisq.desktop.main.portfolio.pendingoffer.PendingOffersDataModel;
import bisq.desktop.util.DisplayUtils;
import bisq.desktop.util.GUIUtil;

import bisq.core.btc.model.XmrAddressEntry;
import bisq.core.locale.CurrencyUtil;
import bisq.core.locale.Res;
import bisq.core.monetary.Price;
import bisq.core.offer.Offer;
import bisq.core.offer.OpenOffer;
import bisq.core.util.FormattingUtils;
import bisq.core.util.coin.CoinFormatter;

import bisq.network.p2p.P2PService;

import bisq.common.handlers.ErrorMessageHandler;
import bisq.common.handlers.ResultHandler;

import com.google.inject.Inject;

import javax.inject.Named;

import javafx.collections.ObservableList;

import static com.google.common.base.Preconditions.checkNotNull;

class PendingOffersViewModel extends ActivatableWithDataModel<PendingOffersDataModel> implements ViewModel {
    private final P2PService p2PService;
    private final CoinFormatter btcFormatter;


    @Inject
    public PendingOffersViewModel(PendingOffersDataModel dataModel,
                                  P2PService p2PService,
                                  @Named(FormattingUtils.BTC_FORMATTER_KEY) CoinFormatter btcFormatter) {
        super(dataModel);

        this.p2PService = p2PService;
        this.btcFormatter = btcFormatter;
    }

    @Override
    protected void activate() {
    }
//
//    void onActivateOpenOffer(OpenOffer openOffer,
//                             ResultHandler resultHandler,
//                             ErrorMessageHandler errorMessageHandler) {
//        dataModel.onActivateOpenOffer(openOffer, resultHandler, errorMessageHandler);
//    }
//
//    void onDeactivateOpenOffer(OpenOffer openOffer,
//                               ResultHandler resultHandler,
//                               ErrorMessageHandler errorMessageHandler) {
//        dataModel.onDeactivateOpenOffer(openOffer, resultHandler, errorMessageHandler);
//    }

    void onRemovePendingOffer(Offer pendingOffer, ResultHandler resultHandler, ErrorMessageHandler errorMessageHandler) {
        dataModel.onRemovePendingOffer(pendingOffer, resultHandler, errorMessageHandler);
    }

    public ObservableList<XmrAddressEntry> getList() {
        return dataModel.getList();
    }

    String getOfferId(XmrAddressEntry item) {
        return item.getOfferId();
    }

//    String getAmount(XmrAddressEntry item) {
//        return (item != null) ? DisplayUtils.formatAmount(item.getOffer(), btcFormatter) : "";
//    }
//
//    String getPrice(XmrAddressEntry item) {
//        if ((item == null))
//            return "";
//
//        Offer offer = item.getOffer();
//        Price price = offer.getPrice();
//        if (price != null) {
//            return FormattingUtils.formatPrice(price);
//        } else {
//            return Res.get("shared.na");
//        }
//    }

    String getMarketLabel(PendingOfferListItem item) {
        if ((item == null))
            return "";

        return CurrencyUtil.getCurrencyPair(item.getOffer().getCurrencyCode());
    }

    String getPaymentMethod(PendingOfferListItem item) {
        String result = "";
        if (item != null) {
            Offer offer = item.getOffer();
            checkNotNull(offer);
            checkNotNull(offer.getPaymentMethod());
            result = offer.getPaymentMethodNameWithCountryCode();
        }
        return result;
    }

    String getDate(PendingOfferListItem item) {
        return DisplayUtils.formatDateTime(item.getOffer().getDate());
    }

//    boolean isDeactivated(PendingOfferListItem item) {
//        return item != null && item.getOpenOffer() != null && item.getOpenOffer().isDeactivated();
//    }

    boolean isBootstrappedOrShowPopup() {
        return GUIUtil.isBootstrappedOrShowPopup(p2PService);
    }

    public String getMakerFeeAsString(Offer pendingOffer) {
        Offer offer = pendingOffer;
        return btcFormatter.formatCoinWithCode(offer.getMakerFee());
    }

//    String getTriggerPrice(PendingOfferListItem item) {
//        if ((item == null)) {
//            return "";
//        }
//
//        Offer offer = item.getOffer();
//        long triggerPrice = item.getOpenOffer().getTriggerPrice();
//        if (!offer.isUseMarketBasedPrice() || triggerPrice <= 0) {
//            return Res.get("shared.na");
//        } else {
//            return PriceUtil.formatMarketPrice(triggerPrice, offer.getCurrencyCode());
//        }
//    }
}
