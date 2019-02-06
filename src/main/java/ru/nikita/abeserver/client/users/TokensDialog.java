package ru.nikita.abeserver.client.users;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.*;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
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
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.extras.datepicker.client.ui.DatePicker;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;
import ru.nikita.abeserver.client.dto.*;
import ru.nikita.abeserver.client.util.ButtonCell;

import java.util.*;

public class TokensDialog extends Modal {
    Map<String, AttributeDTO> attrs = new HashMap<>();
    private AsyncDataProvider<TokenDTO> provider;
    private AsyncDataProvider<TableAttr> attrsProvider;
    List<TableAttr> attrsTableData = new LinkedList<>();
    private DTOFactory factory = GWT.create(DTOFactory.class);
    @UiField
    CellTable<TableAttr> attrsTable;
    @UiField
    CellTable<TokenDTO> table;
    @UiField
    SimplePager pager = new SimplePager();
    UserDTO user;
    DatePicker dateInput;
    Select attrSelect;
    Input attrValueInput;
    public TokensDialog(UserDTO userDTO) {
        super();
        this.user = userDTO;
        setTitle("Ключи пользователя " + userDTO.getLastName() + " " + userDTO.getName());
        ModalBody body = new ModalBody();
        ModalFooter foot = new ModalFooter();
        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.setWidth("100%");
        HorizontalPanel horizontalPanel = new HorizontalPanel();
        Span attrSelectSpan = new Span();
        attrSelectSpan.setText("Задайте необходимые атрибуты");
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
            table.redraw();
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
                    if(i != tableAttr.getValues().size()-1) sb.append(", ");

                }
                return sb.toString();
            }
        }, "Значение");
        Column<TableAttr, String> remove = new Column<TableAttr, String>(new ButtonCell(IconType.REMOVE, ButtonType.DANGER, ButtonSize.EXTRA_SMALL, true)) {
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

        Span attrSpan = new Span();
        attrSpan.setText("Введите дату истечения действия ключа");
        dateInput = new DatePicker();
        Button button = new Button("Добавить");
        button.addClickHandler(clickEvent -> {
            createToken();
        });

        table = new CellTable<>();
        table.addColumn(new TextColumn<TokenDTO>() {
            @Override
            public String getValue(TokenDTO tokenDTO) {
                return tokenDTO.getGuid();
            }
        }, "ID");
        table.addColumn(new TextColumn<TokenDTO>() {
            @Override
            public String getValue(TokenDTO tokenDTO) {
                return tokenDTO.getAttrbutes().replaceAll("\\|", ", ");
            }
        }, "Атрибуты");
        table.addColumn(new TextColumn<TokenDTO>() {
            @Override
            public String getValue(TokenDTO tokenDTO) {
                if(tokenDTO.getUsed()){
                    return "Использован " + new Date(tokenDTO.getGenerate());
                }else {
                    return "Не использован";
                }
            }
        }, "Статус");
        Column<TokenDTO, String> delete = new Column<TokenDTO, String>(new ButtonCell(IconType.REMOVE, ButtonType.DANGER, ButtonSize.EXTRA_SMALL, true)) {
            @Override
            public String getValue(TokenDTO tokenDTO) {
                return "";
            }
        };
        table.addColumn(delete);
        delete.setFieldUpdater((i, tokenDTO, s) -> {
            deleteToken(tokenDTO);
        });

        provider = new AsyncDataProvider<TokenDTO>() {
            @Override
            protected void onRangeChanged(HasData<TokenDTO> hasData) {
            }
        };
        provider.addDataDisplay(table);
        attrsProvider = new AsyncDataProvider<TableAttr>() {
            @Override
            protected void onRangeChanged(HasData<TableAttr> hasData) {
            }
        };
        attrsProvider.addDataDisplay(attrsTable);
        pager.setDisplay(table);
        table.setCondensed(true);
        verticalPanel.add(attrSpan);
        verticalPanel.add(dateInput);
        verticalPanel.add(attrSelectSpan);
        verticalPanel.add(attrSelect);
        verticalPanel.add(horizontalPanel);
        verticalPanel.add(attrsTable);
        verticalPanel.add(button);
        verticalPanel.add(table);
        body.add(verticalPanel);
        Button cancel = new Button("Закрыть");
        cancel.addClickHandler(event -> {
            hide();
        });
        foot.add(cancel);
        add(body);
        add(foot);
        updateTokens();
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

    void updateTokens(){
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.POST, GWT.getHostPageBaseURL()+"tokens/get");
        requestBuilder.setHeader("Content-Type", "application/json");
        try {
            requestBuilder.sendRequest("{\"userId\":\"" + user.getId()+ "\"}", new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    AutoBean<TokenDTOList> bean = AutoBeanCodex.decode(factory, TokenDTOList.class, response.getText());
                    TokenDTOList list = bean.as();
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

    void deleteToken(TokenDTO tokenDTO){
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.DELETE, GWT.getHostPageBaseURL()+"tokens");
        requestBuilder.setHeader("Content-Type", "application/json");
        try {
            requestBuilder.sendRequest("{\"guid\":\"" + tokenDTO.getGuid()+ "\"}", new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    updateTokens();
                }

                @Override
                public void onError(Request request, Throwable throwable) {

                }
            });
        } catch (RequestException e) {
            e.printStackTrace();
        }
    }

    void createToken(){
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.POST, GWT.getHostPageBaseURL()+"tokens");
        try {
            requestBuilder.setHeader("Content-Type", "application/json");
            StringBuilder attrs = new StringBuilder();
            for(TableAttr attr : attrsTableData){
                attr.getValues().forEach(item -> {
                    attrs.append(attr.getAttributeDTO().getValName())
                            .append("_")
                            .append(item.replaceAll(" ", "_"))
                            .append("|");
                });
            }
            attrs.deleteCharAt(attrs.lastIndexOf("|"));
            requestBuilder.sendRequest("{" +
                        "\"userId\":\"" + user.getId()+ "\", " +
                        "\"expTime\":" + dateInput.getValue().getTime() + ", " +
                        "\"attrs\":\""+attrs.toString()+"\"" +
                    "}", new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    updateTokens();
                }

                @Override
                public void onError(Request request, Throwable throwable) {

                }
            });
        } catch (RequestException e) {
            e.printStackTrace();
        }
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
