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

package bisq.core.offer;

import bisq.core.btc.TxFeeEstimationService;
import bisq.core.btc.wallet.Restrictions;
import bisq.core.btc.wallet.XmrWalletService;
import bisq.core.locale.CurrencyUtil;
import bisq.core.locale.Res;
import bisq.core.monetary.Price;
import bisq.core.offer.availability.DisputeAgentSelection;
import bisq.core.payment.PaymentAccount;
import bisq.core.payment.PaymentAccountUtil;
import bisq.core.provider.price.MarketPrice;
import bisq.core.provider.price.PriceFeedService;
import bisq.core.support.dispute.arbitration.arbitrator.Arbitrator;
import bisq.core.support.dispute.arbitration.arbitrator.ArbitratorManager;
import bisq.core.trade.statistics.TradeStatisticsManager;
import bisq.core.user.Preferences;
import bisq.core.user.User;
import bisq.core.util.coin.CoinUtil;

import bisq.network.p2p.NodeAddress;
import bisq.network.p2p.P2PService;

import bisq.common.app.Version;
import bisq.common.crypto.PubKeyRingProvider;
import bisq.common.util.Tuple2;
import bisq.common.util.Utilities;

import org.bitcoinj.core.Coin;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

import static bisq.core.payment.payload.PaymentMethod.HAL_CASH_ID;

@Slf4j
@Singleton
public class CreateOfferService {
    private final OfferUtil offerUtil;
    private final TxFeeEstimationService txFeeEstimationService;
    private final PriceFeedService priceFeedService;
    private final P2PService p2PService;
    private final PubKeyRingProvider pubKeyRingProvider;
    private final User user;
    private final XmrWalletService xmrWalletService;
    private final TradeStatisticsManager tradeStatisticsManager;
    private final ArbitratorManager arbitratorManager;


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Constructor, Initialization
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    public CreateOfferService(OfferUtil offerUtil,
                              TxFeeEstimationService txFeeEstimationService,
                              PriceFeedService priceFeedService,
                              P2PService p2PService,
                              PubKeyRingProvider pubKeyRingProvider,
                              User user,
                              XmrWalletService xmrWalletService,
                              TradeStatisticsManager tradeStatisticsManager,
                              ArbitratorManager arbitratorManager) {
        this.offerUtil = offerUtil;
        this.txFeeEstimationService = txFeeEstimationService;
        this.priceFeedService = priceFeedService;
        this.p2PService = p2PService;
        this.pubKeyRingProvider = pubKeyRingProvider;
        this.user = user;
        this.xmrWalletService = xmrWalletService;
        this.tradeStatisticsManager = tradeStatisticsManager;
        this.arbitratorManager = arbitratorManager;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // API
    ///////////////////////////////////////////////////////////////////////////////////////////

    public String getRandomOfferId() {
        return Utilities.getRandomPrefix(5, 8) + "-" +
                UUID.randomUUID().toString() + "-" +
                Version.VERSION.replace(".", "");
    }

    public Offer createAndGetOffer(String offerId,
                                   OfferDirection direction,
                                   String currencyCode,
                                   Coin amount,
                                   Coin minAmount,
                                   Price price,
                                   Coin txFee,
                                   boolean useMarketBasedPrice,
                                   double marketPriceMargin,
                                   double buyerSecurityDepositAsDouble,
                                   PaymentAccount paymentAccount) {

        log.info("create and get offer with offerId={}, \n" +
                        "currencyCode={}, \n" +
                        "direction={}, \n" +
                        "price={}, \n" +
                        "useMarketBasedPrice={}, \n" +
                        "marketPriceMargin={}, \n" +
                        "amount={}, \n" +
                        "minAmount={}, \n" +
                        "buyerSecurityDeposit={}",
                offerId,
                currencyCode,
                direction,
                price.getValue(),
                useMarketBasedPrice,
                marketPriceMargin,
                amount.value,
                minAmount.value,
                buyerSecurityDepositAsDouble);

        long creationTime = new Date().getTime();
        NodeAddress makerAddress = p2PService.getAddress();
        boolean useMarketBasedPriceValue = useMarketBasedPrice &&
                isMarketPriceAvailable(currencyCode) &&
                !paymentAccount.hasPaymentMethodWithId(HAL_CASH_ID);

        long priceAsLong = price != null && !useMarketBasedPriceValue ? price.getValue() : 0L;
        double marketPriceMarginParam = useMarketBasedPriceValue ? marketPriceMargin : 0;
        long amountAsLong = amount != null ? amount.getValue() : 0L;
        long minAmountAsLong = minAmount != null ? minAmount.getValue() : 0L;
        boolean isCryptoCurrency = CurrencyUtil.isCryptoCurrency(currencyCode);
        String baseCurrencyCode = isCryptoCurrency ? currencyCode : Res.getBaseCurrencyCode();
        String counterCurrencyCode = isCryptoCurrency ? Res.getBaseCurrencyCode() : currencyCode;
        List<NodeAddress> acceptedArbitratorAddresses = user.getAcceptedArbitratorAddresses();
        ArrayList<NodeAddress> arbitratorNodeAddresses = acceptedArbitratorAddresses != null ?
                Lists.newArrayList(acceptedArbitratorAddresses) :
                new ArrayList<>();
        List<NodeAddress> acceptedMediatorAddresses = user.getAcceptedMediatorAddresses();
        ArrayList<NodeAddress> mediatorNodeAddresses = acceptedMediatorAddresses != null ?
                Lists.newArrayList(acceptedMediatorAddresses) :
                new ArrayList<>();
        String countryCode = PaymentAccountUtil.getCountryCode(paymentAccount);
        List<String> acceptedCountryCodes = PaymentAccountUtil.getAcceptedCountryCodes(paymentAccount);
        String bankId = PaymentAccountUtil.getBankId(paymentAccount);
        List<String> acceptedBanks = PaymentAccountUtil.getAcceptedBanks(paymentAccount);
        double sellerSecurityDeposit = getSellerSecurityDepositAsDouble(buyerSecurityDepositAsDouble);
        Coin makerFeeAsCoin = offerUtil.getMakerFee(amount);
        Coin buyerSecurityDepositAsCoin = getBuyerSecurityDeposit(amount, buyerSecurityDepositAsDouble);
        Coin sellerSecurityDepositAsCoin = getSellerSecurityDeposit(amount, sellerSecurityDeposit);
        long maxTradeLimit = offerUtil.getMaxTradeLimit(paymentAccount, currencyCode, direction);
        long maxTradePeriod = paymentAccount.getMaxTradePeriod();

        // reserved for future use cases
        // Use null values if not set
        boolean isPrivateOffer = false;
        boolean useAutoClose = false;
        boolean useReOpenAfterAutoClose = false;
        long lowerClosePrice = 0;
        long upperClosePrice = 0;
        String hashOfChallenge = null;
        Map<String, String> extraDataMap = offerUtil.getExtraDataMap(paymentAccount,
                currencyCode,
                direction);

        offerUtil.validateOfferData(
                buyerSecurityDepositAsDouble,
                paymentAccount,
                currencyCode,
                makerFeeAsCoin);

        // select signing arbitrator
        Arbitrator arbitrator = DisputeAgentSelection.getLeastUsedArbitrator(tradeStatisticsManager, arbitratorManager);
        if (arbitrator == null) throw new RuntimeException("No arbitrators available");

        OfferPayload offerPayload = new OfferPayload(offerId,
                creationTime,
                makerAddress,
                pubKeyRingProvider.get(),
                OfferDirection.valueOf(direction.name()),
                priceAsLong,
                marketPriceMarginParam,
                useMarketBasedPriceValue,
                amountAsLong,
                minAmountAsLong,
                baseCurrencyCode,
                counterCurrencyCode,
                paymentAccount.getPaymentMethod().getId(),
                paymentAccount.getId(),
                null,
                countryCode,
                acceptedCountryCodes,
                bankId,
                acceptedBanks,
                Version.VERSION,
                xmrWalletService.getWallet().getHeight(),
                0, // todo: remove txFeeToUse from data model
                makerFeeAsCoin.value,
                buyerSecurityDepositAsCoin.value,
                sellerSecurityDepositAsCoin.value,
                maxTradeLimit,
                maxTradePeriod,
                useAutoClose,
                useReOpenAfterAutoClose,
                upperClosePrice,
                lowerClosePrice,
                isPrivateOffer,
                hashOfChallenge,
                extraDataMap,
                Version.TRADE_PROTOCOL_VERSION,
                arbitrator.getNodeAddress(),
                null,
                null);
        Offer offer = new Offer(offerPayload);
        offer.setPriceFeedService(priceFeedService);
        return offer;
    }

    public Tuple2<Coin, Integer> getEstimatedFeeAndTxVsize(Coin amount,
                                                           OfferDirection direction,
                                                           double buyerSecurityDeposit,
                                                           double sellerSecurityDeposit) {
        Coin reservedFundsForOffer = getReservedFundsForOffer(direction,
                amount,
                buyerSecurityDeposit,
                sellerSecurityDeposit);
        return txFeeEstimationService.getEstimatedFeeAndTxVsizeForMaker(reservedFundsForOffer,
                offerUtil.getMakerFee(amount));
    }

    public Coin getReservedFundsForOffer(OfferDirection direction,
                                         Coin amount,
                                         double buyerSecurityDeposit,
                                         double sellerSecurityDeposit) {

        Coin reservedFundsForOffer = getSecurityDeposit(direction,
                amount,
                buyerSecurityDeposit,
                sellerSecurityDeposit);
        if (!offerUtil.isBuyOffer(direction))
            reservedFundsForOffer = reservedFundsForOffer.add(amount);

        return reservedFundsForOffer;
    }

    public Coin getSecurityDeposit(OfferDirection direction,
                                   Coin amount,
                                   double buyerSecurityDeposit,
                                   double sellerSecurityDeposit) {
        return offerUtil.isBuyOffer(direction) ?
                getBuyerSecurityDeposit(amount, buyerSecurityDeposit) :
                getSellerSecurityDeposit(amount, sellerSecurityDeposit);
    }

    public double getSellerSecurityDepositAsDouble(double buyerSecurityDeposit) {
        return Preferences.USE_SYMMETRIC_SECURITY_DEPOSIT ? buyerSecurityDeposit :
                Restrictions.getSellerSecurityDepositAsPercent();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // Private
    ///////////////////////////////////////////////////////////////////////////////////////////

    private boolean isMarketPriceAvailable(String currencyCode) {
        MarketPrice marketPrice = priceFeedService.getMarketPrice(currencyCode);
        return marketPrice != null && marketPrice.isExternallyProvidedPrice();
    }

    private Coin getBuyerSecurityDeposit(Coin amount, double buyerSecurityDeposit) {
        Coin percentOfAmountAsCoin = CoinUtil.getPercentOfAmountAsCoin(buyerSecurityDeposit, amount);
        return getBoundedBuyerSecurityDeposit(percentOfAmountAsCoin);
    }

    private Coin getSellerSecurityDeposit(Coin amount, double sellerSecurityDeposit) {
        Coin amountAsCoin = (amount == null) ? Coin.ZERO : amount;
        Coin percentOfAmountAsCoin = CoinUtil.getPercentOfAmountAsCoin(sellerSecurityDeposit, amountAsCoin);
        return getBoundedSellerSecurityDeposit(percentOfAmountAsCoin);
    }

    private Coin getBoundedBuyerSecurityDeposit(Coin value) {
        // We need to ensure that for small amount values we don't get a too low BTC amount. We limit it with using the
        // MinBuyerSecurityDepositAsCoin from Restrictions.
        return Coin.valueOf(Math.max(Restrictions.getMinBuyerSecurityDepositAsCoin().value, value.value));
    }

    private Coin getBoundedSellerSecurityDeposit(Coin value) {
        // We need to ensure that for small amount values we don't get a too low BTC amount. We limit it with using the
        // MinSellerSecurityDepositAsCoin from Restrictions.
        return Coin.valueOf(Math.max(Restrictions.getMinSellerSecurityDepositAsCoin().value, value.value));
    }
}
