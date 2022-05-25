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

import bisq.desktop.Navigation;
import bisq.desktop.common.view.ActivatableViewAndModel;
import bisq.desktop.common.view.FxmlView;
import bisq.desktop.components.AddressTextField;
import bisq.desktop.components.AutoTooltipButton;
import bisq.desktop.components.AutoTooltipLabel;
import bisq.desktop.components.AutoTooltipSlideToggleButton;
import bisq.desktop.components.HyperlinkWithIcon;
import bisq.desktop.components.InputTextField;
import bisq.desktop.components.TitledGroupBg;
import bisq.desktop.main.MainView;
import bisq.desktop.main.offer.createoffer.CreateOfferView;
import bisq.desktop.main.overlays.popups.Popup;
import bisq.desktop.main.overlays.windows.OfferDetailsWindow;
import bisq.desktop.main.overlays.windows.QRCodeWindow;
import bisq.desktop.main.portfolio.PortfolioView;
import bisq.desktop.util.GUIUtil;
import bisq.desktop.util.Layout;

import bisq.core.locale.Res;
import bisq.core.trade.Trade;

import bisq.common.UserThread;
import bisq.common.handlers.FaultHandler;
import bisq.common.handlers.ResultHandler;

import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;

import javax.inject.Inject;

import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import javafx.geometry.Insets;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;

import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

import javafx.util.Callback;

import org.bouncycastle.crypto.params.KeyParameter;

import java.io.ByteArrayInputStream;

import java.math.BigInteger;

import java.util.concurrent.TimeUnit;


import javax.annotation.Nullable;

import static bisq.desktop.util.FormBuilder.addAddressTextField;
import static bisq.desktop.util.FormBuilder.addCompactTopLabelTextField;
import static bisq.desktop.util.FormBuilder.addInputTextField;
import static bisq.desktop.util.FormBuilder.addTitledGroupBg;



import monero.wallet.MoneroWallet;
import monero.wallet.model.MoneroOutputWallet;
import monero.wallet.model.MoneroTxConfig;
import monero.wallet.model.MoneroTxWallet;
import zmq.io.net.Address;

@FxmlView
public class FundView extends ActivatableViewAndModel<VBox, FundViewModel> {

    @FXML
    TableView<MoneroTxWallet> tableView;
    @FXML
    TableColumn<MoneroTxWallet, MoneroTxWallet> dateColumn, txHashColumn, numConfirmationsColumn, directionColumn, sumColumn, deactivateItemColumn, removeItemColumn, editItemColumn;
    @FXML
    TableView<MoneroOutputWallet> tableView2;
    @FXML
    TableColumn<MoneroOutputWallet, MoneroOutputWallet> outputColumn, outputHashColumn, outputAmountColumn;
    @FXML
    Button withdrawToExternalWalletButton;
    @FXML
    InputTextField withdrawAddressTextField, withdrawMemoTextField;;
    @FXML
    AddressTextField addressTextField;
    @FXML
    Region footerSpacer;
    @FXML
    AutoTooltipSlideToggleButton selectToggleButton;
    @FXML
    GridPane gridPane;
    @FXML
    TitledGroupBg withdrawTitledGroupBg, depositTitledGroupBg;


    private final Navigation navigation;
    private ChangeListener<Number> widthListener;
    private ImageView qrCodeImageView;
    private int gridRow = 0;
    String primaryAddress;


    @Inject
    public FundView(FundViewModel model, Navigation navigation, OfferDetailsWindow offerDetailsWindow) {
        super(model);
        this.navigation = navigation;

    }

    @Override
    public void initialize() {
        primaryAddress = model.getPrimaryAddress();
        qrCodeImageView = new ImageView();
        qrCodeImageView.getStyleClass().add("qr-code");
        Tooltip.install(qrCodeImageView, new Tooltip(Res.get("shared.openLargeQRWindow")));
        qrCodeImageView.setOnMouseClicked(e -> GUIUtil.showFeeInfoBeforeExecute(
                () -> UserThread.runAfter(
                        () -> new QRCodeWindow(primaryAddress).show(),
                        //model.getPrimaryAddress().getText()).show(),
                        200, TimeUnit.MILLISECONDS)));
        GridPane.setRowIndex(qrCodeImageView, gridRow);
        GridPane.setRowSpan(qrCodeImageView, 4);
        GridPane.setColumnIndex(qrCodeImageView, 1);
        GridPane.setMargin(qrCodeImageView, new Insets(Layout.FIRST_ROW_DISTANCE, 0, 0, 10));
        gridPane.getChildren().add(qrCodeImageView);
        qrCodeImageView.setVisible(true);
        qrCodeImageView.setManaged(true);
        updateQRCode(primaryAddress);

        depositTitledGroupBg = addTitledGroupBg(gridPane, ++gridRow, 1, Res.get("portfolio.funds.deposit"), Layout.COMPACT_GROUP_DISTANCE);
        depositTitledGroupBg.getStyleClass().add("last");
        addressTextField = addAddressTextField(gridPane, ++gridRow, Res.get("shared.depositAddress"), Layout.FIRST_ROW_DISTANCE);
        addressTextField.setPaymentLabel("Deposit address");
        addressTextField.setAddress(primaryAddress);
        //==========
        withdrawTitledGroupBg = addTitledGroupBg(gridPane, ++gridRow, 1, Res.get("portfolio.funds.withdraw"), Layout.COMPACT_GROUP_DISTANCE);
        withdrawTitledGroupBg.getStyleClass().add("last");
        //addCompactTopLabelTextField(gridPane, gridRow, Res.get("portfolio.pending.step5_buyer.amount"), model.getPayoutAmount(), Layout.FIRST_ROW_AND_GROUP_DISTANCE);

        withdrawAddressTextField = addInputTextField(gridPane, ++gridRow, Res.get("portfolio.pending.step5_buyer.withdrawToAddress"));
        withdrawAddressTextField.setManaged(true);
        withdrawAddressTextField.setVisible(true);

        HBox hBox = new HBox();
        hBox.setSpacing(10);
        withdrawToExternalWalletButton = new AutoTooltipButton(Res.get("portfolio.funds.sweepunlocked"));
        withdrawToExternalWalletButton.setDefaultButton(false);
        hBox.getChildren().addAll(withdrawToExternalWalletButton);
        GridPane.setRowIndex(hBox, ++gridRow);
        GridPane.setMargin(hBox, new Insets(5, 10, 0, 0));
        gridPane.getChildren().add(hBox);

        withdrawToExternalWalletButton.setOnAction(e -> {
            model.onWithdrawRequest(withdrawAddressTextField.getText());
        });
    }

    private void updateQRCode(String address) {
            final byte[] imageBytes = QRCode
                    .from(address)
                    .withSize(150, 150) // code has 41 elements 8 px is border with 150 we get 3x scale and min. border
                    .to(ImageType.PNG)
                    .stream()
                    .toByteArray();
            Image qrImage = new Image(new ByteArrayInputStream(imageBytes));
            qrCodeImageView.setImage(qrImage);
    }

}

