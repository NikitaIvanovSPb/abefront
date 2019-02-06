package ru.nikita.abeserver.client.users;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.gwt.CellTable;
import ru.nikita.abeserver.client.dto.DTOFactory;
import ru.nikita.abeserver.client.dto.UserDTO;
import ru.nikita.abeserver.client.dto.UserDTOList;
import ru.nikita.abeserver.client.util.ButtonCell;
import ru.nikita.abeserver.client.util.Modals;

import java.util.logging.Logger;

public class Users extends Composite {
    private AsyncDataProvider<UserDTO> provider;
    private DTOFactory factory = GWT.create(DTOFactory.class);
    Logger log = Logger.getLogger("Users");
    interface UsersUiBinder extends UiBinder<Widget, Users> {
    }

    private static UsersUiBinder ourUiBinder = GWT.create(UsersUiBinder.class);
    @UiField
    CellTable<UserDTO> table;
    @UiField
    SimplePager pager;
    @UiField
    Button badd;
    @UiField
    Modal deleteDialog;
    @UiField
    Button deleteDialogCancel;
    @UiField
    Button deleteDialogOk;
    UserDTO userForDelete;
    UserCreateUpdate userCreateUpdate = new UserCreateUpdate();

    public Users() {
        initWidget(ourUiBinder.createAndBindUi(this));
        createTable();
        updateUsers();
        RootPanel.get().add(userCreateUpdate);
        badd.addClickHandler(clickEvent -> {
            UserDTO userDTO = factory.usersDTOFactory().as();
            userDTO.setId(-1L);
            userCreateUpdate.create(userDTO, this::updateUsers);
        });
    }

    void updateUsers(){
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, GWT.getHostPageBaseURL()+"users");
        try {
            requestBuilder.sendRequest(null, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
//                    AutoBean<Person> bean = AutoBeanUtils.getAutoBean(person);
//                    return AutoBeanCodex.encode(bean).getPayload();
                    AutoBean<UserDTOList> bean = AutoBeanCodex.decode(factory, UserDTOList.class, response.getText());
                    String str =  AutoBeanCodex.encode(bean).getPayload();
                    UserDTOList list = bean.as();
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
        table.addColumn(new TextColumn<UserDTO>() {
            @Override
            public String getValue(UserDTO userDTO) {
                return userDTO.getName();
            }
        }, "Имя");
        table.addColumn(new TextColumn<UserDTO>() {
            @Override
            public String getValue(UserDTO userDTO) {
                return userDTO.getLastName();
            }
        }, "Фамилия");
        table.addColumn(new TextColumn<UserDTO>() {
            @Override
            public String getValue(UserDTO userDTO) {
                return userDTO.getPatronymic();
            }
        }, "Отчество");
        table.addColumn(new TextColumn<UserDTO>() {
            @Override
            public String getValue(UserDTO userDTO) {
                return userDTO.getEmail();
            }
        }, "Email");
        table.addColumn(new TextColumn<UserDTO>() {
            @Override
            public String getValue(UserDTO userDTO) {
                return userDTO.getAdmin()? "Администратор" : "Пользователь";
            }
        }, "Роль");
        Column<UserDTO, String> tokens = new Column<UserDTO, String>(new ButtonCell(IconType.KEY, ButtonType.PRIMARY, ButtonSize.EXTRA_SMALL, true)) {
            @Override
            public String getValue(UserDTO userDTO) {
                return "";
            }
        };
        tokens.setFieldUpdater((i, userDTO, s) -> {
            TokensDialog tokensDialog = new TokensDialog(userDTO);
            tokensDialog.show();
        });
        table.addColumn(tokens);
        Column<UserDTO, String> groups = new Column<UserDTO, String>(new ButtonCell(IconType.GROUP, ButtonType.PRIMARY, ButtonSize.EXTRA_SMALL, true)) {
            @Override
            public String getValue(UserDTO userDTO) {
                return "";
            }
        };
//        groups.setFieldUpdater((i, userDTO, s) -> userCreateUpdate.update(userDTO, this::updateUsers)); TODO редактирование групп
        table.addColumn(groups);
        Column<UserDTO, String> update = new Column<UserDTO, String>(new ButtonCell(IconType.PENCIL, ButtonType.PRIMARY, ButtonSize.EXTRA_SMALL, true)) {
            @Override
            public String getValue(UserDTO userDTO) {
                return "";
            }
        };
        update.setFieldUpdater((i, userDTO, s) -> userCreateUpdate.update(userDTO, this::updateUsers));
        table.addColumn(update);
        Column<UserDTO, String> remove = new Column<UserDTO, String>(new ButtonCell(IconType.REMOVE, ButtonType.DANGER, ButtonSize.EXTRA_SMALL, true)) {
            @Override
            public String getValue(UserDTO userDTO) {
                return "";
            }
        };
        remove.setFieldUpdater((i, userDTO, s) -> {
            userForDelete = userDTO;
            deleteDialog.show();
        });
        deleteDialogCancel.addClickHandler(clickEvent -> deleteDialog.hide());
        deleteDialogOk.addClickHandler(clickEvent -> {
            RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.DELETE, GWT.getHostPageBaseURL()+"users/" + userForDelete.getId());
            try {
                requestBuilder.sendRequest(null, new RequestCallback() {
                    @Override
                    public void onResponseReceived(Request request, Response response) {
                        Modals.natificationModal("Пользователь успешно удалён!", "Ок", () -> updateUsers());
                    }

                    @Override
                    public void onError(Request request, Throwable throwable) {
                        Modals.natificationModal("Произошла ошибка, пользователь не был удалён!", "Ок", null);
                    }
                });
            } catch (RequestException e) {
                e.printStackTrace();
            }
        });
        table.addColumn(remove);
        table.setColumnWidth(5,"1px");
        table.setColumnWidth(6,"1px");
        table.setColumnWidth(7,"1px");
        table.setColumnWidth(8,"1px");
        provider = new AsyncDataProvider<UserDTO>() {
            @Override
            protected void onRangeChanged(HasData<UserDTO> hasData) {
            }
        };
        provider.addDataDisplay(table);
        pager.setDisplay(table);
        table.setCondensed(true);
    }



}