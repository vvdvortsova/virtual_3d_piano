package piano3D;

import javafx.scene.Group;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;

/**
 * Class for grouping MeshView objects
 * @autor Dvortsova Varvara
 * @version 1.0
 */
class SmartGroup extends Group {

    private Rotate r;
    private Transform t = new Rotate();

    public SmartGroup(MeshView mesh) {
        super(mesh);
    }

    /**
     * Method for rotating a group along the OX axis
     * @param ang angle
     */
    void rotateByX(int ang) {
        r = new Rotate(ang, Rotate.X_AXIS);
        t = t.createConcatenation(r);
        this.getTransforms().clear();
        this.getTransforms().addAll(t);
    }

    /**
     * Method for rotating a group along the OY axis
     * @param ang angle
     */
    void rotateByY(int ang) {
        r = new Rotate(ang, Rotate.Y_AXIS);
        t = t.createConcatenation(r);
        this.getTransforms().clear();
        this.getTransforms().addAll(t);
    }
}