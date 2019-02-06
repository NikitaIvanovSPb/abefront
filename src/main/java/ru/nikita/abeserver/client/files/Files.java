package ru.nikita.abeserver.client.files;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.*;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.gwt.CellTable;
import ru.nikita.abeserver.client.dto.DTOFactory;
import ru.nikita.abeserver.client.dto.FileDTO;
import ru.nikita.abeserver.client.dto.FileDTOList;
import ru.nikita.abeserver.client.util.ButtonCell;
import ru.nikita.abeserver.client.util.Modals;

import java.util.Date;
import java.util.logging.Logger;


public class Files extends Composite {

    interface FilesUiBinder extends UiBinder<FlowPanel, Files> {
    }

    private static FilesUiBinder ourUiBinder = GWT.create(FilesUiBinder.class);
    private AsyncDataProvider<FileDTO> provider;
    private DTOFactory factory = GWT.create(DTOFactory.class);
    Logger log = Logger.getLogger("Files");
    FileDTO fileForDelete;
    @UiField
    CellTable<FileDTO> table;
    @UiField
    SimplePager pager;
    @UiField
    Modal deleteDialog;
    @UiField
    Button deleteDialogOk;
    @UiField
    Button deleteDialogCancel;
    @UiField
    Button badd;
    @UiField
    Button bsetting;
    @UiField
    Button bftpsetting;

    public Files() {
        initWidget(ourUiBinder.createAndBindUi(this));
        badd.addClickHandler(clickEvent -> {
            UploadDialog uploadDialog = new UploadDialog();
            uploadDialog.setFinishAction(event -> {
                uploadDialog.hide();
                updateFiles();
            });
            uploadDialog.show();
        });
        bsetting.addClickHandler(clickEvent -> {
            SettingsMasterKeyDialog settingsMasterKeyDialog = new SettingsMasterKeyDialog();
            settingsMasterKeyDialog.show();
        });
        bftpsetting.addClickHandler(clickEvent -> {
            SettingsFTPDialog settingsFTPDialog = new SettingsFTPDialog();
            settingsFTPDialog.show();
        });
        createTable();
        updateFiles();
    }

    void updateFiles(){
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, GWT.getHostPageBaseURL()+"files");
        try {
            requestBuilder.sendRequest(null, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    AutoBean<FileDTOList> bean = AutoBeanCodex.decode(factory, FileDTOList.class, response.getText());
                    String str =  AutoBeanCodex.encode(bean).getPayload();
                    FileDTOList list = bean.as();
                    provider.updateRowCount(list.getResults().size(), true);
                    provider.updateRowData(0, list.getResults());
                }

                @Override
                public void onError(Request request, Throwable throwable) {

                }
            });
        } catch (RequestException e) {
            e.printStackTrace();
        }
    }

    void createTable(){
        table.addColumn(new TextColumn<FileDTO>() {
            @Override
            public String getValue(FileDTO fileDTO) {
                return fileDTO.getName();
            }
        }, "Название");
        table.addColumn(new TextColumn<FileDTO>() {
            @Override
            public String getValue(FileDTO fileDTO) {
                return fileDTO.getGuid();
            }
        }, "ID");
        table.addColumn(new TextColumn<FileDTO>() {
            @Override
            public String getValue(FileDTO fileDTO) {
                return fileDTO.getFtp().getUrl() + ":" + fileDTO.getFtp().getPort();
            }
        }, "FTP сервер");
        table.addColumn(new TextColumn<FileDTO>() {
            @Override
            public String getValue(FileDTO fileDTO) {
                return DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_SHORT).format(new Date(fileDTO.getCreate()));
            }
        }, "Время создания");
        table.addColumn(new TextColumn<FileDTO>() {
            @Override
            public String getValue(FileDTO fileDTO) {
                return fileDTO.getAttributes();
            }
        }, "Необходимые атрибуты");
        Column<FileDTO, String> send = new Column<FileDTO, String>(new ButtonCell(IconType.SEND, ButtonType.PRIMARY, ButtonSize.EXTRA_SMALL, true)) {
            @Override
            public String getValue(FileDTO fileDTO) {
                return "";
            }
        };
        send.setFieldUpdater((i, fileDTO, s) -> {
            SendDialog sendDialog = new SendDialog(fileDTO);
            sendDialog.show();
        });
        table.addColumn(send);
        table.setColumnWidth(4,"1px");
        Column<FileDTO, String> groups = new Column<FileDTO, String>(new ButtonCell(IconType.GROUP, ButtonType.PRIMARY, ButtonSize.EXTRA_SMALL, true)) {
            @Override
            public String getValue(FileDTO fileDTO) {
                return "";
            }
        };
        groups.setFieldUpdater((i, fileDTO, s) -> {
            // TODO realize
        });
        table.addColumn(groups);
        table.setColumnWidth(5,"1px");
        Column<FileDTO, String> remove = new Column<FileDTO, String>(new ButtonCell(IconType.REMOVE, ButtonType.DANGER, ButtonSize.EXTRA_SMALL, true)) {
            @Override
            public String getValue(FileDTO fileDTO) {
                return "";
            }
        };
        remove.setFieldUpdater((i, fileDTO, s) -> {
            fileForDelete = fileDTO;
            deleteDialog.show();
        });
        table.addColumn(remove);
        table.setColumnWidth(6,"1px");
        deleteDialogCancel.addClickHandler(clickEvent -> deleteDialog.hide());
        deleteDialogOk.addClickHandler(clickEvent -> {
            RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.DELETE, GWT.getHostPageBaseURL()+"files/" + fileForDelete.getGuid());
            try {
                requestBuilder.sendRequest(null, new RequestCallback() {
                    @Override
                    public void onResponseReceived(Request request, Response response) {
                        Modals.natificationModal("Файл успешно удалён!", "Ок", () -> updateFiles());
                    }

                    @Override
                    public void onError(Request request, Throwable throwable) {
                        Modals.natificationModal("Произошла ошибка, файл не удалён!", "Ок", null);
                    }
                });
            } catch (RequestException e) {
                e.printStackTrace();
            }
        });
        provider = new AsyncDataProvider<FileDTO>() {
            @Override
            protected void onRangeChanged(HasData<FileDTO> hasData) {
            }
        };
        provider.addDataDisplay(table);
        pager.setDisplay(table);
        table.setCondensed(true);
    }
}