import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;

/**
 * Flat, rounded buttons in the Mocha palette. A button fills with the blue accent unless it is
 * marked {@link MochaTheme#QUIET_BUTTON}, which gives it a surface fill for actions that should
 * not compete with the main ones. Hover and press shift the fill, and keyboard focus draws a
 * lavender outline in place of Metal's dotted rectangle.
 */
public final class MochaButtonUI extends BasicButtonUI {

    /** The fills one button cycles through. */
    private record Shades(Color normal, Color hover, Color pressed, Color disabled) {}

    private static final Shades ACCENT = new Shades(
            MochaTheme.BLUE, MochaTheme.LAVENDER, MochaTheme.SAPPHIRE, MochaTheme.SURFACE1);
    private static final Shades QUIET = new Shades(
            MochaTheme.SURFACE0, MochaTheme.SURFACE1, MochaTheme.SURFACE2, MochaTheme.SURFACE0);

    private static final int CORNER_RADIUS = 10;
    private static final BorderUIResource PADDING =
            new BorderUIResource(new EmptyBorder(8, 18, 8, 18));

    /** Stateless, so one delegate serves every button the way BasicButtonUI's does. */
    private static final MochaButtonUI SHARED = new MochaButtonUI();

    private MochaButtonUI() {}

    /** UIManager looks this method up by name, so the signature must stay as it is. */
    public static ComponentUI createUI(JComponent component) {
        return SHARED;
    }

    /** The fill is painted here with rounded corners, so the button must not paint a square one. */
    @Override
    protected void installDefaults(AbstractButton button) {
        super.installDefaults(button);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setRolloverEnabled(true);
        button.setBorder(PADDING);
    }

    @Override
    public void paint(Graphics g, JComponent component) {
        AbstractButton button = (AbstractButton) component;
        int width = button.getWidth();
        int height = button.getHeight();

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(fill(button));
        g2.fillRoundRect(0, 0, width, height, CORNER_RADIUS, CORNER_RADIUS);
        if (button.isEnabled() && button.isFocusOwner()) {
            g2.setColor(MochaTheme.LAVENDER);
            g2.drawRoundRect(0, 0, width - 1, height - 1, CORNER_RADIUS, CORNER_RADIUS);
        }

        g2.dispose();
        super.paint(g, component);
    }

    @Override
    protected void paintText(Graphics g, AbstractButton button, Rectangle textRect, String text) {
        FontMetrics metrics = button.getFontMetrics(button.getFont());
        g.setColor(textColor(button));
        BasicGraphicsUtils.drawStringUnderlineCharAt(button, (Graphics2D) g, text,
                button.getDisplayedMnemonicIndex(), textRect.x, textRect.y + metrics.getAscent());
    }

    private static Color fill(AbstractButton button) {
        Shades shades = isQuiet(button) ? QUIET : ACCENT;
        ButtonModel model = button.getModel();
        if (!button.isEnabled()) {
            return shades.disabled();
        }

        if (model.isArmed() && model.isPressed()) {
            return shades.pressed();
        }

        return model.isRollover() ? shades.hover() : shades.normal();
    }

    /** Dark text on the bright accent, light text on a surface, muted text when disabled. */
    private static Color textColor(AbstractButton button) {
        if (!button.isEnabled()) {
            return MochaTheme.OVERLAY0;
        }

        return isQuiet(button) ? MochaTheme.TEXT : MochaTheme.CRUST;
    }

    private static boolean isQuiet(AbstractButton button) {
        return Boolean.TRUE.equals(button.getClientProperty(MochaTheme.QUIET_BUTTON));
    }
}
