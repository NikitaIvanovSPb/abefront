package ru.nikita.abeserver.client.files;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.*;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import org.gwtbootstrap3.client.ui.*;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.InputType;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;
import ru.nikita.abeserver.client.dto.*;
import ru.nikita.abeserver.client.util.Modals;

import java.util.List;
import java.util.logging.Logger;

public class SettingsFTPDialog extends Modal {
    private Logger log = Logger.getLogger("UploadDialog");
    private Paragraph nameParagraph;
    private DTOFactory factory = GWT.create(DTOFactory.class);
    private Select select;
    private List<FTPDTO> data;
    private Button newNameSaveButton;
    private Input newUrlInput;
    private Input newPortInput;
    private Input newAdminLoginInput;
    private Input newAdminPassInput;
    private Input newUserLoginInput;
    private Input newUserPassInput;
    private FTPDTO selectFTPDTO;
    public SettingsFTPDialog() {
        super();

        setTitle("Настройки FTP сервера");
        ModalBody body = new ModalBody();
        ModalFooter foot = new ModalFooter();

        VerticalPanel panel = new VerticalPanel();
        VerticalPanel verticalPanel1 = new VerticalPanel();
        Span newNameSpan = new Span();
        newNameSpan.setText("Добавить или изменить сервер:");
        newUrlInput = new Input(InputType.TEXT);
        newUrlInput.setPlaceholder("Адрес");
        newPortInput = new Input(InputType.NUMBER);
        newPortInput.setPlaceholder("Порт");
        newAdminLoginInput = new Input(InputType.TEXT);
        newAdminLoginInput.setPlaceholder("Логин администратора");
        newAdminPassInput = new Input(InputType.TEXT);
        newAdminPassInput.setPlaceholder("Пароль администратора");
        newUserLoginInput = new Input(InputType.TEXT);
        newUserLoginInput.setPlaceholder("Логин пользователя");
        newUserPassInput = new Input(InputType.TEXT);
        newUserPassInput.setPlaceholder("Пароль пользователя");
        newNameSaveButton = new Button("Создать");
        newUrlInput.addChangeHandler(changeEvent -> enableAddButton());
        newPortInput.addChangeHandler(changeEvent -> enableAddButton());
        newAdminLoginInput.addChangeHandler(changeEvent -> enableAddButton());
        newAdminPassInput.addChangeHandler(changeEvent -> enableAddButton());
        newNameSaveButton.setEnabled(false);
        newNameSaveButton.setType(ButtonType.PRIMARY);
        newNameSaveButton.addClickHandler(clickEvent -> {
            createFTP();
        });
        newUrlInput.addKeyUpHandler(changeEvent -> {
            newNameSaveButton.setEnabled(!newUrlInput.getValue().isEmpty());
        });
        Span setKeySpan = new Span();
        setKeySpan.setText("Выберите FTP сервер и необходимое действие");
        select = new Select();
        select.addValueChangeHandler(valueChangeEvent -> {
            Long longVal = Long.parseLong(valueChangeEvent.getValue());
            if(longVal.equals(-1L)){
                selectFTPDTO = null;
                newUrlInput.setValue("");
                newPortInput.setValue("");
                newAdminLoginInput.setValue("");
                newAdminPassInput.setValue("");
                newUserLoginInput.setValue("");
                newUserPassInput.setValue("");
            }else {
                for (FTPDTO ftpdto : data) {
                    if (ftpdto.getId().equals(longVal)) {
                        selectFTPDTO = ftpdto;
                        newUrlInput.setValue(ftpdto.getUrl());
                        newPortInput.setValue(ftpdto.getPort().toString());
                        newAdminLoginInput.setValue(ftpdto.getAdminLogin());
                        newAdminPassInput.setValue(ftpdto.getAdminPass());
                        newUserLoginInput.setValue(ftpdto.getUserLogin());
                        newUserPassInput.setValue(ftpdto.getUserPass());
                    }
                }
            }

        });
        HorizontalPanel horizontalPanel2 = new HorizontalPanel();
        Button updateButton = new Button("Обновить данные FTP сервера");
        updateButton.setType(ButtonType.SUCCESS);
        updateButton.addClickHandler(clickEvent -> {
            updateFTP();
        });
        Button deleteKeyButton = new Button("Удалить FTP сервер");
        deleteKeyButton.setType(ButtonType.DANGER);
        deleteKeyButton.addClickHandler(clickEvent -> {
            deleteFTP();
        });
        horizontalPanel2.add(updateButton);
        horizontalPanel2.add(deleteKeyButton);


        panel.add(verticalPanel1);
        panel.add(newNameSpan);
        panel.add(new Span("<p>Адрес</p>"));
        panel.add(newUrlInput);
        panel.add(new Span("<p>Порт</p>"));
        panel.add(newPortInput);
        panel.add(new Span("<p>Логин администратора</p>"));
        panel.add(newAdminLoginInput);
        panel.add(new Span("<p>Пароль администратора</p>"));
        panel.add(newAdminPassInput);
        panel.add(new Span("<p>Логин пользователя</p>"));
        panel.add(newUserLoginInput);
        panel.add(new Span("<p>Пароль пользователя</p>"));
        panel.add(newUserPassInput);
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
                        Option option = new Option();
                        option.setText("-");
                        option.setValue("-1");
                        option.setSelected(true);
                        select.add(option);
                        for (FTPDTO ftpdto : list.getResults()) {
                            option = new Option();
                            option.setText(ftpdto.getUrl()+":"+ftpdto.getPort());
                            option.setValue(ftpdto.getId().toString());
                            select.add(option);
                        }
                        select.refresh();
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

    private void deleteFTP(){
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.DELETE, GWT.getHostPageBaseURL() + "ftp");
        requestBuilder.setHeader("Content-Type", "application/json");
        try {
            AutoBean<FTPDTO> autoBean = AutoBeanUtils.getAutoBean(selectFTPDTO);
            requestBuilder.sendRequest(AutoBeanCodex.encode(autoBean).getPayload(), new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == 200) {
                        updateValues();
                        Modals.natificationModal("Сервер успешно удалён!", "Ок", null);
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

    private void updateFTP(){
        for (FTPDTO ftpdto : data){
            if(ftpdto.getName().equals(newUrlInput.getValue()) && ftpdto.getPort() == Integer.parseInt(newPortInput.getValue()) && ftpdto.getId().equals(selectFTPDTO.getId())){
                Modals.natificationModal("FTP сервер с таким адресом уже существует, введите другой адрес и повторите попытку!", "Ок", null);
                return;
            }
        }
        FTPDTO ftpdto = factory.FTPDTOFactory().as();
        ftpdto.setId(selectFTPDTO.getId());
        ftpdto.setUrl(newUrlInput.getValue());
        ftpdto.setPort(Integer.parseInt(newPortInput.getValue()));
        ftpdto.setAdminLogin(newAdminLoginInput.getValue());
        ftpdto.setAdminPass(newAdminPassInput.getValue());
        ftpdto.setUserLogin(newUserLoginInput.getValue());
        ftpdto.setUserPass(newUserPassInput.getValue());
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.POST, GWT.getHostPageBaseURL() + "ftp");
        requestBuilder.setHeader("Content-Type", "application/json");
        try {
            AutoBean<FTPDTO> autoBean = AutoBeanUtils.getAutoBean(ftpdto);
            requestBuilder.sendRequest(AutoBeanCodex.encode(autoBean).getPayload(), new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == 200) {
                        AutoBean<FTPDTO> respBean = AutoBeanCodex.decode(factory, FTPDTO.class, response.getText());
                        selectFTPDTO = respBean.as();
                        updateValues();
                        Modals.natificationModal("Сервер успешно обновлён!", "Ок", null);
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

    private void createFTP(){
        for (FTPDTO ftpdto : data){
            if(ftpdto.getName().equals(newUrlInput.getValue()) && ftpdto.getPort() == Integer.parseInt(newPortInput.getValue())){
                Modals.natificationModal("FTP сервер с таким адресом уже существует, введите другой адрес и повторите попытку!", "Ок", null);
                return;
            }
        }
        FTPDTO ftpdto = factory.FTPDTOFactory().as();
        ftpdto.setId(-1L);
        ftpdto.setUrl(newUrlInput.getValue());
        ftpdto.setPort(Integer.parseInt(newPortInput.getValue()));
        ftpdto.setAdminLogin(newAdminLoginInput.getValue());
        ftpdto.setAdminPass(newAdminPassInput.getValue());
        ftpdto.setUserLogin(newUserLoginInput.getValue());
        ftpdto.setUserPass(newUserPassInput.getValue());
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.POST, GWT.getHostPageBaseURL() + "ftp");
        requestBuilder.setHeader("Content-Type", "application/json");
        try {
            AutoBean<FTPDTO> autoBean = AutoBeanUtils.getAutoBean(ftpdto);
            requestBuilder.sendRequest(AutoBeanCodex.encode(autoBean).getPayload(), new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == 200) {
                        AutoBean<FTPDTO> respBean = AutoBeanCodex.decode(factory, FTPDTO.class, response.getText());
                        selectFTPDTO = respBean.as();
                        updateValues();
                        Modals.natificationModal("Сервер успешно добавлён!", "Ок", null);
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


    private void enableAddButton(){
        if(newUrlInput.getValue().isEmpty() || newPortInput.getValue().isEmpty() || newAdminLoginInput.getValue().isEmpty() || newAdminPassInput.getValue().isEmpty()){
            newNameSaveButton.setEnabled(false);
        }else {
            newNameSaveButton.setEnabled(true);
        }
    }
}
