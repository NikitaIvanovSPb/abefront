package ru.nikita.abeserver.client.files;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.*;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import org.gwtbootstrap3.client.ui.*;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.InputType;
import org.gwtbootstrap3.client.ui.gwt.CellTable;
import org.gwtbootstrap3.client.ui.gwt.FormPanel;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;
import ru.nikita.abeserver.client.dto.*;
import ru.nikita.abeserver.client.users.TokensDialog;
import ru.nikita.abeserver.client.util.ButtonCell;
import ru.nikita.abeserver.client.util.Modals;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class UploadDialog extends Modal {
    private final FormPanel form;
    Logger log = Logger.getLogger("UploadDialog");
    List<FTPDTO> data;
    Select select;
    Input selectValue;
    Button sendButton;
    Select attrSelect;
    Input attrValueInput;
    CellTable<TableAttr> attrsTable;
    Input hideInput;
    Map<String, AttributeDTO> attrs = new HashMap<>();
    private AsyncDataProvider<TableAttr> attrsProvider;
    List<TableAttr> attrsTableData = new LinkedList<>();
    private DTOFactory factory = GWT.create(DTOFactory.class);
    public UploadDialog() {
        super();
        setTitle("Загрузка файлов");
        ModalBody body = new ModalBody();
        ModalFooter foot = new ModalFooter();
        form = new FormPanel();


        form.setEncoding(FormPanel.ENCODING_MULTIPART);
        form.setMethod(FormPanel.METHOD_POST);

        VerticalPanel panel = new VerticalPanel();
        form.setWidget(panel);
        Span nameSpan = new Span();
        nameSpan.setText("Название с расширением");
        Input name = new Input(InputType.TEXT);
        name.setName("name");
        Span attrSelectSpan = new Span();
        attrSelectSpan.setText("Задайте необходимые атрибуты");


        HorizontalPanel horizontalPanel = new HorizontalPanel();
        attrSelect = new Select();
        attrValueInput = new Input(InputType.TEXT);
        Button addButton = new Button();
        horizontalPanel.add(attrValueInput);
        horizontalPanel.add(addButton);
        addButton.setType(ButtonType.SUCCESS);
        addButton.setIcon(IconType.PLUS);
        addButton.addClickHandler(clickEvent -> {
            if(attrValueInput.getText() == null || attrValueInput.getText().isEmpty()) return;
            boolean setted = false;
            for(TableAttr tableAttr : attrsTableData){
                if(tableAttr.getAttributeDTO().getValName().equals(attrSelect.getValue())){
                    tableAttr.getValues().add(attrValueInput.getText());
                    setted = true;
                }
            }
            if(!setted) {
                TableAttr tableAttr = new TableAttr(attrs.get(attrSelect.getValue()));
                tableAttr.getValues().add(attrValueInput.getText());
                attrsTableData.add(tableAttr);
            }
            updateAttrsTableDate();
        });
        attrsTable = new CellTable<>();
        attrsTable.addColumn(new TextColumn<TableAttr>() {
            @Override
            public String getValue(TableAttr tableAttr) {
                return tableAttr.getAttributeDTO().getRusName() + "(" + tableAttr.getAttributeDTO().getValName() + ")";
            }
        }, "Атрибут");
        attrsTable.addColumn(new TextColumn<TableAttr>() {
            @Override
            public String getValue(TableAttr tableAttr) {
                StringBuilder sb = new StringBuilder();
                for(int i = 0; i < tableAttr.getValues().size(); i++){
                    sb.append(tableAttr.getValues().get(i));
                    if(i != tableAttr.getValues().size()-1) sb.append(" OR ");

                }
                return sb.toString();
            }
        }, "Значение");
        com.google.gwt.user.cellview.client.Column<TableAttr, String> remove = new Column<TableAttr, String>(new ButtonCell(IconType.REMOVE, ButtonType.DANGER, ButtonSize.EXTRA_SMALL, true)) {
            @Override
            public String getValue(TableAttr fileDTO) {
                return "";
            }
        };
        remove.setFieldUpdater((i, tableAttr, s) -> {
            attrsTableData.remove(tableAttr);
            updateAttrsTableDate();
        });
        attrsTable.addColumn(remove);
        attrsTable.setColumnWidth(2,"1px");
        attrsProvider = new AsyncDataProvider<TableAttr>() {
            @Override
            protected void onRangeChanged(HasData<TableAttr> hasData) {
            }
        };
        attrsProvider.addDataDisplay(attrsTable);

        Span selectSpan = new Span();
        selectSpan.setText("FTP сервер");
        select = new Select();
        select.addValueChangeHandler(valueChangeEvent -> {
            selectValue.setValue(valueChangeEvent.getValue());
        });
        selectValue = new Input(InputType.TEXT);
        selectValue.setVisible(false);
        selectValue.setName("ftpId");
        hideInput = new Input(InputType.TEXT);
        hideInput.setVisible(false);
        hideInput.setName("attributes");
        panel.add(nameSpan);
        panel.add(name);
        panel.add(hideInput);
        panel.add(selectSpan);
        panel.add(select);
        panel.add(selectValue);
        sendButton = new Button("Загрузить", event -> {
            StringBuilder attrs = new StringBuilder();
            for(int j = 0; j < attrsTableData.size(); j++){
                TableAttr attr = attrsTableData.get(j);
                for(int i =0; i< attr.getValues().size(); i++){
                    if(i==0) attrs.append("(");
                    String item = attr.getValues().get(i);
                    attrs.append(attr.getAttributeDTO().getValName())
                            .append("_")
                            .append(item.replaceAll(" ", "_"));
                    if(i!=attr.getValues().size()-1) attrs.append(" OR ");
                    if(i==attr.getValues().size()-1) attrs.append(")");
                }
                if(j != attrsTableData.size()-1) attrs.append(" AND ");
            }
            hideInput.setValue(attrs.toString());
            form.setAction(GWT.getHostPageBaseURL() + "upload");
            form.submit();
        });
        sendButton.setEnabled(false);
        sendButton.setType(ButtonType.SUCCESS);

        FileUpload upload = new FileUpload();
        upload.setName("upload");
        upload.addChangeHandler(event -> sendButton.setEnabled(upload.getFilename() != null));
        panel.add(upload);
        panel.add(attrSelectSpan);
        panel.add(attrSelect);
        panel.add(horizontalPanel);
        panel.add(attrsTable);
        panel.setWidth("100%");
        foot.add(sendButton);

        Button cancel = new Button("Отменить");
        cancel.addClickHandler(event -> hide());
        foot.add(cancel);
        body.add(form);

        add(body);
        add(foot);
        updateValues();
        getAttrs();
        updateAttrsTableDate();
    }

    void getAttrs(){
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, GWT.getHostPageBaseURL()+"attrs");
        requestBuilder.setHeader("Content-Type", "application/json");
        try {
            requestBuilder.sendRequest(null, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    AutoBean<AttributeDTOList> bean = AutoBeanCodex.decode(factory, AttributeDTOList.class, response.getText());
                    AttributeDTOList list = bean.as();
                    attrSelect.clear();
                    attrs.clear();
                    for (AttributeDTO attr : list.getResults()) {
                        Option option = new Option();
                        option.setText(attr.getRusName() + "(" + attr.getValName() + ")");
                        option.setValue(attr.getValName());
                        attrs.put(attr.getValName(), attr);
                        attrSelect.add(option);
                    }
                    attrSelect.refresh();
                }

                @Override
                public void onError(Request request, Throwable throwable) {

                }
            });
        } catch (RequestException e) {
            e.printStackTrace();
        }

    }

    void updateAttrsTableDate(){
        attrsProvider.updateRowCount(attrsTableData.size(), true);
        attrsProvider.updateRowData(0, attrsTableData);
    }

    private void updateValues() {
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, GWT.getHostPageBaseURL() + "ftp");
        requestBuilder.setHeader("Content-Type", "application/json");
        try {
            requestBuilder.sendRequest(null, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == 200) {
                        AutoBean<FTPDTOList> bean = AutoBeanCodex.decode(factory, FTPDTOList.class, response.getText());
                        FTPDTOList list = bean.as();
                        data = list.getResults();
                        select.clear();
                        for (FTPDTO ftpdto : list.getResults()) {
                            Option option = new Option();
                            option.setText(ftpdto.getUrl()+":"+ftpdto.getPort());
                            option.setValue(ftpdto.getId().toString());
                            select.add(option);
                        }
                        selectValue.setValue(select.getItems().get(0).getValue());
                        select.refresh();
                        if(select.getItems().size() == 0){
                            sendButton.setEnabled(false);
                        }
                    } else {
                        Modals.natificationModal("Произошла ошибка при загрузке данных!", "Ок", null);
                    }
                }

                @Override
                public void onError(Request request, Throwable throwable) {
                    Modals.natificationModal("Произошла ошибка при загрузке данных!", "Ок", null);
                }
            });
        } catch (RequestException e) {
            e.printStackTrace();
        }
    }

    public void setFinishAction(com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler submitCompleteHandler) {
        form.addSubmitCompleteHandler(submitCompleteHandler);
    }

    private class TableAttr{
        private List<String> values = new LinkedList<>();
        private AttributeDTO attributeDTO;

        public TableAttr(AttributeDTO attributeDTO) {
            this.attributeDTO = attributeDTO;
        }

        public List<String> getValues() {
            return values;
        }

        public void setValues(List<String> values) {
            this.values = values;
        }

        public AttributeDTO getAttributeDTO() {
            return attributeDTO;
        }

        public void setAttributeDTO(AttributeDTO attributeDTO) {
            this.attributeDTO = attributeDTO;
        }
    }
}
