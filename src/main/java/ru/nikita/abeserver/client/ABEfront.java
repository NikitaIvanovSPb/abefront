package ru.nikita.abeserver.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import ru.nikita.abeserver.client.files.Files;
import ru.nikita.abeserver.client.users.Users;

public class ABEfront implements EntryPoint {
    @Override
    public void onModuleLoad() {
        DockLayoutPanel outer = ourUiBinder.createAndBindUi(this);
        RootLayoutPanel root = RootLayoutPanel.get();

        users.addClickHandler(clickEvent -> {
            if(users.isActive()) return;
            clearActives();
            users.setActive(true);
            content.clear();
            content.add(new Users());
        });
        files.addClickHandler(clickEvent -> {
            if(files.isActive()) return;
            clearActives();
            files.setActive(true);
            content.clear();
            content.add(new Files());
        });

        root.add(outer);
        root.forceLayout();
    }

    interface ABEfrontUiBinder extends UiBinder<DockLayoutPanel, ABEfront> {
    }

    private static ABEfrontUiBinder ourUiBinder = GWT.create(ABEfrontUiBinder.class);
    @UiField
    AnchorListItem users;
    @UiField
    FlowPanel content;
    @UiField
    AnchorListItem files;


    private void clearActives(){
        users.setActive(false);
        files.setActive(false);
    }
}