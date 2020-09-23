package customShapes;

import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;

import org.fxyz3d.geometry.Point3D;
import org.fxyz3d.shapes.primitives.TriangulatedMesh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The class represents methods for creating all types of keys
 * Sets of points can be changed as you like
 *
 * @version 1.0
 * @autor Dvortsova Varvara
 */
public class PianoKeys {

    public enum KeyType {
        GR,
        GL,
        T,
        TL,
        TR,
        WHITE,
        BLACK
    }

    /**
     * Point Set for 2D-Right G(Г) Key
     */
    final static List<Point3D> pointsGR = new ArrayList<>(Arrays.asList(
            new Point3D(0, 0, 0),
            new Point3D(0, 200, 0), new Point3D(30, 200, 0),
            new Point3D(30, 135, 0), new Point3D(20, 135, 0),
            new Point3D(20, 0, 0), new Point3D(0, 0, 0)));

    /**
     * Point Set for 2D-Left G(Г) Key
     */
    final static List<Point3D> pointsGL = new ArrayList<>(Arrays.asList(
            new Point3D(0, 200, 0),
            new Point3D(0, 135, 0),
            new Point3D(10, 135, 0),
            new Point3D(10, 0, 0),
            new Point3D(30, 0, 0),
            new Point3D(30, 200, 0)

    ));

    /**
     * Set of points for 2D T key
     */
    final static List<Point3D> pointsT = new ArrayList<>(Arrays.asList(

            new Point3D(0, 135, 0),
            new Point3D(0, 200, 0),

            new Point3D(30, 200, 0),
            new Point3D(30, 135, 0),

            new Point3D(25, 135, 0),
            new Point3D(25, 0, 0),

            new Point3D(5, 0, 0),
            new Point3D(5, 135, 0)
    )
    );
    /**
     * Point set for 2D T key with long distance left
     */
    final static List<Point3D> pointsTL = new ArrayList<>(Arrays.asList(

            new Point3D(0, 135, 0),
            new Point3D(0, 200, 0),

            new Point3D(30, 200, 0),
            new Point3D(30, 135, 0),

            new Point3D(25, 135, 0),
            new Point3D(25, 0, 0),

            new Point3D(7.5, 0, 0),
            new Point3D(7.5, 135, 0)
    )
    );
    /**
     * Point set for 2D T key with long distance to the right
     */
    final static List<Point3D> pointsTR = new ArrayList<>(Arrays.asList(

            new Point3D(0, 135, 0),
            new Point3D(0, 200, 0),

            new Point3D(30, 200, 0),
            new Point3D(30, 135, 0),

            new Point3D(20.5, 135, 0),
            new Point3D(20.5, 0, 0),

            new Point3D(5, 0, 0),
            new Point3D(5, 135, 0)
    )
    );
    /**
     * Set of points for 2D white key
     */
    final static List<Point3D> pointsWhite = new ArrayList<>(Arrays.asList(

            new Point3D(0, 0, 0),
            new Point3D(0, 200, 0),
            new Point3D(30, 200, 0),
            new Point3D(30, 0, 0)
    )
    );
    /**
     * Set of points for 2D black black key
     */
    final static List<Point3D> pointsBlack = new ArrayList<>(Arrays.asList(

            new Point3D(0, 132, 0),

            new Point3D(14, 132, 0),

            new Point3D(14, 0, 0),

            new Point3D(0, 0, 0)
    ));

    /**
     * Method for creating 3D figures for keys
     * Adds width for 2D shapes
     * Sets white color and CullFace.NONE
     *
     * @return MeshView key object
     */
    public static MeshView getKeyByType(KeyType type) {

        TriangulatedMesh customShape = null;
        switch (type) {
            case GR:
                customShape = new TriangulatedMesh(pointsGR, 12);
                break;
            case GL:
                customShape = new TriangulatedMesh(pointsGL, 12);
                break;
            case T:
                customShape = new TriangulatedMesh(pointsT, 12);
                break;
            case TL:
                customShape = new TriangulatedMesh(pointsTL, 12);
                break;
            case TR:
                customShape = new TriangulatedMesh(pointsTR, 12);
                break;
            case WHITE:
                customShape = new TriangulatedMesh(pointsWhite, 12);
                break;
            case BLACK:
                customShape = new TriangulatedMesh(pointsBlack, 12);
                break;
        }

        MeshView mesh = new MeshView(customShape.getMesh());
        if (type != KeyType.BLACK)
            mesh.setMaterial(new PhongMaterial(Color.WHITE));
        else mesh.setMaterial(new PhongMaterial(Color.web("#202020")));

        customShape.setLevel(0);
        customShape.setCullFace(CullFace.NONE);
        mesh.setTranslateZ(0);
        mesh.setCullFace(CullFace.NONE);
        return mesh;
    }

    ;

}
