package ru.nikita.abeserver.client.util;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SimpleSafeHtmlRenderer;
import com.google.gwt.user.client.ui.HasEnabled;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Styles;

public class ButtonCell extends com.google.gwt.cell.client.ButtonCell implements HasEnabled {
    private IconType icon;

    private ButtonType type = ButtonType.DEFAULT;

    private ButtonSize size = ButtonSize.DEFAULT;

    private boolean enabled = true;

    public ButtonCell() {
        super(SimpleSafeHtmlRenderer.getInstance());
    }

    public ButtonCell(IconType icon, ButtonType type, ButtonSize size, boolean enabled) {
        this.icon = icon;
        this.type = type;
        this.size = size;
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void render(Context context, SafeHtml data, SafeHtmlBuilder sb) {
        if (data!=null) {
            String cssClasses = new StringBuilder("btn")
                    .append(" ")
                    .append(type.getCssName())
                    .append(" ")
                    .append(size.getCssName())
                    .toString();

            String disabled = "";
            if (!enabled) {
                disabled = " disabled=\"disabled\"";
            }

            sb.appendHtmlConstant("<button type=\"button\" class=\"" + cssClasses + "\" tabindex=\"-1\"" + disabled + ">");
            if (icon != null) {
                String iconHtml = new StringBuilder("<i class=\"")
                        .append(Styles.FONT_AWESOME_BASE)
                        .append(" ")
                        .append(icon.getCssName())
                        .append("\"></i> ")
                        .toString();
                sb.appendHtmlConstant(iconHtml);
            }
            if (data != null) {
                sb.append(data);
            }
            sb.appendHtmlConstant("</button>");
        }
    }



}
