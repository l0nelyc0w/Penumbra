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
import bisq.desktop.main.portfolio.duplicateoffer.DuplicateOfferView;
import bisq.desktop.main.portfolio.pendingoffer.PendingOfferListItem;
import bisq.desktop.main.portfolio.pendingoffer.PendingOffersViewModel;
import bisq.desktop.util.GUIUtil;

import bisq.core.btc.model.XmrAddressEntry;
import bisq.core.btc.model.XmrAddressEntryList;
import bisq.core.locale.Res;
import bisq.core.offer.Offer;
import bisq.core.offer.OfferPayload;
//import bisq.core.offer.PendingOffer;
import bisq.core.user.DontShowAgainLookup;

import com.googlecode.jcsv.writer.CSVEntryConverter;

import javax.inject.Inject;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;

import javafx.fxml.FXML;

import javafx.stage.Stage;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import javafx.geometry.Insets;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

import javafx.util.Callback;

import java.util.Comparator;

import org.jetbrains.annotations.NotNull;

import static bisq.desktop.util.FormBuilder.getRegularIconButton;

@FxmlView
public class PendingOffersView extends ActivatableViewAndModel<VBox, PendingOffersViewModel> {

    @FXML
    TableView<XmrAddressEntry> tableView;
    @FXML
    TableColumn<XmrAddressEntry, XmrAddressEntry> contextColumn, addressColumn, lockedInMultiColumn, dateColumn, offerIdColumn, deactivateItemColumn,
            removeItemColumn, editItemColumn;
    @FXML
    HBox searchBox;
    @FXML
    AutoTooltipLabel filterLabel;
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
    private SortedList<XmrAddressEntry> sortedList;
    private FilteredList<XmrAddressEntry> filteredList;
    private ChangeListener<String> filterTextFieldListener;
    private PortfolioView.PendingOfferActionHandler pendingOfferActionHandler;
    private ChangeListener<Number> widthListener;

    @Inject
    public PendingOffersView(PendingOffersViewModel model, Navigation navigation, OfferDetailsWindow offerDetailsWindow) {
        super(model);
        this.navigation = navigation;
        this.offerDetailsWindow = offerDetailsWindow;
    }

    @Override
    public void initialize() {
        widthListener = (observable, oldValue, newValue) -> onWidthChange((double) newValue);
        dateColumn.setGraphic(new AutoTooltipLabel(Res.get("shared.dateTime")));
        offerIdColumn.setGraphic(new AutoTooltipLabel(Res.get("shared.offerId")));
        deactivateItemColumn.setGraphic(new AutoTooltipLabel(Res.get("shared.enabled")));
        editItemColumn.setGraphic(new AutoTooltipLabel(""));
        removeItemColumn.setGraphic(new AutoTooltipLabel(""));

        contextColumn.setGraphic(new AutoTooltipLabel(Res.get("shared.context")));
        addressColumn.setGraphic(new AutoTooltipLabel(Res.get("shared.address")));
        lockedInMultiColumn.setGraphic(new AutoTooltipLabel(Res.get("shared.lockedInMulti")));


        setOfferIdColumnCellFactory();
        setContextColumnCellFactory();
        setAddressColumnCellFactory();
        setLockedInMultiColumnCellFactory();

        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPlaceholder(new AutoTooltipLabel(Res.get("table.placeholder.noItems", Res.get("shared.openOffers"))));

        offerIdColumn.setComparator(Comparator.comparing(o -> o.getOfferId()));

//        contextColumn.setComparator(Comparator.comparing(o -> o.getContext().toString()));
//       addressColumn.setComparator(Comparator.comparing(o -> o.getAddressString()));
//      lockedInMultiColumn.setComparator(Comparator.comparing(o -> o.getCoinLockedInMultiSig().toString()));


        dateColumn.setSortType(TableColumn.SortType.DESCENDING);
        tableView.getSortOrder().add(dateColumn);

        tableView.setRowFactory(
                tableView -> {
                    final TableRow<XmrAddressEntry> row = new TableRow<>();
                    final ContextMenu rowMenu = new ContextMenu();
                    MenuItem editItem = new MenuItem(Res.get("portfolio.context.offerLikeThis"));
                    editItem.setOnAction((event) -> {
                        try {
                            String offerID = row.getItem().getOfferId();
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
        sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedList);

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
//        exportButton.setOnAction(event -> {
//            ObservableList<TableColumn<XmrAddressEntry, ?>> tableColumns = tableView.getColumns();
//            int reportColumns = tableColumns.size() - 3;    // CSV report excludes the last columns (icons)
//            CSVEntryConverter<XmrAddressEntry> headerConverter = item -> {
//                String[] columns = new String[reportColumns];
//                for (int i = 0; i < columns.length; i++) {
//                    Node graphic = tableColumns.get(i).getGraphic();
//                    if (graphic instanceof AutoTooltipLabel) {
//                        columns[i] = ((AutoTooltipLabel) graphic).getText();
//                    } else if (graphic instanceof HBox) {
//                        // Deviation has a Hbox with AutoTooltipLabel as first child in header
//                        columns[i] = ((AutoTooltipLabel) ((Parent) graphic).getChildrenUnmodifiable().get(0)).getText();
//                    } else {
//                        // Not expected
//                        columns[i] = "N/A";
//                    }
//                }
//                return columns;
//            };
//            CSVEntryConverter<XmrAddressEntry> contentConverter = item -> {
//                String[] columns = new String[reportColumns];
//                columns[0] = item.getOfferId();
////                columns[1] = model.getDate(item);
////                columns[2] = model.getMarketLabel(item);
////                columns[3] = model.getPrice(item);
////                columns[4] = model.getPriceDeviation(item);
////                //columns[5] = model.getTriggerPrice(item);
////                columns[6] = model.getAmount(item);
////                columns[7] = model.getVolume(item);
////                columns[8] = model.getPaymentMethod(item);
////                columns[9] = model.getDirectionLabel(item);
//                //columns[10] = String.valueOf(!item.getPendingOffer().isDeactivated());
//                return columns;
//            };
//
//            GUIUtil.exportCSV("pendingOffers.csv",
//                    headerConverter,
//                    contentConverter,
//                    new PendingOfferListItem(),
//                    sortedList,
//                    (Stage) root.getScene().getWindow());
//        });

        filterTextField.textProperty().addListener(filterTextFieldListener);
        applyFilteredListPredicate(filterTextField.getText());

        root.widthProperty().addListener(widthListener);
        onWidthChange(root.getWidth());
    }

    @Override
    protected void deactivate() {
        sortedList.comparatorProperty().unbind();
        exportButton.setOnAction(null);

        filterTextField.textProperty().removeListener(filterTextFieldListener);
        root.widthProperty().removeListener(widthListener);
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

            String offerId = item.getOfferId();
            if (offerId == null){ offerId = "null";}
            if (offerId.contains(filterString)) {
                return true;
            }
//            if (model.getDate(item).contains(filterString)) {
//                return true;
//            }
//            if (model.getMarketLabel(item).contains(filterString)) {
//                return true;
//            }
//            if (model.getPrice(item).contains(filterString)) {
//                return true;
//            }
//            if (model.getPriceDeviation(item).contains(filterString)) {
//                return true;
//            }
//            if (model.getPaymentMethod(item).contains(filterString)) {
//                return true;
//            }
//            if (model.getVolume(item).contains(filterString)) {
//                return true;
//            }
//            if (model.getAmount(item).contains(filterString)) {
//                return true;
//            }
//            if (model.getDirectionLabel(item).contains(filterString)) {
//                return true;
//            }
//            if (offer.getOfferFeePaymentTxId().contains(filterString)) {
//                return true;
//            }
            return false;
        });
    }

    private void onWidthChange(double width) {
        contextColumn.setVisible(width > 1200);
    }


    private void onRemovePendingOffer(Offer pendingOffer) {
        if (model.isBootstrappedOrShowPopup()) {
            String key = "RemoveOfferWarning";
            if (DontShowAgainLookup.showAgain(key)) {
                new Popup().warning(Res.get("popup.warning.removeOffer", model.getMakerFeeAsString(pendingOffer)))
                        .actionButtonText(Res.get("shared.removeOffer"))
                        .onAction(() -> doRemovePendingOffer(pendingOffer))
                        .closeButtonText(Res.get("shared.dontRemoveOffer"))
                        .dontShowAgainId(key)
                        .show();
            } else {
                doRemovePendingOffer(pendingOffer);
            }
            updateSelectToggleButtonState();
        }
    }

    private void doRemovePendingOffer(Offer pendingOffer) {
        model.onRemovePendingOffer(pendingOffer,
                () -> {
                    log.debug("Remove pending offer was successful");

                    tableView.refresh();

                    String key = "WithdrawFundsAfterRemoveOfferInfo";
                    if (DontShowAgainLookup.showAgain(key)) {
                        new Popup().instruction(Res.get("offerbook.withdrawFundsHint", Res.get("navigation.funds.availableForWithdrawal")))
                                .actionButtonTextWithGoTo("navigation.funds.availableForWithdrawal")
                                .onAction(() -> navigation.navigateTo(MainView.class, FundsView.class, WithdrawalView.class))
                                .dontShowAgainId(key)
                                .show();
                    }
                },
                (message) -> {
                    log.error(message);
                    new Popup().warning(Res.get("offerbook.removeOffer.failed", message)).show();
                });
    }

    private void onEditPendingOffer(Offer pendingOffer) {
        if (model.isBootstrappedOrShowPopup()) {
            //pendingOfferActionHandler.onEditPendingOffer(pendingOffer);
        }
    }

    private void setOfferIdColumnCellFactory() {
        offerIdColumn.setCellValueFactory((XmrAddressEntry) -> new ReadOnlyObjectWrapper<>(XmrAddressEntry.getValue()));
        offerIdColumn.getStyleClass().addAll("number-column", "first-column");
        offerIdColumn.setCellFactory(
                new Callback<>() {

                    @Override
                    public TableCell<XmrAddressEntry, XmrAddressEntry> call(TableColumn<XmrAddressEntry,
                            XmrAddressEntry> column) {
                        return new TableCell<>() {
                            private HyperlinkWithIcon field;

                            @Override
                            public void updateItem(final XmrAddressEntry item, boolean empty) {
                                super.updateItem(item, empty);

                                if (item != null && !empty) {
                                    field = new HyperlinkWithIcon(item.getOfferId());
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


    private void setContextColumnCellFactory() {
        contextColumn.setCellValueFactory((XmrAddressEntry) -> new ReadOnlyObjectWrapper<>(XmrAddressEntry.getValue()));
        contextColumn.getStyleClass().addAll("number-column", "first-column");
        contextColumn.setCellFactory(
                new Callback<>() {

                    @Override
                    public TableCell<XmrAddressEntry, XmrAddressEntry> call(TableColumn<XmrAddressEntry,
                            XmrAddressEntry> column) {
                        return new TableCell<>() {
                            private HyperlinkWithIcon field;

                            @Override
                            public void updateItem(final XmrAddressEntry item, boolean empty) {
                                super.updateItem(item, empty);

                                if (item != null && !empty) {
                                    field = new HyperlinkWithIcon(item.getContext().toString());
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

    private void setAddressColumnCellFactory() {
        addressColumn.setCellValueFactory((XmrAddressEntry) -> new ReadOnlyObjectWrapper<>(XmrAddressEntry.getValue()));
        addressColumn.getStyleClass().addAll("number-column", "first-column");
        addressColumn.setCellFactory(
                new Callback<>() {

                    @Override
                    public TableCell<XmrAddressEntry, XmrAddressEntry> call(TableColumn<XmrAddressEntry,
                            XmrAddressEntry> column) {
                        return new TableCell<>() {
                            private HyperlinkWithIcon field;

                            @Override
                            public void updateItem(final XmrAddressEntry item, boolean empty) {
                                super.updateItem(item, empty);

                                if (item != null && !empty) {
                                    field = new HyperlinkWithIcon(item.getAddressString());
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

    private void setLockedInMultiColumnCellFactory() {
        lockedInMultiColumn.setCellValueFactory((XmrAddressEntry) -> new ReadOnlyObjectWrapper<>(XmrAddressEntry.getValue()));
        lockedInMultiColumn.getStyleClass().addAll("number-column", "first-column");
        lockedInMultiColumn.setCellFactory(
                new Callback<>() {

                    @Override
                    public TableCell<XmrAddressEntry, XmrAddressEntry> call(TableColumn<XmrAddressEntry,
                            XmrAddressEntry> column) {
                        return new TableCell<>() {
                            private HyperlinkWithIcon field;

                            @Override
                            public void updateItem(final XmrAddressEntry item, boolean empty) {
                                super.updateItem(item, empty);

                                if (item != null && !empty) {
                                    field = new HyperlinkWithIcon(item.getCoinLockedInMultiSig().toString());
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
//
//    private void setDateColumnCellFactory() {
//        dateColumn.setCellValueFactory((openOfferListItem) -> new ReadOnlyObjectWrapper<>(openOfferListItem.getValue()));
//        dateColumn.setCellFactory(
//                new Callback<>() {
//                    @Override
//                    public TableCell<PendingOfferListItem, PendingOfferListItem> call(
//                            TableColumn<PendingOfferListItem, PendingOfferListItem> column) {
//                        return new TableCell<>() {
//                            @Override
//                            public void updateItem(final PendingOfferListItem item, boolean empty) {
//                                super.updateItem(item, empty);
//                                getStyleClass().removeAll("offer-disabled");
//                                if (item != null) {
//                                    //if (model.isDeactivated(item)) getStyleClass().add("offer-disabled");
//                                    setGraphic(new AutoTooltipLabel(model.getDate(item)));
//                                } else {
//                                    setGraphic(null);
//                                }
//                            }
//                        };
//                    }
//                });
//    }
//
//    private void setAmountColumnCellFactory() {
//        amountColumn.setCellValueFactory((offer) -> new ReadOnlyObjectWrapper<>(offer.getValue()));
//        amountColumn.setCellFactory(
//                new Callback<>() {
//                    @Override
//                    public TableCell<XmrAddressEntry, XmrAddressEntry> call(
//                            TableColumn<XmrAddressEntry, XmrAddressEntry> column) {
//                        return new TableCell<>() {
//                            @Override
//                            public void updateItem(final XmrAddressEntry item, boolean empty) {
//                                super.updateItem(item, empty);
//                                getStyleClass().removeAll("offer-disabled");
//
//                                if (item != null) {
//                                    //if (model.isDeactivated(item)) getStyleClass().add("offer-disabled");
//                                    setGraphic(new AutoTooltipLabel(model.getAmount(item)));
//                                } else {
//                                    setGraphic(null);
//                                }
//                            }
//                        };
//                    }
//                });
//    }
//
//    private void setPriceColumnCellFactory() {
//        priceColumn.setCellValueFactory((offer) -> new ReadOnlyObjectWrapper<>(offer.getValue()));
//        priceColumn.setCellFactory(
//                new Callback<>() {
//                    @Override
//                    public TableCell<PendingOfferListItem, PendingOfferListItem> call(
//                            TableColumn<PendingOfferListItem, PendingOfferListItem> column) {
//                        return new TableCell<>() {
//                            @Override
//                            public void updateItem(final PendingOfferListItem item, boolean empty) {
//                                super.updateItem(item, empty);
//                                getStyleClass().removeAll("offer-disabled");
//
//                                if (item != null) {
//                                    //if (model.isDeactivated(item)) getStyleClass().add("offer-disabled");
//                                    setGraphic(new AutoTooltipLabel(model.getPrice(item)));
//                                } else {
//                                    setGraphic(null);
//                                }
//                            }
//                        };
//                    }
//                });
//    }
//
//    private void setDeviationColumnCellFactory() {
//        deviationColumn.setCellValueFactory((offer) -> new ReadOnlyObjectWrapper<>(offer.getValue()));
//        deviationColumn.setCellFactory(
//                new Callback<>() {
//                    @Override
//                    public TableCell<PendingOfferListItem, PendingOfferListItem> call(
//                            TableColumn<PendingOfferListItem, PendingOfferListItem> column) {
//                        return new TableCell<>() {
//                            @Override
//                            public void updateItem(final PendingOfferListItem item, boolean empty) {
//                                super.updateItem(item, empty);
//                                getStyleClass().removeAll("offer-disabled");
//
//                                if (item != null) {
//                                    //if (model.isDeactivated(item)) getStyleClass().add("offer-disabled");
//                                    AutoTooltipLabel autoTooltipLabel = new AutoTooltipLabel(model.getPriceDeviation(item));
//                                    autoTooltipLabel.setOpacity(item.getOffer().isUseMarketBasedPrice() ? 1 : 0.4);
//                                    setGraphic(autoTooltipLabel);
//                                } else {
//                                    setGraphic(null);
//                                }
//                            }
//                        };
//                    }
//                });
//    }
//
//    private void setTriggerPriceColumnCellFactory() {
//        triggerPriceColumn.setCellValueFactory((offerListItem) -> new ReadOnlyObjectWrapper<>(offerListItem.getValue()));
//        triggerPriceColumn.setCellFactory(
//                new Callback<>() {
//                    @Override
//                    public TableCell<PendingOfferListItem, PendingOfferListItem> call(
//                            TableColumn<PendingOfferListItem, PendingOfferListItem> column) {
//                        return new TableCell<>() {
//                            @Override
//                            public void updateItem(final PendingOfferListItem item, boolean empty) {
//                                super.updateItem(item, empty);
//                                getStyleClass().removeAll("offer-disabled");
//                                if (item != null) {
//                                    //if (model.isDeactivated(item)) getStyleClass().add("offer-disabled");
//                                    //setGraphic(new AutoTooltipLabel(model.getTriggerPrice(item)));
//                                } else {
//                                    setGraphic(null);
//                                }
//                            }
//                        };
//                    }
//                });
//    }
//
//    private void setVolumeColumnCellFactory() {
//        volumeColumn.setCellValueFactory((offer) -> new ReadOnlyObjectWrapper<>(offer.getValue()));
//        volumeColumn.setCellFactory(
//                new Callback<>() {
//                    @Override
//                    public TableCell<PendingOfferListItem, PendingOfferListItem> call(
//                            TableColumn<PendingOfferListItem, PendingOfferListItem> column) {
//                        return new TableCell<>() {
//                            @Override
//                            public void updateItem(final PendingOfferListItem item, boolean empty) {
//                                super.updateItem(item, empty);
//                                getStyleClass().removeAll("offer-disabled");
//
//                                if (item != null) {
//                                    //if (model.isDeactivated(item)) getStyleClass().add("offer-disabled");
//                                    setGraphic(new AutoTooltipLabel(model.getVolume(item)));
//                                } else {
//                                    setGraphic(null);
//                                }
//                            }
//                        };
//                    }
//                });
//    }
//
//    private void setPaymentMethodColumnCellFactory() {
//        paymentMethodColumn.setCellValueFactory((offer) -> new ReadOnlyObjectWrapper<>(offer.getValue()));
//        paymentMethodColumn.setCellFactory(
//                new Callback<>() {
//                    @Override
//                    public TableCell<PendingOfferListItem, PendingOfferListItem> call(
//                            TableColumn<PendingOfferListItem, PendingOfferListItem> column) {
//                        return new TableCell<>() {
//                            @Override
//                            public void updateItem(final PendingOfferListItem item, boolean empty) {
//                                super.updateItem(item, empty);
//                                getStyleClass().removeAll("offer-disabled");
//
//                                if (item != null) {
//                                    //if (model.isDeactivated(item)) getStyleClass().add("offer-disabled");
//                                    setGraphic(new AutoTooltipLabel(model.getPaymentMethod(item)));
//                                } else {
//                                    setGraphic(null);
//                                }
//                            }
//                        };
//                    }
//                });
//    }
//
//    private void setDirectionColumnCellFactory() {
//        directionColumn.setCellValueFactory((offer) -> new ReadOnlyObjectWrapper<>(offer.getValue()));
//        directionColumn.setCellFactory(
//                new Callback<>() {
//                    @Override
//                    public TableCell<PendingOfferListItem, PendingOfferListItem> call(
//                            TableColumn<PendingOfferListItem, PendingOfferListItem> column) {
//                        return new TableCell<>() {
//                            @Override
//                            public void updateItem(final PendingOfferListItem item, boolean empty) {
//                                super.updateItem(item, empty);
//                                getStyleClass().removeAll("offer-disabled");
//
//                                if (item != null) {
//                                    //if (model.isDeactivated(item)) getStyleClass().add("offer-disabled");
//                                    setGraphic(new AutoTooltipLabel(model.getDirectionLabel(item)));
//                                } else {
//                                    setGraphic(null);
//                                }
//                            }
//                        };
//                    }
//                });
//    }
//
//    private void setMarketColumnCellFactory() {
//        marketColumn.setCellValueFactory((offer) -> new ReadOnlyObjectWrapper<>(offer.getValue()));
//        marketColumn.setCellFactory(
//                new Callback<>() {
//                    @Override
//                    public TableCell<PendingOfferListItem, PendingOfferListItem> call(
//                            TableColumn<PendingOfferListItem, PendingOfferListItem> column) {
//                        return new TableCell<>() {
//                            @Override
//                            public void updateItem(final PendingOfferListItem item, boolean empty) {
//                                super.updateItem(item, empty);
//                                getStyleClass().removeAll("offer-disabled");
//
//                                if (item != null) {
//                                    //if (model.isDeactivated(item)) getStyleClass().add("offer-disabled");
//                                    setGraphic(new AutoTooltipLabel(model.getMarketLabel(item)));
//                                } else {
//                                    setGraphic(null);
//                                }
//                            }
//                        };
//                    }
//                });
//    }
//
//    private void setDeactivateColumnCellFactory() {
//        deactivateItemColumn.setCellValueFactory((offerListItem) -> new ReadOnlyObjectWrapper<>(offerListItem.getValue()));
//        deactivateItemColumn.setCellFactory(
//                new Callback<>() {
//                    @Override
//                    public TableCell<PendingOfferListItem, PendingOfferListItem> call(TableColumn<PendingOfferListItem, PendingOfferListItem> column) {
//                        return new TableCell<>() {
//                            final ImageView iconView = new ImageView();
//                            AutoTooltipSlideToggleButton checkBox;
//
////                            private void updateState(@NotNull Offer pendingOffer) {
////                                checkBox.setSelected(!pendingOffer.isDeactivated());
////                            }
//
//                            @Override
//                            public void updateItem(final PendingOfferListItem item, boolean empty) {
//                                super.updateItem(item, empty);
//
//                                if (item != null && !empty) {
//                                    Offer pendingOffer = item.getPendingOffer();
//                                    if (checkBox == null) {
//                                        checkBox = new AutoTooltipSlideToggleButton();
//                                        checkBox.setPadding(new Insets(-7, 0, -7, 0));
//                                        checkBox.setGraphic(iconView);
//                                    }
//                                    //checkBox.setDisable(model.dataModel.wasTriggered(pendingOffer));
//                                    checkBox.setOnAction(event -> {
////                                        if (openOffer.isDeactivated()) {
////                                            onActivateOpenOffer(openOffer);
////                                        } else {
////                                            onDeactivateOpenOffer(openOffer);
////                                        }
//                                        //updateState(pendingOffer);
//                                        tableView.refresh();
//                                    });
//                                    //updateState(openOffer);
//                                    setGraphic(checkBox);
//                                } else {
//                                    setGraphic(null);
//                                    if (checkBox != null) {
//                                        checkBox.setOnAction(null);
//                                        checkBox = null;
//                                    }
//                                }
//                            }
//                        };
//                    }
//                });
//    }
//
//    private void setRemoveColumnCellFactory() {
//        removeItemColumn.getStyleClass().addAll("last-column", "avatar-column");
//        removeItemColumn.setCellValueFactory((offerListItem) -> new ReadOnlyObjectWrapper<>(offerListItem.getValue()));
//        removeItemColumn.setCellFactory(
//                new Callback<>() {
//                    @Override
//                    public TableCell<PendingOfferListItem, PendingOfferListItem> call(TableColumn<PendingOfferListItem, PendingOfferListItem> column) {
//                        return new TableCell<>() {
//                            Button button;
//
//                            @Override
//                            public void updateItem(final PendingOfferListItem item, boolean empty) {
//                                super.updateItem(item, empty);
//
//                                if (item != null && !empty) {
//                                    if (button == null) {
//                                        button = getRegularIconButton(MaterialDesignIcon.DELETE_FOREVER, "delete");
//                                        button.setTooltip(new Tooltip(Res.get("shared.removeOffer")));
//                                        setGraphic(button);
//                                    }
//                                    button.setOnAction(event -> onRemovePendingOffer(item.getPendingOffer()));
//                                } else {
//                                    setGraphic(null);
//                                    if (button != null) {
//                                        button.setOnAction(null);
//                                        button = null;
//                                    }
//                                }
//                            }
//                        };
//                    }
//                });
//    }
//
//    private void setTriggerIconColumnCellFactory() {
//        triggerIconColumn.setCellValueFactory((offerListItem) -> new ReadOnlyObjectWrapper<>(offerListItem.getValue()));
//        triggerIconColumn.setCellFactory(
//                new Callback<>() {
//                    @Override
//                    public TableCell<PendingOfferListItem, PendingOfferListItem> call(TableColumn<PendingOfferListItem, PendingOfferListItem> column) {
//                        return new TableCell<>() {
//                            Button button;
//
//                            @Override
//                            public void updateItem(final PendingOfferListItem item, boolean empty) {
//                                super.updateItem(item, empty);
//
//                                if (item != null && !empty) {
//                                    if (button == null) {
//                                        button = getRegularIconButton(MaterialDesignIcon.SHIELD_HALF_FULL);
//                                        //boolean triggerPriceSet = item.getPendingOffer().getTriggerPrice() > 0;
//                                        //button.setVisible(triggerPriceSet);
//
////                                        if (model.dataModel.wasTriggered(item.getPendingOffer())) {
////                                            button.getGraphic().getStyleClass().add("warning");
////                                            button.setTooltip(new Tooltip(Res.get("openOffer.triggered")));
////                                        } else {
////                                            button.getGraphic().getStyleClass().remove("warning");
////                                            //button.setTooltip(new Tooltip(Res.get("openOffer.triggerPrice", model.getTriggerPrice(item))));
////                                        }
//                                        setGraphic(button);
//                                    }
//                                    button.setOnAction(event -> onEditPendingOffer(item.getPendingOffer()));
//                                } else {
//                                    setGraphic(null);
//                                    if (button != null) {
//                                        button.setOnAction(null);
//                                        button = null;
//                                    }
//                                }
//                            }
//                        };
//                    }
//                });
//    }
//
//    private void setEditColumnCellFactory() {
//        editItemColumn.setCellValueFactory((offerListItem) -> new ReadOnlyObjectWrapper<>(offerListItem.getValue()));
//        editItemColumn.setCellFactory(
//                new Callback<>() {
//                    @Override
//                    public TableCell<PendingOfferListItem, PendingOfferListItem> call(TableColumn<PendingOfferListItem, PendingOfferListItem> column) {
//                        return new TableCell<>() {
//                            Button button;
//
//                            @Override
//                            public void updateItem(final PendingOfferListItem item, boolean empty) {
//                                super.updateItem(item, empty);
//
//                                if (item != null && !empty) {
//                                    if (button == null) {
//                                        button = getRegularIconButton(MaterialDesignIcon.PENCIL);
//                                        button.setTooltip(new Tooltip(Res.get("shared.editOffer")));
//                                        setGraphic(button);
//                                    }
//                                    button.setOnAction(event -> onEditPendingOffer(item.getPendingOffer()));
//                                } else {
//                                    setGraphic(null);
//                                    if (button != null) {
//                                        button.setOnAction(null);
//                                        button = null;
//                                    }
//                                }
//                            }
//                        };
//                    }
//                });
//    }

    public void setPendingOfferActionHandler(PortfolioView.PendingOfferActionHandler pendingOfferActionHandler) {
        this.pendingOfferActionHandler = pendingOfferActionHandler;
    }
}

