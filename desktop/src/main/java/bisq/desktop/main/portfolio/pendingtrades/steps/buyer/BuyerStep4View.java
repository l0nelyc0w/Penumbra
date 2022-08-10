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

package bisq.desktop.main.portfolio.pendingtrades.steps.buyer;

import bisq.desktop.components.AutoTooltipButton;
import bisq.desktop.components.TitledGroupBg;
import bisq.desktop.main.MainView;
import bisq.desktop.main.overlays.notifications.Notification;
import bisq.desktop.main.overlays.popups.Popup;
import bisq.desktop.main.overlays.windows.TradeFeedbackWindow;
import bisq.desktop.main.portfolio.PortfolioView;
import bisq.desktop.main.portfolio.closedtrades.ClosedTradesView;
import bisq.desktop.main.portfolio.pendingtrades.PendingTradesViewModel;
import bisq.desktop.main.portfolio.pendingtrades.steps.TradeStepView;
import bisq.desktop.util.Layout;

import bisq.core.btc.model.XmrAddressEntry;
import bisq.core.btc.wallet.XmrWalletService;
import bisq.core.locale.Res;
import bisq.core.payment.validation.AltCoinAddressValidator;
import bisq.core.trade.TradeUtils;
import bisq.core.util.ParsingUtils;
import bisq.core.trade.txproof.AssetTxProofResult;
import bisq.core.user.DontShowAgainLookup;
import bisq.core.util.coin.CoinUtil;


import bisq.asset.AssetRegistry;

import bisq.common.UserThread;
import bisq.common.app.DevEnv;

import com.jfoenix.controls.JFXBadge;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

import org.bouncycastle.crypto.params.KeyParameter;

import java.math.BigInteger;

import java.util.concurrent.TimeUnit;

import static bisq.desktop.util.FormBuilder.addCompactTopLabelTextField;



import monero.wallet.MoneroWallet;
import monero.wallet.model.MoneroTransfer;
import monero.wallet.model.MoneroTxConfig;
import monero.wallet.model.MoneroTxWallet;

public class BuyerStep4View extends TradeStepView {

    private Button closeButton;

    ///////////////////////////////////////////////////////////////////////////////////////////
    // Constructor, Initialisation
    ///////////////////////////////////////////////////////////////////////////////////////////

    public BuyerStep4View(PendingTradesViewModel model) {
        super(model);
    }

    @Override
    public void activate() {
        super.activate();
        // Don't display any trade step info when trade is complete
        hideTradeStepInfo();
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Content
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void addContent() {
        gridPane.getColumnConstraints().get(1).setHgrow(Priority.SOMETIMES);

        TitledGroupBg completedTradeLabel = new TitledGroupBg();
        if (trade.getDisputeState().isMediated()) {
            completedTradeLabel.setText(Res.get("portfolio.pending.step5_buyer.groupTitle.mediated"));
        } else if (trade.getDisputeState().isArbitrated()) {
            completedTradeLabel.setText(Res.get("portfolio.pending.step5_buyer.groupTitle.arbitrated"));
        } else {
            completedTradeLabel.setText(Res.get("portfolio.pending.step5_buyer.groupTitle"));
        }
        JFXBadge autoConfBadge = new JFXBadge(new Label(""), Pos.BASELINE_RIGHT);
        autoConfBadge.setText(Res.get("portfolio.pending.autoConf"));
        autoConfBadge.getStyleClass().add("auto-conf");

        HBox hBox2 = new HBox(1, completedTradeLabel, autoConfBadge);
        GridPane.setMargin(hBox2, new Insets(18, -10, -12, -10));
        gridPane.getChildren().add(hBox2);
        GridPane.setRowSpan(hBox2, 5);
        autoConfBadge.setVisible(AssetTxProofResult.COMPLETED == trade.getAssetTxProofResult());

        if (trade.getDisputeState().isNotDisputed()) {
            addCompactTopLabelTextField(gridPane, gridRow, getBtcTradeAmountLabel(), model.getTradeVolume(), Layout.TWICE_FIRST_ROW_DISTANCE);
            addCompactTopLabelTextField(gridPane, ++gridRow, getFiatTradeAmountLabel(), model.getFiatVolume());
            addCompactTopLabelTextField(gridPane, ++gridRow, Res.get("portfolio.pending.step5_buyer.refunded"), model.getSecurityDeposit());
            addCompactTopLabelTextField(gridPane, ++gridRow, Res.get("portfolio.pending.step5_buyer.tradeFee"), model.getTradeFee());
            final String miningFee = model.dataModel.isMaker() ?
                    Res.get("portfolio.pending.step5_buyer.makersMiningFee") :
                    Res.get("portfolio.pending.step5_buyer.takersMiningFee");
            addCompactTopLabelTextField(gridPane, ++gridRow, miningFee, model.getTxFee());
        }

        closeButton = new AutoTooltipButton(Res.get("shared.close"));
        closeButton.setDefaultButton(true);
        closeButton.getStyleClass().add("action-button");
        GridPane.setRowIndex(closeButton, ++gridRow);
        GridPane.setMargin(closeButton, new Insets(Layout.GROUP_DISTANCE, 10, 0, 0));
        gridPane.getChildren().add(closeButton);

        //lolen, menu keep/withdraw actions
        useSavingsWalletButton.setOnAction(e -> {
            handleTradeCompleted();
            model.dataModel.tradeManager.onTradeCompleted(trade);
        });

        String key = "tradeCompleted" + trade.getId();
        if (!DevEnv.isDevMode() && DontShowAgainLookup.showAgain(key)) {
            DontShowAgainLookup.dontShowAgain(key, true);
            new Notification().headLine(Res.get("notification.tradeCompleted.headline"))
                    .notification(Res.get("notification.tradeCompleted.msg"))
                    .autoClose()
                    .show();
        }
    }

    private void onWithdrawal() {
        withdrawAddressTextField.setManaged(true);
        withdrawAddressTextField.setVisible(true);
        withdrawMemoTextField.setManaged(true);
        withdrawMemoTextField.setVisible(true);
        GridPane.setRowSpan(withdrawTitledGroupBg, 3);
        withdrawToExternalWalletButton.setDefaultButton(true);
        useSavingsWalletButton.setDefaultButton(false);
        withdrawToExternalWalletButton.getStyleClass().add("action-button");
        useSavingsWalletButton.getStyleClass().remove("action-button");

        withdrawToExternalWalletButton.setOnAction(e -> {
            if (model.dataModel.isReadyForTxBroadcast()) {
                reviewWithdrawal();
            }
        });

    }

    private void reviewWithdrawal() {
      //throw new RuntimeException("BuyerStep4View.reviewWithdrawal() not yet updated for XMR");
        Coin amount_coin = trade.getPayoutAmount();
        BigInteger amount = ParsingUtils.coinToAtomicUnits(trade.getPayoutAmount());
        XmrWalletService walletService = model.dataModel.xmrWalletService;

        XmrAddressEntry fromAddressesEntry = walletService.getOrCreateAddressEntry(trade.getId(), XmrAddressEntry.Context.TRADE_PAYOUT);
        String fromAddresses = fromAddressesEntry.getAddressString();
        String toAddresses = withdrawAddressTextField.getText();
        AltCoinAddressValidator xmr_validator = new AltCoinAddressValidator(new AssetRegistry());
        xmr_validator.setCurrencyCode("XMR");
        Coin balance = walletService.getAvailableConfirmedBalance();
        if (true){//xmr_validator.validate(toAddresses).isValid){
           // Coin balance = walletService.getAvailableConfirmedBalance();
            try {
                MoneroWallet wallet = walletService.getWallet();
                MoneroTxWallet withdraw_tx = wallet.createTx(new MoneroTxConfig()
                        .setAccountIndex(0)
                        .addDestination(toAddresses, amount));
                //.setRelay(true));
                BigInteger miningFee = withdraw_tx.getFee();
                //GETS-FEE
//                MoneroTransfer Transaction feeEstimationTransaction = walletService.g .getFeeEstimationTransaction(fromAddresses, toAddresses, amount, AddressEntry.Context.TRADE_PAYOUT);
//                Coin fee = feeEstimationTransaction.getFee();
//                Coin receiverAmount = amount.subtract(fee);
//                if (balance.isZero()) {
//                    new Popup().warning(Res.get("portfolio.pending.step5_buyer.alreadyWithdrawn")).show();
//                    model.dataModel.tradeManager.onTradeCompleted(trade);
                //ALREADY-DONE
//                } else {
//                    if (toAddresses.isEmpty()) {
//                        validateWithdrawAddress();
                //NO-ADDRESS
//                    } else if (Restrictions.isAboveDust(receiverAmount)) {
//                        CoinFormatter formatter = model.btcFormatter;
//                        int txVsize = feeEstimationTransaction.getVsize();
//                        double feePerVbyte = CoinUtil.getFeePerVbyte(fee, txVsize);
//                        double vkb = txVsize / 1000d;
//                        String recAmount = formatter.formatCoinWithCode(receiverAmount);

                //ARE-YOU-SURE?
                //lolen: need to compare amount with balance to see if available
                if (balance.isGreaterThan(amount_coin) || balance.equals(amount_coin)){
//                        new Popup().headLine(Res.get("portfolio.pending.step5_buyer.confirmWithdrawal"))
                new Popup().headLine(Res.get("portfolio.pending.step5_buyer.confirmWithdrawal"))
                        .confirmation(Res.get("shared.sendFundsDetailsWithFee",
                                fromAddresses,
                                toAddresses,
                                amount))
                        .actionButtonText(Res.get("shared.yes"))
                        .onAction(() -> doWithdrawal(amount, miningFee))
                        .closeButtonText(Res.get("shared.cancel"))
                        .onClose(() -> {
                            useSavingsWalletButton.setDisable(false);
                            withdrawToExternalWalletButton.setDisable(false);
                        })
                        .show();
                    } else {
                new Popup().warning(Res.get("portfolio.pending.step5_buyer.amountTooLow")).show();
                }
//            } catch (AddressFormatException e) {
//                validateWithdrawAddress();
//            } catch (AddressEntryException e) {
//                log.error(e.getMessage());
//            } catch (InsufficientFundsException e) {
            }catch (Exception e){
                log.error(e.getMessage());
                e.printStackTrace();
//            }catch (Exception e){
                new Popup().warning(e.getMessage()).show();
            }
        } else {
            new Popup().warning(Res.get("validation.btc.invalidAddress")).show();
        }
    }

    private void doWithdrawal(BigInteger amount, BigInteger fee) {
        String toAddress = withdrawAddressTextField.getText();
        ResultHandler resultHandler = this::handleTradeCompleted;
        FaultHandler faultHandler = (errorMessage, throwable) -> {
            useSavingsWalletButton.setDisable(false);
            withdrawToExternalWalletButton.setDisable(false);
            if (throwable != null && throwable.getMessage() != null)
                new Popup().error(errorMessage + "\n\n" + throwable.getMessage()).show();
            else
                new Popup().error(errorMessage).show();
        };
        //WHY-NO-SEND-MONIES?
//        if (true) throw new RuntimeException("BuyerStep4View.doWithdrawal() not yet updated for XMR");
//        if (model.dataModel.xmrWalletService.isEncrypted()) {
//            UserThread.runAfter(() -> model.dataModel.walletPasswordWindow.onAesKey(aesKey ->
//                    doWithdrawRequest(toAddress, amount, fee, aesKey, resultHandler, faultHandler))
//                    .show(), 300, TimeUnit.MILLISECONDS);
//        } else
            doWithdrawRequest(toAddress, amount, fee, null, resultHandler, faultHandler);
    }

    private void doWithdrawRequest(String toAddress,
                                   BigInteger amount,
                                   BigInteger fee,
                                   KeyParameter aesKey,
                                   ResultHandler resultHandler,
                                   FaultHandler faultHandler) {
        useSavingsWalletButton.setDisable(true);
        withdrawToExternalWalletButton.setDisable(true);
        String memo = withdrawMemoTextField.getText();
        if (memo.isEmpty()) {
            memo = null;
        }
        model.dataModel.onWithdrawRequest(toAddress,
                amount,
                fee,
                aesKey,
                memo,
                resultHandler,
                faultHandler);
        handleTradeCompleted();
        model.dataModel.tradeManager.onTradeCompleted(trade);
    }

    private void handleTradeCompleted() {
        closeButton.setDisable(true);
        model.dataModel.xmrWalletService.swapTradeEntryToAvailableEntry(trade.getId(), XmrAddressEntry.Context.TRADE_PAYOUT);

        openTradeFeedbackWindow();
    }

    private void openTradeFeedbackWindow() {
        String key = "feedbackPopupAfterTrade";
        if (!DevEnv.isDevMode() && preferences.showAgain(key)) {
            UserThread.runAfter(() -> new TradeFeedbackWindow()
                    .dontShowAgainId(key)
                    .onAction(this::showNavigateToClosedTradesViewPopup)
                    .show(), 500, TimeUnit.MILLISECONDS);
        } else {
            showNavigateToClosedTradesViewPopup();
        }
    }

    private void showNavigateToClosedTradesViewPopup() {
        if (!DevEnv.isDevMode()) {
            UserThread.runAfter(() -> new Popup().headLine(Res.get("portfolio.pending.step5_buyer.tradeCompleted.headline"))
                    .feedback(Res.get("portfolio.pending.step5_buyer.tradeCompleted.msg"))
                    .actionButtonTextWithGoTo("navigation.portfolio.closedTrades")
                    .onAction(() -> model.dataModel.navigation.navigateTo(MainView.class, PortfolioView.class, ClosedTradesView.class))
                    .dontShowAgainId("tradeCompleteWithdrawCompletedInfo")
                    .show(), 500, TimeUnit.MILLISECONDS);
        }
    }

    protected String getBtcTradeAmountLabel() {
        return Res.get("portfolio.pending.step5_buyer.bought");
    }

    protected String getFiatTradeAmountLabel() {
        return Res.get("portfolio.pending.step5_buyer.paid");
    }
}
