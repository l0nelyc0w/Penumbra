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

package bisq.core.payment;

import bisq.core.api.model.PaymentAccountFormField;
import bisq.core.locale.CurrencyUtil;
import bisq.core.locale.TradeCurrency;
import bisq.core.payment.payload.CashByMailAccountPayload;
import bisq.core.payment.payload.PaymentAccountPayload;
import bisq.core.payment.payload.PaymentMethod;

import java.util.List;

import lombok.NonNull;

public final class CashByMailAccount extends PaymentAccount {

    public static final List<TradeCurrency> SUPPORTED_CURRENCIES = CurrencyUtil.getAllFiatCurrencies();

    public CashByMailAccount() {
        super(PaymentMethod.CASH_BY_MAIL);
    }

    @Override
    protected PaymentAccountPayload createPayload() {
        return new CashByMailAccountPayload(paymentMethod.getId(), id);
    }

    @Override
    public @NonNull List<TradeCurrency> getSupportedCurrencies() {
        return SUPPORTED_CURRENCIES;
    }

    @Override
    public @NonNull List<PaymentAccountFormField.FieldId> getInputFieldIds() {
        throw new RuntimeException("Not implemented");
    }

    public void setPostalAddress(String postalAddress) {
        ((CashByMailAccountPayload) paymentAccountPayload).setPostalAddress(postalAddress);
    }

    public String getPostalAddress() {
        return ((CashByMailAccountPayload) paymentAccountPayload).getPostalAddress();
    }

    public void setContact(String contact) {
        ((CashByMailAccountPayload) paymentAccountPayload).setContact(contact);
    }

    public String getContact() {
        return ((CashByMailAccountPayload) paymentAccountPayload).getContact();
    }

    public void setExtraInfo(String extraInfo) {
        ((CashByMailAccountPayload) paymentAccountPayload).setExtraInfo(extraInfo);
    }

    public String getExtraInfo() {
        return ((CashByMailAccountPayload) paymentAccountPayload).getExtraInfo();
    }
}
