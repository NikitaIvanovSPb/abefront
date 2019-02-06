package ru.nikita.abeserver.client.users;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import org.gwtbootstrap3.client.ui.*;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import ru.nikita.abeserver.client.dto.UserDTO;
import ru.nikita.abeserver.client.util.DoAfter;

public class UserCreateUpdate extends Composite {
    interface UserCreateUpdateUiBinder extends UiBinder<Modal, UserCreateUpdate> {
    }

    private static UserCreateUpdateUiBinder ourUiBinder = GWT.create(UserCreateUpdateUiBinder.class);
    @UiField
    Input login;
    @UiField
    Input password;
    @UiField
    Input name;
    @UiField
    Input lastname;
    @UiField
    Input patronymic;
    @UiField
    Input email;
    @UiField
    CheckBox admin;
    @UiField
    Button saveButton;
    @UiField
    Button cancelButton;
    Modal modal;
    UserDTO data;
    DoAfter doAfter;
    public UserCreateUpdate() {
        modal = ourUiBinder.createAndBindUi(this);
        initWidget(modal);
        saveButton.addClickHandler(clickEvent -> {
            RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.POST, GWT.getHostPageBaseURL()+"users");
            requestBuilder.setHeader("Content-Type", "application/json");
            readFields();
            AutoBean<UserDTO> bean = AutoBeanUtils.getAutoBean(data);
            try {
                requestBuilder.sendRequest(AutoBeanCodex.encode(bean).getPayload(), new RequestCallback() {
                    @Override
                    public void onResponseReceived(Request request, Response response) {
                        if(response.getStatusCode()==200){
                            successModal();
                        }else{
                            errorModal();
                        }
                    }

                    @Override
                    public void onError(Request request, Throwable throwable) {
                        errorModal();
                    }
                });
            } catch (RequestException e) {
                e.printStackTrace();
            }
        });
        cancelButton.addClickHandler(clickEvent -> {
            modal.hide();
        });
    }

    public void create(UserDTO userDTO, DoAfter doAfter){
        modal.setTitle("Добавление пользователя");
        data = userDTO;
        clearFields();
        modal.show();
    }

    public void update(UserDTO userDTO, DoAfter doAfter){
        modal.setTitle("Редактирование пользователя");
        data = userDTO;
        clearFields();
        insertFields();
        modal.show();
    }

    private void clearFields(){
        login.setValue("");
        name.setValue("");
        lastname.setValue("");
        patronymic.setValue("");
        email.setValue("");
        password.setValue("");
        admin.setValue(false);
    }

    private void insertFields(){
        login.setValue(data.getLogin());
        name.setValue(data.getName());
        lastname.setValue(data.getLastName());
        patronymic.setValue(data.getPatronymic());
        email.setValue(data.getEmail());
        admin.setValue(data.getAdmin());
    }

    private void readFields(){
        data.setLogin(login.getValue());
        data.setName(name.getValue());
        data.setLastName(lastname.getValue());
        data.setPatronymic(patronymic.getValue());
        data.setEmail(email.getValue());
        data.setAdmin(admin.getValue());
        data.setPassword(password.getValue());
    }

    private void successModal(){
        modal.hide();
        Modal littleModal = new Modal();
        ModalBody modalBody = new ModalBody();
        modalBody.add(new Heading(HeadingSize.H1, "Изменения успешно сохранены!"));
        Button button = new Button("Ок");
        button.addClickHandler(clickEvent -> {
            littleModal.hide();
            doAfter.doAfter();
        });
        modalBody.add(button);
        littleModal.add(modalBody);
        littleModal.show();
    }

    private void errorModal(){
        modal.hide();
        Modal littleModal = new Modal();
        ModalBody modalBody = new ModalBody();
        modalBody.add(new Heading(HeadingSize.H1, "Ошибка во время сохранения изменений!"));
        Button button = new Button("Ок");
        button.addClickHandler(clickEvent -> {
            littleModal.hide();
            modal.show();
        });
        modalBody.add(button);
        littleModal.add(modalBody);
        littleModal.show();
    }
}