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

import bisq.core.offer.Offer;
import bisq.core.offer.placeoffer.PlaceOfferModel;

import bisq.common.taskrunner.Task;
import bisq.common.taskrunner.TaskRunner;

public class AddToPendingOffers extends Task<PlaceOfferModel> {

    public AddToPendingOffers(TaskRunner<PlaceOfferModel> taskHandler, PlaceOfferModel model) {
        super(taskHandler, model);
    }

    @Override
    protected void run() {
        try {
            runInterceptHook();
            model.getOfferBookService().addPendingOffer(new Offer(model.getOffer().getOfferPayload()),
                    () -> {
                        model.setOfferAddedToOfferBook(true);
                        complete();
                    },
                    errorMessage -> {
                        model.getOffer().setErrorMessage("Could not add offer to pending offers.\n" +
                                "Please check your network connection and try again.");

                        failed(errorMessage);
                    });
        } catch (Throwable t) {
            model.getOffer().setErrorMessage("An error occurred.\n" +
                    "Error message:\n"
                    + t.getMessage());

            failed(t);
        }
    }
}
