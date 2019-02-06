package ru.nikita.abeserver.client.files;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.*;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import org.gwtbootstrap3.client.ui.*;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.InputType;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.extras.select.client.ui.MultipleSelect;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;
import ru.nikita.abeserver.client.dto.*;
import ru.nikita.abeserver.client.util.Modals;

import java.util.List;

public class SendDialog extends Modal {
    MultipleSelect select;
    Button send;
    FileDTO fileDTO;
    private DTOFactory factory = GWT.create(DTOFactory.class);
    public SendDialog(FileDTO fileDTO) {
        super();
        this.fileDTO = fileDTO;
        setTitle("Отправить ссылку на файл");
        ModalBody body = new ModalBody();
        ModalFooter foot = new ModalFooter();
        VerticalPanel panel = new VerticalPanel();
        Span selectSpan = new Span();
        selectSpan.setText("Выберите пользователей для отправки файла");
        select = new MultipleSelect();

        panel.add(selectSpan);
        panel.add(select);
        send = new Button("Отправить", event -> {
            sendMessage();
            this.hide();
        });
        send.setType(ButtonType.SUCCESS);
        foot.add(send);
        Button cancel = new Button("Отменить");
        cancel.addClickHandler(event -> hide());
        foot.add(cancel);
        body.add(panel);
        add(body);
        add(foot);
        updateUsers();
    }

    void updateUsers(){
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, GWT.getHostPageBaseURL()+"users");
        try {
            requestBuilder.sendRequest(null, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    AutoBean<UserDTOList> bean = AutoBeanCodex.decode(factory, UserDTOList.class, response.getText());
                    String str =  AutoBeanCodex.encode(bean).getPayload();
                    UserDTOList list = bean.as();
                    select.clear();
                    for (UserDTO userDTO : list.getResults()) {
                        Option option = new Option();
                        option.setText(userDTO.getLogin()+":"+userDTO.getLastName() + " " + userDTO.getName());
                        option.setValue(userDTO.getId().toString());
                        select.add(option);
                    }
                    select.refresh();
                }

                @Override
                public void onError(Request request, Throwable throwable) {

                }
            });
        } catch (RequestException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        List<Option> selectedItems = select.getSelectedItems();
        if (selectedItems.size() == 0) {
            Modals.natificationModal("Не выбрано ни одного пользователя!", "Ок", null);
            return;
        }
        for(Option item: selectedItems) {
            RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.POST, GWT.getHostPageBaseURL() + "files/send");
            requestBuilder.setHeader("Content-Type", "application/json");
            try {
                requestBuilder.sendRequest("{\"userId\": " + item.getValue() + ", \"fileId\": \""+fileDTO.getGuid()+"\"}", new RequestCallback() {
                    @Override
                    public void onResponseReceived(Request request, Response response) {
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
        Modals.natificationModal("Сообщения были отправлены!", "Ок", null);
    }
}
