package ru.nikita.abeserver.client.files;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.*;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import org.gwtbootstrap3.client.ui.*;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.InputType;
import org.gwtbootstrap3.client.ui.gwt.FormPanel;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;
import ru.nikita.abeserver.client.dto.*;
import ru.nikita.abeserver.client.util.Modals;

import java.util.List;
import java.util.logging.Logger;

public class SettingsMasterKeyDialog extends Modal {
    Logger log = Logger.getLogger("UploadDialog");
    Paragraph nameParagraph;
    private DTOFactory factory = GWT.create(DTOFactory.class);
    Select select;
    List<KeyDTO> data;
    KeyDTO activeKey;

    public SettingsMasterKeyDialog() {
        super();

        setTitle("Настройки шифрования");
        ModalBody body = new ModalBody();
        ModalFooter foot = new ModalFooter();

        VerticalPanel panel = new VerticalPanel();
        VerticalPanel verticalPanel1 = new VerticalPanel();
        Span nameSpan = new Span();
        nameSpan.setText("В данный момент установлен ключ:");
        nameParagraph = new Paragraph();
        verticalPanel1.add(nameSpan);
        verticalPanel1.add(nameParagraph);
        Button deactivateButton = new Button("Деактивировать ключ");
        deactivateButton.setType(ButtonType.DANGER);
        deactivateButton.addClickHandler(clickEvent -> {
            unsetKey();
        });
        verticalPanel1.add(deactivateButton);
        Span newNameSpan = new Span();
        newNameSpan.setText("Сгенерировать новый ключ:");
        Input newNameInput = new Input(InputType.TEXT);
        newNameInput.setPlaceholder("Имя");
        Button newNameSaveButton = new Button("Сгенерировать");
        newNameInput.addChangeHandler(changeEvent -> {
            newNameSaveButton.setEnabled(!newNameInput.getValue().isEmpty());
        });
        newNameSaveButton.setEnabled(false);
        newNameSaveButton.setType(ButtonType.PRIMARY);
        newNameSaveButton.addClickHandler(clickEvent -> {
            createkey(newNameInput.getValue());
            newNameInput.setValue("");
        });
        newNameInput.addKeyUpHandler(changeEvent -> {
            newNameSaveButton.setEnabled(!newNameInput.getValue().isEmpty());
        });
        Span setKeySpan = new Span();
        setKeySpan.setText("Выберете ключ и необходимое действие");
        select = new Select();
        HorizontalPanel horizontalPanel2 = new HorizontalPanel();
        Button setButton = new Button("Сделать активным");
        setButton.setType(ButtonType.SUCCESS);
        setButton.addClickHandler(clickEvent -> {
            setKey();
        });
        Button deleteKeyButton = new Button("Удалить ключ из базы данных");
        deleteKeyButton.setType(ButtonType.DANGER);
        deleteKeyButton.addClickHandler(clickEvent -> {
            deleteKey();
        });
        horizontalPanel2.add(setButton);
        horizontalPanel2.add(deleteKeyButton);


        panel.add(verticalPanel1);
        panel.add(newNameSpan);
        panel.add(newNameInput);
        panel.add(newNameSaveButton);
        panel.add(setKeySpan);
        panel.add(select);
        panel.add(horizontalPanel2);
        Button cancel = new Button("Закрыть");
        cancel.addClickHandler(event -> {
            hide();
        });
        body.add(panel);
        foot.add(cancel);
        add(body);
        add(foot);
        updateValues();
    }


    private void updateValues() {
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, GWT.getHostPageBaseURL() + "keys");
        requestBuilder.setHeader("Content-Type", "application/json");
        try {
            requestBuilder.sendRequest(null, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == 200) {
                        AutoBean<KeyDTOList> bean = AutoBeanCodex.decode(factory, KeyDTOList.class, response.getText());
                        KeyDTOList list = bean.as();
                        data = list.getResults();
                        activeKey = null;
                        select.clear();
                        for (KeyDTO keyDTO : list.getResults()) {
                            Option option = new Option();
                            option.setText(keyDTO.getName());
                            option.setValue(keyDTO.getId().toString());
                            if (keyDTO.getActive()) {
                                activeKey = keyDTO;
                                option.setSelected(true);
                            }
                            select.add(option);
                        }
                        select.refresh();
                        if (activeKey != null) nameParagraph.setText(activeKey.getName());
                        else nameParagraph.setText("Ключ не установлен!");

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

    private void setKey() {
        String selectedItem = select.getValue();
        if (selectedItem == null) {
            Modals.natificationModal("Ключ для установки не выбран, выберите ключ и повторите попытку!", "Ок", null);
            return;
        }
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.POST, GWT.getHostPageBaseURL() + "keys/set");
        requestBuilder.setHeader("Content-Type", "application/json");
        try {
            requestBuilder.sendRequest("{\"id\": " + selectedItem + "}", new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == 200) {
                        Modals.natificationModal("Ключ успешно установлен!", "Ок", null);
                        updateValues();
                    } else {
                        Modals.natificationModal("Произошла ошибка!", "Ок", null);
                    }
                }

                @Override
                public void onError(Request request, Throwable throwable) {
                    Modals.natificationModal("Произошла ошибка!", "Ок", null);
                }
            });
        } catch (RequestException e) {
            e.printStackTrace();
        }
    }

    private void unsetKey() {
        if (activeKey == null) {
            Modals.natificationModal("Активный ключ отсутствует!", "Ок", null);
            return;
        }
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.POST, GWT.getHostPageBaseURL() + "keys/unset");
        requestBuilder.setHeader("Content-Type", "application/json");
        try {
            requestBuilder.sendRequest(null, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == 200) {
                        Modals.natificationModal("Активный ключ успешно сброшен!", "Ок", null);
                        updateValues();
                    } else {
                        Modals.natificationModal("Произошла ошибка!", "Ок", null);
                    }
                }

                @Override
                public void onError(Request request, Throwable throwable) {
                    Modals.natificationModal("Произошла ошибка!", "Ок", null);
                }
            });
        } catch (RequestException e) {
            e.printStackTrace();
        }
    }

    private void deleteKey() {
        String selectedItem = select.getValue();
        if (selectedItem == null) {
            Modals.natificationModal("Ключ для удаления не выбран, выберите ключ и повторите попытку!", "Ок", null);
            return;
        }
        Modals.confirmDangerModal("Данный ключ будет безвозвратно удалён!", () -> {
            RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.DELETE, GWT.getHostPageBaseURL() + "keys");
            requestBuilder.setHeader("Content-Type", "application/json");
            try {
                requestBuilder.sendRequest("{\"id\": " + selectedItem + "}", new RequestCallback() {
                    @Override
                    public void onResponseReceived(Request request, Response response) {
                        if (response.getStatusCode() == 200) {
                            Modals.natificationModal("Ключ успешно удалён!", "Ок", null);
                            updateValues();
                        } else {
                            Modals.natificationModal("Произошла ошибка!", "Ок", null);
                        }
                    }

                    @Override
                    public void onError(Request request, Throwable throwable) {
                        Modals.natificationModal("Произошла ошибка!", "Ок", null);
                    }
                });
            } catch (RequestException e) {
                e.printStackTrace();
            }
        });
    }

    private void createkey(String name){
        if(name.isEmpty()){
            Modals.natificationModal("Имя ключа не задано, введите имя и повторите попытку!", "Ок", null);
            return;
        }
        for (KeyDTO keyDTO : data){
            if(keyDTO.getName().equals(name)){
                Modals.natificationModal("Ключ с таким именем уже существует, введите другое имя повторите попытку!", "Ок", null);
                return;
            }
        }
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.POST, GWT.getHostPageBaseURL() + "keys");
        requestBuilder.setHeader("Content-Type", "application/json");
        try {
            requestBuilder.sendRequest("{\"name\": \"" + name + "\"}", new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == 200) {
                        Modals.natificationModal("Настройки успешно сохранены!", "Ок", null);
                        nameParagraph.setText(name);
                    } else {
                        Modals.natificationModal("Произошла ошибка!", "Ок", null);
                    }
                }

                @Override
                public void onError(Request request, Throwable throwable) {
                    Modals.natificationModal("Произошла ошибка!", "Ок", null);
                }
            });
        } catch (RequestException e) {
            e.printStackTrace();
        }
    }


}
