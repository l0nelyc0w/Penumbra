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

package bisq.core.trade.protocol.tasks;

import bisq.common.app.Version;
import bisq.common.taskrunner.TaskRunner;
import bisq.core.btc.model.XmrAddressEntry;
import bisq.core.trade.Trade;
import bisq.core.trade.messages.SignContractRequest;
import bisq.network.p2p.SendDirectMessageListener;
import java.util.Date;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import monero.wallet.MoneroWallet;
import monero.wallet.model.MoneroTxWallet;

// TODO (woodser): separate classes for deposit tx creation and contract request, or combine into ProcessInitMultisigRequest
@Slf4j
public class SendSignContractRequestAfterMultisig extends TradeTask {

    private boolean ack1 = false;
    private boolean ack2 = false;

    @SuppressWarnings({"unused"})
    public SendSignContractRequestAfterMultisig(TaskRunner taskHandler, Trade trade) {
        super(taskHandler, trade);
    }

    @Override
    protected void run() {
        try {
          runInterceptHook();

          // skip if multisig wallet not complete
          if (!processModel.isMultisigSetupComplete()) return; // TODO: woodser: this does not ack original request?

          // skip if deposit tx already created
          if (processModel.getDepositTxXmr() != null) {
              complete();
              return;
          }

          // thaw reserved outputs
          MoneroWallet wallet = trade.getXmrWalletService().getWallet();
          for (String reserveTxKeyImage : trade.getSelf().getReserveTxKeyImages()) {
              wallet.thawOutput(reserveTxKeyImage);
          }

          // create deposit tx
          BigInteger tradeFee = ParsingUtils.coinToAtomicUnits(trade instanceof MakerTrade ? trade.getOffer().getMakerFee() : trade.getTakerFee());
          Offer offer = processModel.getOffer();
          BigInteger depositAmount = ParsingUtils.coinToAtomicUnits(trade instanceof SellerTrade ? offer.getAmount().add(offer.getSellerSecurityDeposit()) : offer.getBuyerSecurityDeposit());
          MoneroWallet multisigWallet = processModel.getProvider().getXmrWalletService().getMultisigWallet(trade.getId());
          String multisigAddress = multisigWallet.getPrimaryAddress();
          MoneroTxWallet depositTx = TradeUtils.createDepositTx(trade.getXmrWalletService(), tradeFee, multisigAddress, depositAmount);

          // freeze deposit outputs
          // TODO (woodser): save frozen key images and unfreeze if trade fails before deposited to multisig
          for (MoneroOutput input : depositTx.getInputs()) {
              wallet.freezeOutput(input.getKeyImage().getHex());
          }

          // save process state
          processModel.setDepositTxXmr(depositTx);
          trade.getSelf().setDepositTxHash(depositTx.getHash());

          // complete on successful ack messages
          TradeListener ackListener = new TradeListener() {
              @Override
              public void onArrived() {
                  log.info("{} arrived: trading peer={}; offerId={}; uid={}", request.getClass().getSimpleName(), trade.getArbitratorNodeAddress(), trade.getId());
                  ack2 = true;
                  if (ack1 && ack2) completeAux();
              }
              @Override
              public void onFault(String errorMessage) {
                  log.error("Sending {} failed: uid={}; peer={}; error={}", request.getClass().getSimpleName(), trade.getArbitratorNodeAddress(), trade.getId(), errorMessage);
                  appendToErrorMessage("Sending message failed: message=" + request + "\nerrorMessage=" + errorMessage);
                  failed();
              }
          });
        } catch (Throwable t) {
          failed(t);
        }
    }

    private void sendSignContractRequest(NodeAddress nodeAddress, PubKeyRing pubKeyRing, Offer offer, MoneroTxWallet depositTx) {

        // create request to sign contract
        SignContractRequest request = new SignContractRequest(
                trade.getOffer().getId(),
                processModel.getMyNodeAddress(),
                processModel.getPubKeyRing(),
                UUID.randomUUID().toString(), // TODO: ensure not reusing request id across protocol
                Version.getP2PMessageVersion(),
                new Date().getTime(),
                trade.getProcessModel().getAccountId(),
                trade.getProcessModel().getPaymentAccountPayload(trade).getHash(),
                trade.getXmrWalletService().getAddressEntry(offer.getId(), XmrAddressEntry.Context.TRADE_PAYOUT).get().getAddressString(),
                depositTx.getHash());

        // send request
        processModel.getP2PService().sendEncryptedDirectMessage(nodeAddress, pubKeyRing, request, new SendDirectMessageListener() {
            @Override
            public void onArrived() {
                log.info("{} arrived: trading peer={}; offerId={}; uid={}", request.getClass().getSimpleName(), nodeAddress, trade.getId());
            }
            @Override
            public void onFault(String errorMessage) {
                log.error("Sending {} failed: uid={}; peer={}; error={}", request.getClass().getSimpleName(), nodeAddress, trade.getId(), errorMessage);
                appendToErrorMessage("Sending message failed: message=" + request + "\nerrorMessage=" + errorMessage);
                failed();
            }
        });
    }

    private void completeAux() {
        processModel.getXmrWalletService().saveWallet(processModel.getXmrWalletService().getWallet());
        complete();
    }
}
