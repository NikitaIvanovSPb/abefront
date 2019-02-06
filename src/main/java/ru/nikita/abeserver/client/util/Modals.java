package ru.nikita.abeserver.client.util;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;

public class Modals {
    public static void natificationModal(String message, String buttonText, DoAfter doAfter){
        Modal littleModal = new Modal();
        ModalBody modalBody = new ModalBody();
        modalBody.add(new Heading(HeadingSize.H1, message));
        Button button = new Button(buttonText);
        button.addClickHandler(clickEvent -> {
            if(doAfter!= null) doAfter.doAfter();
            littleModal.hide();
        });
        modalBody.add(button);
        littleModal.add(modalBody);
        littleModal.show();
    }

    public static void confirmDangerModal(String message, DoAfter doAfter){
        Modal littleModal = new Modal();
        ModalBody modalBody = new ModalBody();
        modalBody.add(new Heading(HeadingSize.H1, message));
        HorizontalPanel horizontalPanel = new HorizontalPanel();
        Button okButton = new Button("Продолжить");
        okButton.setType(ButtonType.DANGER);
        okButton.addClickHandler(clickEvent -> {
            if(doAfter!= null) doAfter.doAfter();
            littleModal.hide();
        });
        Button cancelButton = new Button("Отмена");
        cancelButton.setType(ButtonType.PRIMARY);
        cancelButton.addClickHandler(clickEvent -> {
            littleModal.hide();
        });
        horizontalPanel.add(okButton);
        horizontalPanel.add(cancelButton);
        modalBody.add(horizontalPanel);
        littleModal.add(modalBody);
        littleModal.show();
    }
}
