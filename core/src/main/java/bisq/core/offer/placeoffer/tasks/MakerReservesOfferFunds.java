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

package bisq.core.offer.placeoffer.tasks;

import bisq.common.taskrunner.Task;
import bisq.common.taskrunner.TaskRunner;
import bisq.core.btc.model.XmrAddressEntry;
import bisq.core.offer.Offer;
import bisq.core.offer.placeoffer.PlaceOfferModel;
import bisq.core.util.ParsingUtils;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import monero.daemon.model.MoneroOutput;
import monero.wallet.MoneroWallet;
import monero.wallet.model.MoneroTxWallet;

public class MakerReservesOfferFunds extends Task<PlaceOfferModel> {

    public MakerReservesOfferFunds(TaskRunner taskHandler, PlaceOfferModel model) {
        super(taskHandler, model);
    }

    @Override
    protected void run() {

        Offer offer = model.getOffer();
        MoneroTxWallet reserveTx = null;
        try {
            runInterceptHook();

            // freeze offer funds and get reserve tx
            String returnAddress = model.getXmrWalletService().getOrCreateAddressEntry(offer.getId(), XmrAddressEntry.Context.TRADE_PAYOUT).getAddressString();
            BigInteger makerFee = ParsingUtils.coinToAtomicUnits(offer.getMakerFee());
            BigInteger depositAmount = ParsingUtils.coinToAtomicUnits(model.getReservedFundsForOffer());
            reserveTx = model.getXmrWalletService().createReserveTx(makerFee, returnAddress, depositAmount);

            // collect reserved key images // TODO (woodser): switch to proof of reserve?
            List<String> reservedKeyImages = new ArrayList<String>();
            for (MoneroOutput input : reserveTx.getInputs()) reservedKeyImages.add(input.getKeyImage().getHex());

            // save offer state
            // TODO (woodser): persist
            model.setReserveTx(reserveTx);
            offer.getOfferPayload().setReserveTxKeyImages(reservedKeyImages);
            offer.setOfferFeePaymentTxId(reserveTx.getHash()); // TODO (woodser): don't use this field
            complete();
        } catch (Throwable t) {
            offer.setErrorMessage("An error occurred.\n" + "Error message:\n" + t.getMessage());
            if (reserveTx != null) { unfreezeOutputs(reserveTx);}
            offer.getOfferPayload().setReserveTxKeyImages(null);
            offer.setOfferFeePaymentTxId(null);
            model.setReserveTx(null);
            failed(t);
        }
    }

    private void unfreezeOutputs(MoneroTxWallet reserveTx){
        List<String> reservedKeyImages = new ArrayList<String>();
        MoneroWallet wallet = model.getXmrWalletService().getWallet();
        for (MoneroOutput input : reserveTx.getInputs()) {
            reservedKeyImages.remove(input.getKeyImage().getHex());
            wallet.thawOutput(input.getKeyImage().getHex());
        }
    }
}
