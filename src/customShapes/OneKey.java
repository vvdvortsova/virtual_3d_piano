package customShapes;

import javafx.scene.input.KeyCode;
import javafx.scene.shape.MeshView;

/**
 * Class represents any piano key
 * with additional parameters
 *
 * @version 1.0
 * @autor Dvortsova Varvara
 */
public class OneKey {

    private boolean keyPressed = false;
    private final String nameOfKey;
    private final KeyCode keyCode;
    private final int octaveShift;
    private final int octaveValue;
    private final MeshView meshView;

    public OneKey(MeshView meshView,
                  String nameOfKey,
                  KeyCode keyCode,
                  int octaveShift,
                  int octaveValue,
                  double x, double z) {
        this.meshView = meshView;
        this.nameOfKey = nameOfKey;
        this.keyCode = keyCode;
        this.octaveShift = octaveShift;
        this.octaveValue = octaveValue;
        this.meshView.setTranslateX(x);
        this.meshView.setTranslateZ(z);
    }

    public MeshView getMeshView() {
        return meshView;
    }

    public int getOctaveShift() {
        return octaveShift;
    }

    public int getOctaveValue() {
        return octaveValue;
    }

    public boolean isKeyPressed() {
        return keyPressed;
    }

    public void setKeyPressed(boolean keyPressed) {
        this.keyPressed = keyPressed;
    }

    public String getNameOfKey() {
        return nameOfKey;
    }
}
