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

import bisq.desktop.Navigation;
import bisq.desktop.common.view.ActivatableViewAndModel;
import bisq.desktop.common.view.FxmlView;
import bisq.desktop.components.AutoTooltipButton;
import bisq.desktop.components.AutoTooltipLabel;
import bisq.desktop.components.AutoTooltipSlideToggleButton;
import bisq.desktop.components.AutoTooltipTableColumn;
import bisq.desktop.components.HyperlinkWithIcon;
import bisq.desktop.components.InputTextField;
import bisq.desktop.main.MainView;
import bisq.desktop.main.funds.FundsView;
import bisq.desktop.main.funds.withdrawal.WithdrawalView;
import bisq.desktop.main.offer.createoffer.CreateOfferView;
import bisq.desktop.main.overlays.popups.Popup;
import bisq.desktop.main.overlays.windows.OfferDetailsWindow;
import bisq.desktop.main.portfolio.PortfolioView;

import bisq.core.btc.model.XmrAddressEntry;
import bisq.core.locale.Res;
import bisq.core.offer.Offer;
import bisq.core.user.DontShowAgainLookup;

import org.bitcoinj.core.Coin;

import javax.inject.Inject;

import javafx.fxml.FXML;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
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

import java.math.BigInteger;

import java.util.Comparator;



import monero.wallet.model.MoneroOutputWallet;
import monero.wallet.model.MoneroTxWallet;

@FxmlView
public class TransactionsView extends ActivatableViewAndModel<VBox, TransactionsViewModel> {

    @FXML
    TableView<MoneroTxWallet> tableView;
    @FXML
    TableColumn<MoneroTxWallet, MoneroTxWallet> dateColumn, txHashColumn, numConfirmationsColumn, directionColumn, sumColumn, deactivateItemColumn, removeItemColumn, editItemColumn;
    @FXML
    TableView<MoneroOutputWallet> tableView2;
    @FXML
    TableColumn<MoneroOutputWallet, MoneroOutputWallet> outputColumn, outputHashColumn, outputAmountColumn;
    @FXML
    HBox searchBox;
    @FXML
    AutoTooltipLabel filterLabel, frozenLabel;
    @FXML
    InputTextField filterTextField;
    @FXML
    Pane searchBoxSpacer;
    @FXML
    Label numItems;
    @FXML
    Region footerSpacer;
    @FXML
    AutoTooltipButton exportButton;
    @FXML
    AutoTooltipSlideToggleButton selectToggleButton;

    private final Navigation navigation;
    private final OfferDetailsWindow offerDetailsWindow;
    private SortedList<MoneroTxWallet> sortedList;
    private FilteredList<MoneroTxWallet> filteredList;
    private FilteredList<MoneroOutputWallet> frozenList;
    private ChangeListener<String> filterTextFieldListener;
    //private PortfolioView.PendingOfferActionHandler pendingOfferActionHandler;
    private ChangeListener<Number> widthListener;

    @Inject
    public TransactionsView(TransactionsViewModel model, Navigation navigation, OfferDetailsWindow offerDetailsWindow) {
        super(model);
        this.navigation = navigation;
        this.offerDetailsWindow = offerDetailsWindow;
    }

    @Override
    public void initialize() {
        //widthListener = (observable, oldValue, newValue) -> onWidthChange((double) newValue);
        dateColumn.setGraphic(new AutoTooltipLabel(Res.get("shared.dateTime")));
        //offerIdColumn.setGraphic(new AutoTooltipLabel(Res.get("shared.offerId")));
        deactivateItemColumn.setGraphic(new AutoTooltipLabel(Res.get("shared.enabled")));
        editItemColumn.setGraphic(new AutoTooltipLabel(""));
        removeItemColumn.setGraphic(new AutoTooltipLabel(""));

        txHashColumn.setGraphic(new AutoTooltipLabel(Res.get("shared.txHash")));
        numConfirmationsColumn.setGraphic(new AutoTooltipLabel(Res.get("shared.numConfirmations")));
        directionColumn.setGraphic(new AutoTooltipLabel(Res.get("shared.direction")));
        sumColumn.setGraphic(new AutoTooltipLabel(Res.get("shared.sum")));

        outputColumn.setGraphic(new AutoTooltipLabel(Res.get("shared.output")));
        outputHashColumn.setGraphic(new AutoTooltipLabel(Res.get("shared.outputHash")));
        outputAmountColumn.setGraphic(new AutoTooltipLabel(Res.get("shared.outputAmount")));


        setTxHashColumnCellFactory();
        setNumConfirmationsColumnCellFactory();
        setDirectionColumnCellFactory();
        setSumColumnCellFactory();

        setOutputColumnCellFactory();
        setOutputHashColumnCellFactory();
        setOutputAmountColumnCellFactory();


        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPlaceholder(new AutoTooltipLabel(Res.get("table.placeholder.noItems", Res.get("shared.openOffers"))));

        tableView2.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        dateColumn.setSortType(TableColumn.SortType.DESCENDING);
        tableView.getSortOrder().add(dateColumn);

        tableView.setRowFactory(
                tableView -> {
                    final TableRow<MoneroTxWallet> row = new TableRow<>();
                    final ContextMenu rowMenu = new ContextMenu();
                    MenuItem editItem = new MenuItem(Res.get("portfolio.context.offerLikeThis"));
                    editItem.setOnAction((event) -> {
                        try {
                            String txHash = row.getItem().getHash();
                            navigation.navigateToWithData(MainView.class, PortfolioView.class, CreateOfferView.class);
                        } catch (NullPointerException e) {
                            log.warn("Unable to get offerPayload - {}", e.toString());
                        }
                    });
                    rowMenu.getItems().add(editItem);
                    row.contextMenuProperty().bind(
                            Bindings.when(Bindings.isNotNull(row.itemProperty()))
                                    .then(rowMenu)
                                    .otherwise((ContextMenu) null));
                    return row;
                });

        tableView2.setRowFactory(
                tableView2 -> {
                    final TableRow<MoneroOutputWallet> row = new TableRow<>();
                    final ContextMenu rowMenu = new ContextMenu();
                    MenuItem editItem = new MenuItem(Res.get("portfolio.context.offerLikeThis"));
                    editItem.setOnAction((event) -> {
                        try {
                            String output = row.getItem().getKeyImage().toString();
                            navigation.navigateToWithData(MainView.class, PortfolioView.class, CreateOfferView.class);
                        } catch (NullPointerException e) {
                            log.warn("Unable to get offerPayload - {}", e.toString());
                        }
                    });
                    rowMenu.getItems().add(editItem);
                    row.contextMenuProperty().bind(
                            Bindings.when(Bindings.isNotNull(row.itemProperty()))
                                    .then(rowMenu)
                                    .otherwise((ContextMenu) null));
                    return row;
                });

        filterLabel.setText(Res.get("shared.filter"));
        frozenLabel.setText(Res.get("shared.frozen"));
        HBox.setMargin(filterLabel, new Insets(5, 0, 0, 10));
        filterTextFieldListener = (observable, oldValue, newValue) -> applyFilteredListPredicate(filterTextField.getText());
        searchBox.setSpacing(5);
        HBox.setHgrow(searchBoxSpacer, Priority.ALWAYS);

        selectToggleButton.setPadding(new Insets(0, 60, -20, 0));
        selectToggleButton.setText(Res.get("shared.enabled"));
        selectToggleButton.setDisable(true);

        numItems.setId("num-offers");
        numItems.setPadding(new Insets(-5, 0, 0, 10));
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);
        HBox.setMargin(exportButton, new Insets(0, 10, 0, 0));
        exportButton.updateText(Res.get("shared.exportCSV"));
    }

    @Override
    protected void activate() {
        filteredList = new FilteredList<>(model.getList());
        frozenList = new FilteredList<>(model.getFrozen());
        sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedList);
        tableView2.setItems(frozenList);

        updateSelectToggleButtonState();

        selectToggleButton.setOnAction(event -> {
            if (model.isBootstrappedOrShowPopup()) {
                if (selectToggleButton.isSelected()) {
                    //sortedList.forEach(openOfferListItem -> onActivateOpenOffer(openOfferListItem.getOpenOffer()));
                } else {
                    //sortedList.forEach(openOfferListItem -> onDeactivateOpenOffer(openOfferListItem.getOpenOffer()));
                }
            }
            tableView.refresh();
        });

//        numItems.setText(Res.get("shared.numItemsLabel", sortedList.size()));

        filterTextField.textProperty().addListener(filterTextFieldListener);
        applyFilteredListPredicate(filterTextField.getText());

    //    root.widthProperty().addListener(widthListener);
        //onWidthChange(root.getWidth());
    }

    @Override
    protected void deactivate() {
        sortedList.comparatorProperty().unbind();
        exportButton.setOnAction(null);

        filterTextField.textProperty().removeListener(filterTextFieldListener);
    //   root.widthProperty().removeListener(widthListener);
    }

    private void updateSelectToggleButtonState() {
        if (sortedList.size() == 0) {
            selectToggleButton.setDisable(true);
            selectToggleButton.setSelected(false);
        } else {
            selectToggleButton.setDisable(false);
//            long numDeactivated = sortedList.stream()
//                    .filter(pendingOfferListItem -> pendingOfferListItem.getPendingOffer().isDeactivated())
//                    .count();
//            if (numDeactivated == sortedList.size()) {
//                selectToggleButton.setSelected(false);
//            } else if (numDeactivated == 0) {
//                selectToggleButton.setSelected(true);
//            }
        }
    }

    private void applyFilteredListPredicate(String filterString) {
        filteredList.setPredicate(item -> {
            if (filterString.isEmpty())
                return true;

            String txHash = item.getHash();
            if (txHash.contains(filterString)) {
                return true;
            }
            return false;
        });
    }

    private void setTxHashColumnCellFactory() {
        txHashColumn.setCellValueFactory((MoneroTxWallet) -> new ReadOnlyObjectWrapper<>(MoneroTxWallet.getValue()));
        txHashColumn.getStyleClass().addAll("number-column", "first-column");
        txHashColumn.setCellFactory(
                new Callback<>() {

                    @Override
                    public TableCell<MoneroTxWallet, MoneroTxWallet> call(TableColumn<MoneroTxWallet,
                            MoneroTxWallet> column) {
                        return new TableCell<>() {
                            private HyperlinkWithIcon field;

                            @Override
                            public void updateItem(final MoneroTxWallet item, boolean empty) {
                                super.updateItem(item, empty);

                                if (item != null && !empty) {
                                    field = new HyperlinkWithIcon(item.getHash());
                                    //field.setOnAction(event -> offerDetailsWindow.show(item.getOffer(item));
                                    field.setTooltip(new Tooltip(Res.get("tooltip.openPopupForDetails")));
                                    setGraphic(field);
                                } else {
                                    setGraphic(null);
                                    if (field != null)
                                        field.setOnAction(null);
                                }
                            }
                        };
                    }
                });
    }

    private void setNumConfirmationsColumnCellFactory() {
        numConfirmationsColumn.setCellValueFactory((MoneroTxWallet) -> new ReadOnlyObjectWrapper<>(MoneroTxWallet.getValue()));
        numConfirmationsColumn.getStyleClass().addAll("number-column", "first-column");
        numConfirmationsColumn.setCellFactory(
                new Callback<>() {

                    @Override
                    public TableCell<MoneroTxWallet, MoneroTxWallet> call(TableColumn<MoneroTxWallet,
                            MoneroTxWallet> column) {
                        return new TableCell<>() {
                            private HyperlinkWithIcon field;

                            @Override
                            public void updateItem(final MoneroTxWallet item, boolean empty) {
                                super.updateItem(item, empty);

                                if (item != null && !empty) {
                                    field = new HyperlinkWithIcon(item.getNumConfirmations().toString());
                                    //field.setOnAction(event -> offerDetailsWindow.show(item.getOffer(item));
                                    field.setTooltip(new Tooltip(Res.get("tooltip.openPopupForDetails")));
                                    setGraphic(field);
                                } else {
                                    setGraphic(null);
                                    if (field != null)
                                        field.setOnAction(null);
                                }
                            }
                        };
                    }
                });
    }

    private void setDirectionColumnCellFactory() {
        directionColumn.setCellValueFactory((MoneroTxWallet) -> new ReadOnlyObjectWrapper<>(MoneroTxWallet.getValue()));
        directionColumn.getStyleClass().addAll("number-column", "first-column");
        directionColumn.setCellFactory(
                new Callback<>() {

                    @Override
                    public TableCell<MoneroTxWallet, MoneroTxWallet> call(TableColumn<MoneroTxWallet,
                            MoneroTxWallet> column) {
                        return new TableCell<>() {
                            private HyperlinkWithIcon field;
                            private boolean isIncoming;

                            @Override
                            public void updateItem(final MoneroTxWallet item, boolean empty) {
                                super.updateItem(item, empty);

                                if (item != null && !empty) {
                                    isIncoming = item.isIncoming();
                                    if (isIncoming == true){field = new HyperlinkWithIcon("in");}
                                    else {field = new HyperlinkWithIcon("out");}
                                    //field.setOnAction(event -> offerDetailsWindow.show(item.getOffer(item));
                                    field.setTooltip(new Tooltip(Res.get("tooltip.openPopupForDetails")));
                                    setGraphic(field);
                                } else {
                                    setGraphic(null);
                                    if (field != null)
                                        field.setOnAction(null);
                                }
                            }
                        };
                    }
                });
    }

    private static String longToXmr(long amt) {
        BigInteger AU_PER_XMR = new BigInteger("1000000000000");
        BigInteger auAmt = BigInteger.valueOf(amt);
        BigInteger[] quotientAndRemainder = auAmt.divideAndRemainder(AU_PER_XMR);
        double decimalRemainder = quotientAndRemainder[1].doubleValue() / AU_PER_XMR.doubleValue();
        return quotientAndRemainder[0].doubleValue() + decimalRemainder + " XMR";
    }

    private void setSumColumnCellFactory() {
        sumColumn.setCellValueFactory((MoneroTxWallet) -> new ReadOnlyObjectWrapper<>(MoneroTxWallet.getValue()));
        sumColumn.getStyleClass().addAll("number-column", "first-column");
        sumColumn.setCellFactory(
                new Callback<>() {

                    @Override
                    public TableCell<MoneroTxWallet, MoneroTxWallet> call(TableColumn<MoneroTxWallet,
                            MoneroTxWallet> column) {
                        return new TableCell<>() {
                            private HyperlinkWithIcon field;
                            private boolean isIncoming;

                            @Override
                            public void updateItem(final MoneroTxWallet item, boolean empty) {
                                super.updateItem(item, empty);

                                if (item != null && !empty) {
                                    isIncoming = item.isIncoming();
                                    if (isIncoming == true){field = new HyperlinkWithIcon(longToXmr(item.getIncomingAmount().longValueExact()));}
                                    else {field = new HyperlinkWithIcon(longToXmr(item.getOutgoingAmount().longValueExact()));}
                                    //field.setOnAction(event -> offerDetailsWindow.show(item.getOffer(item));
                                    field.setTooltip(new Tooltip(Res.get("tooltip.openPopupForDetails")));
                                    setGraphic(field);
                                } else {
                                    setGraphic(null);
                                    if (field != null)
                                        field.setOnAction(null);
                                }
                            }
                        };
                    }
                });
    }

    private void setOutputColumnCellFactory() {
        outputColumn.setCellValueFactory((MoneroOutputWallet) -> new ReadOnlyObjectWrapper<>(MoneroOutputWallet.getValue()));
        outputColumn.getStyleClass().addAll("number-column", "first-column");
        outputColumn.setCellFactory(
                new Callback<>() {

                    @Override
                    public TableCell<MoneroOutputWallet, MoneroOutputWallet> call(TableColumn<MoneroOutputWallet,
                            MoneroOutputWallet> column) {
                        return new TableCell<>() {
                            private HyperlinkWithIcon field;
                            private boolean isIncoming;

                            @Override
                            public void updateItem(final MoneroOutputWallet item, boolean empty) {
                                super.updateItem(item, empty);

                                if (item != null && !empty) {
                                    field = new HyperlinkWithIcon(item.getKeyImage().toString());
                                    //field.setOnAction(event -> offerDetailsWindow.show(item.getOffer(item));
                                    field.setTooltip(new Tooltip(Res.get("tooltip.openPopupForDetails")));
                                    setGraphic(field);
                                } else {
                                    setGraphic(null);
                                    if (field != null)
                                        field.setOnAction(null);
                                }
                            }
                        };
                    }
                });
    }

    private void setOutputHashColumnCellFactory() {
        outputHashColumn.setCellValueFactory((MoneroOutputWallet) -> new ReadOnlyObjectWrapper<>(MoneroOutputWallet.getValue()));
        outputHashColumn.getStyleClass().addAll("number-column", "first-column");
        outputHashColumn.setCellFactory(
                new Callback<>() {

                    @Override
                    public TableCell<MoneroOutputWallet, MoneroOutputWallet> call(TableColumn<MoneroOutputWallet,
                            MoneroOutputWallet> column) {
                        return new TableCell<>() {
                            private HyperlinkWithIcon field;
                            private boolean isIncoming;

                            @Override
                            public void updateItem(final MoneroOutputWallet item, boolean empty) {
                                super.updateItem(item, empty);

                                if (item != null && !empty) {
                                    field = new HyperlinkWithIcon(item.getTx().getHash().toString());
                                    //field.setOnAction(event -> offerDetailsWindow.show(item.getOffer(item));
                                    field.setTooltip(new Tooltip(Res.get("tooltip.openPopupForDetails")));
                                    setGraphic(field);
                                } else {
                                    setGraphic(null);
                                    if (field != null)
                                        field.setOnAction(null);
                                }
                            }
                        };
                    }
                });
    }

    private void setOutputAmountColumnCellFactory() {
        outputAmountColumn.setCellValueFactory((MoneroOutputWallet) -> new ReadOnlyObjectWrapper<>(MoneroOutputWallet.getValue()));
        outputAmountColumn.getStyleClass().addAll("number-column", "first-column");
        outputAmountColumn.setCellFactory(
                new Callback<>() {

                    @Override
                    public TableCell<MoneroOutputWallet, MoneroOutputWallet> call(TableColumn<MoneroOutputWallet,
                            MoneroOutputWallet> column) {
                        return new TableCell<>() {
                            private HyperlinkWithIcon field;
                            private boolean isIncoming;

                            @Override
                            public void updateItem(final MoneroOutputWallet item, boolean empty) {
                                super.updateItem(item, empty);

                                if (item != null && !empty) {
                                    field = new HyperlinkWithIcon(longToXmr(item.getAmount().longValueExact()));
                                    //field.setOnAction(event -> offerDetailsWindow.show(item.getOffer(item));
                                    field.setTooltip(new Tooltip(Res.get("tooltip.openPopupForDetails")));
                                    setGraphic(field);
                                } else {
                                    setGraphic(null);
                                    if (field != null)
                                        field.setOnAction(null);
                                }
                            }
                        };
                    }
                });
    }

//    public void setPendingOfferActionHandler(PortfolioView.PendingOfferActionHandler pendingOfferActionHandler) {
//        this.pendingOfferActionHandler = pendingOfferActionHandler;
//    }
}

